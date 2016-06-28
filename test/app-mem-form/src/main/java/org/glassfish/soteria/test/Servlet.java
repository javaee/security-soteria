package org.glassfish.soteria.test;

import java.io.IOException;

import javax.annotation.security.DeclareRoles;
import javax.security.authentication.mechanism.http.annotation.FormAuthenticationMechanismDefinition;
import javax.security.authentication.mechanism.http.annotation.LoginToContinue;
import javax.security.identitystore.annotation.Credentials;
import javax.security.identitystore.annotation.EmbeddedIdentityStoreDefinition;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Test Servlet that prints out the name of the authenticated caller and whether
 * this caller is in any of the roles {foo, bar, kaz}
 */
@FormAuthenticationMechanismDefinition(
    loginToContinue = @LoginToContinue(
        loginPage="/login-servlet",
        errorPage="/login-error-servlet"
    )
)

@EmbeddedIdentityStoreDefinition({ 
    @Credentials(callerName = "reza", password = "secret1", groups = { "foo", "bar" }),
    @Credentials(callerName = "alex", password = "secret2", groups = { "foo", "kaz" }),
    @Credentials(callerName = "arjan", password = "secret3", groups = { "foo" }) }
)

@WebServlet("/servlet")
@DeclareRoles({ "foo", "bar", "kaz" })
@ServletSecurity(@HttpConstraint(rolesAllowed = "foo"))
public class Servlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String webName = null;
        if (request.getUserPrincipal() != null) {
            webName = request.getUserPrincipal().getName();
        }
        
        response.getWriter().write(
                "<html><body> This is a servlet <br><br>\n" +
        
                    "web username: " + webName + "<br><br>\n" +
                            
                    "web user has role \"foo\": " + request.isUserInRole("foo") + "<br>\n" +
                    "web user has role \"bar\": " + request.isUserInRole("bar") + "<br>\n" +
                    "web user has role \"kaz\": " + request.isUserInRole("kaz") + "<br><br>\n" + 

                        
                    "<form method=\"POST\">" +
                        "<input type=\"hidden\" name=\"logout\" value=\"true\"  >" +
                        "<input type=\"submit\" value=\"Logout\">" +
                    "</form>" +
                "</body></html>");
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("true".equals(request.getParameter("logout"))) {
            request.logout();
            request.getSession().invalidate();
        }
        
        doGet(request, response);
    }

}
