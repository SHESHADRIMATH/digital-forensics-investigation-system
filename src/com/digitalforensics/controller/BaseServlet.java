package com.digitalforensics.controller;

import com.digitalforensics.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Parent of all controllers (inheritance). Holds the helpers every servlet needs:
 * who is logged in, reading parameters safely, forwarding to JSPs and flash messages.
 */
public abstract class BaseServlet extends HttpServlet {

    protected static final String USER = "currentUser";

    protected User currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (User) session.getAttribute(USER);
    }

    /**
     * Shows /WEB-INF/jsp/{view}.jsp. JSPs live under WEB-INF so the browser can
     * never open them directly - every page must go through a controller (MVC).
     */
    protected void render(HttpServletRequest req, HttpServletResponse resp, String view, String activeNav)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("flash") != null) {
            // move the flash message from session to request so it is shown exactly once
            req.setAttribute("flash", session.getAttribute("flash"));
            req.setAttribute("flashType", session.getAttribute("flashType"));
            session.removeAttribute("flash");
            session.removeAttribute("flashType");
        }
        req.setAttribute("nav", activeNav);
        req.getRequestDispatcher("/WEB-INF/jsp/" + view + ".jsp").forward(req, resp);
    }

    /** Post/Redirect/Get: after a POST we redirect, so refreshing the page never re-submits a form. */
    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    protected void flash(HttpServletRequest req, String type, String message) {
        req.getSession().setAttribute("flash", message);
        req.getSession().setAttribute("flashType", type); // success | error | info
    }

    protected Integer intParam(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.isBlank()) return null;
        try {
            return Integer.valueOf(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? null : v.trim();
    }

    /** Reads an enum parameter; returns null for blank or unknown values instead of crashing. */
    protected <E extends Enum<E>> E enumParam(HttpServletRequest req, String name, Class<E> type) {
        String v = req.getParameter(name);
        if (v == null || v.isBlank()) return null;
        try {
            return Enum.valueOf(type, v.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
