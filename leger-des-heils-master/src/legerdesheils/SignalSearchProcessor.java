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
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import legerdesheils.BusinessRules.BusinessRule;
import legerdesheils.Database.DatabaseConnect;

/**
 * Reads the aggregated database for signals and impact
 */
public class SignalSearchProcessor {

    private final DatabaseConnect aggregatedDatabase;

    public SignalSearchProcessor(DatabaseConnect aggregatedDatabase) {
        this.aggregatedDatabase = aggregatedDatabase;
    }

    /**
     * Searches for signals by using the business rules. One query for each
     * businessrule. Searches in aggregated database
     *
     * @param businessRules the rules used to find the signals
     * @return returns all found signals
     * @throws SQLException
     */
    public ArrayList<Signal> findSignals(ArrayList<BusinessRule> businessRules, User user) throws SQLException {
        ArrayList<Signal> newSignals     = new ArrayList<>();
        String            connectionData = aggregatedDatabase.getConnectionData();
        // Get results for every businessrule
        ExecutorService es = Executors.newCachedThreadPool();
        for (BusinessRule businessRule : businessRules) {
            es.execute(() -> {
                try {
                    int       foundAmount = 0;
                    ResultSet resultSet   = aggregatedDatabase.executeQuery(businessRule.getQuery(user));
                    // Parse the results to signals
                    while (resultSet.next()) {
                        Date   date   = new Date();
                        String entity = resultSet.getString("entity");
                        newSignals.add(new Signal(
                                resultSet.getString("account"),
                                businessRule.getIdCode(),
                                entity == null ? "" : entity,
                                date,
                                String.format("%s, %s", connectionData, date)));
                        // keep track of the amount of signals a businessrule found
                        foundAmount++;
                    }
                    businessRule.setFoundSignals(foundAmount);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        }
        es.shutdown();
        try {
            es.awaitTermination(5, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newSignals;
    }

    /**
     * Connects to aggregated database
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public void connect() throws ClassNotFoundException, SQLException {
        aggregatedDatabase.connect();
    }

    /**
     * Disconnects the database server
     *
     * @throws SQLException
     */
    public void disconnect() throws SQLException {
        aggregatedDatabase.disconnect();
    }

    public void getImpactActiviteit(Signal signal) throws SQLException {
        ArrayList<Impact> impacts = new ArrayList<>();

        Date beginDate = signal.getDateFound();
        Date endDate;

        // als signaal niet opgelost is, datum van vandaag, anders datum opgelost
        if (signal.getDateSolved() != null) {
            endDate = signal.getDateSolved();
        } else {
            endDate = new Date();
        }

        // query to get activity
        PreparedStatement preparedStatement = aggregatedDatabase.getPreparedStatement("SELECT\n"
                + "A.Status,\n"
                + "A.ActiviteitSoortID,\n"
                + "count(*) as 'aantal'\n"
                + "FROM PersoonCodes PC\n"
                + "JOIN TeamLid TL ON PC.PersoonID = TL.PersoonID\n"
                + "JOIN Activiteit A ON TL.TeamID = A.TeamID\n"
                + "WHERE PC.Code = ? \n"
                + "AND pc.CodesoortenID = 981\n"
                + "AND (TL.Begindatum >= ? \n"
                + "OR TL.EindDatum <= ?)\n"
                + "GROUP BY a.Status, a.ActiviteitSoortID;");

        preparedStatement.setString(1, signal.getAccount());
        preparedStatement.setTimestamp(2, new java.sql.Timestamp(beginDate.getTime()));
        preparedStatement.setTimestamp(3, new java.sql.Timestamp(endDate.getTime()));

        // Results of query
        ResultSet resultsActiviteit = preparedStatement.executeQuery();

        // get de needed information
        while (resultsActiviteit.next()) {
            Impact impact = new Impact(String.format("Status: %s, ActiviteitSoortID: %s",
                    resultsActiviteit.getString("Status"),
                    resultsActiviteit.getString("ActiviteitSoortID")),
                    resultsActiviteit.getInt("aantal"));
            impacts.add(impact);
        }
        signal.addImpact(impacts);
    }

    /**
     * Determine organisation imapact
     *
     * @param signal the singal impact has to be found for
     * @throws SQLException
     */
    public void getImpactOrganisatie(Signal signal) throws SQLException {
        ArrayList<Impact> impacts = new ArrayList<>();
        PreparedStatement preparedStatement = aggregatedDatabase.getPreparedStatement("select o.Naam, count(*)as 'aantal'\n"
                + "from PersoonCodes pc\n"
                + "join medewerker m on pc.PersoonID = m.PersoonID\n"
                + "join Werkzaam w on m.ID = w.MedewerkerID\n"
                + "join OrganisatieEenheid o on w.OrganisatieEenheidID = o.OrganisatieID\n"
                + "join PersoonTotRol p on p.ID = pc.PersoonID\n"
                + "where CodesoortenID = 981\n"
                + "and o.OrganisatieEenheidNiveauID >= 3\n"
                + "and pc.Code = ? \n"
                + "group by o.Naam");
        preparedStatement.setString(1, signal.getAccount());

        // Results of query
        ResultSet resultsOrganisatie = preparedStatement.executeQuery();

        while (resultsOrganisatie.next()) {
            Impact impact = new Impact(resultsOrganisatie.getString("Naam"), resultsOrganisatie.getInt("aantal"));
            impacts.add(impact);
        }
        signal.addImpact(impacts);
    }

    /**
     * Determine entity impact
     *
     * @param signal the signal that has de impact
     * @throws SQLException
     */
    public void getImpactEntiteit(Signal signal) throws SQLException {
        ArrayList<Impact> impacts = new ArrayList<>();
        PreparedStatement preparedStatement = aggregatedDatabase.getPreparedStatement("select \n"
                + "case when e.naam is null \n"
                + "then 'Entiteit Onbekend'\n"
                + "else e.naam end as 'naam', \n"
                + "count(*) as 'aantal'\n"
                + "from PersoonCodes pc\n"
                + "join PersoonTotRol p on pc.PersoonID = p.PersoonID\n"
                + "join rol r on p.RolID = r.RolID\n"
                + "join Recht re on re.RolID= r.RolID\n"
                + "join Entiteit e on e.ID = re.EntiteitID\n"
                + "where pc.CodesoortenID = 981\n"
                + "and pc.Code = ?\n"
                + "group by e.Naam");

        preparedStatement.setString(1, signal.getAccount());
        // Results of query
        ResultSet resultsEntiteit = preparedStatement.executeQuery();

        while (resultsEntiteit.next()) {
            Impact impact = new Impact(resultsEntiteit.getString("naam"), resultsEntiteit.getInt("aantal"));
            impacts.add(impact);
        }
        signal.addImpact(impacts);
    }

}
