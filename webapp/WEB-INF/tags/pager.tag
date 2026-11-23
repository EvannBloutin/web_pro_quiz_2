<%@ tag description="Pagination links" pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="result" required="true" type="com.campuslibrary.util.Page" %>
<%@ attribute name="path" required="true" %>
<%@ attribute name="query" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cl" uri="https://campuslibrary.example/tags" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:if test="${result.pages > 1}">
<c:set var="base" value="${ctx}${path}?${query}${empty query ? '' : '&'}page="/>
<nav class="pager" aria-label="Pagination">
  <c:choose>
    <c:when test="${result.hasPrev}"><a class="pg" href="${base}${result.page - 1}" aria-label="Previous page">&lsaquo;</a></c:when>
    <c:otherwise><span class="pg is-off">&lsaquo;</span></c:otherwise>
  </c:choose>
  <c:forEach begin="1" end="${result.pages}" var="p">
    <c:set var="gap" value="${p - result.page}"/>
    <c:choose>
      <c:when test="${p == result.page}"><span class="pg is-on" aria-current="page">${p}</span></c:when>
      <c:when test="${p == 1 or p == result.pages or (gap >= -1 and gap <= 1)}"><a class="pg" href="${base}${p}">${p}</a></c:when>
      <c:when test="${gap == -2 or gap == 2}"><span class="pg is-off">&hellip;</span></c:when>
    </c:choose>
  </c:forEach>
  <c:choose>
    <c:when test="${result.hasNext}"><a class="pg" href="${base}${result.page + 1}" aria-label="Next page">&rsaquo;</a></c:when>
    <c:otherwise><span class="pg is-off">&rsaquo;</span></c:otherwise>
  </c:choose>
</nav>
</c:if>
