<%@ tag description="Book card used in grids" pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="b" required="true" type="com.campuslibrary.model.Book" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cl" uri="https://campuslibrary.example/tags" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<a class="bk" href="${ctx}/books/${b.id}">
  <span class="bk-cover">
    <t:cover id="${b.id}" title="${b.title}" author="${b.author}" isbn="${b.isbn}" url="${b.coverUrl}" color="${b.categoryColor}"/>
    <span class="bk-stock ${b.available ? 'ok' : 'none'}">${b.available ? b.copiesAvailable : 'On loan'}${b.available ? ' left' : ''}</span>
    <c:if test="${b.favorite}"><span class="bk-fav" title="In your favorites"><t:icon name="heart"/></span></c:if>
  </span>
  <span class="bk-title">${cl:h(b.title)}</span>
  <span class="bk-author">${cl:h(b.author)}<c:if test="${not empty b.publishedYear}">, ${b.publishedYear}</c:if></span>
  <span class="bk-meta">
    <span class="dot" style="background:${cl:h(b.categoryColor)}"></span><span class="bk-cat">${cl:h(b.categoryName)}</span>
    <c:if test="${b.reviewCount > 0}"><span class="bk-rate"><t:stars value="${b.avgRating}"/> ${b.avgRatingText}</span></c:if>
  </span>
</a>
