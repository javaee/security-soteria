/*
 * Copyright (c) 2015-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.glassfish.soteria.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.sql.DataSourceDefinition;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.security.enterprise.identitystore.Pbkdf2PasswordHash;

@DataSourceDefinition(
    // global to circumvent https://java.net/jira/browse/GLASSFISH-21447
    name = "java:global/MyDS",
    className = "org.h2.jdbcx.JdbcDataSource",
    // :mem:test would be better, but TomEE insists on this being a file
    url="jdbc:h2:~/test;DB_CLOSE_ON_EXIT=FALSE"
)
@Singleton
@Startup
public class DatabaseSetup {
    
    @Resource(lookup="java:global/MyDS")
    private DataSource dataSource;

    @Inject
    private Pbkdf2PasswordHash passwordHash;
    
    @PostConstruct
    public void init() {
        
        Map<String, String> parameters= new HashMap<>();
        parameters.put("Pbkdf2PasswordHash.Iterations", "3072");
        parameters.put("Pbkdf2PasswordHash.Algorithm", "PBKDF2WithHmacSHA512");
        parameters.put("Pbkdf2PasswordHash.SaltSizeBytes", "64");
        passwordHash.initialize(parameters);
        
        executeUpdate(dataSource, "DROP TABLE IF EXISTS caller");
        executeUpdate(dataSource, "DROP TABLE IF EXISTS caller_groups");
        
        executeUpdate(dataSource, "CREATE TABLE IF NOT EXISTS caller(name VARCHAR(64) PRIMARY KEY, password VARCHAR(255))");
        executeUpdate(dataSource, "CREATE TABLE IF NOT EXISTS caller_groups(caller_name VARCHAR(64), group_name VARCHAR(64))");
        
        executeUpdate(dataSource, "INSERT INTO caller VALUES('reza', '" + passwordHash.generate("secret1".toCharArray()) + "')");
        executeUpdate(dataSource, "INSERT INTO caller VALUES('alex', '" + passwordHash.generate("secret2".toCharArray()) + "')");
        executeUpdate(dataSource, "INSERT INTO caller VALUES('arjan', '" + passwordHash.generate("secret2".toCharArray()) + "')");
        executeUpdate(dataSource, "INSERT INTO caller VALUES('werner', '" + passwordHash.generate("secret2".toCharArray()) + "')");
        
        executeUpdate(dataSource, "INSERT INTO caller_groups VALUES('reza', 'foo')");
        executeUpdate(dataSource, "INSERT INTO caller_groups VALUES('reza', 'bar')");
        
        executeUpdate(dataSource, "INSERT INTO caller_groups VALUES('alex', 'foo')");
        executeUpdate(dataSource, "INSERT INTO caller_groups VALUES('alex', 'bar')");
        
        executeUpdate(dataSource, "INSERT INTO caller_groups VALUES('arjan', 'foo')");
        executeUpdate(dataSource, "INSERT INTO caller_groups VALUES('werner', 'foo')");
    }
    
    @PreDestroy
    public void destroy() {
    	try {
    		executeUpdate(dataSource, "DROP TABLE IF EXISTS caller");
    		executeUpdate(dataSource, "DROP TABLE IF EXISTS caller_groups");
    	} catch (Exception e) {
    		// silently ignore, concerns in-memory database
    	}
    }
    
    private void executeUpdate(DataSource dataSource, String query) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
           throw new IllegalStateException(e);
        }
    }
    
}
