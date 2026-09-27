<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="pageTitle" value="Evidence"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <div>
        <h1>Evidence register</h1>
        <p>Every item is fingerprinted with SHA-256 on upload so tampering can be detected later.</p>
    </div>
    <c:if test="${me.role.canUploadEvidence()}">
        <div class="actions"><a class="btn btn-primary" href="${ctx}/evidence?action=upload"><svg class="icon" aria-hidden="true"><use href="#i-upload"/></svg>Upload evidence</a></div>
    </c:if>
</div>

<section class="card">
    <div class="card-body flush">
        <c:choose>
            <c:when test="${empty evidenceList}">
                <div class="empty">
                    <svg class="icon icon-lg" aria-hidden="true"><use href="#i-file"/></svg>
                    <p>No evidence uploaded yet.</p>
                    <c:if test="${me.role.canUploadEvidence()}"><a class="btn btn-primary btn-sm" href="${ctx}/evidence?action=upload">Upload evidence</a></c:if>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table>
                        <thead><tr><th>Evidence</th><th>Case</th><th>Type</th><th>SHA-256 (first 16)</th><th>Integrity</th><th>Status</th><th>Collected by</th></tr></thead>
                        <tbody>
                        <c:forEach var="ev" items="${evidenceList}">
                            <tr>
                                <td><a class="row-link" href="${ctx}/evidence?id=${ev.id}"><c:out value="${ev.fileName}"/></a>
                                    <span class="sub"><span class="code">${ev.displayCode}</span> · ${ev.fileSizeText}</span></td>
                                <td><a class="code" href="${ctx}/cases?id=${ev.caseId}"><c:out value="${ev.caseNumber}"/></a></td>
                                <td class="nowrap">${ev.evidenceType.label}</td>
                                <td><span class="hash-short" title="${ev.sha256Original}">${fn:substring(ev.sha256Original, 0, 16)}…</span></td>
                                <td><span class="badge b-${ev.integrity.css}">${ev.integrity.label}</span></td>
                                <td><span class="badge b-${ev.status.css}">${ev.status.label}</span></td>
                                <td class="nowrap"><c:out value="${ev.collectedByName}"/><span class="sub">${ev.collectedAtText}</span></td>
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
