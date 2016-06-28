package org.glassfish.soteria.test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that is invoked when the caller did not authenticate correctly
 * 
 *
 */
@WebServlet({"/login-error-servlet"})
public class LoginErrorServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().write(
            // Just as example for the mechanism, not likely to be used
            // in practice like this
            "<html><body> Login failed! \n" +
                "<a href=\"login-servlet\">Try again</a>" +
            "</body></html>");
    }

}
