<%@ page contentType="text/html;charset=UTF-8" %>
<t:layout title="Catalogue" active="books">
<div class="ph">
  <div><h1>Catalogue</h1><p class="muted">${result.total} book${result.total == 1 ? '' : 's'} ${not empty q or cat > 0 or avail ? 'match your filters' : 'on the shelves'}</p></div>
  <c:if test="${user.admin}"><a class="btn btn-primary" href="${ctx}/books/new"><t:icon name="plus"/>Add a book</a></c:if>
</div>

<form class="filters" method="get" action="${ctx}/books">
  <label class="search"><t:icon name="search"/><span class="sr">Search</span><input type="search" name="q" value="${cl:h(q)}" placeholder="Title, author or ISBN"></label>
  <label class="sel"><span class="sr">Category</span>
    <select name="category"><option value="0">All categories</option>
      <c:forEach items="${categories}" var="c"><option value="${c.id}" ${cat == c.id ? 'selected' : ''}>${cl:h(c.name)}</option></c:forEach>
    </select></label>
  <label class="sel"><span class="sr">Sort by</span>
    <select name="sort">
      <option value="" ${empty sort ? 'selected' : ''}>Newest first</option>
      <option value="title" ${sort == 'title' ? 'selected' : ''}>Title A-Z</option>
      <option value="author" ${sort == 'author' ? 'selected' : ''}>Author A-Z</option>
      <option value="year" ${sort == 'year' ? 'selected' : ''}>Publication year</option>
      <option value="rating" ${sort == 'rating' ? 'selected' : ''}>Best rated</option>
      <option value="popular" ${sort == 'popular' ? 'selected' : ''}>Most borrowed</option>
    </select></label>
  <label class="check"><input type="checkbox" name="available" value="1" ${avail ? 'checked' : ''}> Available only</label>
  <button class="btn btn-primary" type="submit">Apply</button>
  <c:if test="${not empty q or cat > 0 or avail or not empty sort}"><a class="btn btn-ghost" href="${ctx}/books">Reset</a></c:if>
</form>

<c:choose>
  <c:when test="${result.total == 0}">
    <div class="empty empty-lg"><h2>No book found</h2><p>Try another keyword, or remove a filter.</p><a class="btn btn-ghost" href="${ctx}/books">Show every book</a></div>
  </c:when>
  <c:otherwise>
    <div class="shelf"><c:forEach items="${result.items}" var="b"><t:bookcard b="${b}"/></c:forEach></div>
    <t:pager result="${result}" path="/books" query="q=${cl:enc(q)}&category=${cat}&sort=${cl:enc(sort)}${avail ? '&available=1' : ''}"/>
  </c:otherwise>
</c:choose>
</t:layout>
