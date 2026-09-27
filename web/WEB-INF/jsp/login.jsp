<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="pageTitle" value="Sign in"/>
<!DOCTYPE html>
<html lang="en">
<head>
<%@ include file="/WEB-INF/jsp/common/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/jsp/common/icons.jspf" %>
<div class="login-page">
    <section class="login-art" aria-hidden="true">
        <div class="brand" style="padding:0">
            <span class="brand-mark"><svg class="icon"><use href="#i-shield"/></svg></span>
            <span><span class="brand-name">ForensiTrack</span><br><span class="brand-sub">ABC Cyber Forensics &amp; IR Center</span></span>
        </div>
        <div>
            <h1>Digital Forensics Investigation System</h1>
            <p>Manage cases, preserve digital evidence with SHA-256 integrity checks, and keep an unbroken chain of custody from collection to report.</p>
            <ul class="flow">
                <li>CREATE CASE</li><li>UPLOAD EVIDENCE</li><li>SHA-256</li><li>CHAIN OF CUSTODY</li>
                <li>ANALYZE</li><li>TIMELINE</li><li>FINDINGS</li><li>REPORT</li>
            </ul>
        </div>
        <p class="small" style="color:#7f90aa">Java EE · Servlets · JSP · JDBC · MySQL</p>
    </section>

    <section class="login-panel">
        <button type="button" class="icon-btn theme-toggle" aria-label="Toggle theme">
            <svg class="icon show-in-light" aria-hidden="true"><use href="#i-moon"/></svg>
            <svg class="icon show-in-dark" aria-hidden="true"><use href="#i-sun"/></svg>
        </button>
        <div class="login-box">
            <h2>Sign in</h2>
            <p class="lead">Authorized investigators only. All activity is recorded.</p>

            <c:if test="${not empty error}">
                <div class="alert alert-error" role="alert">
                    <svg class="icon" aria-hidden="true"><use href="#i-alert"/></svg><span><c:out value="${error}"/></span>
                </div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/login" novalidate>
                <div class="field">
                    <label for="username">Username</label>
                    <input type="text" id="username" name="username" value="${fn:escapeXml(username)}"
                           autocomplete="username" autocapitalize="none" spellcheck="false" required autofocus>
                </div>
                <div class="field">
                    <label for="password">Password</label>
                    <div class="password-wrap">
                        <input type="password" id="password" name="password" autocomplete="current-password" required>
                        <button type="button" id="togglePassword" aria-label="Show password" aria-pressed="false">
                            <svg class="icon" aria-hidden="true"><use href="#i-eye"/></svg>
                        </button>
                    </div>
                </div>
                <button type="submit" class="btn btn-primary btn-block" style="margin-top:20px" data-loading="Signing in…">
                    <span class="spinner" aria-hidden="true"></span><span class="label">Sign in</span>
                </button>
            </form>

            <div class="demo-accounts">
                <strong>Demo accounts</strong> <span class="muted">(passwords are in README.md)</span>
                <table>
                    <tr><td class="mono">admin</td><td class="muted">Administrator: everything, incl. closing cases</td></tr>
                    <tr><td class="mono">investigator</td><td class="muted">Creates cases, uploads evidence</td></tr>
                    <tr><td class="mono">analyst</td><td class="muted">Analyses logs, verifies hashes, adds findings</td></tr>
                </table>
            </div>
        </div>
    </section>
</div>
<script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>
