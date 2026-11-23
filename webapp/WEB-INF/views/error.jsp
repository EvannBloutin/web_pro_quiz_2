<%@ page contentType="text/html;charset=UTF-8" isErrorPage="false" %>
<c:choose>
<c:when test="${not empty user}">
<t:layout title="Error ${status}" active="">
  <section class="empty empty-lg">
    <p class="err-code">${status}</p>
    <h1>${status == 404 ? 'Page not found' : (status == 403 ? 'Not allowed' : 'Something went wrong')}</h1>
    <p>${cl:h(message)}</p>
    <a class="btn btn-primary" href="${ctx}/dashboard"><t:icon name="back"/>Back to the dashboard</a>
  </section>
</t:layout>
</c:when>
<c:otherwise>
<t:layout title="Error ${status}" bare="true">
  <section class="auth-card">
    <p class="err-code">${status}</p>
    <h1>${status == 403 ? 'Not allowed' : 'Something went wrong'}</h1>
    <p>${cl:h(message)}</p>
    <a class="btn btn-primary" href="${ctx}/login">Go to the login page</a>
  </section>
</t:layout>
</c:otherwise>
</c:choose>
