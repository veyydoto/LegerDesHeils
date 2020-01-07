/**
 * Created by:
 * Adnan Akbas, 17005116
 * Bart Willems, 17098335
 * Joel Duurkoop, 17076021
 * Jari van Menxel, 17030072
 * Vedat Yilmaz, 17118700
 */
package legerdesheils.Database;

import legerdesheils.EnvironmentVariables;

import java.sql.*;

/**
 * This class sets up the signal database if it doesnt exist yet AND
 * creates neccisary SQL functions to run the program succesfully 
 */
public class SetupScript {

    public void setupSignalDatabase(EnvironmentVariables signalDatabaseVariables) throws SQLException, ClassNotFoundException {
        Connection con       = DriverManager.getConnection("jdbc:sqlserver://" + signalDatabaseVariables.getData("url") + ";integratedSecurity=true");
        Statement  statement = con.createStatement();
        statement.execute("IF NOT(EXISTS (SELECT * FROM sys.databases WHERE name = 'signalsdatabase')) "
                + "CREATE DATABASE signalsdatabase");
        statement.execute("USE signalsdatabase IF NOT(EXISTS(SELECT * FROM sys.tables WHERE name = 'signal'))" +
                "create table signal\n" +
                "(\n" +
                "entity          varchar(150)  	not null,\n" +
                "account         varchar(50)  	not null,\n" +
                "businessrule    varchar(10)  	not null,\n" +
                "date_found      datetime   not null,\n" +
                "date_solved     datetime,\n" +
                "connection_data varchar(200) not null,\n" +
                "primary key (entity, account, businessrule, date_found)\n" +
                ");");
        con.close();
    }

    public void modifyAggregatedDatabase(EnvironmentVariables aggregatedDatabaseVariables) throws SQLException {
        Connection con       = DriverManager.getConnection("jdbc:sqlserver://" + aggregatedDatabaseVariables.getData("url") + ";integratedSecurity=true");
        Statement  statement = con.createStatement();
        ResultSet results = statement.executeQuery("USE AuditBlackBox IF(EXISTS(SELECT * FROM sys.objects WHERE name = 'getOrganisatieNaamNiveau5'))\n" +
                "SELECT 'true' " +
                "ELSE " +
                "SELECT 'false'");
        results.next();
        String exists = results.getString(1);
        if (!"true".equals(exists)) {
            statement.execute("CREATE FUNCTION dbo.getOrganisatieNaamNiveau5(@ID INTEGER)\n" +
                    " RETURNS VARCHAR(4000)\n" +
                    "AS BEGIN\n" +
                    "DECLARE @organisatieNaam VARCHAR(400);\n" +
                    " WITH cte(organisatieID, OuderUnitID, Niveau, Naam)\n" +
                    " AS (\n" +
                    "   SELECT\n" +
                    "     OrganisatieID,\n" +
                    "     OuderUnitID,\n" +
                    "     OrganisatieEenheidNiveau.Niveau,\n" +
                    "     OrganisatieEenheid.Naam\n" +
                    "   FROM OrganisatieEenheid\n" +
                    "     JOIN OrganisatieEenheidNiveau ON OrganisatieEenheid.OrganisatieEenheidNiveauID = OrganisatieEenheidNiveau.id\n" +
                    "   WHERE OrganisatieEenheid.OrganisatieID = @ID\n" +
                    "   UNION ALL\n" +
                    "   SELECT\n" +
                    "     OE.OrganisatieID,\n" +
                    "     OE.OuderUnitID,\n" +
                    "     OEN.Niveau,\n" +
                    "     OE.Naam\n" +
                    "   FROM OrganisatieEenheid OE\n" +
                    "     JOIN OrganisatieEenheidNiveau OEN ON\n" +
                    "OE.OrganisatieEenheidNiveauID = OEN.id\n" +
                    "     JOIN cte c ON c.OuderUnitID = OE.OrganisatieID\n" +
                    " )\n" +
                    " SELECT @organisatieNaam = cte.Naam\n" +
                    " FROM cte WHERE Niveau = 5;\n" +
                    " return @organisatieNaam;\n" +
                    "END;");
        }

        con.close();
    }
}
