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

import legerdesheils.BusinessRules.BusinessRule;

/**
 * The model of the business rules table
 * Used to show business rules and results to the user
 */
public class BusinessRuleTableModel extends AbstractTableModel {

    private final String[]                COLUMNS = {"#ID", "Businessrule", "Aantal onopgeloste signalen"};
    private       ArrayList<BusinessRule> businessRules;

    public BusinessRuleTableModel() {
        businessRules = new ArrayList<>();
    }

    public void setBusinessRules(ArrayList<BusinessRule> businesRules) {
        this.businessRules = businesRules;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return businessRules.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    // Fills the table
    @Override
    public Object getValueAt(int row, int column) {
        switch (column) {
            case 0:
                return businessRules.get(row).getIdCode();
            case 1:
                return businessRules.get(row).getDescription();
            case 2:
                return businessRules.get(row).getFoundSignals();
            default:
                return null;
        }
    }
}