<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="pageTitle" value="Upload evidence"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <div>
        <a href="${ctx}/evidence" class="small"><svg class="icon icon-sm" aria-hidden="true"><use href="#i-back"/></svg> Back to evidence</a>
        <h1 style="margin-top:6px">Upload evidence</h1>
        <p>The server calculates a SHA-256 hash and starts the chain of custody automatically.</p>
    </div>
</div>

<c:choose>
    <c:when test="${not me.role.canUploadEvidence()}">
        <div class="alert alert-info"><svg class="icon" aria-hidden="true"><use href="#i-info"/></svg>Your role (${me.role.label}) cannot upload evidence.</div>
    </c:when>
    <c:when test="${empty openCases}">
        <div class="card"><div class="empty"><p>There are no open cases. Create a case first.</p>
            <a class="btn btn-primary btn-sm" href="${ctx}/cases?action=new">New case</a></div></div>
    </c:when>
    <c:otherwise>
        <%-- enctype multipart/form-data is required to send a file --%>
        <form class="card" method="post" action="${ctx}/evidence" enctype="multipart/form-data" style="max-width:820px">
            <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
            <input type="hidden" name="action" value="upload">
            <div class="card-body">
                <div class="form-grid">
                    <div class="field full">
                        <span class="label" id="fileLabel">Evidence file<span class="req" aria-hidden="true">*</span></span>
                        <div class="dropzone">
                            <input type="file" id="evidenceFile" name="file" required aria-labelledby="fileLabel" aria-describedby="fileHelp fileError"
                                   data-allowed="${allowed}" data-max="20971520" accept=".log,.txt,.csv,.pdf,.docx,.png,.jpg,.jpeg">
                            <svg class="icon icon-lg" aria-hidden="true"><use href="#i-upload"/></svg>
                            <div><strong>Click to choose a file</strong> or drag it here</div>
                            <div class="hint" id="fileHelp">Allowed: ${allowed} · max 20 MB</div>
                            <div class="file-name" id="fileName" aria-live="polite"></div>
                        </div>
                        <span class="hint" id="fileError" role="alert" style="color:var(--danger)"></span>
                    </div>
                    <div class="field">
                        <label for="caseId">Case<span class="req" aria-hidden="true">*</span></label>
                        <select id="caseId" name="caseId" required>
                            <option value="">Choose a case…</option>
                            <c:forEach var="c" items="${openCases}">
                                <option value="${c.id}" ${c.id == selectedCaseId ? 'selected' : ''}><c:out value="${c.caseNumber} - ${c.title}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="field">
                        <label for="evidenceType">Evidence type</label>
                        <select id="evidenceType" name="evidenceType">
                            <c:forEach var="t" items="${types}"><option value="${t}">${t.label}</option></c:forEach>
                        </select>
                        <span class="hint">Guessed from the file extension. Change it if needed.</span>
                    </div>
                    <div class="field">
                        <label for="collectedAt">Collected at</label>
                        <input type="datetime-local" id="collectedAt" name="collectedAt" value="${now}" max="${now}">
                        <span class="hint">When the evidence was seized or acquired</span>
                    </div>
                    <div class="field">
                        <span class="label">Collected by</span>
                        <input type="text" value="${fn:escapeXml(me.fullName)}" readonly aria-label="Collected by">
                    </div>
                    <div class="field full">
                        <label for="description">Description</label>
                        <textarea id="description" name="description" rows="3" placeholder="Source device, how it was acquired, anything notable"></textarea>
                    </div>
                </div>
                <div class="form-actions">
                    <a class="btn btn-ghost" href="${ctx}/evidence">Cancel</a>
                    <button type="submit" class="btn btn-primary" data-loading="Uploading &amp; hashing…">
                        <span class="spinner" aria-hidden="true"></span><svg class="icon" aria-hidden="true"><use href="#i-upload"/></svg><span class="label">Upload &amp; hash</span>
                    </button>
                </div>
            </div>
        </form>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
