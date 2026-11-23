<%@ page contentType="text/html;charset=UTF-8" %>
<t:layout title="Dashboard" active="dashboard">
<section class="hello">
  <div>
    <h1>${greeting}, ${cl:h(user.firstName)}</h1>
    <p class="lead">
      <c:choose>
        <c:when test="${stats.myOverdue > 0}">You have <strong>${stats.myOverdue}</strong> overdue book${stats.myOverdue > 1 ? 's' : ''}. Please return ${stats.myOverdue > 1 ? 'them' : 'it'} soon to stop the fine.</c:when>
        <c:when test="${stats.myActive > 0}">You have <strong>${stats.myActive}</strong> book${stats.myActive > 1 ? 's' : ''} on your desk. Nothing is overdue.</c:when>
        <c:otherwise>Your desk is empty. Find something to read in the catalogue.</c:otherwise>
      </c:choose>
    </p>
  </div>
  <dl class="nums">
    <div><dt>On loan</dt><dd>${stats.myActive}</dd></div>
    <div><dt>Overdue</dt><dd class="${stats.myOverdue > 0 ? 'bad' : ''}">${stats.myOverdue}</dd></div>
    <div><dt>Returned</dt><dd>${stats.myRead}</dd></div>
    <div><dt>Favorites</dt><dd>${stats.myFavorites}</dd></div>
  </dl>
</section>

<c:forEach items="${readyHolds}" var="h">
  <div class="banner banner-hot"><t:icon name="bookmark"/><span><strong>${cl:h(h.bookTitle)}</strong> is ready for you. Borrow it before someone else does.</span>
    <a class="btn btn-sm btn-primary" href="${ctx}/books/${h.bookId}">Open the book</a></div>
</c:forEach>

<div class="cols">
  <section class="card">
    <div class="card-h"><h2>On your desk</h2><a class="link" href="${ctx}/loans">All loans</a></div>
    <c:choose>
      <c:when test="${empty myLoans}">
        <div class="empty"><p>No book on loan right now.</p><a class="btn btn-primary btn-sm" href="${ctx}/books"><t:icon name="search"/>Browse the catalogue</a></div>
      </c:when>
      <c:otherwise>
        <ul class="desk">
          <c:forEach items="${myLoans}" var="l">
            <li>
              <a class="desk-cover" href="${ctx}/books/${l.bookId}"><t:cover id="${l.bookId}" title="${l.bookTitle}" author="${l.bookAuthor}" isbn="${l.bookIsbn}" url="${l.bookCoverUrl}" color="${l.categoryColor}"/></a>
              <div class="desk-txt">
                <a href="${ctx}/books/${l.bookId}"><strong>${cl:h(l.bookTitle)}</strong></a>
                <span class="muted">${cl:h(l.bookAuthor)}</span>
                <c:choose>
                  <c:when test="${l.overdue}"><span class="badge badge-bad">Overdue by ${l.overdueDays} day${l.overdueDays > 1 ? 's' : ''} &middot; fine ${cl:money(l.fine)}</span></c:when>
                  <c:when test="${l.dueSoon}"><span class="badge badge-warn"><c:choose><c:when test="${l.daysLeft == 0}">Due today</c:when><c:otherwise>Due in ${l.daysLeft} day${l.daysLeft == 1 ? '' : 's'}</c:otherwise></c:choose></span></c:when>
                  <c:otherwise><span class="badge badge-ok">Due ${cl:date(l.dueAt)}</span></c:otherwise>
                </c:choose>
              </div>
              <form method="post" action="${ctx}/loans/${l.id}/return"><t:csrf/><input type="hidden" name="back" value="/dashboard"><button class="btn btn-ghost btn-sm" type="submit"><t:icon name="return"/>Return</button></form>
            </li>
          </c:forEach>
        </ul>
      </c:otherwise>
    </c:choose>
  </section>

  <section class="card">
    <div class="card-h"><h2>Announcements</h2><a class="link" href="${ctx}/announcements">All</a></div>
    <c:if test="${empty announcements}"><div class="empty"><p>No announcement.</p></div></c:if>
    <c:forEach items="${announcements}" var="a">
      <article class="note">
        <h3><c:if test="${a.pinned}"><span class="pin" title="Pinned"><t:icon name="pin"/></span></c:if>${cl:h(a.title)}</h3>
        <p>${cl:h(cl:truncate(a.body, 150))}</p>
        <small class="muted">${cl:date(a.createdAt)} &middot; ${cl:h(a.authorName)}</small>
      </article>
    </c:forEach>
  </section>
</div>

<c:if test="${user.admin}">
<section class="strip" aria-label="Library figures">
  <div><strong>${stats.books}</strong><span>titles (${stats.copies} copies)</span></div>
  <div><strong>${stats.categories}</strong><span>categories</span></div>
  <div><strong>${stats.members}</strong><span>accounts</span></div>
  <div><strong>${stats.activeLoans}</strong><span>books out</span></div>
  <div><strong class="${stats.overdueLoans > 0 ? 'bad' : ''}">${stats.overdueLoans}</strong><span>overdue</span></div>
</section>
</c:if>

<div class="cols cols-even">
  <section class="card">
    <div class="card-h"><h2>Loans by category</h2></div>
    <ul class="hbars">
      <c:forEach items="${byCategory}" var="m">
        <li><span class="hb-l">${cl:h(m.name)}</span><span class="hb-track"><span class="hb-fill" style="width:${cl:pct(m.count, maxCategory)}%;background:${cl:h(m.color)}"></span></span><span class="hb-n">${m.count}</span></li>
      </c:forEach>
    </ul>
  </section>
  <section class="card">
    <div class="card-h"><h2>Loans per month</h2><span class="muted">last 6 months</span></div>
    <div class="vbars">
      <c:forEach items="${byMonth}" var="m">
        <div class="vb"><span class="vb-wrap"><span class="vb-bar" style="height:${cl:pct(m.count, maxMonth)}%"></span></span><span class="vb-n">${m.count}</span><span class="vb-l">${m.label}</span></div>
      </c:forEach>
    </div>
  </section>
</div>

<section class="block">
  <div class="card-h"><h2>Most borrowed</h2><a class="link" href="${ctx}/books?sort=popular">See more</a></div>
  <div class="shelf shelf-5"><c:forEach items="${popular}" var="b"><t:bookcard b="${b}"/></c:forEach></div>
</section>
<section class="block">
  <div class="card-h"><h2>Recently added</h2><a class="link" href="${ctx}/books">Full catalogue</a></div>
  <div class="shelf shelf-6"><c:forEach items="${recent}" var="b"><t:bookcard b="${b}"/></c:forEach></div>
</section>
</t:layout>
