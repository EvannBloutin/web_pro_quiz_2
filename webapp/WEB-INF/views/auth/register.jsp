<%@ page contentType="text/html;charset=UTF-8" %>
<t:layout title="Create an account" bare="true">
<div class="auth">
  <section class="auth-art" aria-hidden="true">
    <div class="auth-brand"><span class="brand-mark"><i></i><i></i><i></i></span><span class="brand-name">Campus Library</span></div>
    <div class="spines">
      <b style="--h:60%;--c:#D6456F"></b><b style="--h:84%;--c:#4C5BD4"></b><b style="--h:70%;--c:#F2A93B"></b><b style="--h:92%;--c:#1F8A8A"></b>
      <b style="--h:56%;--c:#F6F7FB"></b><b style="--h:76%;--c:#7A4BA8"></b><b style="--h:66%;--c:#3E8E4F"></b><b style="--h:88%;--c:#D98E1F"></b><b style="--h:62%;--c:#D6456F"></b>
    </div>
    <p class="auth-tag">One account to borrow up to three books at a time.</p>
  </section>
  <section class="auth-card">
    <h1>Create an account</h1>
    <p class="muted">It takes less than a minute.</p>
    <form method="post" action="${ctx}/register" novalidate>
      <t:csrf/>
      <t:field label="Full name" name="name" value="${form.name}" error="${errors.name}" autocomplete="name" required="true"/>
      <t:field label="E-mail" name="email" type="email" value="${form.email}" error="${errors.email}" autocomplete="username" required="true"/>
      <t:field label="Password" name="password" type="password" error="${errors.password}" hint="At least 8 characters." autocomplete="new-password" required="true"/>
      <t:field label="Confirm password" name="password2" type="password" error="${errors.password2}" autocomplete="new-password" required="true"/>
      <button class="btn btn-primary btn-block" type="submit">Create my account</button>
    </form>
    <p class="auth-alt">Already registered? <a href="${ctx}/login">Log in</a></p>
  </section>
</div>
</t:layout>
