/**
 * Created by:
 * Adnan Akbas, 17005116
 * Bart Willems, 17098335
 * Joel Duurkoop, 17076021
 * Jari van Menxel, 17030072
 * Vedat Yilmaz, 17118700
 */
package legerdesheils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import legerdesheils.Database.DatabaseConnect;

/**
 * Authenticates the user
 */
public class Authenticator {

    private DatabaseConnect aggregatedDatabase;

    private final String[] adminWorkUnits = {"002 Stichtingsbureau Almere", "018 Dienstverlening"};


    public Authenticator(DatabaseConnect aggregatedDatabase) {
        this.aggregatedDatabase = aggregatedDatabase;
    }

    public User getLoggedInUser() throws SQLException {
        this.aggregatedDatabase.connect();
        User   user             = null;
        String metadataUsername = aggregatedDatabase.getMetaData().getUserName();
        String microsoftAccount = metadataUsername.substring(metadataUsername.indexOf("\\") + 1);
        System.out.println("microsoftAccount::"+microsoftAccount);
        // comment this if you want disable login
        // microsoftAccount = "u3e100hhs@hhs.rb.corp";

        ResultSet resultSet = this.aggregatedDatabase.executeQuery("SELECT DISTINCT AD.Username_Pre2000 as 'username', AD.ParentContainer as 'workunit'FROM [AD-Export] AD WHERE AD.Username= '" + microsoftAccount + "'");
        if (resultSet.next()) {
            String  username = resultSet.getString("username");

            boolean isAdmin  = false;
            for (String adminWorkUnit : this.adminWorkUnits) {
                if (resultSet.getString("workunit").toLowerCase().contains(adminWorkUnit.toLowerCase())) {
                    isAdmin = true;
                    break;
                }
            }

            ArrayList<String> workunits = new ArrayList<>();

            workunits.add(resultSet.getString("workunit"));

            /**
             * CLEVER workunit
             */
            String    cleverWorkunit  = "SELECT distinct dbo.getOrganisatieNaamNiveau5(W.OrganisatieEenheidID) as 'workunit' FROM PersoonCodes PC   JOIN Medewerker M ON M.PersoonID = PC.PersoonID  JOIN Werkzaam W on M.ID = W.MedewerkerID WHERE PC.Code = '%s';";
            ResultSet cleverResultSet = this.aggregatedDatabase.executeQuery(String.format(cleverWorkunit, username));

            while (cleverResultSet.next()) {
                workunits.add(cleverResultSet.getString("workunit"));
            }

            String    afasWorkunit  = "SELECT DISTINCT EmployerName AS 'workunit' FROM [AfasProfit-Export] where EmployeeUsername = '%s';";
            ResultSet afasResultSet = this.aggregatedDatabase.executeQuery(String.format(afasWorkunit, username));

            while (afasResultSet.next()) {
                workunits.add(afasResultSet.getString("workunit"));
            }

            user = new User(username, workunits, isAdmin);
        }

        this.aggregatedDatabase.disconnect();
        return user;
    }
}