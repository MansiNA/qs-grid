package de.dbuss.example.data.service;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import de.dbuss.example.data.entity.ProjectConnection;
import de.dbuss.example.data.repository.ProjectConnectionRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Service
public class ProjectConnectionService {
    private final ProjectConnectionRepository repository;
    private JdbcTemplate jdbcTemplate;

    @Getter
    private String errorMessage = "";

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    HashMap<String, String> defaultConnectionParams;

    public ProjectConnectionService(ProjectConnectionRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    private void init() {
        defaultConnectionParams = new HashMap<>();
        defaultConnectionParams.put("dbUrl", dbUrl);
        defaultConnectionParams.put("dbUser", dbUser);
        defaultConnectionParams.put("dbPassword", dbPassword);
    }
    public Optional<ProjectConnection> findByName(String name) {
        return repository.findByName(name);
    }

    public List<ProjectConnection> findAll() {
        return repository.findAll();
    }

    @Primary
    public DataSource getDataSource(String selectedDatabase) {

        // Load database connection details from the ProjectConnection entity
        Optional<ProjectConnection> projectConnection = repository.findByName(selectedDatabase);

        if (projectConnection.isPresent()) {
            System.out.println("jdbc:sqlserver://"+projectConnection.get().getHostname() + ";databaseName="+projectConnection.get().getDbName()+";encrypt=true;trustServerCertificate=true");
            System.out.println("Username = "+projectConnection.get().getUsername()+ " Password = "+projectConnection.get().getPassword());
            DataSource dataSource = DataSourceBuilder
                    .create()
                    .url("jdbc:sqlserver://"+projectConnection.get().getHostname() + ";databaseName="+projectConnection.get().getDbName()+";encrypt=true;trustServerCertificate=true")
                    .username(projectConnection.get().getUsername())
                    .password(projectConnection.get().getPassword())
                    .build();
            return dataSource;
        }

        throw new RuntimeException("Database connection not found: " + selectedDatabase);
    }

    @Primary
    public DataSource getDataSourceUsingParameter(String dbUrl, String dbUser, String dbPassword) {

        if(dbUser != null) {
            System.out.println(dbUrl);
            System.out.println("Username = " + dbUser + " Password = " + dbPassword);
            DataSource dataSource = DataSourceBuilder
                    .create()
                    .url(dbUrl)
                    .username(dbUser)
                    .password(dbPassword)
                    .build();
            return dataSource;
        }

        throw new RuntimeException("Database connection not found: " + dbUser);
    }

    public JdbcTemplate getJdbcConnection(String selectedDatabase) {
        DataSource dataSource = getDataSource(selectedDatabase);
        jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate;
    }
    public JdbcTemplate getJdbcConnection(String dbUrl, String dbUser, String dbPassword) {
        DataSource dataSource = getDataSourceUsingParameter(dbUrl, dbUser, dbPassword);
        jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate;
    }

    public JdbcTemplate getJdbcDefaultConnection () {
        String dbUrl = defaultConnectionParams.get("dbUrl");
        String dbUser = defaultConnectionParams.get("dbUser");
        String dbPassword = defaultConnectionParams.get("dbPassword");
        DataSource dataSource = getDataSourceUsingParameter(dbUrl, dbUser, dbPassword);
        jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate;
    }

}
