<%@ page contentType="text/html;charset=UTF-8" %>
<t:layout title="${mode == 'new' ? 'Add a book' : 'Edit book'}" active="books">
<a class="back" href="${mode == 'new' ? ctx.concat('/books') : ctx.concat('/books/').concat(item.id)}"><t:icon name="back"/>Back</a>
<div class="ph"><h1>${mode == 'new' ? 'Add a book' : 'Edit book'}</h1></div>
<form class="card form" method="post" action="${ctx}${mode == 'new' ? '/books' : '/books/'.concat(item.id).concat('/update')}" novalidate>
  <t:csrf/>
  <div class="form-grid">
    <t:field label="Title" name="title" value="${form.title}" error="${errors.title}" required="true"/>
    <t:field label="Author(s)" name="author" value="${form.author}" error="${errors.author}" required="true"/>
    <t:field label="ISBN" name="isbn" value="${form.isbn}" error="${errors.isbn}" hint="ISBN-10 or ISBN-13, hyphens allowed." required="true"/>
    <t:field label="Category" name="categoryId" type="select" error="${errors.categoryId}" required="true">
      <option value="">Choose a category</option>
      <c:forEach items="${categories}" var="c"><option value="${c.id}" ${form.categoryId == c.id ? 'selected' : ''}>${cl:h(c.name)}</option></c:forEach>
    </t:field>
    <t:field label="Published year" name="publishedYear" type="number" value="${form.publishedYear}" error="${errors.publishedYear}"/>
    <t:field label="Total copies" name="copiesTotal" type="number" min="1" value="${form.copiesTotal}" error="${errors.copiesTotal}" required="true"/>
  </div>
  <t:field label="Cover image link (optional)" name="coverUrl" type="url" value="${form.coverUrl}" error="${errors.coverUrl}" hint="Leave empty to use the generated cover, or the real cover found from the ISBN."/>
  <t:field label="Description" name="description" type="textarea" rows="5" value="${form.description}" error="${errors.description}"/>
  <div class="form-actions">
    <button class="btn btn-primary" type="submit">${mode == 'new' ? 'Add to the catalogue' : 'Save changes'}</button>
    <a class="btn btn-ghost" href="${ctx}/books${mode == 'new' ? '' : '/'.concat(item.id)}">Cancel</a>
  </div>
</form>
</t:layout>
