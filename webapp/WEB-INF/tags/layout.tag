<%@ tag description="Application shell: sidebar, flash messages, content" pageEncoding="UTF-8" body-content="scriptless" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="active" required="false" %>
<%@ attribute name="bare" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cl" uri="https://campuslibrary.example/tags" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${cl:h(title)} | Campus Library</title>
<link rel="icon" href="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 32 32'%3E%3Crect width='32' height='32' rx='7' fill='%2317214D'/%3E%3Crect x='7' y='7' width='5' height='18' rx='1' fill='%23F2A93B'/%3E%3Crect x='14' y='10' width='5' height='15' rx='1' fill='%23fff'/%3E%3Crect x='21' y='7' width='4' height='18' rx='1' fill='%234C5BD4'/%3E%3C/svg%3E">
<link rel="stylesheet" href="${ctx}/assets/css/app.css">
</head>
<body class="${bare == 'true' ? 'auth-body' : 'app-body'}">
<c:choose>
<c:when test="${bare == 'true'}">
  <main class="auth-main" id="main"><c:if test="${not empty flash}"><div class="flash flash-${flash.type}" role="status">${cl:h(flash.message)}</div></c:if><jsp:doBody/></main>
</c:when>
<c:otherwise>
<div class="shell">
  <aside class="side" id="side">
    <a class="brand" href="${ctx}/dashboard">
      <span class="brand-mark" aria-hidden="true"><i></i><i></i><i></i></span>
      <span class="brand-name">Campus Library</span>
    </a>
    <nav class="nav" aria-label="Main">
      <a class="nav-link ${active == 'dashboard' ? 'is-active' : ''}" href="${ctx}/dashboard"><t:icon name="home"/>Dashboard</a>
      <a class="nav-link ${active == 'books' ? 'is-active' : ''}" href="${ctx}/books"><t:icon name="book"/>Catalogue</a>
      <a class="nav-link ${active == 'loans' ? 'is-active' : ''}" href="${ctx}/loans"><t:icon name="clock"/>${user.admin ? 'Loans' : 'My loans'}<c:if test="${navLoans > 0}"><span class="count">${navLoans}</span></c:if></a>
      <a class="nav-link ${active == 'reservations' ? 'is-active' : ''}" href="${ctx}/reservations"><t:icon name="bookmark"/>Reservations<c:if test="${navReady > 0}"><span class="count count-hot" title="Ready to borrow">${navReady}</span></c:if></a>
      <a class="nav-link ${active == 'favorites' ? 'is-active' : ''}" href="${ctx}/favorites"><t:icon name="heart"/>Favorites</a>
      <a class="nav-link ${active == 'announcements' ? 'is-active' : ''}" href="${ctx}/announcements"><t:icon name="bell"/>Announcements</a>
      <c:if test="${user.admin}">
        <p class="nav-sep">Librarian</p>
        <a class="nav-link ${active == 'categories' ? 'is-active' : ''}" href="${ctx}/categories"><t:icon name="layers"/>Categories</a>
        <a class="nav-link ${active == 'members' ? 'is-active' : ''}" href="${ctx}/members"><t:icon name="users"/>Members</a>
      </c:if>
    </nav>
    <div class="side-foot">
      <a class="me ${active == 'profile' ? 'is-active' : ''}" href="${ctx}/profile">
        <span class="avatar">${cl:h(user.initials)}</span>
        <span class="me-txt"><strong>${cl:h(user.name)}</strong><small>${user.role}</small></span>
      </a>
      <form method="post" action="${ctx}/logout"><t:csrf/><button class="icon-btn" type="submit" title="Log out" aria-label="Log out"><t:icon name="logout"/></button></form>
    </div>
  </aside>
  <div class="content">
    <header class="topbar">
      <button class="icon-btn" type="button" id="menu-btn" aria-label="Open menu" aria-controls="side"><t:icon name="menu"/></button>
      <a class="brand brand-sm" href="${ctx}/dashboard"><span class="brand-mark" aria-hidden="true"><i></i><i></i><i></i></span><span class="brand-name">Campus Library</span></a>
    </header>
    <main class="main" id="main">
      <c:if test="${not empty flash}"><div class="flash flash-${flash.type}" role="status"><t:icon name="${flash.type == 'error' ? 'alert' : 'check'}"/><span>${cl:h(flash.message)}</span></div></c:if>
      <jsp:doBody/>
    </main>
    <footer class="foot">Campus Library &middot; Web Programming Quiz 2 &middot; Built with Jakarta Servlet and JSP</footer>
  </div>
</div>
</c:otherwise>
</c:choose>
<script src="${ctx}/assets/js/app.js" defer></script>
</body>
</html>
