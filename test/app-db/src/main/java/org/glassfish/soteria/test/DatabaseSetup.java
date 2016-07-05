package org.glassfish.soteria.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.sql.DataSourceDefinition;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.sql.DataSource;

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

    @PostConstruct
    public void init() {
        
        executeUpdate(dataSource, "DROP TABLE IF EXISTS caller");
        executeUpdate(dataSource, "DROP TABLE IF EXISTS caller_groups");
        
        executeUpdate(dataSource, "CREATE TABLE IF NOT EXISTS caller(name VARCHAR(64) PRIMARY KEY, password VARCHAR(64))");
        executeUpdate(dataSource, "CREATE TABLE IF NOT EXISTS caller_groups(caller_name VARCHAR(64), group_name VARCHAR(64))");
        
        executeUpdate(dataSource, "INSERT INTO caller VALUES('reza', 'secret1')");
        executeUpdate(dataSource, "INSERT INTO caller VALUES('alex', 'secret2')");
        executeUpdate(dataSource, "INSERT INTO caller VALUES('arjan', 'secret2')");
        executeUpdate(dataSource, "INSERT INTO caller VALUES('werner', 'secret2')");
        
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
