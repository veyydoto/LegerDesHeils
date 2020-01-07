/**
 * Created by:
 * Adnan Akbas, 17005116
 * Bart Willems, 17098335
 * Joel Duurkoop, 17076021
 * Jari van Menxel, 17030072
 * Vedat Yilmaz, 17118700
 */
package legerdesheils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import legerdesheils.Database.DatabaseConnect;

/**
 * Communicates with the Signal database to read and write and update
 */
public class SignalStoredProcessor {

    private final DatabaseConnect signalDatabase;

    public SignalStoredProcessor(DatabaseConnect signalDatabase) {
        this.signalDatabase = signalDatabase;
    }

    /**
     * Writes new signals to the signal database
     *
     * @param newSignals signals that have to be written to the signal database
     * @throws SQLException
     */
    public void writeFoundSignals(ArrayList<Signal> newSignals) throws SQLException {
        int sentAmount = 0;
        for (Signal signal : newSignals) {

            PreparedStatement preparedStatement = signalDatabase.getPreparedStatement("INSERT INTO dbo.signal (account,businessrule,entity,date_found,connection_data) VALUES(?, ?, ?, ?, ?)");

            preparedStatement.setString(1, signal.getAccount());
            preparedStatement.setString(2, signal.getBusinessRule());
            preparedStatement.setString(3, signal.getEntity());
            preparedStatement.setTimestamp(4, new java.sql.Timestamp(signal.getDateFound().getTime()));
            preparedStatement.setString(5, signal.getConnectionData());

            try {
                preparedStatement.execute();
            } catch (SQLException e) {
                throw new SQLException(String.format("Fout bij het schrijven: %d / %d verzonden. %s", sentAmount, newSignals.size(), e.getMessage()));
            }
            sentAmount += 1;
        }
    }

    /**
     * Reads signals stored in the signal database
     *
     * @return signals found in database
     * @throws SQLException
     */
    public ArrayList<Signal> readStoredSignals(User user) throws SQLException {
        ArrayList<Signal> oldSignals = new ArrayList<>();
        PreparedStatement preparedStatement = signalDatabase.getPreparedStatement("SELECT * FROM dbo.signal WHERE date_solved IS NULL");
        if (!user.isAdmin()) {
            StringBuilder     stringBuilderWorkUnits = new StringBuilder();
            ArrayList<String> workUnits              = user.getWorkunit();
            for (int i = 0; i < workUnits.size(); i++) {
                stringBuilderWorkUnits.append("'").append(workUnits.get(i)).append("'");
                if (i + 1 != workUnits.size()) {
                    stringBuilderWorkUnits.append(",");
                }
            }
            
            preparedStatement = signalDatabase.getPreparedStatement("SELECT * FROM dbo.signal WHERE date_solved IS NULL AND entity IN (?)");
            preparedStatement.setString(1, stringBuilderWorkUnits.toString());
        }
        // get all unsolved signals from signal database
        
        ResultSet resultSet = preparedStatement.executeQuery();
        
        while (resultSet.next()) {
            Signal signal = new Signal(
                    resultSet.getString("account"),
                    resultSet.getString("businessrule"),
                    resultSet.getString("entity"),
                    resultSet.getTimestamp("date_found"),
                    resultSet.getTimestamp("date_solved"),
                    resultSet.getString("connection_data"));
            oldSignals.add(signal);

        }

        return oldSignals;
    }

    /**
     * updates stored signals that are solved
     *
     * @param oldSignals
     * @throws SQLException
     */
    public void updateStoredSignals(ArrayList<Signal> oldSignals) throws SQLException {
        int updatedAmount = 0;

        for (Signal signal : oldSignals) {
            PreparedStatement preparedStatement = signalDatabase.getPreparedStatement("UPDATE dbo.signal SET date_solved= ? WHERE account= ? AND businessrule= ? AND entity= ? AND date_solved is NULL");
            preparedStatement.setTimestamp(1, new java.sql.Timestamp(signal.getDateSolved().getTime()));
            preparedStatement.setString(2, signal.getAccount());
            preparedStatement.setString(3, signal.getBusinessRule());
            preparedStatement.setString(4, signal.getEntity());

            try {
                preparedStatement.execute();
            } catch (SQLException e) {
                throw new SQLException(String.format("Fout bij het updaten: %d / %d geupdated. %s", updatedAmount, oldSignals.size(), e.getMessage()));
            }
        }
    }

    /**
     * Connects to aggregated and signal database servers
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public void connect() throws ClassNotFoundException, SQLException {
        signalDatabase.connect();
    }

    /**
     * Disconnects from both servers
     *
     * @throws SQLException
     */
    public void disconnect() throws SQLException {
        signalDatabase.disconnect();
    }

}
