<%@ tag description="Book cover: generated typographic cover, replaced by the real cover when one can be loaded" pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="id" required="true" type="java.lang.Long" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="author" required="true" %>
<%@ attribute name="isbn" required="false" %>
<%@ attribute name="url" required="false" %>
<%@ attribute name="color" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cl" uri="https://campuslibrary.example/tags" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<div class="cover p${id % 4}" style="--c:${cl:h(color)}"><span class="cover-t">${cl:h(cl:truncate(title, 52))}</span><span class="cover-a">${cl:h(cl:truncate(author, 34))}</span><c:if test="${applicationScope.coversOnline}"><c:choose><c:when test="${not empty url}"><img class="cover-img" loading="lazy" alt="" src="${cl:h(url)}" onerror="this.remove()"></c:when><c:when test="${not empty isbn}"><img class="cover-img" loading="lazy" alt="" src="https://covers.openlibrary.org/b/isbn/${cl:enc(isbn)}-M.jpg?default=false" onerror="this.remove()"></c:when></c:choose></c:if></div>
