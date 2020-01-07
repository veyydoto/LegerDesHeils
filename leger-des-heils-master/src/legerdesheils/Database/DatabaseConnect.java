/**
 * Created by:
 * Adnan Akbas, 17005116
 * Bart Willems, 17098335
 * Joel Duurkoop, 17076021
 * Jari van Menxel, 17030072
 * Vedat Yilmaz, 17118700
 */
package legerdesheils.Database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import legerdesheils.EnvironmentVariables;

/**
 * Is used to connect to the database server
 * Is used to execute queries
 */
public class DatabaseConnect {

    /**
     * Connection to our database
     *
     * @see Connection
     */
    private Connection           connection;
    /**
     * envorimentVariables is used to read database.properties
     *
     * @see EnvironmentVariables
     */
    private EnvironmentVariables enviromentVariables;

    /**
     * @param environmentVariables This object is used to get properties
     */
    public DatabaseConnect(EnvironmentVariables environmentVariables) {
        this.enviromentVariables = environmentVariables;
    }

    /**
     * connect method connects to database with data from database.properties .
     *
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     */
    public void connect() throws SQLException, NullPointerException {
        if (enviromentVariables == null) {
            throw new NullPointerException("Connection data unavailable");
        }

        //setting up connection to our database
        String url              = enviromentVariables.getData("url");
        String databaseName     = enviromentVariables.getData("database_name");
        String connectionString = String.format("jdbc:sqlserver://%s; databaseName=%s;integratedSecurity=true", url, databaseName);
        this.connection = DriverManager.getConnection(connectionString);
    }

    /**
     * This method checks if there is a connection with the database
     *
     * @return boolean value to indicate that connection has been established
     * @throws java.sql.SQLException
     */
    public boolean isConnected() throws SQLException {
        return (this.connection != null && !this.connection.isClosed());
    }

    /**
     * This method disconnects the database
     *
     * @return boolean value to indicate if connection has been disconnected
     * @throws java.sql.SQLException
     */
    public boolean disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
            return connection.isClosed();
        }
        return true;
    }

    public ResultSet executeQuery(String query) throws SQLException {
        if (isConnected()) {
            Statement statement = connection.createStatement();
            return statement.executeQuery(query);
        } else {
            throw new SQLException("No connection... [%s]", getConnectionData());
        }
    }

    public String getConnectionData() throws SQLException {
        DatabaseMetaData metaData       = this.connection.getMetaData();
        String           serverName     = enviromentVariables.getData("url");
        String           databaseName   = enviromentVariables.getData("database_name");
        String           userConnection = metaData.getUserName();

        // name server, name database, name user, date timestamp
        return String.format("%s, %s, %s", serverName, databaseName, userConnection);
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return this.connection.getMetaData();
    }

    public PreparedStatement getPreparedStatement(String query) throws SQLException {
        return connection.prepareStatement(query);
    }
}
