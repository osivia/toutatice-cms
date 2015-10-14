<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page contentType="text/html" isELIgnored="false" %>


<portlet:defineObjects/>

<c:set var="namespace"><portlet:namespace/></c:set>


<!-- Tabs -->
<c:set var="currentTab" value="tags" scope="request" />
<jsp:include page="includes/tabs.jsp" />


<section>
    <!-- Title -->
    <h3>
        <op:translate key="TAGS" />
    </h3>


    <!-- Document link -->
    <ttc:documentLink document="${document}" displayContext="breadcrumb" var="documentLink" />
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">Document link</h4>
        </div>
        
        <div class="panel-body">
            <dl class="dl-horizontal">
                <dt>Chemin du document</dt>
                <dd>${configuration.path}</dd>
            
                <dt>Syntaxe</dt>
                <dd>
                    <code>&lt;ttc:documentLink document=&quot;&dollar;&lbrace;document&rbrace;&quot; displayContext=&quot;breadcrumb&quot; var=&quot;link&quot; /&gt;</code>
                </dd>
                
                <dt>R&eacute;sultat</dt>
                <dd>
                    <code>&dollar;&lbrace;link.url&rbrace;</code>
                    <span> = </span>
                    <a href="${documentLink.url}" class="no-ajax-link">${documentLink.url}</a>
                </dd>
            </dl>
        </div>
    </div>
    
    
    <!-- Picture link -->
    <ttc:pictureLink document="${document}" property="ttc:vignette" var="pictureLink" />
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">Picture link</h4>
        </div>
        
        <div class="panel-body">
            <dl class="dl-horizontal">
                <dt>Chemin du document</dt>
                <dd>${configuration.path}</dd>
            
                <dt>Syntaxe</dt>
                <dd>
                    <code>&lt;ttc:pictureLink document=&quot;&dollar;&lbrace;document&rbrace;&quot; property=&quot;ttc:vignette&quot; var=&quot;link&quot; /&gt;</code>
                </dd>
                
                <dt>R&eacute;sultat</dt>
                <dd>
                    <code>&dollar;&lbrace;link.url&rbrace;</code>
                    <span> = </span>
                    <a href="${pictureLink.url}" class="no-ajax-link">${pictureLink.url}</a>
                    
                    <br>
                    
                    <code>&lt;img src=&quot;&dollar;&lbrace;link.url&rbrace;&quot; src=&quot;&quot;&gt;</code>
                    <span> = </span>
                    <img src="${pictureLink.url}" alt="">
                </dd>
            </dl>
        </div>
    </div>
    
    
    <!-- Attachment link -->
    <ttc:attachmentLink document="${document}" index="0" var="attachmentLink" />
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">Attachment link</h4>
        </div>
        
        <div class="panel-body">
            <dl class="dl-horizontal">
                <dt>Chemin du document</dt>
                <dd>${configuration.path}</dd>
            
                <dt>Syntaxe</dt>
                <dd>
                    <code>&lt;ttc:attachmentLink document=&quot;&dollar;&lbrace;document&rbrace;&quot; index=&quot;0&quot; var=&quot;link&quot; /&gt;</code>
                </dd>
                
                <dt>R&eacute;sultat</dt>
                <dd>
                    <code>&dollar;&lbrace;link.url&rbrace;</code>
                    <span> = </span>
                    <a href="${attachmentLink.url}" class="no-ajax-link">${attachmentLink.url}</a>
                </dd>
            </dl>
        </div>
    </div>
    
    
    <!-- User -->
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">User</h4>
        </div>
        
        <div class="panel-body">
            <dl class="dl-horizontal">
                <dt>Nom de l'utilisateur</dt>
                <dd>${configuration.user}</dd>
            
                <dt>Syntaxe</dt>
                <dd>
                    <code>&lt;ttc:user name=&quot;&dollar;&lbrace;user&rbrace;&quot; /&gt;</code>
                </dd>
                
                <dt>R&eacute;sultat</dt>
                <dd>
                    <ttc:user name="${configuration.user}" />
                </dd>
            </dl>
        </div>
    </div>
    
    
    <!-- Transform HTML content -->
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">Transform HTML content</h4>
        </div>
        
        <div class="panel-body">
            <dl class="dl-horizontal">
                <dt>Chemin du document</dt>
                <dd>${configuration.path}</dd>
            
                <dt>Syntaxe</dt>
                <dd>
                    <code>&lt;ttc:transform document=&quot;&dollar;&lbrace;document&rbrace;&quot; property=&quot;note:note&quot; /&gt;</code>
                </dd>
                
                <dt>R&eacute;sultat</dt>
                <dd>
                    <ttc:transform document="${document}" property="note:note" />
                </dd>
            </dl>
        </div>
    </div>
    
    
    <!-- File size -->
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">File size</h4>
        </div>
        
        <div class="panel-body">
            <dl class="dl-horizontal">
                <dt>Syntaxe</dt>
                <dd>
                    <code>&lt;ttc:fileSize size=&quot;100000&quot; /&gt;</code>
                </dd>
                
                <dt>R&eacute;sultat</dt>
                <dd>
                    <ttc:fileSize size="100000" />
                </dd>
            </dl>
        </div>
    </div>
    
    
    <!-- Add menubar item -->
    <ttc:addMenubarItem id="WIKIPEDIA" url="https://fr.wikipedia.org" glyphicon="social social-wikipedia" target="_blank" labelKey="WIKIPEDIA" />
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">Add menubar item</h4>
        </div>
        
        <div class="panel-body">
            <dl class="dl-horizontal">
                <dt>Syntaxe</dt>
                <dd>
                    <code>&lt;ttc:addMenubarItem id=&quot;WIKIPEDIA&quot; url=&quot;https://fr.wikipedia.org&quot; target=&quot;_blank&quot; labelKey=&quot;WIKIPEDIA&quot; glyphicon=&quot;social social-wikipedia&quot; /&gt;</code>
                </dd>
                
                <dt>R&eacute;sultat</dt>
                <dd class="text-muted">Voir barre de menu.</dd>
            </dl>
        </div>
    </div>
    
    
    <!-- Vocabulary label -->
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">Vocabulary label</h4>
        </div>
        
        <div class="panel-body">
            <dl class="dl-horizontal">
                <dt>Syntaxe</dt>
                <dd>
                    <code>&lt;ttc:vocabularyLabel name=&quot;continent&quot; key=&quot;africa&quot; /&gt;</code>
                </dd>
                
                <dt>R&eacute;sultat</dt>
                <dd>
                    <ttc:vocabularyLabel name="continent" key="africa" />
                </dd>
            </dl>
        </div>
    </div>
    
    
    <!-- Comments -->
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">Comments</h4>
        </div>
        
        <div class="panel-body">
            <dl class="dl-horizontal">
                <dt>Chemin du document</dt>
                <dd>${configuration.path}</dd>
            
                <dt>Syntaxe</dt>
                <dd>
                    <code>&lt;ttc:comments document=&quot;&dollar;&lbrace;document&rbrace;&quot; /&gt;</code>
                </dd>
                
                <dt>R&eacute;sultat</dt>
                <dd class="text-muted">Les commentaires ne s'affichent qu'en mode contextualisé.</dd>
            </dl>
        </div>
    </div>
    
</section>
