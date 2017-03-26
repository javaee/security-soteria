/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import javax.enterprise.context.ApplicationScoped;
import javax.security.authentication.mechanism.http.annotation.AutoApplySession;
import javax.security.authentication.mechanism.http.annotation.BasicAuthenticationMechanismDefinition;
import javax.security.identitystore.annotation.Credentials;
import javax.security.identitystore.annotation.EmbeddedIdentityStoreDefinition;

/**
 *
 * @author Guillermo
 */
@BasicAuthenticationMechanismDefinition(
    realmName="test realm 2323"
)

@EmbeddedIdentityStoreDefinition({ 
    @Credentials(callerName = "rez2a", password = "secret1", groups = { "foo", "bar" }),
    @Credentials(callerName = "alesx", password = "secresdt2", groups = { "foo", "kaz" }),
    @Credentials(callerName = "arjan", password = "secrest3", groups = { "foos" }) }
)
@AutoApplySession
@ApplicationScoped
public class Bean {
    
}
