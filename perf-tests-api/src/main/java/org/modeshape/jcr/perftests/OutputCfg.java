package org.modeshape.jcr.perftests;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Stack;

/**
 * Class which exposes properties related to report & test data output paths, as defined in the output.properties file.
 *
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
public class OutputCfg {

    private final static String CONFIG_FILE = "output.properties";

    private final static String TEST_DATA_OUTPUT_FOLDER;
    private final static String TEST_DATA_OUTPUT_PACKAGE;
    private final static String REPORT_OUTPUT_FOLDER;

    static {
        Properties configFile = new Properties();
        try {
            configFile.load(OutputCfg.class.getClassLoader().getResourceAsStream(CONFIG_FILE));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TEST_DATA_OUTPUT_FOLDER = readProperty(configFile, "test.data.output.folder");
        REPORT_OUTPUT_FOLDER = readProperty(configFile, "reports.output.folder");
        TEST_DATA_OUTPUT_PACKAGE = readProperty(configFile, "test.data.output.package");
    }

    /**
     * Returns the folder where the raw data of each test run should be placed
     *
     * @return an valid folder
     */
    public static File testDataOutputFolder() {
        return writableFolder(TEST_DATA_OUTPUT_FOLDER);
    }

    /**
     * Returns the folder where graphic reports should be placed.
     *
     * @return an valid folder
     */
    public static File reportOutputFolder() {
        return writableFolder(REPORT_OUTPUT_FOLDER);
    }

    /**
     * Returns the name of the package where the test data can be found in the classpath
     *
     * @return the package name; never null
     */
    public static String testDataOutputPackage() {
       return TEST_DATA_OUTPUT_PACKAGE;
    }

    private static File writableFolder( String folderPath ) {
        //always interpret the folder relative to the current working directory, which should be ${baseDir}
        File folder = new File(".", folderPath);
        if (folder.exists() && (!folder.isDirectory() || !folder.canWrite())) {
            throw new IllegalStateException("The path: " + folder.getAbsolutePath() + " does not represent a valid, writable folder");
        }

        if (!folder.exists()) {
            File parentFolder = folder.getParentFile();
            Stack<String> segmentsToCreate = new Stack<String>();
            segmentsToCreate.push(folder.getName());
            while (parentFolder != null && !parentFolder.exists()) {
                segmentsToCreate.push(parentFolder.getName());
                parentFolder = parentFolder.getParentFile();
            }

            if (parentFolder == null) {
                throw new IllegalStateException("The path: " + folder.getAbsolutePath() + " represents a non existent path");
            }

            while (!segmentsToCreate.isEmpty()) {
                File segment = new File(parentFolder, segmentsToCreate.pop());
                if (!segment.mkdir()) {
                    throw new IllegalStateException("Cannot create the " + segment.getAbsolutePath() + " folder ");
                }
                parentFolder = segment;
            }
        }
        return folder;
    }

    private static String readProperty(Properties configFile, String propertyName) {
        String property = configFile.getProperty(propertyName);
        if (property == null) {
            throw new IllegalStateException("The property " + propertyName + " must be defined in the " + CONFIG_FILE);
        }
        return property;
    }
}
