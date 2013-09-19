/*
 * JBoss, Home of Professional Open Source
 * Copyright [2011], Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.modeshape.jcr.perftests;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryFactory;
import org.modeshape.jcr.perftests.output.CsvOutput;
import org.reflections.Reflections;
import org.reflections.scanners.TypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Class which runs a set of test suites against all the JCR repositories which are found in the classpath. The
 * <code>ServiceLoader</code> mechanism is used for scanning the <code>RepositoryFactory</code> instances. All subclasses of
 * {@link org.modeshape.jcr.perftests.AbstractPerformanceTestSuite} found within the org.modeshape.jcr.perftests package (or
 * subpackages) will be loaded by default.
 *
 * @author Horia Chiorean
 */
public final class SuiteRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuiteRunner.class);

    private final TestData testData;
    private final RunnerCfg runnerConfig;

    /**
     * Creates a new default test runner instance, which loads its properties from a file called "runner.properties" in the
     * classpath.
     *
     * @param repositoryName the repository name
     */
    public SuiteRunner( String repositoryName ) {
        this(repositoryName, new RunnerCfg());
    }

    /**
     * Creates a new runner instance passing a custom config.
     *
     * @param repositoryName the repository name
     * @param runnerConfig the runner configuration
     */
    public SuiteRunner( String repositoryName,
                        RunnerCfg runnerConfig ) {
        this.testData = new TestData(repositoryName);
        this.runnerConfig = runnerConfig;
    }

    /**
     * Uses the given map of parameters together with the <code>ServiceLoader</code> mechanism to get all the
     * <code>RepositoryFactory</code> instances and the subsequent repositories against which the tests will be run.
     *
     * @param repositoryConfigParams a map of config params {@link javax.jcr.RepositoryFactory#getRepository(java.util.Map)}
     * @param credentials a set of credentials which may be needed by a certain repo to run. It can be null.
     * @throws Exception if anything unexpected happens during the run. In case there are test exceptions, those will just be
     * logged and the suite will continue to run.
     */
    public void runPerformanceTests( Map<?, ?> repositoryConfigParams,
                                     Credentials credentials ) throws Exception {
        // Load the test suite, and run each suite by itself with a clean repository ...
        Set<Class<? extends AbstractPerformanceTestSuite>> testSuites = loadPerformanceTestSuites();
        for (Class<? extends AbstractPerformanceTestSuite> testSuiteClass : testSuites) {

            // Before running each suite ...
            runnerConfig.beforeRunningSuite();

            // Run the suite ...
            RepositoryFactory repositoryFactory = null;
            Repository repository = null;
            try {
                for (RepositoryFactory factory : ServiceLoader.load(RepositoryFactory.class)) {
                    repository = new RepositoryInitRun(credentials, factory, repositoryConfigParams).execute();
                    if (repository == null) {
                        continue;
                    }

                    repositoryFactory = factory;
                    SuiteConfiguration suiteConfiguration = new SuiteConfiguration(repository, credentials,
                                                                                   "testsuite.properties");
                    runTestSuite(suiteConfiguration, testSuiteClass);
                    break;
                }
            } finally {
                // Always after the suite is run ...
                runnerConfig.afterRunningSuite(repositoryFactory, repository);
            }
        }

        new CsvOutput().generateOutput(testData);
    }

    /**
     * Returns the test data which has been recorded by this runner.
     *
     * @return a {@link TestData} instance.
     */
    public TestData getTestData() {
        return testData;
    }

    private void runTestSuite( SuiteConfiguration suiteConfiguration,
                               Class<? extends AbstractPerformanceTestSuite> testSuiteClass ) throws Exception {
        final AbstractPerformanceTestSuite testSuite = testSuiteClass.getConstructor(SuiteConfiguration.class).newInstance(
                suiteConfiguration);

        if (isSuiteExcluded(testSuiteClass)) {
            return;
        }

        if (!testSuite.isCompatibleWithCurrentRepository()) {
            LOGGER.warn("Test suite {} not compatible with {}",
                        new Object[] { testSuite.getClass().getSimpleName(), testData.getRepositoryName() });
            return;
        }

        LOGGER.info("Starting suite: {}[warmup #:{}, repeat#{}]",
                    new Object[] { testSuiteClass.getSimpleName(), runnerConfig.warmupCount, runnerConfig.repeatCount });

        new SuiteRun(testSuite, runnerConfig.warmupCount, runnerConfig.repeatCount).execute();
    }

    private boolean isSuiteExcluded( Class<? extends AbstractPerformanceTestSuite> testSuiteClass ) {
        // first search excluded list
        if (patternMatchesSuiteName(testSuiteClass, runnerConfig.excludeTestsRegExp)) {
            return true;
        }
        // then search included list
        return !runnerConfig.includeTestsRegExp.isEmpty() && !patternMatchesSuiteName(testSuiteClass,
                                                                                      runnerConfig.includeTestsRegExp);
    }

    private boolean patternMatchesSuiteName( Class<? extends AbstractPerformanceTestSuite> suiteClass,
                                             List<String> patternsList ) {
        for (Iterator<String> iterator = patternsList.iterator(); iterator.hasNext(); ) {
            String patternString = iterator.next();
            try {
                Pattern pattern = Pattern.compile(patternString);
                if (pattern.matcher(suiteClass.getName()).matches() || pattern.matcher(suiteClass.getSimpleName()).matches()) {
                    return true;
                }
            } catch (PatternSyntaxException e) {
                LOGGER.warn("Invalid regex " + patternString, e);
                iterator.remove();
            }
        }
        return false;
    }

    private Set<Class<? extends AbstractPerformanceTestSuite>> loadPerformanceTestSuites() {
        ConfigurationBuilder builder = new ConfigurationBuilder().addUrls(ClasspathHelper.forPackage("org.modeshape"))
                                                                 .setScanners(new TypesScanner()).useParallelExecutor();
        Reflections reflections = new Reflections(builder);
        return reflections.getSubTypesOf(AbstractPerformanceTestSuite.class);
    }

    private final class SuiteRun {

        private final AbstractPerformanceTestSuite suite;
        private final int warmupCount;
        private final int runCount;

        private SuiteRun( AbstractPerformanceTestSuite suite,
                          int warmupCount,
                          int runCount ) {
            this.suite = suite;
            this.warmupCount = warmupCount;
            this.runCount = runCount;
        }

        void execute() throws Exception {
            String suiteName = suite.getClass().getSimpleName();
            LOGGER.info("Starting {} ....", suiteName);
            //run the warmup without recording
            try {
                LOGGER.info("{} setUp()....", suiteName);
                suite.setUp();

                LOGGER.info("{} warming up....", suiteName);
                for (int i = 0; i < warmupCount; i++) {
                    suite.run();
                }

                //run & record
                for (int i = 0; i < runCount; i++) {
                    long start = System.nanoTime();
                    suite.run();
                    long duration = System.nanoTime() - start;
                    getTestData().recordSuccess(suiteName, duration, i + 1);
                }
                LOGGER.info("{} tearDown()....", suiteName);
                suite.tearDown();
            } catch (Throwable throwable) {
                LOGGER.error("Error while running " + suiteName, throwable);
                getTestData().recordFailure(suiteName, throwable);
            }
        }
    }

    private final class RepositoryInitRun {

        private final RepositoryFactory repositoryFactory;
        private final Map<?, ?> repositoryConfigParams;
        private final Credentials credentials;

        private RepositoryInitRun( Credentials credentials,
                                   RepositoryFactory repositoryFactory,
                                   Map<?, ?> repositoryConfigParams ) {
            this.credentials = credentials;
            this.repositoryFactory = repositoryFactory;
            this.repositoryConfigParams = repositoryConfigParams;
        }

        Repository execute() throws Exception {
            String operationName = "Repository initialization";
            try {
                long start = System.nanoTime();

                Repository repository = repositoryFactory.getRepository(repositoryConfigParams);
                if (repository == null) {
                    return null;
                }
                repository.login(credentials).logout();

                long duration = System.nanoTime() - start;
                getTestData().recordSuccess(operationName, duration, 1);
                return repository;
            } catch (Throwable t) {
                getTestData().recordFailure(operationName, t);
                return null;
            }
        }
    }
}
