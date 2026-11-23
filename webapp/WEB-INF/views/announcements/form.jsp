<%@ page contentType="text/html;charset=UTF-8" %>
<t:layout title="${mode == 'new' ? 'New announcement' : 'Edit announcement'}" active="announcements">
<a class="back" href="${ctx}/announcements"><t:icon name="back"/>Announcements</a>
<div class="ph"><h1>${mode == 'new' ? 'New announcement' : 'Edit announcement'}</h1></div>
<form class="card form form-narrow" method="post" action="${ctx}${mode == 'new' ? '/announcements' : '/announcements/'.concat(item.id).concat('/update')}" novalidate>
  <t:csrf/>
  <t:field label="Title" name="title" value="${form.title}" error="${errors.title}" required="true"/>
  <t:field label="Message" name="body" type="textarea" rows="6" value="${form.body}" error="${errors.body}" required="true"/>
  <label class="check"><input type="checkbox" name="pinned" ${not empty form.pinned ? 'checked' : ''}> Pin to the top of the dashboard</label>
  <div class="form-actions"><button class="btn btn-primary" type="submit">${mode == 'new' ? 'Publish' : 'Save changes'}</button><a class="btn btn-ghost" href="${ctx}/announcements">Cancel</a></div>
</form>
</t:layout>
