<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="pageTitle" value="${type.label}"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>
<c:set var="c" value="${theCase}"/>
<c:set var="t" value="${type.name}"/>

<div class="page-head no-print">
    <div>
        <a href="${ctx}/reports?caseId=${c.id}" class="small"><svg class="icon icon-sm" aria-hidden="true"><use href="#i-back"/></svg> Back to reports</a>
    </div>
    <div class="actions">
        <c:if test="${t != 'SUMMARY'}">
            <a class="btn btn-secondary" href="${ctx}/reports?caseId=${c.id}&amp;type=${t}&amp;format=csv"><svg class="icon" aria-hidden="true"><use href="#i-download"/></svg>CSV</a>
        </c:if>
        <button type="button" class="btn btn-primary" data-print><svg class="icon" aria-hidden="true"><use href="#i-printer"/></svg>Print / Save as PDF</button>
    </div>
</div>

<article class="report">
    <header class="report-head">
        <div>
            <div class="small muted">ABC Cyber Forensics &amp; Incident Response Center</div>
            <h1 style="margin-top:4px">${type.label}</h1>
            <div style="margin-top:6px"><span class="code"><c:out value="${c.caseNumber}"/></span> · <c:out value="${c.title}"/></div>
        </div>
        <div class="small" style="text-align:right">
            <div><span class="muted">Generated:</span> ${generatedAt}</div>
            <div><span class="muted">By:</span> <c:out value="${generatedBy}"/></div>
            <div style="margin-top:6px"><span class="badge b-${c.status.css}">${c.status.label}</span> <span class="badge b-${c.priority.css}">${c.priority.label}</span></div>
        </div>
    </header>

    <%-- ===== Case details (summary only) ===== --%>
    <c:if test="${t == 'SUMMARY'}">
        <h2>1. Case details</h2>
        <dl class="details">
            <div><dt>Incident type</dt><dd><c:out value="${c.incidentType}"/></dd></div>
            <div><dt>Investigator</dt><dd><c:out value="${empty c.assignedToName ? 'Unassigned' : c.assignedToName}"/></dd></div>
            <div><dt>Opened</dt><dd>${c.createdAtText}</dd></div>
            <div><dt>Closed</dt><dd>${c.closedAtText}</dd></div>
            <div class="full"><dt>Description</dt><dd style="white-space:pre-line"><c:out value="${c.description}"/></dd></div>
        </dl>
        <h2>2. Overview</h2>
        <dl class="details">
            <div><dt>Evidence items</dt><dd>${fn:length(evidence)}</dd></div>
            <div><dt>Findings</dt><dd>${fn:length(findings)}</dd></div>
            <div><dt>Custody entries</dt><dd>${fn:length(custody)}</dd></div>
            <div><dt>Timeline events</dt><dd>${fn:length(timeline)}</dd></div>
        </dl>
    </c:if>

    <%-- ===== Evidence ===== --%>
    <c:if test="${t == 'SUMMARY' or t == 'EVIDENCE'}">
        <h2>${t == 'SUMMARY' ? '3. ' : ''}Evidence</h2>
        <c:choose>
            <c:when test="${empty evidence}"><p class="muted">No evidence registered.</p></c:when>
            <c:otherwise>
                <div class="table-wrap"><table>
                    <thead><tr><th>ID</th><th>File</th><th>Type</th><th>SHA-256</th><th>Integrity</th><th>Collected</th></tr></thead>
                    <tbody>
                    <c:forEach var="ev" items="${evidence}">
                        <tr><td class="code">${ev.displayCode}</td>
                            <td><c:out value="${ev.fileName}"/><span class="sub">${ev.fileSizeText}</span></td>
                            <td class="nowrap">${ev.evidenceType.label}</td>
                            <td class="hash" style="font-size:.72rem;min-width:180px">${ev.sha256Original}</td>
                            <td><span class="badge b-${ev.integrity.css}">${ev.integrity.label}</span></td>
                            <td class="nowrap"><c:out value="${ev.collectedByName}"/><span class="sub">${ev.collectedAtText}</span></td></tr>
                    </c:forEach>
                    </tbody>
                </table></div>
            </c:otherwise>
        </c:choose>
    </c:if>

    <%-- ===== Hash verification ===== --%>
    <c:if test="${t == 'HASH'}">
        <h2>Hash verification history</h2>
        <c:choose>
            <c:when test="${empty hashChecks}"><p class="muted">No integrity checks have been run for this case yet.</p></c:when>
            <c:otherwise>
                <div class="table-wrap"><table>
                    <thead><tr><th>Checked at</th><th>Evidence</th><th>Computed SHA-256</th><th>Result</th><th>Checked by</th></tr></thead>
                    <tbody>
                    <c:forEach var="h" items="${hashChecks}">
                        <tr><td class="nowrap mono small">${h.checkedAtText}</td>
                            <td class="code">${h.evidenceCode}</td>
                            <td class="hash" style="font-size:.72rem">${h.computedHash}</td>
                            <td><span class="badge b-${h.result.css}">${h.result.label}</span></td>
                            <td class="nowrap"><c:out value="${h.checkedByName}"/></td></tr>
                    </c:forEach>
                    </tbody>
                </table></div>
            </c:otherwise>
        </c:choose>
    </c:if>

    <%-- ===== Findings ===== --%>
    <c:if test="${t == 'SUMMARY' or t == 'FINDINGS'}">
        <h2>${t == 'SUMMARY' ? '4. ' : ''}Findings</h2>
        <c:choose>
            <c:when test="${empty findings}"><p class="muted">No findings recorded.</p></c:when>
            <c:otherwise>
                <div class="table-wrap"><table>
                    <thead><tr><th>Finding</th><th>Severity</th><th>Status</th><th>Evidence</th><th>Recorded</th></tr></thead>
                    <tbody>
                    <c:forEach var="f" items="${findings}">
                        <tr><td><strong style="font-weight:500"><c:out value="${f.title}"/></strong>
                                <c:if test="${not empty f.description}"><span class="sub"><c:out value="${f.description}"/></span></c:if></td>
                            <td><span class="badge b-${f.severity.css}">${f.severity.label}</span></td>
                            <td><span class="badge b-${f.status.css}">${f.status.label}</span></td>
                            <td class="code">${f.evidenceCode}</td>
                            <td class="nowrap"><c:out value="${f.createdByName}"/><span class="sub">${f.createdAtText}</span></td></tr>
                    </c:forEach>
                    </tbody>
                </table></div>
            </c:otherwise>
        </c:choose>
    </c:if>

    <%-- ===== Chain of custody ===== --%>
    <c:if test="${t == 'SUMMARY' or t == 'CUSTODY'}">
        <h2>${t == 'SUMMARY' ? '5. ' : ''}Chain of custody</h2>
        <c:choose>
            <c:when test="${empty custody}"><p class="muted">No custody records.</p></c:when>
            <c:otherwise>
                <div class="table-wrap"><table>
                    <thead><tr><th>Time</th><th>Evidence</th><th>Action</th><th>Performed by</th><th>Handed to</th><th>Notes</th></tr></thead>
                    <tbody>
                    <c:forEach var="r" items="${custody}">
                        <tr><td class="nowrap mono small">${r.actionTimeText}</td><td class="code">${r.evidenceCode}</td>
                            <td><c:out value="${r.action}"/></td><td class="nowrap"><c:out value="${r.performedByName}"/></td>
                            <td class="nowrap"><c:out value="${empty r.handedToName ? '-' : r.handedToName}"/></td>
                            <td class="small" style="overflow-wrap:anywhere;max-width:280px"><c:out value="${r.notes}"/></td></tr>
                    </c:forEach>
                    </tbody>
                </table></div>
            </c:otherwise>
        </c:choose>
    </c:if>

    <%-- ===== Timeline ===== --%>
    <c:if test="${t == 'SUMMARY' or t == 'TIMELINE'}">
        <h2>${t == 'SUMMARY' ? '6. ' : ''}Timeline of events</h2>
        <c:choose>
            <c:when test="${empty timeline}"><p class="muted">No timeline events.</p></c:when>
            <c:otherwise>
                <div class="table-wrap"><table>
                    <thead><tr><th>Time</th><th>Event</th><th>Source</th></tr></thead>
                    <tbody>
                    <c:forEach var="e" items="${timeline}">
                        <tr><td class="nowrap mono small">${e.eventTimeText}</td>
                            <td><c:out value="${e.title}"/><c:if test="${not empty e.description}"><span class="sub"><c:out value="${e.description}"/></span></c:if></td>
                            <td><span class="badge b-${e.source.css}">${e.source.label}</span></td></tr>
                    </c:forEach>
                    </tbody>
                </table></div>
            </c:otherwise>
        </c:choose>
    </c:if>

    <footer class="report-foot">
        <span>ForensiTrack · Digital Forensics Investigation System</span>
        <span>Confidential. For authorized investigation use only.</span>
    </footer>
</article>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
