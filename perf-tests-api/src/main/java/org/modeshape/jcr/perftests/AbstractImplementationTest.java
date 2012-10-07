/*
 * ModeShape (http://www.modeshape.org)
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * See the AUTHORS.txt file in the distribution for a full listing of 
 * individual contributors.
 *
 * ModeShape is free software. Unless otherwise indicated, all code in ModeShape
 * is licensed to you under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * ModeShape is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.modeshape.jcr.perftests;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryFactory;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import org.junit.Before;
import org.junit.Test;
import org.modeshape.jcr.perftests.RunnerCfg.AfterOperation;
import org.modeshape.jcr.perftests.report.TextFileReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for a test class used to run all of the performance tests against a particular implementation. This is a
 * simple JUnit class, and it follows several conventions.
 * <p>
 * Multiple JCR configurations are supported, where each configuration's name is given on the Maven command line via a profile
 * (e.g., "local-inmemory", "local-filesystem", etc.). Each configuration should have a properties file in "src/test/resources"
 * that is named the same as the configuration name and has the standard ".properties" extension. This class reads that file to
 * load the data for the test, sets up additional information (such as the {@link Credentials} and parameters passed to
 * {@link RepositoryFactory#getRepository(Map)}), and initializes directories specified in the tests.
 * </p>
 * Each properties file can also contain several optional properties used by this framework:
 * <ul>
 * <li>{@code tests.description} - The description of the test that will be included in the results; the default is simply the
 * configuration name
 * <li>
 * <li>{@code tests.username} - The username that should be used to obtain {@link Session} instances within the tests. This may be
 * blank or empty if anonymous Sessions are to be used.
 * <li>
 * <li>{@code tests.password} - The password for authenticating. Useful only when specifying a {@code test.username}.
 * <li>{@code tests.dirs} - A comma-separated list of paths relative to the target directory, where each path specifies a
 * directory that should be created prior to each test and removed after each test. Any files written by the implementation should
 * be done in these directories, so that runs of the same tests are repeatable.
 * <li>
 * <li>{@code tests.exclude} - Comma separated list of patterns representing the names of tests which are to be excluded (has
 * precedence over includes).
 * <li>
 * <li>{@code tests.include} - Comma separated list of patterns representing the names of tests which are to be included.
 * <li>
 * <li>{@code repeat.count} - The number of times each test suite should be run against a repo. To achieve good statistical
 * samplings, this should be greater than 5. The default value is defined the parent POM.
 * <li>
 * <li>{@code warmup.count} - The number of times each test suite is warmed up before starting to capture statistics. The default
 * value is defined the parent POM.
 * <li>
 * </ul>
 * And, each properties file can contain other properties that subclasses can utilize.
 */
public abstract class AbstractImplementationTest {

    // protected static final String DEFAULT_CONFIGURATION_NAME = "local-inmemory";
    protected static final String DEFAULT_CONFIGURATION_NAME = "local-filesystem";

    protected static final String CONFIGURATION_NAME_SYSTEM_PROPERTY_NAME = "test.jcr.configuration.name";
    protected static final String TEST_DESCRIPTION = "tests.description";
    protected static final String TEST_USERNAME_PROPERTY = "tests.username";
    protected static final String TEST_PASSWORD_PROPERTY = "tests.password";
    protected static final String TEST_DIRECTORIES_PROPERTY = "tests.dirs";

    protected Logger logger;
    protected RunnerCfg runnerConfig;
    protected String configurationName;
    protected String testDescription;
    protected Credentials credentials;
    protected List<File> testDirectories;
    protected Map<String, Object> repositoryFactoryParameters;
    private Exception initializationError;

    /**
     * Method that is called before each of this class' test methods are called.
     */
    @Before
    public void before() {
        try {
            logger = LoggerFactory.getLogger(getClass());

            // Read the system property for the configuration name ...
            configurationName = System.getProperty(CONFIGURATION_NAME_SYSTEM_PROPERTY_NAME);
            if (configurationName == null) {
                logger.warn("The '" + CONFIGURATION_NAME_SYSTEM_PROPERTY_NAME
                            + "' system property must be set. See the Maven POM and parent POM for details. Using '"
                            + DEFAULT_CONFIGURATION_NAME + "' as a default.");
                configurationName = DEFAULT_CONFIGURATION_NAME;
            }

            // Set up the runner configuration ...
            runnerConfig = new RunnerCfg(configurationName + ".properties");

            // Find the description of the test ...
            testDescription = runnerConfig.getProperty(TEST_DESCRIPTION, configurationName);

            // Create the credentials ...
            String username = runnerConfig.getProperty(TEST_USERNAME_PROPERTY);
            String password = runnerConfig.getProperty(TEST_PASSWORD_PROPERTY);
            if (username != null && !username.trim().isEmpty()) {
                credentials = new SimpleCredentials(username, password.toCharArray());
            } else {
                credentials = null;
            }

            // Find the test directories ...
            final File targetDir = new File("target");
            assert targetDir.exists();
            assert targetDir.isDirectory();
            List<String> testDirPaths = runnerConfig.getPropertyAsList(TEST_DIRECTORIES_PROPERTY);
            final List<File> testDirectories = new ArrayList<File>(testDirPaths.size());
            for (String testDir : testDirPaths) {
                File file = new File(targetDir, testDir);
                testDirectories.add(file);
            }
            this.testDirectories = Collections.unmodifiableList(testDirectories);

            // Initialize the repository factory parameters (after all the fields are set) ...
            Map<String, Object> repositoryFactoryParameters = new HashMap<String, Object>();
            this.repositoryFactoryParameters = Collections.unmodifiableMap(repositoryFactoryParameters);
            initializeRepositoryFactoryProperties(repositoryFactoryParameters);

            // Register handler to create the test dirs before each test suite run ...
            runnerConfig.runBeforeRunningSuite(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    for (File testDir : testDirectories) {
                        TestUtil.delete(testDir);
                        testDir.mkdirs();
                    }
                    initializeBeforeRunningTest();
                    return true;
                }
            });

            // Register handler to remove the test dirs after each test suite run ...
            runnerConfig.runAfterRunningSuite(new AfterOperation() {
                @Override
                public void call( RepositoryFactory repositoryFactory,
                                  Repository repository ) throws Exception {
                    cleanupAfterRunningTest(repositoryFactory, repository);
                    for (File testDir : testDirectories) {
                        TestUtil.delete(testDir);
                    }
                }
            });
        } catch (Throwable t) {
            initializationError = new IllegalStateException("Error setting the RepositoryFactory parameters", t);
        }
    }

    /**
     * Override this method to perform custom initialization before each test is run. Note that the test directories will have
     * been created before this method is called.
     * <p>
     * By default, this method does nothing.
     * </p>
     * 
     * @throws Exception if there is a problem with the initialization
     */
    protected void initializeBeforeRunningTest() throws Exception {
    }

    /**
     * Override this method to perform custom initialization after each test is run. Note that the test directories will always be
     * removed after this method is called (unless an exception is thrown).
     * <p>
     * By default, this method does nothing.
     * </p>
     * 
     * @param repositoryFactory the factory that was used to obtain the repository
     * @param repository the repository
     * @throws Exception if there is a problem with the initialization
     */
    protected void cleanupAfterRunningTest( RepositoryFactory repositoryFactory,
                                            Repository repository ) throws Exception {
    }

    /**
     * Method that is called to initialize the parameters passed to the JCR {@link RepositoryFactory#getRepository(Map)} method to
     * obtain the repository.
     * 
     * @param parameters the map of parameters; never null
     * @throws Exception if there is a problem or error seting the parameters
     */
    protected abstract void initializeRepositoryFactoryProperties( Map<String, Object> parameters ) throws Exception;

    /**
     * A JUnit test method that runs the entire performance suite with the current configuration.
     * <p>
     * Generally, subclasses don't need to add any more test methods. However, if subclasses wish to change this method, simply
     * override its behavior.
     * </p>
     * 
     * @throws Exception if there is a problem or error in the test
     */
    @Test
    public void runPerformanceSuite() throws Exception {
        if (initializationError != null) throw initializationError;

        // Run the suite ...
        SuiteRunner performanceTestSuiteRunner = new SuiteRunner(testDescription, runnerConfig);
        performanceTestSuiteRunner.runPerformanceTests(repositoryFactoryParameters, credentials);

        // and produce the report data ...
        new TextFileReport(TimeUnit.SECONDS).generateReport(performanceTestSuiteRunner.getTestData());
    }
}
