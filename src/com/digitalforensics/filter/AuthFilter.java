package com.digitalforensics.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Runs before every request (a Servlet Filter).
 *  1. Authentication - anyone not logged in is sent to the login page.
 *  2. CSRF protection - every POST must carry the secret token stored in the session,
 *     so another website cannot submit forms on the user's behalf.
 *  3. No-cache headers - after logout, the Back button cannot show case data.
 */
@WebFilter("/*")
public class AuthFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        String path = req.getRequestURI().substring(req.getContextPath().length());

        boolean isPublic = path.equals("/login") || path.startsWith("/css/") || path.startsWith("/js/")
                || path.equals("/favicon.svg");
        if (isPublic) {
            chain.doFilter(req, resp);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String expected = (String) session.getAttribute("csrfToken");
            String actual = req.getParameter("csrf");
            if (expected == null || !expected.equals(actual)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Your form session expired. Go back, refresh the page and try again.");
                return;
            }
        }

        resp.setHeader("Cache-Control", "no-store");
        chain.doFilter(req, resp);
    }
}
