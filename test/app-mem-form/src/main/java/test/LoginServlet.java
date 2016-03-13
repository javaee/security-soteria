package test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that is invoked when it's determined that the called needs to authenticate/login.
 *
 */
@WebServlet({"/login-servlet"})
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().write(
            "<html><body> Login to continue \n" +
                "<form method=\"POST\" action=\"j_security_check\">" +
                    "<p><strong>Username </strong>" +
                    "<input type=\"text\" name=\"j_username\">" +
                    
                    "<p><strong>Password </strong>" +
                    "<input type=\"password\" name=\"j_password\">" +
                    "<p>" +
                    "<input type=\"submit\" value=\"Submit\">" +
                "</form>" +
            "</body></html>");
    }

}
