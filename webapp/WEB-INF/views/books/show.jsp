<%@ page contentType="text/html;charset=UTF-8" %>
<t:layout title="${book.title}" active="books">
<a class="back" href="${ctx}/books"><t:icon name="back"/>Catalogue</a>
<article class="detail">
  <div class="detail-cover"><t:cover id="${book.id}" title="${book.title}" author="${book.author}" isbn="${book.isbn}" url="${book.coverUrl}" color="${book.categoryColor}"/></div>
  <div class="detail-main">
    <p class="chip"><span class="dot" style="background:${cl:h(book.categoryColor)}"></span>${cl:h(book.categoryName)}</p>
    <h1>${cl:h(book.title)}</h1>
    <p class="by">${cl:h(book.author)}</p>
    <div class="rate-line">
      <t:stars value="${book.avgRating}"/>
      <span><c:choose><c:when test="${book.reviewCount > 0}"><strong>${book.avgRatingText}</strong> from ${book.reviewCount} review${book.reviewCount == 1 ? '' : 's'}</c:when><c:otherwise>No review yet</c:otherwise></c:choose></span>
    </div>
    <p class="desc">${empty book.description ? 'No description for this book yet.' : cl:h(book.description)}</p>
    <dl class="facts">
      <div><dt>Published</dt><dd>${empty book.publishedYear ? 'n/a' : book.publishedYear}</dd></div>
      <div><dt>ISBN</dt><dd>${cl:h(book.isbn)}</dd></div>
      <div><dt>Copies</dt><dd><span class="badge ${book.available ? 'badge-ok' : 'badge-bad'}">${book.copiesAvailable} of ${book.copiesTotal} available</span></dd></div>
      <div><dt>Borrowed</dt><dd>${book.loanCount} time${book.loanCount == 1 ? '' : 's'}</dd></div>
    </dl>

    <div class="actions">
      <c:choose>
        <c:when test="${not empty activeLoan}">
          <div class="inline-note ${activeLoan.overdue ? 'is-bad' : ''}"><t:icon name="clock"/>You have this book. ${activeLoan.overdue ? 'It is overdue.' : 'Due '}<c:if test="${not activeLoan.overdue}">${cl:date(activeLoan.dueAt)}</c:if></div>
          <form method="post" action="${ctx}/loans/${activeLoan.id}/return"><t:csrf/><input type="hidden" name="back" value="/books/${book.id}"><button class="btn btn-ghost" type="submit"><t:icon name="return"/>Return it</button></form>
        </c:when>
        <c:when test="${canBorrow}">
          <form method="post" action="${ctx}/books/${book.id}/borrow"><t:csrf/><button class="btn btn-primary" type="submit"><t:icon name="book"/>Borrow for 14 days</button></form>
        </c:when>
        <c:when test="${book.available}">
          <span class="inline-note"><t:icon name="alert"/>You reached the limit of 3 active loans.</span>
        </c:when>
        <c:when test="${not empty myReservation}">
          <div class="inline-note"><t:icon name="bookmark"/><c:choose><c:when test="${myReservation.ready}">Your reserved copy is ready.</c:when><c:otherwise>You are number ${myReservation.position} in the queue.</c:otherwise></c:choose></div>
          <form method="post" action="${ctx}/reservations/${myReservation.id}/cancel"><t:csrf/><input type="hidden" name="back" value="/books/${book.id}"><button class="btn btn-ghost" type="submit">Leave the queue</button></form>
        </c:when>
        <c:otherwise>
          <form method="post" action="${ctx}/books/${book.id}/reserve"><t:csrf/><button class="btn btn-primary" type="submit"><t:icon name="bookmark"/>Reserve this book</button></form>
        </c:otherwise>
      </c:choose>
      <form method="post" action="${ctx}/books/${book.id}/favorite"><t:csrf/>
        <button class="btn btn-ghost ${book.favorite ? 'is-fav' : ''}" type="submit"><t:icon name="heart"/>${book.favorite ? 'In favorites' : 'Add to favorites'}</button></form>
      <c:if test="${user.admin}">
        <a class="btn btn-ghost" href="${ctx}/books/${book.id}/edit"><t:icon name="edit"/>Edit</a>
        <form method="post" action="${ctx}/books/${book.id}/delete" data-confirm="Delete this book and its loans, reviews and favorites?"><t:csrf/><button class="btn btn-danger-ghost" type="submit"><t:icon name="trash"/>Delete</button></form>
      </c:if>
    </div>
  </div>
</article>

<section class="reviews" id="reviews">
  <div class="card-h"><h2>Reviews <span class="muted">(${book.reviewCount})</span></h2></div>
  <div class="reviews-grid">
    <form class="card review-form" method="post" action="${ctx}/books/${book.id}/review">
      <t:csrf/>
      <h3>${empty myReview ? 'Write a review' : 'Your review'}</h3>
      <fieldset class="rating">
        <legend class="sr">Your rating</legend>
        <c:forEach begin="1" end="5" var="i">
          <c:set var="v" value="${6 - i}"/>
          <input type="radio" id="r${v}" name="rating" value="${v}" ${form.rating == v ? 'checked' : ''}><label for="r${v}" title="${v} star${v == 1 ? '' : 's'}">&#9733;</label>
        </c:forEach>
      </fieldset>
      <t:field label="Comment (optional)" name="comment" type="textarea" rows="3" value="${form.comment}" placeholder="What did you think of it?"/>
      <button class="btn btn-primary" type="submit">${empty myReview ? 'Post review' : 'Update my review'}</button>
    </form>
    <div class="review-list">
      <c:if test="${empty reviews}"><div class="empty"><p>Be the first to review this book.</p></div></c:if>
      <c:forEach items="${reviews}" var="r">
        <article class="review">
          <div class="review-h">
            <span class="avatar avatar-sm">${cl:h(r.userName.substring(0, 1).toUpperCase())}</span>
            <div><strong>${cl:h(r.userName)}</strong><br><t:stars value="${r.rating}"/> <small class="muted">${cl:date(r.createdAt)}</small></div>
            <c:if test="${r.userId == user.id or user.admin}">
              <form method="post" action="${ctx}/reviews/${r.id}/delete" data-confirm="Delete this review?"><t:csrf/><button class="icon-btn icon-btn-sm" type="submit" aria-label="Delete review" title="Delete"><t:icon name="trash"/></button></form>
            </c:if>
          </div>
          <c:if test="${not empty r.comment}"><p>${cl:h(r.comment)}</p></c:if>
        </article>
      </c:forEach>
    </div>
  </div>
</section>

<c:if test="${not empty related}">
<section class="block">
  <div class="card-h"><h2>More in ${cl:h(book.categoryName)}</h2></div>
  <div class="shelf shelf-4"><c:forEach items="${related}" var="b"><t:bookcard b="${b}"/></c:forEach></div>
</section>
</c:if>
</t:layout>
