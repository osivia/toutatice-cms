<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc"%>

<%@ page isELIgnored="false" %>

<c:set var="pictureURL"><ttc:documentLink document="${doc}" picture="true" displayContext="Medium"/></c:set>

<div class="col-sm-12">
	<img src="${pictureURL}" alt="" class="center-block" />
</div>