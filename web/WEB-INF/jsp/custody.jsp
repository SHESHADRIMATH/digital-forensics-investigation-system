<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="pageTitle" value="Chain of Custody"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <div>
        <h1>Chain of custody</h1>
        <p>A permanent, append-only record of every action on every evidence item. Entries can never be edited or deleted.</p>
    </div>
    <div class="actions">
        <c:if test="${not empty selectedCaseId}">
            <a class="btn btn-secondary" href="${ctx}/reports?caseId=${selectedCaseId}&amp;type=CUSTODY"><svg class="icon" aria-hidden="true"><use href="#i-report"/></svg>Custody report</a>
        </c:if>
    </div>
</div>

<section class="card">
    <div class="card-head">
        <form class="filters" method="get" action="${ctx}/custody">
            <label for="caseId" class="sr-only">Case</label>
            <select id="caseId" name="caseId" onchange="this.form.submit()">
                <option value="">Latest 100 entries (all cases)</option>
                <c:forEach var="c" items="${cases}">
                    <option value="${c.id}" ${c.id == selectedCaseId ? 'selected' : ''}><c:out value="${c.caseNumber} - ${c.title}"/></option>
                </c:forEach>
            </select>
            <noscript><button class="btn btn-secondary" type="submit">Show</button></noscript>
        </form>
        <span class="small muted">${fn:length(records)} entries</span>
    </div>
    <div class="card-body flush">
        <c:choose>
            <c:when test="${empty records}">
                <div class="empty"><svg class="icon icon-lg" aria-hidden="true"><use href="#i-link"/></svg>
                    <p>No custody records yet. They are created automatically when evidence is uploaded.</p></div>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table>
                        <thead><tr><th>Time</th><th>Evidence</th><th>Action</th><th>Performed by</th><th>Handed to</th><th>Notes</th></tr></thead>
                        <tbody>
                        <c:forEach var="r" items="${records}">
                            <tr>
                                <td class="nowrap mono small">${r.actionTimeText}</td>
                                <td><a class="code" href="${ctx}/evidence?id=${r.evidenceId}#custody">${r.evidenceCode}</a>
                                    <span class="sub"><c:out value="${r.evidenceFileName}"/> · <c:out value="${r.caseNumber}"/></span></td>
                                <td class="nowrap">
                                    <c:choose>
                                        <c:when test="${fn:contains(r.action, 'FAILED')}"><span class="badge b-modified"><c:out value="${r.action}"/></span></c:when>
                                        <c:when test="${fn:contains(r.action, 'Verified')}"><span class="badge b-verified"><c:out value="${r.action}"/></span></c:when>
                                        <c:otherwise><c:out value="${r.action}"/></c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="nowrap"><c:out value="${r.performedByName}"/></td>
                                <td class="nowrap"><c:out value="${empty r.handedToName ? '-' : r.handedToName}"/></td>
                                <td class="small muted" style="max-width:320px;overflow-wrap:anywhere"><c:out value="${r.notes}"/></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
