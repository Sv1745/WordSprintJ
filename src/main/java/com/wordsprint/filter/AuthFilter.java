package com.wordsprint.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter({"/dashboard.html", "/profile.html", "/game.html", "/admin.html", "/login.html", "/register.html"})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("user_id") != null);
        String requestURI = req.getRequestURI();

        // Protected pages: redirect unauthenticated users directly to login.html BEFORE rendering DOM
        if (requestURI.endsWith("/dashboard.html") ||
            requestURI.endsWith("/profile.html") ||
            requestURI.endsWith("/game.html") ||
            requestURI.endsWith("/admin.html")) {

            if (!loggedIn) {
                res.sendRedirect(req.getContextPath() + "/login.html");
                return;
            }

            // Role check for admin.html
            if (requestURI.endsWith("/admin.html")) {
                String role = (String) session.getAttribute("role");
                if (role == null || !"admin".equalsIgnoreCase(role.trim())) {
                    res.sendRedirect(req.getContextPath() + "/dashboard.html");
                    return;
                }
            }
        }

        // Public auth pages: redirect authenticated users directly to dashboard.html
        if (requestURI.endsWith("/login.html") || requestURI.endsWith("/register.html")) {
            if (loggedIn) {
                res.sendRedirect(req.getContextPath() + "/dashboard.html");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
