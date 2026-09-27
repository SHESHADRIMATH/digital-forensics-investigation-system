<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="pageTitle" value="Reports"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <div>
        <h1>Reports</h1>
        <p>Generate a printable report for a case, or download the data as CSV (opens in Excel).</p>
    </div>
</div>

<c:choose>
    <c:when test="${empty cases}">
        <div class="card"><div class="empty"><p>No cases to report on yet.</p></div></div>
    </c:when>
    <c:otherwise>
        <form class="card" method="get" action="${ctx}/reports">
            <div class="card-body stack">
                <div class="field" style="max-width:520px">
                    <label for="caseId">Case<span class="req" aria-hidden="true">*</span></label>
                    <select id="caseId" name="caseId" required>
                        <c:forEach var="c" items="${cases}">
                            <option value="${c.id}" ${c.id == selectedCaseId ? 'selected' : ''}><c:out value="${c.caseNumber} - ${c.title}"/></option>
                        </c:forEach>
                    </select>
                </div>

                <fieldset style="border:0;padding:0;margin:0">
                    <legend class="label" style="margin-bottom:10px">Report type</legend>
                    <div class="choice-grid">
                        <c:forEach var="t" items="${types}" varStatus="st">
                            <label class="choice">
                                <input type="radio" name="type" value="${t.name}" ${st.first ? 'checked' : ''}>
                                <span class="choice-body">
                                    <span class="stat-icon tone-primary" style="width:36px;height:36px"><svg class="icon" aria-hidden="true"><use href="#i-report"/></svg></span>
                                    <span><strong>${t.label}</strong><span>${t.description}</span></span>
                                </span>
                            </label>
                        </c:forEach>
                    </div>
                </fieldset>

                <div class="form-actions" style="justify-content:flex-start">
                    <button type="submit" class="btn btn-primary"><svg class="icon" aria-hidden="true"><use href="#i-eye"/></svg>View report</button>
                    <button type="submit" name="format" value="csv" class="btn btn-secondary"><svg class="icon" aria-hidden="true"><use href="#i-download"/></svg>Download CSV</button>
                </div>
                <p class="hint">CSV export is available for every type except the Investigation Summary.</p>
            </div>
        </form>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
