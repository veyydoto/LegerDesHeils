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

import legerdesheils.Signal;

/**
 * Table used to show signals
 */
public class SignalTableModel extends AbstractTableModel {

    // The columnnames
    private final String[]          COLUMNS = {"Entiteit", "Account", "Businessrule", "Datum Gevonden", "Datum Opgelost", "Connectie Data"};
    private       ArrayList<Signal> signals;

    public SignalTableModel() {
        this.signals = new ArrayList<>();
    }

    /**
     * Set the signals that are shown on the table, update table
     *
     * @param signals the signals
     */
    public void setSignals(ArrayList<Signal> signals) {
        this.signals = signals;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return signals.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    /**
     * Returns name of column of the table header
     *
     * @param column
     * @return
     */
    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    /**
     * Makes all cells uneditable
     *
     * @param row
     * @param column
     * @return
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    /**
     * Returns cell value of signal in a table
     *
     * @param row    the row in the table
     * @param column the column in the table
     * @return
     */
    @Override
    public Object getValueAt(int row, int column) {
        switch (column) {
            case 0:
                return signals.get(row).getEntity();
            case 1:
                return signals.get(row).getAccount();
            case 2:
                return signals.get(row).getBusinessRule();
            case 3:
                return signals.get(row).getDateFound();
            case 4:
                return signals.get(row).getDateSolved();
            case 5:
                return signals.get(row).getConnectionData();
            default:
                return null;
        }
    }

    public ArrayList<Signal> getSignals() {
        return signals;
    }


}
