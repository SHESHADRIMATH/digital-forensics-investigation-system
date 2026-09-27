<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="pageTitle" value="Findings"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <div>
        <h1>Findings</h1>
        <p>Conclusions drawn during the investigation, ranked by severity.</p>
    </div>
    <c:if test="${not empty selectedCaseId}">
        <div class="actions"><a class="btn btn-secondary" href="${ctx}/reports?caseId=${selectedCaseId}&amp;type=FINDINGS"><svg class="icon" aria-hidden="true"><use href="#i-report"/></svg>Findings report</a></div>
    </c:if>
</div>

<div class="grid grid-main">
    <section class="card">
        <div class="card-head">
            <form class="filters" method="get" action="${ctx}/findings">
                <label for="filterCase" class="sr-only">Case</label>
                <select id="filterCase" name="caseId" onchange="this.form.submit()">
                    <option value="">All cases</option>
                    <c:forEach var="c" items="${cases}">
                        <option value="${c.id}" ${c.id == selectedCaseId ? 'selected' : ''}><c:out value="${c.caseNumber} - ${c.title}"/></option>
                    </c:forEach>
                </select>
                <noscript><button class="btn btn-secondary" type="submit">Show</button></noscript>
            </form>
            <span class="small muted">${fn:length(findings)} finding(s)</span>
        </div>
        <div class="card-body flush">
            <c:choose>
                <c:when test="${empty findings}">
                    <div class="empty"><svg class="icon icon-lg" aria-hidden="true"><use href="#i-alert"/></svg><p>No findings recorded.</p></div>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead><tr><th>Finding</th><th>Severity</th><th>Status</th><c:if test="${me.role.canReviewFindings()}"><th>Review</th></c:if></tr></thead>
                            <tbody>
                            <c:forEach var="f" items="${findings}">
                                <tr>
                                    <td>
                                        <strong style="font-weight:500"><c:out value="${f.title}"/></strong>
                                        <c:if test="${not empty f.description}"><span class="sub"><c:out value="${f.description}"/></span></c:if>
                                        <span class="sub">
                                            <a class="code" href="${ctx}/cases?id=${f.caseId}"><c:out value="${f.caseNumber}"/></a>
                                            <c:if test="${not empty f.evidenceId}"> · <a class="code" href="${ctx}/evidence?id=${f.evidenceId}">${f.evidenceCode}</a></c:if>
                                            · <c:out value="${f.createdByName}"/> · ${f.createdAtText}
                                        </span>
                                    </td>
                                    <td><span class="badge b-${f.severity.css}">${f.severity.label}</span></td>
                                    <td><span class="badge b-${f.status.css}">${f.status.label}</span></td>
                                    <c:if test="${me.role.canReviewFindings()}">
                                        <td>
                                            <form method="post" action="${ctx}/findings" class="inline-form" style="flex-wrap:nowrap;gap:6px">
                                                <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
                                                <input type="hidden" name="action" value="status">
                                                <input type="hidden" name="id" value="${f.id}">
                                                <input type="hidden" name="caseId" value="${selectedCaseId}">
                                                <label for="st${f.id}" class="sr-only">Status for ${f.displayCode}</label>
                                                <select id="st${f.id}" name="status" style="min-height:32px;padding:4px 8px;font-size:.82rem;width:auto">
                                                    <c:forEach var="s" items="${findingStatuses}"><option value="${s}" ${s == f.status ? 'selected' : ''}>${s.label}</option></c:forEach>
                                                </select>
                                                <button type="submit" class="btn btn-secondary btn-sm">Set</button>
                                            </form>
                                        </td>
                                    </c:if>
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
        <div class="card-head"><h2>Record a finding</h2></div>
        <div class="card-body">
            <c:choose>
                <c:when test="${empty selectedCaseId}">
                    <p class="muted small">Choose a case from the filter first, so the finding can be linked to that case's evidence.</p>
                </c:when>
                <c:otherwise>
                    <form method="post" action="${ctx}/findings" class="stack">
                        <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="caseId" value="${selectedCaseId}">
                        <div class="field">
                            <label for="ftitle">Title<span class="req" aria-hidden="true">*</span></label>
                            <input type="text" id="ftitle" name="title" maxlength="150" required>
                        </div>
                        <div class="field">
                            <label for="severity">Severity<span class="req" aria-hidden="true">*</span></label>
                            <select id="severity" name="severity" required>
                                <c:forEach var="s" items="${severities}"><option value="${s}" ${s == 'MEDIUM' ? 'selected' : ''}>${s.label}</option></c:forEach>
                            </select>
                        </div>
                        <div class="field">
                            <label for="fevidence">Linked evidence</label>
                            <select id="fevidence" name="evidenceId">
                                <option value="">None</option>
                                <c:forEach var="ev" items="${caseEvidence}"><option value="${ev.id}">${ev.displayCode} · <c:out value="${ev.fileName}"/></option></c:forEach>
                            </select>
                        </div>
                        <div class="field">
                            <label for="fdesc">Description</label>
                            <textarea id="fdesc" name="description" rows="4"></textarea>
                        </div>
                        <button type="submit" class="btn btn-primary btn-block"><svg class="icon" aria-hidden="true"><use href="#i-plus"/></svg>Save finding</button>
                    </form>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
