<%@ tag description="Star rating (0-5, decimals allowed)" pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="value" required="true" type="java.lang.Double" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cl" uri="https://campuslibrary.example/tags" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<span class="stars" style="--v:${cl:stars(value)}" role="img" aria-label="Rated ${cl:stars(value)} out of 5"></span>
