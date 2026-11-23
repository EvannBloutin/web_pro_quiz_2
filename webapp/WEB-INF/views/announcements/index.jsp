<%@ page contentType="text/html;charset=UTF-8" %>
<t:layout title="Announcements" active="announcements">
<div class="ph">
  <div><h1>Announcements</h1><p class="muted">News from the librarians.</p></div>
  <c:if test="${user.admin}"><a class="btn btn-primary" href="${ctx}/announcements/new"><t:icon name="plus"/>New announcement</a></c:if>
</div>
<c:if test="${empty items}"><div class="empty empty-lg"><h2>Nothing to announce</h2><p>New announcements will appear here.</p></div></c:if>
<div class="notes">
<c:forEach items="${items}" var="a">
  <article class="card note note-lg ${a.pinned ? 'is-pinned' : ''}">
    <div class="note-h"><h2><c:if test="${a.pinned}"><span class="pin" title="Pinned"><t:icon name="pin"/></span></c:if>${cl:h(a.title)}</h2>
      <c:if test="${user.admin}"><span class="row-actions">
        <a class="btn btn-ghost btn-sm" href="${ctx}/announcements/${a.id}/edit"><t:icon name="edit"/>Edit</a>
        <form method="post" action="${ctx}/announcements/${a.id}/delete" data-confirm="Delete this announcement?"><t:csrf/><button class="btn btn-danger-ghost btn-sm" type="submit"><t:icon name="trash"/>Delete</button></form></span></c:if></div>
    <p>${cl:h(a.body)}</p>
    <small class="muted">${cl:dateTime(a.createdAt)} &middot; ${cl:h(a.authorName)}</small>
  </article>
</c:forEach>
</div>
</t:layout>
