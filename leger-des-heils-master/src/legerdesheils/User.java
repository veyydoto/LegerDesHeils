/**
 * Created by:
 * Adnan Akbas, 17005116
 * Bart Willems, 17098335
 * Joel Duurkoop, 17076021
 * Jari van Menxel, 17030072
 * Vedat Yilmaz, 17118700
 */
package legerdesheils;


import java.util.ArrayList;

/**
 * User contains information about the person that connects with the server
 * and if that person is an admin
 */

public class User {
    private String            username;
    private ArrayList<String> workunit;
    private boolean           isAdmin;

    public User(String username, ArrayList<String> workunit, boolean isAdmin) {
        this.username = username;
        this.workunit = workunit;
        this.isAdmin = isAdmin;
    }

    public String getUsername() {
        return username;
    }

    public ArrayList<String> getWorkunit() {
        return workunit;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}
