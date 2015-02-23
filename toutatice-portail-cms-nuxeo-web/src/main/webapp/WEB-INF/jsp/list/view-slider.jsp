<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="internationalization" prefix="is"%>
<%@ taglib uri="toutatice" prefix="ttc"%>

<%@ page isELIgnored="false"%>


<script src="${pageContext.request.contextPath}/bxslider/jquery.bxslider.min.js"></script>
<link href="${pageContext.request.contextPath}/bxslider/jquery.bxslider.css" rel="stylesheet" />

<script type="text/javascript">
	var temporisation = ${timer}*1000;
</script>

<script src="${pageContext.request.contextPath}/js/bxslider-integration.js"></script>

<div class="bxslider-container">
    <ul class="list-unstyled bxslider clearfix">
        <c:forEach var="document" items="${documents}" varStatus="status">
            <li class="bxslider-slide">
                <article class="clearfix">
                	<!-- To use in included jsp -->
                	<c:set var="doc" value="${document}" scope="request" />
                    <jsp:include page="slider-content/${docType}.jsp" />
                </article>
            </li>
        </c:forEach>
    </ul>
</div>
