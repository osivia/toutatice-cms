<%@ taglib uri="http://www.toutatice.fr/jsp/taglib/toutatice" prefix="ttc" %>

<%@ page isELIgnored="false"%>


<article class="note">
    <!-- Title -->
    <h3 class="hidden">${document.title}</h3>
    
    <!-- Content -->
    <div><ttc:transform document="${document}" property="note:note" /></div>
</article>
