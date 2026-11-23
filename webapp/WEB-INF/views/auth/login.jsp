<%@ page contentType="text/html;charset=UTF-8" %>
<t:layout title="Log in" bare="true">
<div class="auth">
  <section class="auth-art" aria-hidden="true">
    <div class="auth-brand"><span class="brand-mark"><i></i><i></i><i></i></span><span class="brand-name">Campus Library</span></div>
    <div class="spines">
      <b style="--h:72%;--c:#4C5BD4"></b><b style="--h:58%;--c:#F2A93B"></b><b style="--h:86%;--c:#D6456F"></b><b style="--h:64%;--c:#1F8A8A"></b>
      <b style="--h:78%;--c:#7A4BA8"></b><b style="--h:52%;--c:#F6F7FB"></b><b style="--h:90%;--c:#3E8E4F"></b><b style="--h:68%;--c:#D98E1F"></b><b style="--h:80%;--c:#4C5BD4"></b>
    </div>
    <p class="auth-tag">Borrow, reserve and review the books of your campus library.</p>
  </section>
  <section class="auth-card">
    <h1>Log in</h1>
    <p class="muted">Welcome back. Enter your e-mail and password.</p>
    <c:if test="${not empty error}"><p class="form-error" role="alert"><t:icon name="alert"/>${cl:h(error)}</p></c:if>
    <form method="post" action="${ctx}/login" novalidate>
      <t:csrf/>
      <t:field label="E-mail" name="email" type="email" value="${form.email}" autocomplete="username" required="true"/>
      <t:field label="Password" name="password" type="password" autocomplete="current-password" required="true"/>
      <button class="btn btn-primary btn-block" type="submit">Log in</button>
    </form>
    <p class="auth-alt">No account yet? <a href="${ctx}/register">Create one</a></p>
    <div class="demo">
      <strong>Demo accounts</strong>
      <span>Librarian: <code>admin@library.test</code></span>
      <span>Member: <code>member@library.test</code></span>
      <span>Password for both: <code>password</code></span>
    </div>
  </section>
</div>
</t:layout>
