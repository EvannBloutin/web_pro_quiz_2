<%@ page contentType="text/html;charset=UTF-8" %>
<t:layout title="${mode == 'new' ? 'New account' : 'Edit account'}" active="members">
<a class="back" href="${ctx}/members"><t:icon name="back"/>Members</a>
<div class="ph"><h1>${mode == 'new' ? 'New account' : 'Edit account'}</h1></div>
<form class="card form form-narrow" method="post" action="${ctx}${mode == 'new' ? '/members' : '/members/'.concat(item.id).concat('/update')}" novalidate>
  <t:csrf/>
  <t:field label="Full name" name="name" value="${form.name}" error="${errors.name}" required="true"/>
  <t:field label="E-mail" name="email" type="email" value="${form.email}" error="${errors.email}" required="true"/>
  <t:field label="Role" name="role" type="select" error="${errors.role}">
    <option value="member" ${form.role == 'member' ? 'selected' : ''}>Member</option>
    <option value="admin" ${form.role == 'admin' ? 'selected' : ''}>Librarian</option>
  </t:field>
  <t:field label="${mode == 'new' ? 'Password' : 'New password (optional)'}" name="password" type="password" error="${errors.password}" hint="${mode == 'new' ? 'At least 8 characters.' : 'Leave empty to keep the current password.'}" autocomplete="new-password" required="${mode == 'new'}"/>
  <div class="form-actions"><button class="btn btn-primary" type="submit">${mode == 'new' ? 'Create account' : 'Save changes'}</button><a class="btn btn-ghost" href="${ctx}/members">Cancel</a></div>
</form>
</t:layout>
