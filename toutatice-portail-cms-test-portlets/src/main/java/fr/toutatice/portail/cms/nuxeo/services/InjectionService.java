package fr.toutatice.portail.cms.nuxeo.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.AttributeInUseException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapName;

import org.apache.commons.lang.StringUtils;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.commands.CreateDocumentsCommand;

/**
 * Injection service.
 *
 * @author Cédric Krommenhoek
 */
public final class InjectionService {

    /** Group name prefix. */
    public static final String GROUP_NAME_PREFIX = "groupe-";
    /** User name prefix. */
    public static final String USER_NAME_PREFIX = "utilisateur-";


    /** Singleton instance. */
    private static InjectionService instance;

    /** LDAP context. */
    private final InitialLdapContext ldapContext;


    /**
     * Private constructor.
     *
     * @throws IOException
     * @throws NamingException
     */
    private InjectionService() throws IOException, NamingException {
        super();

        // LDAP properties
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, System.getProperty(InternalConstants.ENV_LDAP_CONTEXT_FACTORY));
        String url = "ldap://" + System.getProperty(InternalConstants.ENV_LDAP_HOST) + ":" + System.getProperty(InternalConstants.ENV_LDAP_PORT);
        properties.put(Context.PROVIDER_URL, url);
        properties.put(Context.SECURITY_AUTHENTICATION, "simple");
        properties.put(Context.SECURITY_PRINCIPAL, System.getProperty(InternalConstants.ENV_LDAP_PRINCIPAL));
        properties.put(Context.SECURITY_CREDENTIALS, System.getProperty(InternalConstants.ENV_LDAP_CREDENTIALS));

        // LDAP context
        this.ldapContext = new InitialLdapContext(properties, null);
    }


    /**
     * Singleton instance access.
     *
     * @return singleton instance
     */
    public static synchronized InjectionService getInstance() {
        if (instance == null) {
            try {
                instance = new InjectionService();
            } catch (Exception e) {
                throw new AssertionError(e.getMessage());
            }
        }
        return instance;
    }


    /**
     * Create LDAP groups.
     *
     * @param data injection data
     * @return names of LDAP groups
     * @throws NamingException
     */
    public List<LdapName> createGroups(InjectionData data) throws NamingException {
        int count = data.getCount();
        int depth = data.getDepth();

        List<LdapName> ldapNames = new ArrayList<LdapName>(new Double(Math.pow(count, depth + 1)).intValue());
        String groupsBaseDN = System.getProperty(InternalConstants.ENV_LDAP_GROUPS_BASE_DN);
        LdapName ldapName = new LdapName(groupsBaseDN);

        this.createGroupsRecursivity(data, ldapNames, 1, ldapName, StringUtils.EMPTY);

        return ldapNames;
    }


    /**
     * Utility method used to create LDAP groups with recursivity.
     *
     * @param data injection data
     * @param ldapNames names of LDAP groups
     * @param level current level of depth
     * @param parentName parent LDAP name
     * @param parentNumber parent number
     * @throws NamingException
     */
    private void createGroupsRecursivity(InjectionData data, List<LdapName> ldapNames, int level, LdapName parentName, String parentNumber)
            throws NamingException {
        for (int i = 1; i <= data.getCount(); i++) {
            String number = parentNumber + i;
            String dn = "cn=" + GROUP_NAME_PREFIX + number;

            // Group name
            LdapName ldapName = (LdapName) parentName.clone();
            ldapName.add(dn);

            // Group attributes
            Attributes attributes = new BasicAttributes();

            // Group object class
            Attribute objectClass = new BasicAttribute("objectclass");
            objectClass.add("groupOfUniqueNames");
            objectClass.add("top");
            attributes.put(objectClass);

            // Creation
            try {
                this.ldapContext.createSubcontext(ldapName, attributes);
            } catch (NameAlreadyBoundException e) {
                // Do nothing
            }

            ldapNames.add(ldapName);

            // Recursivity
            if (level < data.getDepth()) {
                this.createGroupsRecursivity(data, ldapNames, level + 1, ldapName, number);
            }
        }
    }


    /**
     * Create LDAP users.
     *
     * @param data injection data
     * @param ldapGroupNames names of LDAP groups
     * @throws NamingException
     */
    public void createUsers(InjectionData data, List<LdapName> ldapGroupNames) throws NamingException {
        String usersBaseDN = System.getProperty(InternalConstants.ENV_LDAP_USERS_BASE_DN);

        // Standard users
        for (LdapName ldapGroupName : ldapGroupNames) {
            String groupName = (String) ldapGroupName.getRdn(ldapGroupName.size() - 1).getValue();
            String userName = USER_NAME_PREFIX + StringUtils.removeStart(groupName, GROUP_NAME_PREFIX);
            LdapName ldapUserName = this.createUser(usersBaseDN, userName);

            // Add user to group
            this.addUserToGroup(ldapUserName, ldapGroupName);
        }

        // Random users
        for (int i = 0; i < (data.getCount() - ldapGroupNames.size()); i++) {
            List<LdapName> randomLdapGroupNames = randomPicking(ldapGroupNames, data.getProbabilities());

            // User name
            StringBuffer buffer = new StringBuffer();
            buffer.append(USER_NAME_PREFIX);
            for (LdapName randomLdapGroupName : randomLdapGroupNames) {
                String groupName = (String) randomLdapGroupName.getRdn(randomLdapGroupName.size() - 1).getValue();
                buffer.append(StringUtils.removeStart(groupName, GROUP_NAME_PREFIX));
                buffer.append("-");
            }
            String userName = buffer.toString();
            LdapName ldapUserName = this.createUser(usersBaseDN, userName);

            // Add user to groups
            for (LdapName randomLdapGroupName : randomLdapGroupNames) {
                this.addUserToGroup(ldapUserName, randomLdapGroupName);
            }
        }
    }


    /**
     * Utility method used to create user.
     *
     * @param usersBaseDN users base DN
     * @param userName user name
     * @return user LDAP name
     * @throws NamingException
     */
    private LdapName createUser(String usersBaseDN, String userName) throws NamingException {
        String dn = "uid=" + userName;

        // User name
        LdapName ldapUserName = new LdapName(usersBaseDN);
        ldapUserName.add(dn);

        // User attributes
        Attributes attributes = new BasicAttributes();

        // Group object class
        Attribute objectClass = new BasicAttribute("objectclass");
        objectClass.add("person");
        objectClass.add("inetOrgPerson");
        objectClass.add("organizationalPerson");
        objectClass.add("top");
        attributes.put(objectClass);

        // Other attributes
        attributes.put("cn", userName);
        attributes.put("sn", userName);
        attributes.put("userPassword", "secret");

        // Creation
        try {
            this.ldapContext.createSubcontext(ldapUserName, attributes);
        } catch (NameAlreadyBoundException e) {
            // Do nothing
        }
        return ldapUserName;
    }


    /**
     * Utility method used to add user to group.
     *
     * @param ldapUserName LDAP user name
     * @param ldapGroupName LDAP group name
     * @throws NamingException
     */
    private void addUserToGroup(LdapName ldapUserName, LdapName ldapGroupName) throws NamingException {
        Attribute uniqueMember = new BasicAttribute("uniqueMember", ldapUserName.toString());
        ModificationItem[] mods = new ModificationItem[]{new ModificationItem(DirContext.ADD_ATTRIBUTE, uniqueMember)};
        try {
            this.ldapContext.modifyAttributes(ldapGroupName, mods);
        } catch (AttributeInUseException e) {
            // Do nothing
        }
    }


    /**
     * Create Nuxeo documents.
     *
     * @param nuxeoController Nuxeo controller
     * @param ldapNames names of LDAP groups
     * @param data injection data
     * @throws CMSException
     */
    public void createDocuments(NuxeoController nuxeoController, List<LdapName> ldapNames, InjectionData data) throws CMSException {
        int authType = nuxeoController.getAuthType();
        nuxeoController.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
        try {
            // Create Nuxeo documents command
            CreateDocumentsCommand command = new CreateDocumentsCommand(nuxeoController, ldapNames, data);
            nuxeoController.executeNuxeoCommand(command);
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        } finally {
            nuxeoController.setAuthType(authType);
        }
    }


    /**
     * Get a random LDAP names picking from a LDAP name list and probabilities.
     *
     * @param ldapNames LDAP names
     * @param probabilities probabilities
     * @return random LDAP names picking
     */
    public static List<LdapName> randomPicking(List<LdapName> ldapNames, float[] probabilities) {
        Random random = new Random();
        Set<LdapName> result = new HashSet<LdapName>();

        for (float probability : probabilities) {
            if (random.nextFloat() < probability) {
                int index = random.nextInt(ldapNames.size());
                LdapName ldapName = ldapNames.get(index);
                result.add(ldapName);
            }
        }

        return new ArrayList<LdapName>(result);
    }

}
