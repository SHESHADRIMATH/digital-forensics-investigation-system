/* =====================================================================
   app.js - small UI helpers only. All business logic runs on the server.
   ===================================================================== */
(function () {
    "use strict";

    // ---- Theme toggle (light/dark), remembered in localStorage ----
    function setTheme(theme) {
        document.documentElement.setAttribute("data-theme", theme);
        try { localStorage.setItem("df-theme", theme); } catch (e) { /* storage blocked - ignore */ }
        document.querySelectorAll(".theme-toggle").forEach(function (b) {
            b.setAttribute("aria-label", theme === "dark" ? "Switch to light theme" : "Switch to dark theme");
        });
    }
    document.querySelectorAll(".theme-toggle").forEach(function (btn) {
        btn.addEventListener("click", function () {
            var current = document.documentElement.getAttribute("data-theme");
            setTheme(current === "dark" ? "light" : "dark");
        });
    });
    setTheme(document.documentElement.getAttribute("data-theme") || "light");

    // ---- Mobile sidebar ----
    var sidebar = document.querySelector(".sidebar");
    var scrim = document.querySelector(".scrim");
    var menuBtn = document.querySelector(".menu-btn");
    function closeMenu() {
        if (!sidebar) return;
        sidebar.classList.remove("open");
        scrim && scrim.classList.remove("show");
        menuBtn && menuBtn.setAttribute("aria-expanded", "false");
    }
    if (menuBtn && sidebar) {
        menuBtn.addEventListener("click", function () {
            var open = sidebar.classList.toggle("open");
            scrim && scrim.classList.toggle("show", open);
            menuBtn.setAttribute("aria-expanded", String(open));
        });
        scrim && scrim.addEventListener("click", closeMenu);
        document.addEventListener("keydown", function (e) { if (e.key === "Escape") closeMenu(); });
    }

    // ---- Confirm before destructive / important actions: <form data-confirm="..."> ----
    document.querySelectorAll("form[data-confirm]").forEach(function (form) {
        form.addEventListener("submit", function (e) {
            if (!window.confirm(form.getAttribute("data-confirm"))) e.preventDefault();
        });
    });

    // ---- Loading state on submit buttons (prevents double submit) ----
    document.querySelectorAll("form").forEach(function (form) {
        form.addEventListener("submit", function (e) {
            if (e.defaultPrevented) return;
            var btn = form.querySelector("button[data-loading]");
            if (btn) {
                btn.classList.add("is-loading");
                btn.setAttribute("aria-busy", "true");
                var label = btn.querySelector(".label");
                if (label) label.textContent = btn.getAttribute("data-loading");
                // disable a moment later so the button's own value is still submitted
                setTimeout(function () { btn.disabled = true; }, 0);
            }
        });
    });

    // ---- File picker: show chosen file and check type/size before uploading ----
    var fileInput = document.querySelector("#evidenceFile");
    if (fileInput) {
        var zone = fileInput.closest(".dropzone");
        var nameOut = document.querySelector("#fileName");
        var errOut = document.querySelector("#fileError");
        var allowed = (fileInput.getAttribute("data-allowed") || "").split(/,\s*/);
        var maxBytes = Number(fileInput.getAttribute("data-max")) || 0;
        var typeSelect = document.querySelector("#evidenceType");
        var guess = { log: "LOG", txt: "TEXT", csv: "CSV", pdf: "DOCUMENT", docx: "DOCUMENT", png: "IMAGE", jpg: "IMAGE", jpeg: "IMAGE" };

        fileInput.addEventListener("change", function () {
            var f = fileInput.files[0];
            errOut.textContent = "";
            if (!f) { nameOut.textContent = ""; return; }
            var ext = f.name.split(".").pop().toLowerCase();
            var size = f.size < 1048576 ? (f.size / 1024).toFixed(1) + " KB" : (f.size / 1048576).toFixed(2) + " MB";
            nameOut.textContent = f.name + " (" + size + ")";
            if (allowed.indexOf(ext) < 0) errOut.textContent = "File type ." + ext + " is not allowed.";
            else if (maxBytes && f.size > maxBytes) errOut.textContent = "File is larger than 20 MB.";
            if (typeSelect && guess[ext] && !typeSelect.dataset.touched) typeSelect.value = guess[ext];
        });
        if (typeSelect) typeSelect.addEventListener("change", function () { typeSelect.dataset.touched = "1"; });
        ["dragenter", "dragover"].forEach(function (t) { zone.addEventListener(t, function () { zone.classList.add("is-drag"); }); });
        ["dragleave", "drop"].forEach(function (t) { zone.addEventListener(t, function () { zone.classList.remove("is-drag"); }); });
    }

    // ---- Copy hash to clipboard ----
    document.querySelectorAll("[data-copy]").forEach(function (btn) {
        btn.addEventListener("click", function () {
            var text = document.getElementById(btn.getAttribute("data-copy")).textContent.trim();
            if (!navigator.clipboard) return;
            navigator.clipboard.writeText(text).then(function () {
                var old = btn.getAttribute("aria-label");
                btn.setAttribute("aria-label", "Copied");
                btn.classList.add("copied");
                var status = document.getElementById("copyStatus");
                if (status) status.textContent = "Hash copied to clipboard";
                setTimeout(function () { btn.setAttribute("aria-label", old); btn.classList.remove("copied"); }, 1500);
            });
        });
    });

    // ---- Log analysis: select-all checkbox + live count ----
    var selectAll = document.querySelector("#selectAll");
    if (selectAll) {
        var boxes = document.querySelectorAll("input[name=line]");
        var countOut = document.querySelector("#selectedCount");
        var addBtn = document.querySelector("#addToTimeline");
        var update = function () {
            var n = Array.prototype.filter.call(boxes, function (b) { return b.checked; }).length;
            countOut.textContent = n + " selected";
            addBtn.disabled = n === 0;
        };
        selectAll.addEventListener("change", function () {
            boxes.forEach(function (b) { b.checked = selectAll.checked; });
            update();
        });
        boxes.forEach(function (b) { b.addEventListener("change", update); });
        update();
    }

    // ---- Dismiss alerts ----
    document.querySelectorAll(".alert-close").forEach(function (b) {
        b.addEventListener("click", function () { b.closest(".alert").remove(); });
    });

    // ---- Show / hide password ----
    var pwBtn = document.querySelector("#togglePassword");
    if (pwBtn) {
        pwBtn.addEventListener("click", function () {
            var input = document.querySelector("#password");
            var show = input.type === "password";
            input.type = show ? "text" : "password";
            pwBtn.setAttribute("aria-label", show ? "Hide password" : "Show password");
            pwBtn.setAttribute("aria-pressed", String(show));
        });
    }

    // ---- Print report ----
    document.querySelectorAll("[data-print]").forEach(function (b) {
        b.addEventListener("click", function () { window.print(); });
    });

    // ---- Custody form: "handed to" only matters for transfers ----
    var actionSel = document.querySelector("#custodyAction");
    if (actionSel) {
        var toField = document.querySelector("#handedToField");
        var sync = function () { toField.hidden = actionSel.value !== "Transferred"; };
        actionSel.addEventListener("change", sync);
        sync();
    }
})();
