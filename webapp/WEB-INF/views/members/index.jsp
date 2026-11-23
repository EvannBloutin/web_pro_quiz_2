<%@ page contentType="text/html;charset=UTF-8" %>
<t:layout title="Members" active="members">
<div class="ph">
  <div><h1>Members</h1><p class="muted">${result.total} account${result.total == 1 ? '' : 's'}</p></div>
  <a class="btn btn-primary" href="${ctx}/members/new"><t:icon name="plus"/>New account</a>
</div>
<form class="filters" method="get" action="${ctx}/members">
  <label class="search"><t:icon name="search"/><span class="sr">Search</span><input type="search" name="q" value="${cl:h(q)}" placeholder="Name or e-mail"></label>
  <button class="btn btn-primary" type="submit">Search</button>
  <c:if test="${not empty q}"><a class="btn btn-ghost" href="${ctx}/members">Reset</a></c:if>
</form>
<div class="card table-wrap">
  <table class="tbl">
    <thead><tr><th>Member</th><th>Role</th><th class="num">Active loans</th><th>Joined</th><th class="actions-h"><span class="sr">Actions</span></th></tr></thead>
    <tbody>
    <c:forEach items="${result.items}" var="m">
      <tr>
        <td><div class="bcell"><span class="avatar">${cl:h(m.initials)}</span><span><strong>${cl:h(m.name)}</strong><br><small class="muted">${cl:h(m.email)}</small></span></div></td>
        <td><span class="badge ${m.admin ? 'badge-info' : 'badge-muted'}">${m.role}</span></td>
        <td class="num">${m.activeLoans}</td>
        <td>${cl:date(m.createdAt)}</td>
        <td class="row-actions">
          <a class="btn btn-ghost btn-sm" href="${ctx}/members/${m.id}/edit"><t:icon name="edit"/>Edit</a>
          <c:if test="${m.id != user.id}"><form method="post" action="${ctx}/members/${m.id}/delete" data-confirm="Delete the account of ${cl:h(m.name)}? Their reviews and favorites are deleted too."><t:csrf/><button class="btn btn-danger-ghost btn-sm" type="submit"><t:icon name="trash"/>Delete</button></form></c:if>
        </td>
      </tr>
    </c:forEach>
    </tbody>
  </table>
</div>
<t:pager result="${result}" path="/members" query="q=${cl:enc(q)}"/>
</t:layout>
