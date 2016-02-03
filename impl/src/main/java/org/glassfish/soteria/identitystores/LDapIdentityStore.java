package org.glassfish.soteria.identitystores;

import static java.lang.String.format;
import static java.util.Collections.list;
import static javax.naming.Context.INITIAL_CONTEXT_FACTORY;
import static javax.naming.Context.PROVIDER_URL;
import static javax.naming.Context.SECURITY_AUTHENTICATION;
import static javax.naming.Context.SECURITY_CREDENTIALS;
import static javax.naming.Context.SECURITY_PRINCIPAL;
import static javax.security.identitystore.CredentialValidationResult.INVALID_RESULT;
import static javax.security.identitystore.CredentialValidationResult.NOT_VALIDATED_RESULT;
import static javax.security.identitystore.CredentialValidationResult.Status.VALID;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.security.CallerPrincipal;
import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.IdentityStore;
import javax.security.identitystore.annotation.LdapIdentityStoreDefinition;
import javax.security.identitystore.credential.Credential;
import javax.security.identitystore.credential.UsernamePasswordCredential;

public class LDapIdentityStore implements IdentityStore {

    private final LdapIdentityStoreDefinition ldapIdentityStoreDefinition;

    public LDapIdentityStore(LdapIdentityStoreDefinition ldapIdentityStoreDefinition) {
        this.ldapIdentityStoreDefinition = ldapIdentityStoreDefinition;
    }

    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (credential instanceof UsernamePasswordCredential) {
            return validate((UsernamePasswordCredential) credential);
        }

        return NOT_VALIDATED_RESULT;
    }

    public CredentialValidationResult validate(UsernamePasswordCredential usernamePasswordCredential) {
        
        // Construct the full distinguished name (dn) of the caller
        String callerDn = createCallerDn(
            ldapIdentityStoreDefinition.callerNameAttribute(), 
            usernamePasswordCredential.getCaller(), 
            ldapIdentityStoreDefinition.callerBaseDn()
        );
        
        // If this doesn't throw an exception internally, the caller dn exists and the password is correct
        LdapContext ldapContext = createLdapContext(
            ldapIdentityStoreDefinition.url(),
            callerDn,
            new String(usernamePasswordCredential.getPassword().getValue())
        );
        
        if (ldapContext == null) {
            return INVALID_RESULT;
        }

        // Search for the groups starting from the groupBaseDn,
        // Search for groupCallerDnAttribute equal to callerDn
        // Return groupNameAttribute
        List<SearchResult> searchResults = search(
            ldapContext,
            ldapIdentityStoreDefinition.groupBaseDn(),
            ldapIdentityStoreDefinition.groupCallerDnAttribute(),
            callerDn,
            ldapIdentityStoreDefinition.groupNameAttribute()
        );
        
        // Collect the groups from the search results
        List<String> groups = new ArrayList<>();
        for (SearchResult searchResult : searchResults) {
            for (Object group : get(searchResult, ldapIdentityStoreDefinition.groupNameAttribute())) {
                groups.add(group.toString());
            }
        }
       
        return new CredentialValidationResult(
            VALID, 
            new CallerPrincipal(usernamePasswordCredential.getCaller()), 
            groups
        );
    }
    
    private static String createCallerDn(String callerNameAttribute, String callerName, String callerBaseDn) {
        return String.format("%s=%s,%s", callerNameAttribute, callerName, callerBaseDn);
    }
     
    private static LdapContext createLdapContext(String url, String bindDn, String bindCredential) {
        try {
            return new InitialLdapContext(getConnectionEnvironment(url, bindDn, bindCredential), null);
        } catch (AuthenticationException e) {
            return null;
        } catch (NamingException e) {
           throw new IllegalStateException(e);
        }
    }
    
    private static Hashtable<String, String> getConnectionEnvironment(String url, String bindDn, String bindCredential) {

        Hashtable<String, String> environment = new Hashtable<>();

        environment.put(INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(PROVIDER_URL, url);
        
        environment.put(SECURITY_AUTHENTICATION, "simple");
        environment.put(SECURITY_PRINCIPAL, bindDn);
        environment.put(SECURITY_CREDENTIALS, bindCredential);

        return environment;
    }
    
    private static List<SearchResult> search(LdapContext ldapContext, String searchBase, String filterAttribute, String filterValue, String returnAttribute) {
        SearchControls controls = new SearchControls();
        controls.setReturningAttributes(new String[]{returnAttribute}); // e.g. cn
        
        try {
            return list(ldapContext.search(
                searchBase,                             // e.g. ou=group,dc=jsr375,dc=net 
                format("(%s={0})", filterAttribute),    // e.g. (member={0})
                new Object[] {filterValue},             // e.g. uid=reza,ou=caller,dc=jsr375,dc=net
                controls
            ));
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
    }
    
    private static List<?> get(SearchResult searchResult, String attributeName) {
        try {
            return list(searchResult.getAttributes().get(attributeName).getAll());
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
    }

}
