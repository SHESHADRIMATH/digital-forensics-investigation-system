<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="pageTitle" value="Dashboard"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <div>
        <h1>Welcome back, <c:out value="${me.fullName}"/></h1>
        <p>Overview of investigations at ABC Cyber Forensics &amp; Incident Response Center.</p>
    </div>
    <div class="actions">
        <c:if test="${me.role.canUploadEvidence()}">
            <a class="btn btn-secondary" href="${ctx}/evidence?action=upload"><svg class="icon" aria-hidden="true"><use href="#i-upload"/></svg>Upload evidence</a>
        </c:if>
        <c:if test="${me.role.canCreateCase()}">
            <a class="btn btn-primary" href="${ctx}/cases?action=new"><svg class="icon" aria-hidden="true"><use href="#i-plus"/></svg>New case</a>
        </c:if>
    </div>
</div>

<section class="stats" aria-label="Key figures">
    <div class="card stat">
        <span class="stat-icon tone-primary"><svg class="icon" aria-hidden="true"><use href="#i-folder"/></svg></span>
        <div><div class="stat-value">${stats.activeCases}</div><div class="stat-label">Active cases</div>
             <div class="stat-note">${stats.totalCases} total</div></div>
    </div>
    <div class="card stat">
        <span class="stat-icon tone-info"><svg class="icon" aria-hidden="true"><use href="#i-file"/></svg></span>
        <div><div class="stat-value">${stats.totalEvidence}</div><div class="stat-label">Evidence items</div>
             <div class="stat-note">${stats.verifiedEvidence} verified</div></div>
    </div>
    <div class="card stat">
        <span class="stat-icon ${stats.modifiedEvidence > 0 ? 'tone-danger' : 'tone-success'}">
            <svg class="icon" aria-hidden="true"><use href="#${stats.modifiedEvidence > 0 ? 'i-shield-x' : 'i-shield'}"/></svg></span>
        <div><div class="stat-value">${stats.modifiedEvidence}</div><div class="stat-label">Integrity failures</div>
             <div class="stat-note">${stats.modifiedEvidence > 0 ? 'Needs attention' : 'All checks passed'}</div></div>
    </div>
    <div class="card stat">
        <span class="stat-icon tone-warning"><svg class="icon" aria-hidden="true"><use href="#i-alert"/></svg></span>
        <div><div class="stat-value">${stats.openFindings}</div><div class="stat-label">Open findings</div>
             <div class="stat-note">Awaiting review</div></div>
    </div>
</section>

<div class="grid grid-main">
    <div class="stack">
        <section class="card">
            <div class="card-head">
                <h2>Recently updated cases</h2>
                <a href="${ctx}/cases" class="small">View all cases</a>
            </div>
            <div class="card-body flush">
                <c:choose>
                    <c:when test="${empty recentCases}">
                        <div class="empty"><svg class="icon icon-lg" aria-hidden="true"><use href="#i-folder"/></svg><p>No cases yet.</p></div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-wrap">
                            <table>
                                <thead><tr><th>Case</th><th>Priority</th><th>Status</th><th>Investigator</th></tr></thead>
                                <tbody>
                                <c:forEach var="c" items="${recentCases}">
                                    <tr>
                                        <td><a class="row-link" href="${ctx}/cases?id=${c.id}"><c:out value="${c.title}"/></a>
                                            <span class="sub"><span class="code"><c:out value="${c.caseNumber}"/></span> · <c:out value="${c.incidentType}"/></span></td>
                                        <td><span class="badge b-${c.priority.css}">${c.priority.label}</span></td>
                                        <td><span class="badge b-${c.status.css}">${c.status.label}</span></td>
                                        <td class="nowrap"><c:out value="${empty c.assignedToName ? 'Unassigned' : c.assignedToName}"/></td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <section class="card">
            <div class="card-head"><h2>Top findings</h2><a href="${ctx}/findings" class="small">All findings</a></div>
            <div class="card-body flush">
                <c:choose>
                    <c:when test="${empty topFindings}">
                        <div class="empty"><p>No findings recorded yet.</p></div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-wrap">
                            <table>
                                <thead><tr><th>Finding</th><th>Severity</th><th>Status</th></tr></thead>
                                <tbody>
                                <c:forEach var="f" items="${topFindings}">
                                    <tr>
                                        <td><c:out value="${f.title}"/><span class="sub"><a href="${ctx}/findings?caseId=${f.caseId}" class="code"><c:out value="${f.caseNumber}"/></a></span></td>
                                        <td><span class="badge b-${f.severity.css}">${f.severity.label}</span></td>
                                        <td><span class="badge b-${f.status.css}">${f.status.label}</span></td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>
    </div>

    <div class="stack">
        <section class="card">
            <div class="card-head"><h2>Cases by status</h2></div>
            <div class="card-body">
                <div class="bars">
                    <c:forEach var="e" items="${stats.casesByStatus}">
                        <div class="bar-row">
                            <span class="bar-label"><span class="badge b-${e.key.css}">${e.key.label}</span></span>
                            <span class="bar-track" role="img" aria-label="${e.key.label}: ${e.value} of ${stats.totalCases} cases">
                                <span class="bar-fill ${e.value == 0 ? 'zero' : ''}" style="display:block;width:${stats.percentOfCases(e.value)}%"></span>
                            </span>
                            <span class="count">${e.value}</span>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </section>

        <section class="card">
            <div class="card-head"><h2>Latest custody activity</h2><a href="${ctx}/custody" class="small">Full log</a></div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${empty recentCustody}">
                        <div class="empty" style="padding:16px"><p>No evidence activity yet. Upload evidence to start the chain of custody.</p></div>
                    </c:when>
                    <c:otherwise>
                        <ol class="steps">
                            <c:forEach var="r" items="${recentCustody}">
                                <li>
                                    <span class="step-dot ${fn:contains(r.action, 'Verified') ? 'ok' : (fn:contains(r.action, 'FAILED') ? 'bad' : '')}"></span>
                                    <div class="step-title"><c:out value="${r.action}"/></div>
                                    <div class="step-meta"><a href="${ctx}/evidence?id=${r.evidenceId}" class="code">${r.evidenceCode}</a>
                                        · <c:out value="${r.performedByName}"/> · ${r.actionTimeText}</div>
                                </li>
                            </c:forEach>
                        </ol>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
