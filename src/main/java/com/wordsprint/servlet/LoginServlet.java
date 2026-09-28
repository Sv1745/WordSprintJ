package com.wordsprint.servlet;

import com.wordsprint.service.AuthService;
import com.wordsprint.dao.UserDAO;
import com.wordsprint.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("login")
public class LoginServlet extends HttpServlet {
    private final AuthService as = new AuthService();
    private final UserDAO dao = new UserDAO();
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String uname = req.getParameter("uname");
        String pwd = req.getParameter("password");

        if (uname == null || pwd == null) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Provide the username and password!");
        }
        boolean authenticated = as.login(uname, pwd);

        if (!authenticated) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("""
                    {
                        "success": false,
                        "message": "Invalid username or password!"
                    }""");
            return;
        }

        User user = dao.findByUsername(uname);
        res.setContentType("application/json");
        HttpSession session = req.getSession(true);
        session.setAttribute("user_id", user.getUserId());
        session.setAttribute("uname", user.getUserName());
        session.setAttribute("role", user.getUserRole());

        res.setStatus(HttpServletResponse.SC_OK);

        res.getWriter().write("""
                {
                    "success": true,
                    "message": "Login successful
                }""");
    }
}
