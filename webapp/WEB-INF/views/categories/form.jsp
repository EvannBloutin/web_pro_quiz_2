<%@ page contentType="text/html;charset=UTF-8" %>
<t:layout title="${mode == 'new' ? 'New category' : 'Edit category'}" active="categories">
<a class="back" href="${ctx}/categories"><t:icon name="back"/>Categories</a>
<div class="ph"><h1>${mode == 'new' ? 'New category' : 'Edit category'}</h1></div>
<form class="card form form-narrow" method="post" action="${ctx}${mode == 'new' ? '/categories' : '/categories/'.concat(item.id).concat('/update')}" novalidate>
  <t:csrf/>
  <t:field label="Name" name="name" value="${form.name}" error="${errors.name}" required="true"/>
  <t:field label="Description" name="description" type="textarea" rows="3" value="${form.description}" error="${errors.description}"/>
  <div class="field ${not empty errors.color ? 'has-error' : ''}">
    <label for="f-color">Shelf colour</label>
    <input id="f-color" class="color-input" type="color" name="color" value="${cl:h(form.color)}">
    <p class="hint">Used on book covers, chips and charts.</p>
    <c:if test="${not empty errors.color}"><p class="err">${cl:h(errors.color)}</p></c:if>
  </div>
  <div class="form-actions"><button class="btn btn-primary" type="submit">${mode == 'new' ? 'Create category' : 'Save changes'}</button><a class="btn btn-ghost" href="${ctx}/categories">Cancel</a></div>
</form>
</t:layout>
