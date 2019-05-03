<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.osivia.org/jsp/taglib/osivia-portal" prefix="op"%>

<portlet:defineObjects />

<portlet:actionURL name="renameDoc" var="renameDocUrl">
</portlet:actionURL>

<c:if test="${not empty error}">
	<div class="alert alert-danger alert-dismissible" role="alert">
		<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
		${error}
	</div>
</c:if>

<form action="${renameDocUrl}" method="post" role="form">
	<div class="form-group">
		<div class="input-group">
			<div class="input-group-addon"><i class="${docIcon}"></i></div>
			<input class="form-control" name="newDocTitle" type="text" value="${currentDocTitle}">
		</div>
	</div>
	<button class="btn btn-primary" type="submit"><op:translate key="RENAME_DOCUMENT"/></button>
	<button class="btn btn-secondary pull-right" type="button" data-dismiss="modal"><op:translate key="CANCEL"/></button>
</form>
