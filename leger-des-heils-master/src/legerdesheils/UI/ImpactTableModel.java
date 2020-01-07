/**
 * Created by:
 * Adnan Akbas, 17005116
 * Bart Willems, 17098335
 * Joel Duurkoop, 17076021
 * Jari van Menxel, 17030072
 * Vedat Yilmaz, 17118700
 */
package legerdesheils.UI;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

import legerdesheils.Impact;

/**
 * Table used to show impact
 */
public class ImpactTableModel extends AbstractTableModel {

    private final String[]          columnNames = {"Entiteit", "Hoeveelheid"};
    private       ArrayList<Impact> impact;

    public ImpactTableModel(ArrayList<Impact> impact) {
        this.impact = impact;
    }

    @Override
    public int getRowCount() {
        return impact.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    // Fills the tabel
    @Override
    public Object getValueAt(int row, int column) {
        switch (column) {
            case 0:
                return impact.get(row).getEntity();
            case 1:
                return impact.get(row).getImpactAmount();
            default:
                return null;
        }
    }
}
