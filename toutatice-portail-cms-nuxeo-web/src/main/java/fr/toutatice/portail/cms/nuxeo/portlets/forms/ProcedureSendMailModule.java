package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.Name;
import javax.portlet.PortletContext;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.directory.v2.DirServiceFactory;
import org.osivia.portal.api.directory.v2.model.Group;
import org.osivia.portal.api.directory.v2.model.Person;
import org.osivia.portal.api.directory.v2.service.GroupService;
import org.osivia.portal.api.directory.v2.service.PersonService;
import org.osivia.portal.api.html.DOM4JUtils;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.tasks.ITasksService;
import org.osivia.portal.api.transaction.IPostcommitResource;

import com.sun.mail.smtp.SMTPTransport;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.forms.IFormsService;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.portlets.service.GetTasksCommand;

/**
 * The Class ProcedureSendMailModule 
 * 
 * This class is implemented as postcommit because we need Elastic Search to be updated 
 */
public class ProcedureSendMailModule implements IPostcommitResource {


    /** The portal controller context. */
    PortalControllerContext portalControllerContext;
    /** The portlet context. */
    PortletContext portletContext;
    /** The bundle factory. */
    IBundleFactory bundleFactory;
    /** The procedure instance id. */
    String procedureInstanceId;
    /** The initiator. */
    String initiator;
    /** Person service. */
    private final PersonService personService;
    /** Group service. */
    private final GroupService groupService;
    /** The forms service. */
    private final FormsServiceImpl formsService;
    /** Tasks service. */
    private final ITasksService tasksService;
    /** Log. */
    private final static Log log = LogFactory.getLog(ProcedureSendMailModule.class);


    private final static Log procLogger = LogFactory.getLog("procedures");

    ProcedureSendMailModule(PortalControllerContext portalControllerContext, PortletContext portletContext, FormsServiceImpl formsService,
            String procedureInstanceId, String initiator) {

        this.portalControllerContext = portalControllerContext;
        this.portletContext = portletContext;
        this.formsService = formsService;
        this.procedureInstanceId = procedureInstanceId;
        this.initiator = initiator;
        // Internationalization bundle
        IInternationalizationService internationalizationService = Locator.findMBean(IInternationalizationService.class,
                IInternationalizationService.MBEAN_NAME);
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());
        // Person service
        this.personService = DirServiceFactory.getService(PersonService.class);
        // Group service
        this.groupService = DirServiceFactory.getService(GroupService.class);
        // Tasks service
        this.tasksService = Locator.findMBean(ITasksService.class, ITasksService.MBEAN_NAME);

    }

    @Override
    public void run() {
        try {
            Locale locale = null;
            if (portalControllerContext.getHttpServletRequest() != null) {
                locale = portalControllerContext.getHttpServletRequest().getLocale();
            }
            // Nuxeo controller
            NuxeoController nuxeoController = new NuxeoController(portletContext);
            nuxeoController.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
            nuxeoController.setCacheType(CacheInfo.CACHE_SCOPE_NONE);

            Bundle bundle = this.bundleFactory.getBundle(locale);

            if (StringUtils.isNotEmpty(procedureInstanceId)) {
                // UUID
                UUID uuid = UUID.fromString(procedureInstanceId);

                // Nuxeo command
                INuxeoCommand command = new GetTasksCommand(null, null, uuid);
                Documents documents = (Documents) nuxeoController.executeNuxeoCommand(command);

                // Task document
                Document task;
                if (documents.size() == 1) {
                    task = documents.get(0);
                } else {
                    throw new Exception("task not found");
                }

                // Task variables
                PropertyMap variables = task.getProperties().getMap("nt:task_variables");

                if (BooleanUtils.isTrue(variables.getBoolean("notifEmail"))) {
                    // Actors
                    PropertyList actors = task.getProperties().getList("nt:actors");

                    if (!actors.isEmpty()) {
                        // User names
                        Set<Name> names = new HashSet<>(actors.size());
                        for (int i = 0; i < actors.size(); i++) {
                            String actor = actors.getString(i);

                            // Group
                            Group group;
                            if (StringUtils.startsWith(actor, IFormsService.ACTOR_USER_PREFIX)) {
                                group = null;
                            } else if (StringUtils.startsWith(actor, IFormsService.ACTOR_GROUP_PREFIX)) {
                                group = this.groupService.get(StringUtils.removeStart(actor, IFormsService.ACTOR_GROUP_PREFIX));
                            } else {
                                group = this.groupService.get(actor);
                            }

                            if (group == null) {
                                String user = StringUtils.removeStart(actor, IFormsService.ACTOR_USER_PREFIX);
                                names.add(this.personService.getEmptyPerson().buildDn(user));
                            } else {
                                for (Name member : group.getMembers()) {
                                    names.add(member);
                                }
                            }
                        }

                        // Email recipients
                        Set<String> emailRecipients = new HashSet<String>(names.size());
                        for (Name name : names) {
                            Person person = this.personService.getPerson(name);
                            if (person != null) {
                                String email = person.getMail();
                                if (StringUtils.isNotBlank(email)) {
                                    emailRecipients.add(email);
                                }
                            }
                        }

                        if (!emailRecipients.isEmpty()) {
                            // Sender email
                            Person sender = this.personService.getPerson(initiator);
                            String emailSender = StringUtils.defaultIfBlank(sender.getMail(), initiator);

                            // Expression
                            String expression = variables.getString("stringMsg");


                            // Mail session
                            Session mailSession = Session.getInstance(System.getProperties(), null);

                            // Message
                            MimeMessage message = new MimeMessage(mailSession);
                            message.setSentDate(new Date());

                            // From
                            InternetAddress from = new InternetAddress(emailSender);
                            message.setFrom(from);

                            // To
                            InternetAddress[] to = InternetAddress.parse(StringUtils.join(emailRecipients, ","));
                            message.setRecipients(Message.RecipientType.TO, to);

                            // Reply to
                            InternetAddress[] replyTo = new InternetAddress[]{from};
                            message.setReplyTo(replyTo);

                            // Subject
                            String subject = StringUtils.substringBefore(formsService.transform(portalControllerContext, expression, task, true),
                                    System.lineSeparator());
                            message.setSubject(subject, CharEncoding.UTF_8);

                            // Body
                            String inlineBody = formsService.transform(portalControllerContext, expression, task, false);
                            StringBuilder body = new StringBuilder();
                            for (String line : StringUtils.split(inlineBody, System.lineSeparator())) {
                                body.append("<p>");
                                body.append(line);
                                body.append("</p>");
                            }
                            // Body actions
                            if (BooleanUtils.isTrue(variables.getBoolean("acquitable"))) {
                                // Accept
                                String acceptActionId = variables.getString("actionIdYes");
                                if (StringUtils.isNotBlank(acceptActionId)) {
                                    String url = this.tasksService.getCommandUrl(portalControllerContext, uuid, acceptActionId);
                                    String title = bundle.getString("ACCEPT");
                                    Element link = DOM4JUtils.generateLinkElement(url, null, null, null, title);

                                    body.append("<p>");
                                    body.append(DOM4JUtils.writeCompact(link));
                                    body.append("</p>");
                                }
                            }

                            // Multipart
                            Multipart multipart = new MimeMultipart();
                            MimeBodyPart htmlPart = new MimeBodyPart();
                            htmlPart.setContent(body.toString(), "text/html; charset=UTF-8");
                            multipart.addBodyPart(htmlPart);
                            message.setContent(multipart);

                            procLogger.info("  About to send mail on " + uuid + " from " + emailSender + " to " + StringUtils.join(emailRecipients, ",")
                                    + " subject " + subject);


                            // SMTP transport
                            SMTPTransport transport = (SMTPTransport) mailSession.getTransport();
                            transport.connect();
                            transport.sendMessage(message, message.getAllRecipients());
                            transport.close();

                            procLogger.info("  Mail sentl on " + uuid);

                        }
                    }
                }
            }
        
        } catch (Exception e) {
            this.log.error("Email sending error - messaging", e.getCause());
        }
    }

}
