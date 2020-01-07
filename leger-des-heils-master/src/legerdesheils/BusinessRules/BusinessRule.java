/**
 * Created by:
 * Adnan Akbas, 17005116
 * Bart Willems, 17098335
 * Joel Duurkoop, 17076021
 * Jari van Menxel, 17030072
 * Vedat Yilmaz, 17118700
 */
package legerdesheils.BusinessRules;

import legerdesheils.User;

import java.util.ArrayList;

/**
 * Is used to store queries and the amount of results
 * Returns a query based on user authentication
 */
public class BusinessRule {

    private String idCode;
    private String adminQuery;
    private int    foundSignals = 0;
    private String userQuery;

    public int getFoundSignals() {
        return foundSignals;
    }

    public void setFoundSignals(int foundSignals) {
        this.foundSignals = foundSignals;
    }

    private String description;

    public BusinessRule(String idCode, String adminQuery, String description, String userQuery) {
        this.idCode = idCode;
        this.adminQuery = adminQuery;
        this.description = description;
        this.userQuery = userQuery;
    }

    public String getIdCode() {
        return idCode;
    }

    public String getQuery(User user) {
        if (user.isAdmin()) {
            return adminQuery;
        } else {
            StringBuilder     stringBuilder = new StringBuilder();
            ArrayList<String> workUnits     = user.getWorkunit();
            for (int i = 0; i < workUnits.size(); i++) {
                stringBuilder.append("'").append(workUnits.get(i)).append("'");
                if (i + 1 != workUnits.size()) {
                    stringBuilder.append(",");
                }
            }
            return String.format(userQuery, stringBuilder);

        }
    }

    public String getDescription() {
        return description;
    }
}
