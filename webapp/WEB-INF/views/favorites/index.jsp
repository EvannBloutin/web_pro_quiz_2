<%@ page contentType="text/html;charset=UTF-8" %>
<t:layout title="Favorites" active="favorites">
<div class="ph"><div><h1>Favorites</h1><p class="muted">Your wish list: ${result.total} book${result.total == 1 ? '' : 's'}.</p></div></div>
<c:choose>
  <c:when test="${result.total == 0}"><div class="empty empty-lg"><h2>No favorite yet</h2><p>Use the heart button on a book page to keep it here.</p><a class="btn btn-primary" href="${ctx}/books">Browse the catalogue</a></div></c:when>
  <c:otherwise>
    <div class="shelf"><c:forEach items="${result.items}" var="b"><t:bookcard b="${b}"/></c:forEach></div>
    <t:pager result="${result}" path="/favorites" query=""/>
  </c:otherwise>
</c:choose>
</t:layout>
