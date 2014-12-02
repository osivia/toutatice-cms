<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ page isELIgnored="false" %>


<portlet:defineObjects />

<portlet:actionURL var="addAction">
    <portlet:param name="action" value="add"/>
</portlet:actionURL>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<c:set var="namespace"><portlet:namespace /></c:set>

<c:set var="idDateFrom" value="${namespace}-date-from" />
<c:set var="idDateTo" value="${namespace}-date-to" />



<script>
    $JQry(function() {
        var dates = $JQry("#${idDateFrom}, #${idDateTo}").datepicker({
            defaultDate: "+1w",
            changeMonth: true,
            numberOfMonths: 1,
            dateFormat: 'dd/mm/yy',
            onSelect: function( selectedDate ) {
                var option = (this.id.indexOf("-date-from", this.id.length - 10) !== -1) ? "minDate" : "maxDate",
                    instance = $JQry( this ).data( "datepicker" ),
                    date = $JQry.datepicker.parseDate(
                        instance.settings.dateFormat ||
                        $JQry.datepicker._defaults.dateFormat,
                        selectedDate, instance.settings );
                dates.not( this ).datepicker( "option", option, date );
            }
        });
    });
</script>


<c:if test="${not empty libelle}">
    <span class="selector-libelle">${libelle}</span>
</c:if>


<div class="nuxeo-keywords-selector">
    <form method="post" action="${addAction}">
        <div class="table">
            <c:choose>
                <c:when test='${datesMonoValued == "1"}'>
                    <!-- Mono-valued -->
                    <c:set var="interval" value="${fn:split(dates[0], '%')}" />
                    <c:set var="dateFrom" value="${interval[0]}" />
                    <c:set var="dateTo" value="${interval[1]}" />
                    <c:set var="imageSource" value="${contextPath}/img/submit.gif" />
                    <c:set var="imageTitle" value="Valider" />
                </c:when>
                
                <c:otherwise>
                    <!-- Multi-valued -->
                    <c:set var="imageSource" value="${contextPath}/img/add.gif" />
                    <c:set var="imageTitle" value="Ajouter" />
                        
                    <c:forEach var="item" items="${dates}" varStatus="status">
                        <c:set var="interval" value="${fn:split(item, '%')}" />
                        <portlet:actionURL var="deleteAction">
                            <portlet:param name="action" value="delete"/>
                            <portlet:param name="occ" value="${status.count}"/>
                        </portlet:actionURL>
                        
                        <div class="table-row">
                            <div class="table-cell">Du ${interval[0]} au ${interval[1]}</div>
                            <div class="table-cell">                                
                                <a href="${deleteAction}" class="delete" title="Supprimer"></a>
                            </div>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            
            <div class="table-row">
                <div class="table-cell input-date">
                    <input type="text" id="${idDateFrom}" name="${idDateFrom}" value="${dateFrom}" />
                    <input type="text" id="${idDateTo}" name="${idDateTo}" value="${dateTo}" />
                </div>
                <div class="table-cell">
                    <portlet:actionURL var="addAction">
                        <portlet:param name="action" value="add"/>
                    </portlet:actionURL>
                        
                    <input type="image" src="${imageSource}" title="${imageTitle}" />
                </div>
                
                <c:if test='${datesMonoValued == "1"}'>
                    <div class="table-cell">
                        <input type="image" src="${contextPath}/img/delete.gif" onclick="clearText(this)" title="Effacer" />
                    </div>
                </c:if>
            </div>
        </div>
    </form>
</div>
