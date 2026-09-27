<%@ page isErrorPage="true" %>
<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="status" value="${requestScope['jakarta.servlet.error.status_code']}"/>
<c:set var="pageTitle" value="Error ${status}"/>
<%-- Log the real exception on the server; show the user only a friendly message --%>
<% if (exception != null) application.log("Unhandled error", exception); %>
<!DOCTYPE html>
<html lang="en">
<head>
<%@ include file="/WEB-INF/jsp/common/head.jspf" %>
</head>
<body>
<div class="error-page">
    <div>
        <div class="code-big"><c:out value="${empty status ? 500 : status}"/></div>
        <h1 style="margin-top:8px">
            <c:choose>
                <c:when test="${status == 404}">Page not found</c:when>
                <c:when test="${status == 403}">Access denied</c:when>
                <c:otherwise>Something went wrong</c:otherwise>
            </c:choose>
        </h1>
        <p class="muted" style="margin:10px auto 22px; max-width:48ch">
            <c:choose>
                <c:when test="${status == 404}">The page you asked for does not exist.</c:when>
                <c:when test="${status == 403}"><c:out value="${requestScope['jakarta.servlet.error.message']}"/></c:when>
                <c:when test="${fn:contains(requestScope['jakarta.servlet.error.message'], 'MySQL')}">
                    The database is not reachable. Check that the MySQL service is running, then try again.
                </c:when>
                <c:otherwise>An unexpected error occurred. The details were written to the server log.</c:otherwise>
            </c:choose>
        </p>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/dashboard">Back to dashboard</a>
    </div>
</div>
</body>
</html>
