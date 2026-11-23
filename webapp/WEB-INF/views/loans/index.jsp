<%@ page contentType="text/html;charset=UTF-8" %>
<t:layout title="${user.admin ? 'Loans' : 'My loans'}" active="loans">
<div class="ph">
  <div><h1>${scopeAll ? 'All loans' : 'My loans'}</h1>
    <p class="muted">Loans last 14 days and can be renewed twice. A late book costs ${cl:money(1000)} per day.</p></div>
  <c:if test="${user.admin}">
    <div class="seg"><a class="${scopeAll ? 'is-on' : ''}" href="${ctx}/loans?status=${status}">Everyone</a><a class="${scopeAll ? '' : 'is-on'}" href="${ctx}/loans?scope=mine&status=${status}">Mine</a></div>
  </c:if>
</div>
<c:set var="scopeQ" value="${scopeAll ? '' : '&scope=mine'}"/>
<div class="tabs" role="tablist">
  <a class="${status == 'all' ? 'is-on' : ''}" href="${ctx}/loans?status=all${scopeQ}">All</a>
  <a class="${status == 'active' ? 'is-on' : ''}" href="${ctx}/loans?status=active${scopeQ}">Active</a>
  <a class="${status == 'overdue' ? 'is-on' : ''}" href="${ctx}/loans?status=overdue${scopeQ}">Overdue</a>
  <a class="${status == 'returned' ? 'is-on' : ''}" href="${ctx}/loans?status=returned${scopeQ}">Returned</a>
</div>
<c:choose>
<c:when test="${result.total == 0}"><div class="empty empty-lg"><h2>No loan here</h2><p>Nothing matches this filter yet.</p><a class="btn btn-primary" href="${ctx}/books">Browse the catalogue</a></div></c:when>
<c:otherwise>
<div class="card table-wrap">
  <table class="tbl">
    <thead><tr><th>Book</th><c:if test="${scopeAll}"><th>Member</th></c:if><th>Borrowed</th><th>Due</th><th>Status</th><th class="actions-h"><span class="sr">Actions</span></th></tr></thead>
    <tbody>
    <c:forEach items="${result.items}" var="l">
      <tr>
        <td><div class="bcell"><a class="thumb" href="${ctx}/books/${l.bookId}"><t:cover id="${l.bookId}" title="${l.bookTitle}" author="${l.bookAuthor}" isbn="${l.bookIsbn}" url="${l.bookCoverUrl}" color="${l.categoryColor}"/></a>
          <span><a href="${ctx}/books/${l.bookId}"><strong>${cl:h(l.bookTitle)}</strong></a><br><small class="muted">${cl:h(l.bookAuthor)}</small></span></div></td>
        <c:if test="${scopeAll}"><td>${cl:h(l.userName)}</td></c:if>
        <td>${cl:date(l.borrowedAt)}</td>
        <td>${cl:date(l.dueAt)}<c:if test="${l.renewals > 0}"><br><small class="muted">renewed ${l.renewals}&times;</small></c:if></td>
        <td>
          <c:choose>
            <c:when test="${l.status == 'RETURNED'}"><span class="badge badge-ok">Returned</span><br><small class="muted">${cl:date(l.returnedAt)}</small><c:if test="${l.fine > 0}"> <small class="bad-text">&middot; late ${cl:money(l.fine)}</small></c:if></c:when>
            <c:when test="${l.status == 'OVERDUE'}"><span class="badge badge-bad">Overdue ${l.overdueDays}d</span><br><small class="bad-text">Fine ${cl:money(l.fine)}</small></c:when>
            <c:when test="${l.dueSoon}"><span class="badge badge-warn">Due soon</span></c:when>
            <c:otherwise><span class="badge badge-info">On loan</span></c:otherwise>
          </c:choose>
        </td>
        <td class="row-actions">
          <c:if test="${l.active}">
            <c:if test="${l.canRenew}"><form method="post" action="${ctx}/loans/${l.id}/renew"><t:csrf/><input type="hidden" name="back" value="/loans?status=${status}${scopeQ}"><button class="btn btn-ghost btn-sm" type="submit"><t:icon name="refresh"/>Renew</button></form></c:if>
            <form method="post" action="${ctx}/loans/${l.id}/return"><t:csrf/><input type="hidden" name="back" value="/loans?status=${status}${scopeQ}"><button class="btn btn-primary btn-sm" type="submit"><t:icon name="return"/>Return</button></form>
          </c:if>
          <c:if test="${user.admin}"><form method="post" action="${ctx}/loans/${l.id}/delete" data-confirm="Delete this loan record?"><t:csrf/><input type="hidden" name="back" value="/loans?status=${status}${scopeQ}"><button class="icon-btn icon-btn-sm" type="submit" aria-label="Delete loan" title="Delete record"><t:icon name="trash"/></button></form></c:if>
        </td>
      </tr>
    </c:forEach>
    </tbody>
  </table>
</div>
<t:pager result="${result}" path="/loans" query="status=${status}${scopeQ}"/>
</c:otherwise>
</c:choose>
</t:layout>
