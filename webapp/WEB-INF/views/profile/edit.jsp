<%@ page contentType="text/html;charset=UTF-8" %>
<t:layout title="My profile" active="profile">
<div class="ph"><div><h1>My profile</h1><p class="muted">${user.role} &middot; member since ${cl:date(user.createdAt)}</p></div></div>
<div class="cols cols-even">
  <form class="card form" method="post" action="${ctx}/profile" novalidate>
    <t:csrf/>
    <h2>Details</h2>
    <t:field label="Full name" name="name" value="${form.name}" error="${errors.name}" autocomplete="name" required="true"/>
    <t:field label="E-mail" name="email" type="email" value="${form.email}" error="${errors.email}" autocomplete="username" required="true"/>
    <div class="form-actions"><button class="btn btn-primary" type="submit">Save changes</button></div>
  </form>
  <form class="card form" method="post" action="${ctx}/profile/password" novalidate>
    <t:csrf/>
    <h2>Change password</h2>
    <t:field label="Current password" name="current" type="password" error="${errors.current}" autocomplete="current-password" required="true"/>
    <t:field label="New password" name="password" type="password" error="${errors.password}" hint="At least 8 characters." autocomplete="new-password" required="true"/>
    <t:field label="Confirm new password" name="password2" type="password" error="${errors.password2}" autocomplete="new-password" required="true"/>
    <div class="form-actions"><button class="btn btn-primary" type="submit">Update password</button></div>
  </form>
</div>
</t:layout>
