<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="pageTitle" value="${theCase.caseNumber}"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>
<c:set var="c" value="${theCase}"/>

<div class="page-head">
    <div>
        <a href="${ctx}/cases" class="small"><svg class="icon icon-sm" aria-hidden="true"><use href="#i-back"/></svg> Back to cases</a>
        <h1 style="margin-top:6px"><c:out value="${c.title}"/></h1>
        <p><span class="code"><c:out value="${c.caseNumber}"/></span> ·
            <span class="badge b-${c.status.css}">${c.status.label}</span>
            <span class="badge b-${c.priority.css}">${c.priority.label} priority</span></p>
    </div>
    <div class="actions">
        <a class="btn btn-secondary" href="${ctx}/timeline?caseId=${c.id}"><svg class="icon" aria-hidden="true"><use href="#i-clock"/></svg>Timeline</a>
        <a class="btn btn-secondary" href="${ctx}/reports?caseId=${c.id}&amp;type=SUMMARY"><svg class="icon" aria-hidden="true"><use href="#i-report"/></svg>Summary report</a>
        <c:if test="${me.role.canUploadEvidence() and not c.closed}">
            <a class="btn btn-primary" href="${ctx}/evidence?action=upload&amp;caseId=${c.id}"><svg class="icon" aria-hidden="true"><use href="#i-upload"/></svg>Add evidence</a>
        </c:if>
    </div>
</div>

<div class="grid grid-main">
    <div class="stack">
        <section class="card">
            <div class="card-head"><h2>Case details</h2></div>
            <div class="card-body">
                <dl class="details">
                    <div><dt>Incident type</dt><dd><c:out value="${c.incidentType}"/></dd></div>
                    <div><dt>Assigned investigator</dt><dd><c:out value="${empty c.assignedToName ? 'Unassigned' : c.assignedToName}"/></dd></div>
                    <div><dt>Opened</dt><dd>${c.createdAtText} by <c:out value="${c.createdByName}"/></dd></div>
                    <div><dt>${c.closed ? 'Closed' : 'Last updated'}</dt><dd>${c.closed ? c.closedAtText : c.updatedAtText}</dd></div>
                    <div class="full"><dt>Description</dt><dd style="white-space:pre-line"><c:out value="${empty c.description ? '-' : c.description}"/></dd></div>
                </dl>
            </div>
        </section>

        <section class="card">
            <div class="card-head">
                <h2>Evidence <span class="muted small">(${fn:length(evidenceList)})</span></h2>
                <a class="small" href="${ctx}/custody?caseId=${c.id}">Chain of custody for this case</a>
            </div>
            <div class="card-body flush">
                <c:choose>
                    <c:when test="${empty evidenceList}">
                        <div class="empty">
                            <svg class="icon icon-lg" aria-hidden="true"><use href="#i-file"/></svg>
                            <p>No evidence registered for this case yet.</p>
                            <c:if test="${me.role.canUploadEvidence() and not c.closed}">
                                <a class="btn btn-primary btn-sm" href="${ctx}/evidence?action=upload&amp;caseId=${c.id}">Upload first evidence</a>
                            </c:if>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-wrap">
                            <table>
                                <thead><tr><th>Evidence</th><th>Type</th><th>Integrity</th><th>Status</th><th>Collected</th></tr></thead>
                                <tbody>
                                <c:forEach var="ev" items="${evidenceList}">
                                    <tr>
                                        <td><a class="row-link" href="${ctx}/evidence?id=${ev.id}"><c:out value="${ev.fileName}"/></a>
                                            <span class="sub"><span class="code">${ev.displayCode}</span> · ${ev.fileSizeText}</span></td>
                                        <td class="nowrap">${ev.evidenceType.label}</td>
                                        <td><span class="badge b-${ev.integrity.css}">${ev.integrity.label}</span></td>
                                        <td><span class="badge b-${ev.status.css}">${ev.status.label}</span></td>
                                        <td class="nowrap muted">${ev.collectedAtText}</td>
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
            <div class="card-head">
                <h2>Findings <span class="muted small">(${fn:length(findings)})</span></h2>
                <a class="small" href="${ctx}/findings?caseId=${c.id}">Manage findings</a>
            </div>
            <div class="card-body flush">
                <c:choose>
                    <c:when test="${empty findings}"><div class="empty"><p>No findings yet.</p></div></c:when>
                    <c:otherwise>
                        <div class="table-wrap">
                            <table>
                                <thead><tr><th>Finding</th><th>Severity</th><th>Status</th></tr></thead>
                                <tbody>
                                <c:forEach var="f" items="${findings}">
                                    <tr><td><c:out value="${f.title}"/></td>
                                        <td><span class="badge b-${f.severity.css}">${f.severity.label}</span></td>
                                        <td><span class="badge b-${f.status.css}">${f.status.label}</span></td></tr>
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
            <div class="card-head"><h2>Manage case</h2></div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${c.closed}">
                        <p class="muted">This case was closed on ${c.closedAtText}. Closed cases are read-only.</p>
                    </c:when>
                    <c:when test="${not me.role.canUpdateCase()}">
                        <p class="muted">Your role (${me.role.label}) can view this case but not change it.</p>
                    </c:when>
                    <c:otherwise>
                        <form method="post" action="${ctx}/cases" class="stack">
                            <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="action" value="update">
                            <input type="hidden" name="id" value="${c.id}">
                            <div class="field">
                                <label for="status">Status</label>
                                <select id="status" name="status">
                                    <c:forEach var="s" items="${statuses}">
                                        <c:if test="${s != 'CLOSED'}"><option value="${s}" ${s == c.status ? 'selected' : ''}>${s.label}</option></c:if>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="field">
                                <label for="priority">Priority</label>
                                <select id="priority" name="priority">
                                    <c:forEach var="p" items="${priorities}"><option value="${p}" ${p == c.priority ? 'selected' : ''}>${p.label}</option></c:forEach>
                                </select>
                            </div>
                            <div class="field">
                                <label for="assignedTo">Investigator</label>
                                <select id="assignedTo" name="assignedTo" ${me.role.canAssignCase() ? '' : 'disabled'}>
                                    <option value="">Unassigned</option>
                                    <c:forEach var="u" items="${users}"><option value="${u.id}" ${u.id == c.assignedToId ? 'selected' : ''}><c:out value="${u.fullName}"/></option></c:forEach>
                                </select>
                                <c:if test="${not me.role.canAssignCase()}"><span class="hint">Only an administrator can reassign.</span></c:if>
                            </div>
                            <button type="submit" class="btn btn-primary btn-block">Save changes</button>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <c:if test="${me.role.canCloseCase() and not c.closed}">
            <section class="card">
                <div class="card-head"><h2>Close case</h2></div>
                <div class="card-body">
                    <p class="small muted" style="margin-bottom:12px">Closing makes the case read-only. No more evidence can be added.</p>
                    <form method="post" action="${ctx}/cases" data-confirm="Close case ${c.caseNumber}? This cannot be undone.">
                        <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="action" value="close">
                        <input type="hidden" name="id" value="${c.id}">
                        <button type="submit" class="btn btn-danger btn-block"><svg class="icon" aria-hidden="true"><use href="#i-x"/></svg>Close case</button>
                    </form>
                </div>
            </section>
        </c:if>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
