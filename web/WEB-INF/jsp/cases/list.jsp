<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="pageTitle" value="Cases"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <div>
        <h1>Investigation cases</h1>
        <p>Search, filter and open cases.</p>
    </div>
    <c:if test="${me.role.canCreateCase()}">
        <div class="actions"><a class="btn btn-primary" href="${ctx}/cases?action=new"><svg class="icon" aria-hidden="true"><use href="#i-plus"/></svg>New case</a></div>
    </c:if>
</div>

<section class="card">
    <div class="card-head">
        <form class="filters" method="get" action="${ctx}/cases" role="search" style="width:100%">
            <label class="sr-only" for="q">Search cases</label>
            <input type="search" id="q" name="q" value="${fn:escapeXml(q)}" placeholder="Search by case number, title or incident type">
            <label class="sr-only" for="status">Status</label>
            <select id="status" name="status">
                <option value="">All statuses</option>
                <c:forEach var="s" items="${statuses}">
                    <option value="${s}" ${s == selectedStatus ? 'selected' : ''}>${s.label}</option>
                </c:forEach>
            </select>
            <button type="submit" class="btn btn-secondary"><svg class="icon" aria-hidden="true"><use href="#i-search"/></svg>Search</button>
            <c:if test="${not empty q or not empty selectedStatus}"><a class="btn btn-ghost" href="${ctx}/cases">Clear</a></c:if>
        </form>
    </div>
    <div class="card-body flush">
        <c:choose>
            <c:when test="${empty cases}">
                <div class="empty">
                    <svg class="icon icon-lg" aria-hidden="true"><use href="#i-search"/></svg>
                    <p>No cases match your search.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table>
                        <thead><tr><th>Case</th><th>Incident type</th><th>Priority</th><th>Status</th><th>Investigator</th><th class="num">Evidence</th><th>Opened</th></tr></thead>
                        <tbody>
                        <c:forEach var="c" items="${cases}">
                            <tr>
                                <td><a class="row-link" href="${ctx}/cases?id=${c.id}"><c:out value="${c.title}"/></a>
                                    <span class="sub code"><c:out value="${c.caseNumber}"/></span></td>
                                <td><c:out value="${c.incidentType}"/></td>
                                <td><span class="badge b-${c.priority.css}">${c.priority.label}</span></td>
                                <td><span class="badge b-${c.status.css}">${c.status.label}</span></td>
                                <td class="nowrap"><c:out value="${empty c.assignedToName ? 'Unassigned' : c.assignedToName}"/></td>
                                <td class="num">${c.evidenceCount}</td>
                                <td class="nowrap muted">${c.createdAtText}</td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</section>
<p class="small muted" style="margin-top:10px">${fn:length(cases)} case(s) shown</p>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
