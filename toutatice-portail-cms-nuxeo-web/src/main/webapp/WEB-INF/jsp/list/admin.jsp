<%@ page import="fr.toutatice.portail.cms.nuxeo.api.NuxeoController"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>


<%@ page isELIgnored="false" %>


<portlet:defineObjects />

<portlet:actionURL name="save" var="saveAdminURL" />

<c:if test="${configuration.beanShell}">
    <c:set var="beanShellChecked" value="checked" />
</c:if>

<c:if test="${configuration.forceVCS}">
    <c:set var="forceVCSChecked" value="checked" />
</c:if>

<c:if test="${configuration.metadataDisplay}">
    <c:set var="metadataDisplayChecked" value="checked" />
</c:if>

<c:if test="${configuration.nuxeoRequestDisplay}">
    <c:set var="nuxeoRequestDisplayChecked" value="checked" />
</c:if>


<form action="${saveAdminURL}" method="post" class="form-horizontal" role="form">
    <fieldset>
        <legend>
            <i class="halflings halflings-cog"></i>
            <span><op:translate key="LIST_REQUEST_CONFIGURATION" /></span>
        </legend>

        <!-- Example -->
        <div class="panel panel-info">
            <div class="panel-heading">
                <a href="#bean-shell-example" class="no-ajax-link" data-toggle="collapse">
                    <i class="halflings halflings-info-sign"></i>
                    <span><op:translate key="LIST_BEAN_SHELL_EXAMPLE" /></span>
                    </a>
                </div>
                <div id="bean-shell-example" class="panel-collapse collapse">
                    <div class="panel-body">
                        <p>Implicits variables :</p>
                        <dl>
                            <dt>navigationPath</dt>
                            <dd>current navigation folder path</dd>
                            
                            <dt>navigationPubInfos</dt>
                            <dd>current navigation publication infos ; navigationPubInfos.getLiveId() to get folder's live ID</dd>
                            
                            <dt>basePath</dt>
                            <dd>page folder path</dd>
                            
                            <dt>contentPath</dt>
                            <dd>current item path</dd>
                            
                            <dt>request</dt>
                            <dd>portlet request</dd>
                            
                            <dt>params</dt>
                            <dd>public selectors (shared parameters)</dd>
                            
                            <dt>spaceId</dt>
                            <dd>space's (workspace or published space) live ID</dd>
                        </dl>
    
<pre>
StringBuilder nuxeoRequest = new StringBuilder();

nuxeoRequest.append("ecm:path STARTSWITH '").append(navigationPath).append("' ");

// Format search by title
if (params.get("title") != null) {
    nuxeoRequest.append("AND ").append(NXQLFormater.formatTextSearch("dc:title", params.get("title"))).append(" ");
}

// Format search by dates
if (params.get("datesId") != null) {
    nuxeoRequest.append("AND ").append(NXQLFormater.formatDateSearch("dc:created", params.get("datesId"))).append(" ");
}

// Format search by nature
if (params.get("nature") != null) {
    nuxeoRequest.append("AND ").append(NXQLFormater.formatVocabularySearch("dc:nature", params.get("nature"))).append(" ");
}

// Get childrens
nuxeoRequest.append("AND ecm:parentId = '").append(navigationPubInfos.getLiveId()).append("' ");

nuxeoRequest.append("ORDER BY dc:modified DESC");

return nuxeoRequest.toString();
</pre>
                    </div>
                </div>
            </div>
    
            <!-- Nuxeo request -->
        <div class="form-group">
            <label for="nuxeo-request" class="control-label col-sm-4"><op:translate key="LIST_NUXEO_REQUEST" /></label>
            <div class="col-sm-8">
                <textarea id="nuxeo-request" name="nuxeoRequest" rows="10" class="form-control">${configuration.nuxeoRequest}</textarea>

                <div class="checkbox">
                    <label>
                        <input type="checkbox" name="beanShell" ${beanShellChecked}>
                        <span><op:translate key="LIST_BEAN_SHELL" /></span>
                    </label>
                </div>
            </div>
        </div>
        <div class="form-group">
            <!-- Set type -->
		    <label for="setType" class="control-label col-sm-4"><op:translate key="LIST_SETTYPE" /></label>
			<div class="col-sm-8">
				<select id="setType" name="setType" class="form-control" onchange="$JQry('#paging').prop('disabled',$JQry(this).val().length > 0)">
					<option value=""></option>
					<c:forEach var="setType" items="${setTypes}">
						<c:remove var="selectedSet" />
						<c:if test="${setType.id eq configuration.setType}">
							<c:set var="selectedSet" value="selected" />
						</c:if>

						<option value="${setType.id}" ${selectedSet}><op:translate key="${setType.key}" classLoader="${setType.customizedClassLoader}"/></option>
					</c:forEach>
				</select>
			</div>
		</div>
		<div class="form-group">
			<div class="checkbox col-sm-8 col-sm-offset-4">
                <label>
                    <input type="checkbox" name="forceVCS" ${forceVCSChecked}>
                    <span><op:translate key="LIST_FORCE_VCS" /></span>
                </label>
            </div>
        </div>

        <!-- Version -->
        <div class="form-group">                
            <label for="cms-version" class="control-label col-sm-4"><op:translate key="LIST_VERSION" /></label>
            <div class="col-sm-8">
                <span>${versions}</span>
            </div>
        </div>
    
        <!-- Content filter -->
        <div class="form-group">                
            <label for="cms-filter" class="control-label col-sm-4"><op:translate key="LIST_CONTENT_FILTER" /></label>
            <div class="col-sm-8">
                <span>${contentFilters}</span>
            </div>
        </div>
        
        <!-- Scope -->
        <div class="form-group">
            <label for="cms-scope" class="control-label col-sm-4"><op:translate key="LIST_SCOPE" /></label>
            <div class="col-sm-8">
                <span>${scopes}</span>
            </div>
        </div>
    </fieldset>
    
    <fieldset>
        <legend>
            <i class="glyphicons glyphicons-display"></i>
            <span><op:translate key="LIST_DISPLAY_CONFIGURATION" /></span>
        </legend>

		 <!-- Template -->
        <div class="form-group">
            <label for="template" class="control-label col-sm-4"><op:translate key="LIST_TEMPLATE" /></label>
            <div class="col-sm-8">
                <select id="template" name="template" class="form-control">
                    <c:forEach var="template" items="${templates}">
                        <c:remove var="selected" />
                        <c:if test="${template.key eq configuration.template}">
                            <c:set var="selected" value="selected" />
                        </c:if>
                    
                        <option value="${template.key}" ${selected}>${template.label}</option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <!-- Request display -->
        <div class="form-group">
            <label for="request-display" class="control-label col-sm-4"><op:translate key="LIST_NUXEO_REQUEST" /></label>
            <div class="col-sm-8">
                <div class="checkbox">
                    <label>
                        <input id="request-display" type="checkbox" name="nuxeoRequestDisplay" ${nuxeoRequestDisplayChecked}>
                        <span><op:translate key="LIST_DISPLAY_NUXEO_REQUEST" /></span>
                    </label>
                </div>
            </div>
        </div>
    	<fieldset id="paging" ${empty configuration.setType? '' : 'disabled'}>
	        <!-- Results limit -->
	        <div class="form-group">                
	            <label for="results-limit" class="control-label col-sm-4"><op:translate key="LIST_RESULTS_LIMIT" /></label>
	            <div class="col-sm-8">
	                <input id="results-limit" type="number" name="resultsLimit" value="${configuration.resultsLimit}" class="form-control" />
	            </div>
	        </div>
	        
	        <!-- Normal view pagination -->
	        <div class="form-group">                
	            <label for="normal-pagination" class="control-label col-sm-4"><op:translate key="LIST_NORMAL_PAGINATION" /></label>
	            <div class="col-sm-8">
	                <input id="normal-pagination" type="number" name="normalPagination" value="${configuration.normalPagination}" class="form-control" />
	            </div>
	        </div>
	        
	        <!-- Maximized view pagination -->
	        <div class="form-group">                
	            <label for="maximized-pagination" class="control-label col-sm-4"><op:translate key="LIST_MAXIMIZED_PAGINATION" /></label>
	            <div class="col-sm-8">
	                <input id="maximized-pagination" type="number" name="maximizedPagination" value="${configuration.maximizedPagination}" class="form-control" />
	            </div>
	        </div>
        </fieldset>
        
		<!-- Metadata -->
        <div class="form-group">
            <label for="metadata-display" class="control-label col-sm-4"><op:translate key="LIST_METADATA" /></label>
            <div class="col-sm-8">
                <div class="checkbox">
                    <label>
                        <input id="metadata-display" type="checkbox" name="metadataDisplay" ${metadataDisplayChecked}>
                        <span><op:translate key="LIST_METADATA_DISPLAY" /></span>
                    </label>
                </div>
            </div>
        </div>            
        

    </fieldset>
    
    <fieldset>
        <legend>
            <i class="social social-rss"></i>
            <span><op:translate key="LIST_PERMALINK_RSS_CONFIGURATION" /></span>
        </legend>
    
        <!-- Permalink reference -->
        <div class="form-group">                
            <label for="permalink-reference" class="control-label col-sm-4"><op:translate key="LIST_PERMALINK_REFERENCE" /></label>
            <div class="col-sm-8">
                <input id="permalink-reference" type="text" name="permalinkReference" value="${configuration.permalinkReference}" class="form-control" />
            </div>
        </div>
        
        <!-- RSS reference -->
        <div class="form-group">                
            <label for="rss-reference" class="control-label col-sm-4"><op:translate key="LIST_RSS_REFERENCE" /></label>
            <div class="col-sm-8">
                <input id="rss-reference" type="text" name="rssReference" value="${configuration.rssReference}" class="form-control" />
            </div>
        </div>
        
        <!-- RSS title -->
        <div class="form-group">                
            <label for="rss-title" class="control-label col-sm-4"><op:translate key="LIST_RSS_TITLE" /></label>
            <div class="col-sm-8">
                <input id="rss-title" type="text" name="rssTitle" value="${configuration.rssTitle}" class="form-control" />
            </div>
        </div>
    </fieldset>
    
    <fieldset>
        <legend>
            <i class="halflings halflings-plus-sign"></i>
            <span><op:translate key="LIST_CONTENT_CREATION_CONFIGURATION" /></span>
        </legend>
    
        <!-- Creation parent container path -->
        <div class="form-group">                
            <label for="parent-path" class="control-label col-sm-4"><op:translate key="LIST_CONTENT_PARENT_PATH" /></label>
            <div class="col-sm-8">
                <input id="parent-path" type="text" name="creationParentPath" value="${configuration.creationParentPath}" class="form-control" />
            </div>
        </div>
        
        <!-- Creation content type -->
        <div class="form-group">                
            <label for="content-type" class="control-label col-sm-4"><op:translate key="LIST_CONTENT_TYPE" /></label>
            <div class="col-sm-8">
                <input id="content-type" type="text" name="creationContentType" value="${configuration.creationContentType}" class="form-control" />
            </div>
        </div>
    </fieldset>
    
    <!-- Buttons -->
    <div class="row">
        <div class="col-sm-offset-4 col-sm-8">
            <button type="submit" class="btn btn-primary navbar-btn">
                <i class="halflings halflings-floppy-disk"></i>
                <span><op:translate key="SAVE" /></span>
            </button>
            
            <button type="button" class="btn btn-secondary navbar-btn" onclick="closeFancybox()"><op:translate key="CANCEL" /></button>
        </div>
    </div>
</div>
