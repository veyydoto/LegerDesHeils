/**
 * Created by:
 * Adnan Akbas, 17005116
 * Bart Willems, 17098335
 * Joel Duurkoop, 17076021
 * Jari van Menxel, 17030072
 * Vedat Yilmaz, 17118700
 */
package legerdesheils;

import legerdesheils.BusinessRules.BusinessRule;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

public class SignalManager {

    // Signals in the Signal Database.
    private ArrayList<Signal> newSignals      = new ArrayList<>();
    private ArrayList<Signal> unsolvedSignals = new ArrayList<>();
    private ArrayList<Signal> solvedSignals   = new ArrayList<>();

    // The rules used to find signals
    private ArrayList<BusinessRule> businessRules;

    private SignalSearchProcessor signalSearchProcessor;
    private SignalStoredProcessor signalStoredProcessor;

    public SignalManager(SignalSearchProcessor signalSearchProcessor, SignalStoredProcessor signalStoredProcessor, ArrayList<BusinessRule> businessRules) {
        this.signalSearchProcessor = signalSearchProcessor;
        this.signalStoredProcessor = signalStoredProcessor;
        this.businessRules = businessRules;
    }

    public void reset() {
        newSignals.clear();
        unsolvedSignals.clear();
        solvedSignals.clear();
        for (BusinessRule b : businessRules) {
            b.setFoundSignals(0);
        }
    }

    /**
     * Contains statements to sort signals found in SignalProcessor object
     *
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     */
    public void searchSignals(User user) throws SQLException, ClassNotFoundException {
        ArrayList<Signal> foundSignals  = signalSearchProcessor.findSignals(businessRules, user);
        ArrayList<Signal> storedSignals = signalStoredProcessor.readStoredSignals(user);


        // eerste dag gevonden signaal, dag dat signaal opgelost is, anders huidige dag
        // Sort all signals
        for (Signal s : foundSignals) {
            if (!storedSignals.contains(s)) {
                // signal is not stored yet, but is found and has to be written to database later
                newSignals.add(s);
            }
        }

        for (Signal s : storedSignals) {
            if (!(foundSignals.contains(s))) {
                // Signal is not found, but is stored. It is solved, has to be updated in database
                if (s.getDateSolved() == null) {
                    s.setDateSolved(new Date());
                    solvedSignals.add(s);
                }
            } else {
                // signal is stored, but not solved
                unsolvedSignals.add(s);
            }
        }
    }

    public void writeSignals() throws SQLException, ClassNotFoundException, Exception {
        if (solvedSignals.size() > 0 || newSignals.size() > 0) {
            this.signalStoredProcessor.updateStoredSignals(solvedSignals);
            this.signalStoredProcessor.writeFoundSignals(newSignals);
        } else {
            throw new Exception("Geen signalen om te schrijven...");
        }
    }

    public ArrayList<Signal> getSignals() {
        ArrayList<Signal> allSignals = new ArrayList<>();
        allSignals.addAll(newSignals);
        allSignals.addAll(solvedSignals);
        allSignals.addAll(unsolvedSignals);
        return allSignals;
    }

    public void addImpactToSignal(Signal signal) throws SQLException {
        this.signalSearchProcessor.getImpactActiviteit(signal);
        this.signalSearchProcessor.getImpactEntiteit(signal);
        this.signalSearchProcessor.getImpactOrganisatie(signal);
    }

    public ArrayList<BusinessRule> getBusinessRules() {
        return businessRules;
    }

    public void connectAll() throws ClassNotFoundException, SQLException {
        signalSearchProcessor.connect();
        signalStoredProcessor.connect();
    }

    public void disconnectAll() throws SQLException {
        signalStoredProcessor.disconnect();
        signalStoredProcessor.disconnect();
    }

    public ArrayList<Signal> getNewSignals() {
        return newSignals;
    }

    public ArrayList<Signal> getSolvedSignals() {
        return solvedSignals;
    }

    public ArrayList<Signal> getUnsolvedSignals() {
        return unsolvedSignals;
    }


}
