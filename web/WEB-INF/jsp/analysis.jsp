<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="pageTitle" value="Log Analysis"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <div>
        <h1>Log analysis</h1>
        <p>Scan a text/log evidence file for suspicious keywords. Matching lines with timestamps can be added to the case timeline.</p>
    </div>
</div>

<section class="card">
    <div class="card-body">
        <c:choose>
            <c:when test="${empty textFiles}">
                <div class="empty" style="padding:16px">
                    <svg class="icon icon-lg" aria-hidden="true"><use href="#i-activity"/></svg>
                    <p>No .log, .txt or .csv evidence uploaded yet. Try <span class="mono">sample-evidence/hr_server_auth.log</span>.</p>
                    <c:if test="${me.role.canUploadEvidence()}"><a class="btn btn-primary btn-sm" href="${ctx}/evidence?action=upload">Upload evidence</a></c:if>
                </div>
            </c:when>
            <c:otherwise>
                <form method="post" action="${ctx}/analysis" class="inline-form">
                    <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
                    <input type="hidden" name="action" value="analyze">
                    <div class="field" style="flex:1 1 280px">
                        <label for="evidenceId">Evidence file</label>
                        <select id="evidenceId" name="evidenceId" required>
                            <c:forEach var="ev" items="${textFiles}">
                                <option value="${ev.id}" ${ev.id == selectedId ? 'selected' : ''}>${ev.displayCode} · <c:out value="${ev.fileName}"/> (<c:out value="${ev.caseNumber}"/>)</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="field" style="flex:2 1 320px">
                        <label for="keywords">Keywords <span class="hint">(comma or space separated)</span></label>
                        <input type="text" id="keywords" name="keywords" value="${fn:escapeXml(keywords)}" spellcheck="false" class="mono">
                    </div>
                    <button type="submit" class="btn btn-primary" data-loading="Scanning…">
                        <span class="spinner" aria-hidden="true"></span><svg class="icon" aria-hidden="true"><use href="#i-search"/></svg><span class="label">Analyse</span>
                    </button>
                </form>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<c:if test="${not empty result}">
    <div class="grid grid-main" style="margin-top:20px">
        <section class="card">
            <div class="card-head">
                <h2>Matching lines <span class="muted small">(${fn:length(result.matches)}${result.truncated ? ', first 500 shown' : ''})</span></h2>
            </div>
            <c:choose>
                <c:when test="${empty result.matches}">
                    <div class="empty"><svg class="icon icon-lg" aria-hidden="true"><use href="#i-shield"/></svg><p>No lines matched these keywords.</p></div>
                </c:when>
                <c:otherwise>
                    <form method="post" action="${ctx}/analysis">
                        <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="action" value="timeline">
                        <input type="hidden" name="evidenceId" value="${analyzed.id}">
                        <div class="card-body" style="display:flex;gap:12px;align-items:center;flex-wrap:wrap;border-bottom:1px solid var(--border)">
                            <label style="display:flex;gap:8px;align-items:center"><input type="checkbox" id="selectAll" class="chk"> Select all</label>
                            <span class="small muted" id="selectedCount" aria-live="polite">0 selected</span>
                            <button type="submit" id="addToTimeline" class="btn btn-primary btn-sm" style="margin-left:auto">
                                <svg class="icon icon-sm" aria-hidden="true"><use href="#i-clock"/></svg>Add selected to timeline</button>
                        </div>
                        <div class="table-wrap" style="max-height:560px;overflow-y:auto">
                            <table>
                                <thead><tr><th style="width:36px"><span class="sr-only">Select</span></th><th class="num">Line</th><th>Keyword</th><th>Content</th></tr></thead>
                                <tbody>
                                <c:forEach var="m" items="${result.matches}">
                                    <tr>
                                        <td><c:if test="${m.hasTimestamp}"><input type="checkbox" class="chk" name="line" value="${m.lineNumber}" aria-label="Add line ${m.lineNumber} to timeline"></c:if></td>
                                        <td class="num mono small muted">${m.lineNumber}</td>
                                        <td><mark class="mono small"><c:out value="${m.keyword}"/></mark></td>
                                        <td class="log-line"><c:out value="${m.text}"/></td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                        <p class="small muted" style="padding:10px 20px">Only lines containing a timestamp (yyyy-MM-dd HH:mm:ss) can be placed on the timeline.</p>
                    </form>
                </c:otherwise>
            </c:choose>
        </section>

        <div class="stack">
            <section class="card">
                <div class="card-head"><h2>Keyword hits</h2></div>
                <div class="card-body">
                    <div class="bars">
                        <c:forEach var="k" items="${result.keywordCounts}">
                            <div class="bar-row">
                                <span class="bar-label mono small" title="${fn:escapeXml(k.key)}"><c:out value="${k.key}"/></span>
                                <span class="bar-track" role="img" aria-label="${fn:escapeXml(k.key)}: ${k.value} lines">
                                    <span class="bar-fill ${k.value == 0 ? 'zero' : ''}" style="display:block;width:${result.maxCount == 0 ? 0 : k.value * 100 / result.maxCount}%"></span>
                                </span>
                                <span class="count">${k.value}</span>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </section>
            <section class="card">
                <div class="card-head"><h2>Scan summary</h2></div>
                <div class="card-body">
                    <dl class="details">
                        <div><dt>File</dt><dd><a href="${ctx}/evidence?id=${analyzed.id}"><c:out value="${analyzed.fileName}"/></a></dd></div>
                        <div><dt>Case</dt><dd class="code"><c:out value="${analyzed.caseNumber}"/></dd></div>
                        <div><dt>Lines scanned</dt><dd>${result.totalLines}</dd></div>
                        <div><dt>Total hits</dt><dd>${result.totalMatches}</dd></div>
                        <div><dt>Scan time</dt><dd>${result.elapsedMillis} ms</dd></div>
                        <div><dt>Thread</dt><dd class="small">Background worker</dd></div>
                    </dl>
                </div>
            </section>
        </div>
    </div>
</c:if>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
