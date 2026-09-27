<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="pageTitle" value="Timeline"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <div>
        <h1>Investigation timeline</h1>
        <p>Events reconstructed in chronological order from manual entries, log analysis and the chain of custody.</p>
    </div>
    <c:if test="${not empty theCase}">
        <div class="actions"><a class="btn btn-secondary" href="${ctx}/reports?caseId=${theCase.id}&amp;type=TIMELINE"><svg class="icon" aria-hidden="true"><use href="#i-report"/></svg>Timeline report</a></div>
    </c:if>
</div>

<c:choose>
    <c:when test="${empty cases}">
        <div class="card"><div class="empty"><p>No cases yet.</p></div></div>
    </c:when>
    <c:otherwise>
        <div class="grid grid-main">
            <section class="card">
                <div class="card-head">
                    <form class="filters" method="get" action="${ctx}/timeline">
                        <label for="caseId" class="sr-only">Case</label>
                        <select id="caseId" name="caseId" onchange="this.form.submit()">
                            <c:forEach var="c" items="${cases}">
                                <option value="${c.id}" ${c.id == theCase.id ? 'selected' : ''}><c:out value="${c.caseNumber} - ${c.title}"/></option>
                            </c:forEach>
                        </select>
                        <label for="source" class="sr-only">Source</label>
                        <select id="source" name="source" onchange="this.form.submit()">
                            <option value="">All sources</option>
                            <c:forEach var="s" items="${sources}"><option value="${s}" ${s == selectedSource ? 'selected' : ''}>${s.label}</option></c:forEach>
                        </select>
                        <noscript><button class="btn btn-secondary" type="submit">Show</button></noscript>
                    </form>
                    <div class="legend">
                        <span><i style="background:var(--info)"></i>Manual</span>
                        <span><i style="background:var(--purple)"></i>Log analysis</span>
                        <span><i style="background:var(--neutral)"></i>System / custody</span>
                    </div>
                </div>
                <div class="card-body">
                    <c:choose>
                        <c:when test="${empty days}">
                            <div class="empty"><svg class="icon icon-lg" aria-hidden="true"><use href="#i-clock"/></svg><p>No events for this filter.</p></div>
                        </c:when>
                        <c:otherwise>
                            <p class="small muted" style="margin-bottom:16px">${eventCount} events, oldest first</p>
                            <c:forEach var="day" items="${days}">
                                <div class="day">
                                    <span class="day-label">${day.key}</span>
                                    <ol class="tl">
                                        <c:forEach var="e" items="${day.value}">
                                            <li>
                                                <span class="tl-time">${e.eventClock}</span>
                                                <span class="tl-rail"><span class="tl-dot ${e.source.css}"></span></span>
                                                <div class="tl-card">
                                                    <h3><c:out value="${e.title}"/></h3>
                                                    <c:if test="${not empty e.description}"><p><c:out value="${e.description}"/></p></c:if>
                                                    <div class="meta">
                                                        <span class="badge b-${e.source.css}">${e.source.label}</span>
                                                        <c:if test="${not empty e.evidenceId}"><a class="code" href="${ctx}/evidence?id=${e.evidenceId}">${e.evidenceCode}</a></c:if>
                                                        <span><c:out value="${e.createdByName}"/></span>
                                                    </div>
                                                </div>
                                            </li>
                                        </c:forEach>
                                    </ol>
                                </div>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>
            </section>

            <section class="card">
                <div class="card-head"><h2>Add event</h2></div>
                <div class="card-body">
                    <form method="post" action="${ctx}/timeline" class="stack">
                        <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="caseId" value="${theCase.id}">
                        <div class="field">
                            <label for="eventTime">Date &amp; time<span class="req" aria-hidden="true">*</span></label>
                            <input type="datetime-local" id="eventTime" name="eventTime" value="${now}" step="1" required>
                        </div>
                        <div class="field">
                            <label for="title">Event<span class="req" aria-hidden="true">*</span></label>
                            <input type="text" id="title" name="title" maxlength="150" required placeholder="e.g. USB device connected">
                        </div>
                        <div class="field">
                            <label for="description">Details</label>
                            <textarea id="description" name="description" rows="3" maxlength="1000"></textarea>
                        </div>
                        <button type="submit" class="btn btn-primary btn-block"><svg class="icon" aria-hidden="true"><use href="#i-plus"/></svg>Add to timeline</button>
                    </form>
                    <p class="small muted" style="margin-top:14px">Tip: use <a href="${ctx}/analysis">Log Analysis</a> to import events straight from log files.</p>
                </div>
            </section>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
