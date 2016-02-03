package test;

import java.io.IOException;

import javax.annotation.security.DeclareRoles;
import javax.security.identitystore.annotation.LdapIdentityStoreDefinition;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Test Servlet that prints out the name of the authenticated caller and whether
 * this caller is in any of the roles {foo, bar, kaz}
 * 
 *
 */
@LdapIdentityStoreDefinition(
    url = "ldap://localhost:33389/",
    callerBaseDn = "ou=caller,dc=jsr375,dc=net",
    groupBaseDn  = "ou=group,dc=jsr375,dc=net"
)
@DeclareRoles({ "foo", "bar", "kaz" })
@WebServlet("/servlet")
public class Servlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.getWriter().write("This is a servlet \n");

        String webName = null;
        if (request.getUserPrincipal() != null) {
            webName = request.getUserPrincipal().getName();
        }

        response.getWriter().write("web username: " + webName + "\n");

        response.getWriter().write("web user has role \"foo\": " + request.isUserInRole("foo") + "\n");
        response.getWriter().write("web user has role \"bar\": " + request.isUserInRole("bar") + "\n");
        response.getWriter().write("web user has role \"kaz\": " + request.isUserInRole("kaz") + "\n");
    }

}
