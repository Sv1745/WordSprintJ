package com.wordsprint.servlet;

import com.wordsprint.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String role = request.getParameter("role");

        if (username == null || password == null || role == null) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Username, password and role are required"
            );
            return;
        }

        boolean registered =
                authService.register(username, password, role);

        response.setContentType("application/json");

        if (registered) {
            response.setStatus(HttpServletResponse.SC_CREATED);

            response.getWriter().write("""
                    {
                        "success": true,
                        "message": "Registration successful"
                    }
                    """);
        } else {
            response.setStatus(HttpServletResponse.SC_CONFLICT);

            response.getWriter().write("""
                    {
                        "success": false,
                        "message": "Username already exists"
                    }
                    """);
        }
    }
}