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
import java.util.Date;
import java.util.Objects;

/**
 * Contains information about signals
 * 2 constructors, 1 for old signals and 1 for new signals
 */

public class Signal {

    private final String account;
    private final String businessRule;
    private final String entity;
    private final Date   dateFound;
    private       Date   dateSolved;
    private final String connectionData;

    private ArrayList<Impact> impactList = new ArrayList<>();

    public void addImpact(ArrayList<Impact> impact) {
        this.impactList.addAll(impact);
    }

    public ArrayList<Impact> getImpact() {
        return impactList;
    }

    /**
     * Constructor for old signals
     *
     * @param account
     * @param businessRule
     * @param entity
     * @param dateFound
     * @param dateSolved
     * @param connectionData
     */
    public Signal(String account, String businessRule, String entity, Date dateFound, Date dateSolved, String connectionData) {
        this.account = account;
        this.businessRule = businessRule;
        this.entity = entity;
        this.dateFound = dateFound;
        this.dateSolved = dateSolved;
        this.connectionData = connectionData;
    }

    /**
     * Constructor for new Signals
     *
     * @param account
     * @param businessRule
     * @param entity
     * @param dateFound
     * @param connectionData
     */
    public Signal(String account, String businessRule, String entity, Date dateFound, String connectionData) {
        this.account = account;
        this.businessRule = businessRule;
        this.entity = entity;
        this.dateFound = dateFound;
        this.dateSolved = null;
        this.connectionData = connectionData;
    }

    public String getAccount() {
        return account;
    }

    public String getBusinessRule() {
        return businessRule;
    }

    public String getEntity() {
        return entity;
    }

    public Date getDateFound() {
        return dateFound;
    }

    public Date getDateSolved() {
        return dateSolved;
    }

    public void setDateSolved(Date dateSolved) {
        this.dateSolved = dateSolved;
    }

    public String getConnectionData() {
        return connectionData;
    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that "textually represents" this
     * object. The result should be a concise but informative representation
     * that is easy for a person to read. It is recommended that all subclasses
     * override this method.
     * <p>
     * The {@code toString} method for class {@code Object} returns a string
     * consisting of the name of the class of which the object is an instance,
     * the at-sign character `{@code @}', and the unsigned hexadecimal
     * representation of the hash code of the object. In other words, this
     * method returns a string equal to the value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return String.format("%s || %s || %s || %s || %s || %s || %s", this.entity, this.account, this.businessRule, this.connectionData, this.entity, this.dateFound, this.dateSolved);
    }

    /**
     * Override equals to sort signals
     *
     * @param obj the object this instance is compared with
     * @return returns true if the instance is comparable
     */
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof Signal)) {
            return false;
        }

        Signal signal = (Signal) obj;

        return this.account.equals(signal.getAccount())
                && this.businessRule.equals(signal.getBusinessRule())
                && this.entity.equals(signal.getEntity());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.account);
        hash = 59 * hash + Objects.hashCode(this.businessRule);
        hash = 59 * hash + Objects.hashCode(this.entity);
        return hash;
    }

    public float getDaysActive() {

        Date endDate = new Date();

        if (dateSolved != null) {
            endDate = dateSolved;
        }

        long  timeActive = endDate.getTime() - this.getDateFound().getTime();
        float daysActive = (float) timeActive / (24 * 3600 * 1000);

        return daysActive;
    }
}
