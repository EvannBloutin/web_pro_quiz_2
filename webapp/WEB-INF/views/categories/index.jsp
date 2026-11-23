<%@ page contentType="text/html;charset=UTF-8" %>
<t:layout title="Categories" active="categories">
<div class="ph">
  <div><h1>Categories</h1><p class="muted">Shelves of the library. A category that still holds books cannot be deleted.</p></div>
  <a class="btn btn-primary" href="${ctx}/categories/new"><t:icon name="plus"/>New category</a>
</div>
<div class="card table-wrap">
  <table class="tbl">
    <thead><tr><th>Category</th><th>Description</th><th class="num">Books</th><th class="actions-h"><span class="sr">Actions</span></th></tr></thead>
    <tbody>
    <c:forEach items="${categories}" var="c">
      <tr>
        <td><span class="swatch" style="background:${cl:h(c.color)}"></span><a href="${ctx}/books?category=${c.id}"><strong>${cl:h(c.name)}</strong></a></td>
        <td class="muted">${cl:h(c.description)}</td>
        <td class="num">${c.bookCount}</td>
        <td class="row-actions">
          <a class="btn btn-ghost btn-sm" href="${ctx}/categories/${c.id}/edit"><t:icon name="edit"/>Edit</a>
          <form method="post" action="${ctx}/categories/${c.id}/delete" data-confirm="Delete the category ${cl:h(c.name)}?"><t:csrf/><button class="btn btn-danger-ghost btn-sm" type="submit"><t:icon name="trash"/>Delete</button></form>
        </td>
      </tr>
    </c:forEach>
    </tbody>
  </table>
</div>
</t:layout>
