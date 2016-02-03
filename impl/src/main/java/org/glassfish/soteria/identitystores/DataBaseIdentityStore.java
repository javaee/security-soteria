package org.glassfish.soteria.identitystores;

import static javax.security.identitystore.CredentialValidationResult.INVALID_RESULT;
import static javax.security.identitystore.CredentialValidationResult.NOT_VALIDATED_RESULT;
import static javax.security.identitystore.CredentialValidationResult.Status.VALID;
import static org.glassfish.soteria.cdi.CdiUtils.jndiLookup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.security.CallerPrincipal;
import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.IdentityStore;
import javax.security.identitystore.annotation.DataBaseIdentityStoreDefinition;
import javax.security.identitystore.credential.Credential;
import javax.security.identitystore.credential.UsernamePasswordCredential;
import javax.sql.DataSource;

public class DataBaseIdentityStore implements IdentityStore {

    private DataBaseIdentityStoreDefinition dataBaseIdentityStoreDefinition;

    public DataBaseIdentityStore(DataBaseIdentityStoreDefinition dataBaseIdentityStoreDefinition) {
        this.dataBaseIdentityStoreDefinition = dataBaseIdentityStoreDefinition;
    }

    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (credential instanceof UsernamePasswordCredential) {
            return validate((UsernamePasswordCredential) credential);
        }

        return NOT_VALIDATED_RESULT;
    }

    public CredentialValidationResult validate(UsernamePasswordCredential usernamePasswordCredential) {

        DataSource dataSource = jndiLookup(dataBaseIdentityStoreDefinition.dataSourceLookup());
        
        List<String> passwords = executeQuery(
            dataSource, 
            dataBaseIdentityStoreDefinition.callerQuery(),
            usernamePasswordCredential.getCaller()
        ); 
        
        if (!passwords.isEmpty() && usernamePasswordCredential.getPassword().compareTo(passwords.get(0))) {
            return new CredentialValidationResult(
                VALID, 
                new CallerPrincipal(usernamePasswordCredential.getCaller()), 
                executeQuery(
                    dataSource, 
                    dataBaseIdentityStoreDefinition.groupsQuery(),
                    usernamePasswordCredential.getCaller()
                )
            );
        }

        return INVALID_RESULT;
    }
    
    private List<String> executeQuery(DataSource dataSource, String query, String parameter) {
        List<String> result = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, parameter);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        result.add(resultSet.getString(1));
                    }
                }
            }
        } catch (SQLException e) {
           throw new IllegalStateException(e);
        }
        
        return result;
    }

}
