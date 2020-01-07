/**
 * Created by:
 * Adnan Akbas, 17005116
 * Bart Willems, 17098335
 * Joel Duurkoop, 17076021
 * Jari van Menxel, 17030072
 * Vedat Yilmaz, 17118700
 */
package legerdesheils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Settings stored in the datafiles are loaded here
 */
public class EnvironmentVariables {
    private Properties properties;

    /**
     * Loads a file with properties
     *
     * @param fileName name of the file that should be loaded
     */
    public EnvironmentVariables(String fileName) throws IOException {
        loadData(fileName);
    }

    /**
     * Load data from file
     *
     * @param fileName name of the file
     * @throws FileNotFoundException file
     * @throws IOException
     */

    public void loadData(String fileName) throws FileNotFoundException, IOException {
        FileInputStream in = new FileInputStream(fileName);
        properties = new Properties();
        properties.load(in);
    }

    /**
     * Get a property from properties object
     *
     * @param key the key to get the value
     * @return value corresponding to key
     */

    public String getData(String key) {
        return properties.getProperty(key);
    }
}
