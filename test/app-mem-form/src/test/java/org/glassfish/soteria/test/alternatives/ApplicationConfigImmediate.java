package org.glassfish.soteria.test.alternatives;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.security.enterprise.authentication.mechanism.http.FormAuthenticationMechanismDefinition;
import javax.security.enterprise.authentication.mechanism.http.LoginToContinue;

@FormAuthenticationMechanismDefinition(
    loginToContinue = @LoginToContinue(
        loginPage="${applicationConfigImmediate.loginPage}",
        errorPage="/login-error-servlet"
    )
)
@ApplicationScoped
@Named
public class ApplicationConfigImmediate {
    
    public String getLoginPage() {
        System.out.print("returning servlet");
        return "/login-servlet-alt";
    }

}
