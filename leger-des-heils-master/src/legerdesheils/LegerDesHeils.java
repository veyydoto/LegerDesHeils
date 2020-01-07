/**
 * Created by:
 * Adnan Akbas, 17005116
 * Bart Willems, 17098335
 * Joel Duurkoop, 17076021
 * Jari van Menxel, 17030072
 * Vedat Yilmaz, 17118700
 */
package legerdesheils;

import legerdesheils.Database.DatabaseConnect;


import java.util.ArrayList;
import javax.swing.JOptionPane;


import legerdesheils.BusinessRules.BusinessRule;
import legerdesheils.Database.SetupScript;
import legerdesheils.UI.UI;

public class LegerDesHeils {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            //importing driver for connection to mysql database
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // read connection data
            EnvironmentVariables aggregatedDatabaseVariables = new EnvironmentVariables("database.properties");
            EnvironmentVariables signalDatabaseVariables     = new EnvironmentVariables("signalDatabase.properties");

            //initializing application
            SetupScript setupScript = new SetupScript();
            setupScript.modifyAggregatedDatabase(aggregatedDatabaseVariables);
            setupScript.setupSignalDatabase(signalDatabaseVariables);

            DatabaseConnect aggregatedDatabase = new DatabaseConnect(aggregatedDatabaseVariables);
            DatabaseConnect signalDatabase     = new DatabaseConnect(signalDatabaseVariables);

            Authenticator authenticator = new Authenticator(aggregatedDatabase);

            ArrayList<BusinessRule> businessRules = new ArrayList<>();
            businessRules.add(new BusinessRule("BE01",
                    "SELECT DISTINCT AD.Username_Pre2000 as 'account', AD.ParentContainer as entity FROM [AD-Export] AD JOIN PersoonCodes PC ON PC.Code = AD.Username_Pre2000 JOIN Medewerker M ON M.PersoonID = PC.PersoonID JOIN Werkzaam W on M.ID = W.MedewerkerID WHERE AD.Username_Pre2000 NOT IN (SELECT Username FROM [AfasProfit-Export]) and pc.CodesoortenID = 981;",
                    "AD Account, onbekend in Profit",
                    "SELECT DISTINCT AD.Username_Pre2000  as 'account', AD.ParentContainer  as entity FROM [AD-Export] AD JOIN PersoonCodes PC ON PC.Code = AD.Username_Pre2000 JOIN Medewerker M ON M.PersoonID = PC.PersoonID JOIN Werkzaam W on M.ID = W.MedewerkerID WHERE AD.Username_Pre2000 NOT IN (SELECT Username  FROM [AfasProfit-Export]) and pc.CodesoortenID = 981 AND AD.ParentContainer in (%s);"));
            businessRules.add(new BusinessRule("BE02",
                    "SELECT DISTINCT afasprofit.EmployeeUsername as 'account', afasprofit.EmployerName as 'entity' FROM[AfasProfit-Export] afasprofit JOIN PersoonCodes PC ON PC.Code = afasprofit.EmployeeUsername JOIN Medewerker M ON M.PersoonID = PC.PersoonID JOIN Werkzaam W on M.ID = W.MedewerkerID WHERE afasprofit.EmployeeUsername NOT IN(SELECT Username_Pre2000 FROM[AD-Export]) and pc.CodesoortenID = 981;",
                    "RDS Usernaam in Profit bestaat niet in de AD",
                    "SELECT DISTINCT afasprofit.EmployeeUsername as 'account',  afasprofit.EmployerName as 'entity' FROM [AfasProfit-Export] afasprofit  JOIN PersoonCodes PC ON PC.Code = afasprofit.EmployeeUsername  JOIN Medewerker M ON M.PersoonID = PC.PersoonID  JOIN Werkzaam W on M.ID = W.MedewerkerID WHERE afasprofit.EmployeeUsername NOT IN (SELECT Username_Pre2000  FROM [AD-Export]) and pc.CodesoortenID = 981 and afasprofit.EmployerName in (%s);"));
            businessRules.add(new BusinessRule("BE03",
                    "SELECT DISTINCT afasprofit.EmployeeUsername as 'account', afasprofit.EmployerName as 'entity' FROM [AfasProfit-Export] afasprofit JOIN PersoonCodes PC ON PC.Code = afasprofit.EmployeeUsername JOIN Medewerker M ON M.PersoonID = PC.PersoonID JOIN Werkzaam W on M.ID = W.MedewerkerID JOIN [AD-Export] ad ON ad.Username_Pre2000 = afasprofit.EmployeeUsername and pc.CodesoortenID = 981 AND afasprofit.ContractEndDate < GETDATE() AND ad.Disabled = 0",
                    "Medewerker uit dienst in Profit, account is in AD actief",
                    "SELECT DISTINCT  afasprofit.EmployeeUsername as 'account', afasprofit.EmployerName as 'entity' FROM [AfasProfit-Export] afasprofit JOIN PersoonCodes PC ON PC.Code = afasprofit.EmployeeUsername JOIN Medewerker M ON M.PersoonID = PC.PersoonID JOIN Werkzaam W on M.ID = W.MedewerkerID JOIN [AD-Export] ad ON ad.Username_Pre2000 = afasprofit.EmployeeUsername and pc.CodesoortenID = 981 AND   afasprofit.ContractEndDate < GETDATE() AND ad.Disabled = 0  and  afasprofit.EmployerName in (%s);"));
            businessRules.add(new BusinessRule("BE04",
                    "SELECT DISTINCT PC.code as 'account', dbo.getOrganisatieNaamNiveau5(W.OrganisatieEenheidID) as 'entity' FROM PersoonCodes PC JOIN Medewerker M ON M.PersoonID = PC.PersoonID JOIN Werkzaam W on M.ID = W.MedewerkerID WHERE pc.CodesoortenID = 981 AND PC.Code NOT IN (SELECT Username_Pre2000 FROM [AD-Export]);",
                    "RDS naam  in CleverNew bestaat niet in AD",
                    "SELECT DISTINCT PC.code as 'account',  dbo.getOrganisatieNaamNiveau5(W.OrganisatieEenheidID) as entity FROM PersoonCodes PC  JOIN Medewerker M ON M.PersoonID = PC.PersoonID  JOIN Werkzaam W on M.ID = W.MedewerkerID WHERE pc.CodesoortenID = 981 AND PC.Code NOT IN (SELECT Username_Pre2000   FROM [AD-Export]) and   dbo.getOrganisatieNaamNiveau5(W.OrganisatieEenheidID) in (%s);"));
            businessRules.add(new BusinessRule("BE05",
                    "SELECT DISTINCT ap.EmployeeUsername as 'account', ap.EmployerName as 'entity' FROM PersoonCodes PC JOIN Medewerker M ON M.PersoonID = PC.PersoonID JOIN Werkzaam W on M.ID = W.MedewerkerID join [AfasProfit-Export] ap on ap.EmployerCode = m.MedewerkerNummer WHERE pc.CodesoortenID <> 981",
                    "RDS naam in Clevernew is niet ingevuld",
                    "SELECT DISTINCT ap.EmployeeUsername as 'account', ap.EmployerName as 'entity' FROM PersoonCodes PC JOIN Medewerker M ON M.PersoonID = PC.PersoonID JOIN Werkzaam W on M.ID = W.MedewerkerID join [AfasProfit-Export] ap on ap.EmployerCode = m.MedewerkerNummer WHERE pc.CodesoortenID <> 981 and   ap.EmployerName in (%s)"));
            businessRules.add(new BusinessRule("BE06",
                    "SELECT DISTINCT PC.code as 'account', afasprofit.EmployerName as 'entity' FROM PersoonCodes PC JOIN Medewerker M ON M.PersoonID = PC.PersoonID JOIN Werkzaam W on M.ID = W.MedewerkerID JOIN [AfasProfit-Export] afasprofit ON pc.Code = afasprofit.EmployeeUsername join [AD-Export] ad on pc.Code = ad.Username_Pre2000 WHERE pc.CodesoortenID = 981 AND w.EindDatum < GETDATE() and ad.Disabled=0",
                    "Medewerker uit dienst in CleverNew, account in AD actief",
                    "SELECT DISTINCT PC.code as 'account', afasprofit.EmployerName as 'entity' FROM PersoonCodes PC JOIN Medewerker M ON M.PersoonID = PC.PersoonID JOIN Werkzaam W on M.ID = W.MedewerkerID JOIN [AfasProfit-Export] afasprofit ON pc.Code = afasprofit.EmployeeUsername join [AD-Export] ad on pc.Code = ad.Username_Pre2000 WHERE pc.CodesoortenID = 981 AND w.EindDatum < GETDATE() and ad.Disabled=0 and  afasprofit.EmployerName in (%s)"));
            businessRules.add(new BusinessRule("BE07",
                    "SELECT DISTINCT Username_Pre2000 as 'account', ParentContainer  as 'entity' FROM [AD-Export] WHERE Username_Pre2000 not in (select code from PersoonCodes where CodesoortenID = 981)",
                    "AD Account, onbekend in Clever",
                    "SELECT DISTINCT Username_Pre2000 as 'account', ParentContainer  as 'entity' FROM [AD-Export] WHERE Username_Pre2000 not in (select code from PersoonCodes where CodesoortenID = 981) and  ParentContainer in (%s)"));
            businessRules.add(new BusinessRule("BE08",
                    "SELECT DISTINCT EmployeeUsername as 'account', EmployerName  as 'entity' from [AfasProfit-Export] where EmployeeUsername not in (select code from PersoonCodes WHERE CodesoortenID = 981)",
                    "RDS User naam in Profit bestaat niet in Clever",
                    "SELECT DISTINCT EmployeeUsername as 'account', EmployerName  as 'entity' from [AfasProfit-Export] where EmployeeUsername not in (select code from PersoonCodes WHERE CodesoortenID = 981) and  EmployerName in (%s)"));
            businessRules.add(new BusinessRule("BE09",
                    "SELECT DISTINCT PC.code  as 'account', afasprofit.EmployerName  as 'entity' FROM PersoonCodes PC JOIN Medewerker M ON M.PersoonID = PC.PersoonID JOIN Werkzaam W on M.ID = W.MedewerkerID JOIN [AfasProfit-Export] afasprofit ON pc.Code = afasprofit.EmployeeUsername WHERE pc.CodesoortenID = 981 AND PC.Einddatum IS NOT null AND afasprofit.Contractenddate < PC.Einddatum",
                    "Medewerker uit dienst in Profit, account is in Clever actief",
                    "SELECT DISTINCT PC.code  as 'account', afasprofit.EmployerName as 'entity' FROM PersoonCodes PC JOIN Medewerker M ON M.PersoonID = PC.PersoonID JOIN Werkzaam W on M.ID = W.MedewerkerID JOIN [AfasProfit-Export] afasprofit ON pc.Code = afasprofit.EmployeeUsername WHERE pc.CodesoortenID = 981 AND PC.Einddatum IS NOT null AND afasprofit.Contractenddate < PC.Einddatum  and  afasprofit.EmployerName  in (%s)"));
            businessRules.add(new BusinessRule("BE10",
                    "SELECT DISTINCT PC.code as 'account', dbo.getOrganisatieNaamNiveau5(W.OrganisatieEenheidID) as 'entity' FROM PersoonCodes PC JOIN Medewerker M ON M.PersoonID = PC.PersoonID JOIN Werkzaam W on M.ID = W.MedewerkerID WHERE pc.CodesoortenID = 981 and pc.Code not in (select employeeusername from [AfasProfit-Export])",
                    "RDS User naam in Clever bestaat niet in Afas Profit",
                    "SELECT DISTINCT PC.code as 'account', dbo.getOrganisatieNaamNiveau5(W.OrganisatieEenheidID) as 'entity' FROM PersoonCodes PC JOIN Medewerker M ON M.PersoonID = PC.PersoonID JOIN Werkzaam W on M.ID = W.MedewerkerID WHERE pc.CodesoortenID = 981 and pc.Code not in (select employeeusername from [AfasProfit-Export]) and  dbo.getOrganisatieNaamNiveau5(W.OrganisatieEenheidID) in (%s)"));


            SignalSearchProcessor signalSearchProcessor = new SignalSearchProcessor(aggregatedDatabase);
            SignalStoredProcessor signalStoredProcessor = new SignalStoredProcessor(signalDatabase);

            SignalManager signalManager = new SignalManager(signalSearchProcessor, signalStoredProcessor, businessRules);

            User user = authenticator.getLoggedInUser();
            if (user == null) {
                showErrorMessage("Geen toegang", "Geen toegang tot de applicatie; neem contact op met systeem beheerder");
            } else {
                UI ui = new UI(signalManager, user);
            }

            // start UI
        } catch (Exception e) {
            //e.printStackTrace();
            String message = String.format("Bestand \"%s\" kan niet gelezen worden. Hierdoor ontbreken belangrijke gegevens voor succesvolle verbinding met database server", e.getMessage());
            showErrorMessage("b instellingen voor database verbinding", message);
        }
    }

    /**
     * Show error messag to user, before the UI is started
     *
     * @param title
     * @param message
     */
    public static void showErrorMessage(String title, String message) {
        JOptionPane.showMessageDialog(null,
                message,
                title,
                JOptionPane.ERROR_MESSAGE);
    }
}
