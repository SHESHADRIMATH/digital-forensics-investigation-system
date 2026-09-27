<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="pageTitle" value="New case"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <div>
        <a href="${ctx}/cases" class="small"><svg class="icon icon-sm" aria-hidden="true"><use href="#i-back"/></svg> Back to cases</a>
        <h1 style="margin-top:6px">Create investigation case</h1>
        <p>A case number (DF-YEAR-NNN) is generated automatically.</p>
    </div>
</div>

<c:choose>
    <c:when test="${not me.role.canCreateCase()}">
        <div class="alert alert-info"><svg class="icon" aria-hidden="true"><use href="#i-info"/></svg>Your role (${me.role.label}) cannot create cases.</div>
    </c:when>
    <c:otherwise>
        <form class="card" method="post" action="${ctx}/cases" style="max-width:820px">
            <input type="hidden" name="csrf" value="${sessionScope.csrfToken}">
            <input type="hidden" name="action" value="create">
            <div class="card-body">
                <div class="form-grid">
                    <div class="field full">
                        <label for="title">Case title<span class="req" aria-hidden="true">*</span></label>
                        <input type="text" id="title" name="title" maxlength="150" required
                               placeholder="e.g. Unauthorized access to finance server">
                    </div>
                    <div class="field">
                        <label for="incidentType">Incident type<span class="req" aria-hidden="true">*</span></label>
                        <select id="incidentType" name="incidentType" required>
                            <option value="">Choose…</option>
                            <c:forEach var="t" items="${incidentTypes}"><option><c:out value="${t}"/></option></c:forEach>
                        </select>
                    </div>
                    <div class="field">
                        <label for="priority">Priority<span class="req" aria-hidden="true">*</span></label>
                        <select id="priority" name="priority" required>
                            <c:forEach var="p" items="${priorities}"><option value="${p}" ${p == 'MEDIUM' ? 'selected' : ''}>${p.label}</option></c:forEach>
                        </select>
                    </div>
                    <div class="field full">
                        <label for="assignedTo">Assign investigator</label>
                        <select id="assignedTo" name="assignedTo">
                            <c:if test="${me.role.canAssignCase()}"><option value="">Unassigned</option></c:if>
                            <c:forEach var="u" items="${users}">
                                <c:if test="${me.role.canAssignCase() or u.id == me.id}">
                                    <option value="${u.id}" ${u.id == me.id and not me.role.canAssignCase() ? 'selected' : ''}><c:out value="${u.fullName}"/> (${u.role.label})</option>
                                </c:if>
                            </c:forEach>
                        </select>
                        <c:if test="${not me.role.canAssignCase()}"><span class="hint">Only an administrator can assign cases to other people.</span></c:if>
                    </div>
                    <div class="field full">
                        <label for="description">Description</label>
                        <textarea id="description" name="description" rows="5" placeholder="What happened, when was it reported, which systems are involved?"></textarea>
                    </div>
                </div>
                <div class="form-actions">
                    <a class="btn btn-ghost" href="${ctx}/cases">Cancel</a>
                    <button type="submit" class="btn btn-primary" data-loading="Creating…">
                        <span class="spinner" aria-hidden="true"></span><svg class="icon" aria-hidden="true"><use href="#i-plus"/></svg><span class="label">Create case</span>
                    </button>
                </div>
            </div>
        </form>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
