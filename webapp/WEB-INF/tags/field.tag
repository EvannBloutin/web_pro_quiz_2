<%@ tag description="Labelled form field with error message" pageEncoding="UTF-8" body-content="scriptless" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="type" required="false" %>
<%@ attribute name="value" required="false" %>
<%@ attribute name="error" required="false" %>
<%@ attribute name="hint" required="false" %>
<%@ attribute name="placeholder" required="false" %>
<%@ attribute name="required" required="false" %>
<%@ attribute name="autocomplete" required="false" %>
<%@ attribute name="rows" required="false" %>
<%@ attribute name="min" required="false" %>
<%@ attribute name="max" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cl" uri="https://campuslibrary.example/tags" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="kind" value="${empty type ? 'text' : type}"/>
<div class="field ${not empty error ? 'has-error' : ''}">
  <label for="f-${name}">${cl:h(label)}<c:if test="${required == 'true'}"><span class="req" title="required">*</span></c:if></label>
  <c:choose>
    <c:when test="${kind == 'textarea'}"><textarea id="f-${name}" name="${name}" rows="${empty rows ? 4 : rows}" placeholder="${cl:h(placeholder)}" ${required == 'true' ? 'required' : ''}>${cl:h(value)}</textarea></c:when>
    <c:when test="${kind == 'select'}"><select id="f-${name}" name="${name}" ${required == 'true' ? 'required' : ''}><jsp:doBody/></select></c:when>
    <c:otherwise><input id="f-${name}" name="${name}" type="${kind}" value="${kind == 'password' ? '' : cl:h(value)}" placeholder="${cl:h(placeholder)}" autocomplete="${empty autocomplete ? 'off' : autocomplete}" ${not empty min ? 'min=' : ''}${not empty min ? min : ''} ${not empty max ? 'max=' : ''}${not empty max ? max : ''} ${required == 'true' ? 'required' : ''}></c:otherwise>
  </c:choose>
  <c:if test="${not empty hint and empty error}"><p class="hint">${cl:h(hint)}</p></c:if>
  <c:if test="${not empty error}"><p class="err" role="alert">${cl:h(error)}</p></c:if>
</div>
