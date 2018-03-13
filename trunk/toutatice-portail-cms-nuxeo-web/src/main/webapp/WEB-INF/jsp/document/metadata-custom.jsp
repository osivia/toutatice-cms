<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op" %>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc"%>

<%@ page isELIgnored="false"%>

<c:set var="subjects" value="${document.properties['dc:subjects']}" />
<c:set var="nature" value="${document.properties['dc:nature']}" />




                <!-- Subjects -->
                <c:if test="${not empty subjects}">
                    <dt><op:translate key="DOCUMENT_METADATA_SUBJECTS" /></dt>
                    <dd>
                        <p>
                            <ttc:formatVocabulary3Level document="${document}" xpath="dc:subjects" vocabulary="topic" child1="subtopic" />
                        </p>
                    </dd>
                </c:if>
            
                <!-- Nature -->
                <c:if test="${not empty nature}">
                    <dt><op:translate key="DOCUMENT_METADATA_NATURE" /></dt>
                    <dd>
                        <p>
                            <ttc:vocabularyLabel name="nature" key="${nature}"/>
                        </p>
                    </dd>
                </c:if>
                
                