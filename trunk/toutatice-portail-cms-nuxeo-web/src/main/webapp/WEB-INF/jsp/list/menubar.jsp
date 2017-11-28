<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false" %>


<c:set var="namespace"><portlet:namespace /></c:set>

<c:set var="permaLinkURL" value="${requestScope['permaLinkURL']}" />
<c:set var="rssLinkURL" value="${requestScope['rssLinkURL']}" />


<c:if test="${not empty permaLinkURL}">
    <jsp:useBean id="dataMap" class="java.util.HashMap" />
    <c:set target="${dataMap}" property="toggle" value="modal" />
    <c:set target="${dataMap}" property="target">#${namespace}-permalink</c:set>
    <ttc:addMenubarItem id="PERMALINK" labelKey="PERMALINK" order="1" url="#" glyphicon="glyphicons glyphicons-link" ajax="false" data="${dataMap}" />
    
    <div id="${namespace}-permalink" class="modal fade" tabindex="-1" role="dialog">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">
                        <span>&times;</span>
                    </button>
                    
                    <h4 class="modal-title">
                        <i class="glyphicons glyphicons-link"></i>
                        <span><op:translate key="PERMALINK"/></span>
                    </h4>
                </div>
                
                <div class="modal-body">
                    <div class="row">
                        <div id="${namespace}-permalink-link" class="col-sm-10"><a href="${permaLinkURL}">${permaLinkURL}</a></div>
                        
                        <div class="col-sm-2">
                            <button type="button" class="btn btn-default pull-right" data-clipboard-target="#${namespace}-permalink-link">
                                <i class="halflings halflings-copy"></i>
                                <span><op:translate key="COPY_PERMALINK" /></span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</c:if>


<c:if test="${not empty rssLinkURL}">
    <ttc:addMenubarItem id="RSS" labelKey="RSS" order="2" url="${rssLinkURL}" glyphicon="social social-rss" ajax="false" />
</c:if>
