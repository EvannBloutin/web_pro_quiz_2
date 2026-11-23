<%@ page contentType="text/html;charset=UTF-8" %>
<t:layout title="Reservations" active="reservations">
<div class="ph">
  <div><h1>${scopeAll ? 'All reservations' : 'My reservations'}</h1><p class="muted">Join the queue for a book with no free copy. When a copy comes back, the first person in line is notified here.</p></div>
  <c:if test="${user.admin}"><div class="seg"><a class="${scopeAll ? '' : 'is-on'}" href="${ctx}/reservations">Mine</a><a class="${scopeAll ? 'is-on' : ''}" href="${ctx}/reservations?scope=all">Everyone</a></div></c:if>
</div>
<c:choose>
<c:when test="${empty items}"><div class="empty empty-lg"><h2>No reservation</h2><p>When every copy of a book is on loan, use Reserve on its page.</p><a class="btn btn-primary" href="${ctx}/books">Browse the catalogue</a></div></c:when>
<c:otherwise>
<div class="card table-wrap">
  <table class="tbl">
    <thead><tr><th>Book</th><c:if test="${scopeAll}"><th>Member</th></c:if><th>Reserved on</th><th>Status</th><th class="actions-h"><span class="sr">Actions</span></th></tr></thead>
    <tbody>
    <c:forEach items="${items}" var="r">
      <tr>
        <td><div class="bcell"><a class="thumb" href="${ctx}/books/${r.bookId}"><t:cover id="${r.bookId}" title="${r.bookTitle}" author="${r.bookAuthor}" isbn="${r.bookIsbn}" url="${r.bookCoverUrl}" color="${r.categoryColor}"/></a>
          <span><a href="${ctx}/books/${r.bookId}"><strong>${cl:h(r.bookTitle)}</strong></a><br><small class="muted">${cl:h(r.bookAuthor)}</small></span></div></td>
        <c:if test="${scopeAll}"><td>${cl:h(r.userName)}</td></c:if>
        <td>${cl:date(r.createdAt)}</td>
        <td>
          <c:choose>
            <c:when test="${r.status == 'READY'}"><span class="badge badge-hot">Ready to borrow</span></c:when>
            <c:when test="${r.status == 'WAITING'}"><span class="badge badge-warn">Waiting &middot; number ${r.position}</span></c:when>
            <c:when test="${r.status == 'FULFILLED'}"><span class="badge badge-ok">Borrowed</span></c:when>
            <c:otherwise><span class="badge badge-muted">Cancelled</span></c:otherwise>
          </c:choose>
        </td>
        <td class="row-actions">
          <c:if test="${r.status == 'READY' and r.userId == user.id}"><form method="post" action="${ctx}/books/${r.bookId}/borrow"><t:csrf/><input type="hidden" name="back" value="/reservations"><button class="btn btn-primary btn-sm" type="submit"><t:icon name="book"/>Borrow now</button></form></c:if>
          <c:if test="${r.open}"><form method="post" action="${ctx}/reservations/${r.id}/cancel" data-confirm="Cancel this reservation?"><t:csrf/><input type="hidden" name="back" value="/reservations${scopeAll ? '?scope=all' : ''}"><button class="btn btn-ghost btn-sm" type="submit">Cancel</button></form></c:if>
        </td>
      </tr>
    </c:forEach>
    </tbody>
  </table>
</div>
</c:otherwise>
</c:choose>
</t:layout>
