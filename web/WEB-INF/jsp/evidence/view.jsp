<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="pageTitle" value="${ev.displayCode}"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <div>
        <a href="${ctx}/cases?id=${ev.caseId}" class="small"><svg class="icon icon-sm" aria-hidden="true"><use href="#i-back"/></svg> Back to <c:out value="${ev.caseNumber}"/></a>
        <h1 style="margin-top:6px;overflow-wrap:anywhere"><c:out value="${ev.fileName}"/></h1>
        <p><span class="code">${ev.displayCode}</span> · ${ev.evidenceType.label} · ${ev.fileSizeText}
            · <span class="badge b-${ev.status.css}">${ev.status.label}</span></p>
    </div>
    <div class="actions">
        <c:if test="${ev.textReadable}">
            <a class="btn btn-secondary" href="${ctx}/analysis?evidenceId=${ev.id}"><svg class="icon" aria-hidden="true"><use href="#i-activity"/></svg>Analyse log</a>
        </c:if>
    </div>
</div>

<div class="grid grid-main">
    <div class="stack">
        <%-- Integrity: the core forensic feature --%>
        <section class="card">
            <div class="card-head"><h2><svg class="icon" aria-hidden="true"><use href="#i-hash"/></svg>Integrity (SHA-256)</h2></div>
            <div class="card-body stack">
                <div class="integrity-panel ${ev.integrity.css}">
                    <svg class="icon" aria-hidden="true"><use href="#${ev.integrity == 'VERIFIED' ? 'i-shield' : (ev.integrity == 'MODIFIED' ? 'i-shield-x' : 'i-shield-q')}"/></svg>
                    <div>
                        <c:choose>
                            <c:when test="${ev.integrity == 'VERIFIED'}"><strong>Evidence integrity verified</strong><span>Current hash matches the original. Last checked ${ev.lastVerifiedAtText}.</span></c:when>
                            <c:when test="${ev.integrity == 'MODIFIED'}"><strong>Evidence may have been modified</strong><span>Current hash does NOT match the original. Checked ${ev.lastVerifiedAtText}.</span></c:when>
                            <c:otherwise><strong>Not verified yet</strong><span>Run a verification to compare the stored file with its original hash.</span></c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div>
                    <div class="label" style="margin-bottom:6px">Original hash (recorded at upload)</div>
                    <div class="hash-box">
                        <span class="hash" id="origHash">${ev.sha256Original}</span>
                        <button type="button" class="icon-btn" data-copy="origHash" aria-label="Copy hash" style="width:34px;height:34px">
                            <svg class="icon icon-sm" aria-hidden="true"><use href="#i-copy"/></svg></button>
                    </div>
                    <span class="sr-only" id="copyStatus" role="status"></span>
                </div>

                <form method="post" action="${ctx}/evidence">
                    <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
                    <input type="hidden" name="action" value="verify">
                    <input type="hidden" name="id" value="${ev.id}">
                    <button type="submit" class="btn btn-primary" data-loading="Re-hashing file…">
                        <span class="spinner" aria-hidden="true"></span><svg class="icon" aria-hidden="true"><use href="#i-shield"/></svg><span class="label">Verify integrity now</span>
                    </button>
                </form>

                <c:if test="${not empty hashChecks}">
                    <div class="table-wrap" style="border:1px solid var(--border);border-radius:8px">
                        <table>
                            <thead><tr><th>Checked</th><th>Computed hash</th><th>Result</th><th>By</th></tr></thead>
                            <tbody>
                            <c:forEach var="h" items="${hashChecks}">
                                <tr><td class="nowrap muted">${h.checkedAtText}</td>
                                    <td><span class="hash-short" title="${h.computedHash}">${fn:substring(h.computedHash, 0, 20)}…</span></td>
                                    <td><span class="badge b-${h.result.css}">${h.result.label}</span></td>
                                    <td class="nowrap"><c:out value="${h.checkedByName}"/></td></tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:if>
            </div>
        </section>

        <section class="card" id="custody">
            <div class="card-head"><h2><svg class="icon" aria-hidden="true"><use href="#i-link"/></svg>Chain of custody</h2>
                <span class="small muted">${fn:length(custody)} entries · append-only</span></div>
            <div class="card-body">
                <ol class="steps">
                    <c:forEach var="r" items="${custody}">
                        <li>
                            <span class="step-dot ${fn:contains(r.action, 'Verified') ? 'ok' : (fn:contains(r.action, 'FAILED') ? 'bad' : '')}"></span>
                            <div class="step-title"><c:out value="${r.action}"/>
                                <c:if test="${not empty r.handedToName}"> → <c:out value="${r.handedToName}"/></c:if></div>
                            <div class="step-meta">${r.actionTimeText} · <c:out value="${r.performedByName}"/></div>
                            <c:if test="${not empty r.notes}"><div class="step-note"><c:out value="${r.notes}"/></div></c:if>
                        </li>
                    </c:forEach>
                </ol>
            </div>
        </section>
    </div>

    <div class="stack">
        <section class="card">
            <div class="card-head"><h2>Metadata</h2></div>
            <div class="card-body">
                <dl class="details" style="grid-template-columns:1fr">
                    <div><dt>Case</dt><dd><a href="${ctx}/cases?id=${ev.caseId}" class="code"><c:out value="${ev.caseNumber}"/></a></dd></div>
                    <div><dt>Collected by</dt><dd><c:out value="${ev.collectedByName}"/></dd></div>
                    <div><dt>Collected at</dt><dd>${ev.collectedAtText}</dd></div>
                    <div><dt>Uploaded at</dt><dd>${ev.uploadedAtText}</dd></div>
                    <div><dt>File size</dt><dd>${ev.fileSizeText} <span class="muted">(${ev.fileSize} bytes)</span></dd></div>
                    <div><dt>Stored as</dt><dd class="mono small"><c:out value="${ev.storedName}"/></dd></div>
                    <div><dt>Description</dt><dd style="white-space:pre-line"><c:out value="${empty ev.description ? '-' : ev.description}"/></dd></div>
                </dl>
            </div>
        </section>

        <c:if test="${me.role.canRecordCustody()}">
            <section class="card">
                <div class="card-head"><h2>Record custody action</h2></div>
                <div class="card-body">
                    <form method="post" action="${ctx}/custody" class="stack">
                        <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="evidenceId" value="${ev.id}">
                        <div class="field">
                            <label for="custodyAction">Action</label>
                            <select id="custodyAction" name="custodyAction">
                                <c:forEach var="a" items="${custodyActions}"><option><c:out value="${a}"/></option></c:forEach>
                            </select>
                        </div>
                        <div class="field" id="handedToField">
                            <label for="handedTo">Transfer to</label>
                            <select id="handedTo" name="handedTo">
                                <option value="">Choose a person…</option>
                                <c:forEach var="u" items="${users}">
                                    <c:if test="${u.id != me.id}"><option value="${u.id}"><c:out value="${u.fullName}"/> (${u.role.label})</option></c:if>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="field">
                            <label for="notes">Notes</label>
                            <textarea id="notes" name="notes" rows="2" maxlength="500" placeholder="Reason, location, seal number…"></textarea>
                        </div>
                        <button type="submit" class="btn btn-primary btn-block">Add to chain of custody</button>
                    </form>
                </div>
            </section>

            <section class="card">
                <div class="card-head"><h2>Evidence status</h2></div>
                <div class="card-body">
                    <form method="post" action="${ctx}/evidence" class="inline-form">
                        <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="action" value="status">
                        <input type="hidden" name="id" value="${ev.id}">
                        <div class="field">
                            <label for="evStatus" class="sr-only">Status</label>
                            <select id="evStatus" name="status">
                                <c:forEach var="s" items="${evidenceStatuses}"><option value="${s}" ${s == ev.status ? 'selected' : ''}>${s.label}</option></c:forEach>
                            </select>
                        </div>
                        <button type="submit" class="btn btn-secondary">Update</button>
                    </form>
                </div>
            </section>
        </c:if>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
