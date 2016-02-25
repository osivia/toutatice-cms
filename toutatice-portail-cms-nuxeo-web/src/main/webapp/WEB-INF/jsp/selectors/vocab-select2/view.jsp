<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="internationalization" prefix="is" %>
<%@ page isELIgnored="false" %>


<portlet:defineObjects />

<!-- <portlet:actionURL var="actionURL" /> -->

<portlet:resourceURL id="vocabSearch" var="vocabSearchUrl" >
	<portlet:param name="vocid" value="${vocabName}"></portlet:param>
</portlet:resourceURL>

<portlet:renderURL var="url">
    <portlet:param name="vocabId" value="SELECTED_VALUE" />
</portlet:renderURL>

<c:set var="namespace"><portlet:namespace /></c:set>

<script type="text/javascript">


	$JQry(document).ready(function() {
		$JQry(".${namespace}-select2").select2({
		    ajax: {
			      url: "${vocabSearchUrl}",
			      dataType: 'json',
			      delay: 300,
			      data: function (params) {
			        return {
			          filter: params.term, // search term
			        };
			      },
			      processResults: function (data, params) {
			        return {
			          results: $JQry.map(data, function(entry) {
			        	  return { id: entry.key, text: entry.value };
			          })
			        };
			      },
			      cache: true
			    },
			    theme: "bootstrap",
		});
	});
</script>

<!-- Label -->
<c:if test="${not empty libelle}">
    <label>${libelle}</label>
</c:if>

<div class="col-sm-12">
	<select name="${vocabName}" class="${namespace}-select2 col-sm-12" onchange="refreshOnVocabularyChange(this, '${url}')">
		
	</select>
</div>