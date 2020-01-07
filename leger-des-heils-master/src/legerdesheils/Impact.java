/**
 * Created by:
 * Adnan Akbas, 17005116
 * Bart Willems, 17098335
 * Joel Duurkoop, 17076021
 * Jari van Menxel, 17030072
 * Vedat Yilmaz, 17118700
 */
package legerdesheils;

public class Impact {

    private String entity;
    private int    impactAmount;

    public Impact(String entity, int impactAmount) {
        this.entity = entity;
        this.impactAmount = impactAmount;
    }

    public String getEntity() {
        return entity;
    }

    public int getImpactAmount() {
        return impactAmount;
    }
}