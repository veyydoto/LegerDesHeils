/**
 * Created by:
 * Adnan Akbas, 17005116
 * Bart Willems, 17098335
 * Joel Duurkoop, 17076021
 * Jari van Menxel, 17030072
 * Vedat Yilmaz, 17118700
 */
package legerdesheils.Helpers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import javax.naming.InvalidNameException;

import legerdesheils.Signal;

/**
 * Writes CSV to excel
 */
public class SignalsCsvWriter {

    private ArrayList<Signal> signals;

    private String filename;

    private String saveFileLocation;

    private static String[] columns = {"account", "business rule", "entity", "date found", "date_solved", "connection data"};


    private static final char DEFAULT_SEPARATOR = ',';

    /**
     * Constructor for SignalsCsvWriter
     *
     * @param signals          list of signals which will be written to csv file
     * @param filename         name of the file we are going to save
     * @param saveFileLocation location where we will save csv file
     * @throws InvalidNameException
     */
    public SignalsCsvWriter(ArrayList<Signal> signals, String filename, String saveFileLocation) throws InvalidNameException {
        //validating given data
        if (filename == null || filename.isEmpty()) {
            throw new InvalidNameException("Excel bestand naam moet minstens 1 character bevatten!");
        }
        if (saveFileLocation == null || saveFileLocation.isEmpty()) {
            throw new InvalidPathException("", "aangegeven locatie is ongeldig");
        }

        this.signals = signals;
        this.filename = filename;
        this.saveFileLocation = saveFileLocation;
    }

    /**
     * setter method for signals
     *
     * @param signals list of signals which will be written to csv file
     */
    public void setSignals(ArrayList<Signal> signals) {
        this.signals = signals;
    }

    /**
     * setter method for variable file name.
     * File name should always contain atleast one letter. Otherwhise there wil exception thrown
     *
     * @param filename name of the file we are going to save
     * @throws InvalidNameException only thrown when the filename is empty or is null value
     */
    public void setFilename(String filename) throws InvalidNameException {
        if (filename == null || filename.isEmpty()) {
            throw new InvalidNameException("Excel bestand naam moet minstens 1 character bevatten!");
        }
        this.filename = filename;
    }

    /**
     * @return where we will save file to
     */
    public String getFullPath() {
        String path = this.saveFileLocation;
        if (!path.substring(path.length() - 1).equals("/")) {
            path += "/";
        }
        return path + this.filename;

    }

    /**
     * This method creates csv file from signals data.
     *
     * @throws IOException only thrown when we can't write csv file for some  weird reason
     */
    public void writeCsvFile() throws IOException {
        Writer writer    = new FileWriter(this.getFullPath());
        String tableHead = this.generateLine(columns);
        writer.append(tableHead);
        for (Signal signal : this.signals) {
            String[] data = {signal.getAccount(),
                    signal.getBusinessRule(),
                    signal.getEntity(),
                    signal.getDateFound().toString(),
                    signal.getDateSolved() != null ? signal.getDateSolved().toString() : "",
                    signal.getConnectionData()};
            writer.append(this.generateLine(data));
        }
        writer.close();
    }

    /**
     * Method creates a row from given values. The created rows will be split with ','
     *
     * @param values list of values which will be written to one row
     * @return string in csv format
     */
    public String generateLine(String[] values) {
        boolean first = true;

        StringBuilder stringBuilder = new StringBuilder();
        for (String value : values) {
            if (!first) {
                stringBuilder.append(DEFAULT_SEPARATOR);
            }
            stringBuilder.append(value);
            first = false;
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    /**
     * @return path to csv file
     */
    public String getSaveFileLocation() {
        return saveFileLocation;
    }

    /**
     * @param saveFileLocation replace file directory
     */
    public void setSaveFileLocation(String saveFileLocation) {
        this.saveFileLocation = saveFileLocation;
    }

}