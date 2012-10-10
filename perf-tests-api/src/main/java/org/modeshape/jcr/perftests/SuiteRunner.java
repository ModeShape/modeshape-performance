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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryFactory;
import org.modeshape.jcr.perftests.report.CsvReport;
import org.reflections.Reflections;
import org.reflections.scanners.TypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     *         logged and the suite will continue to run.
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
                    repository = initRepository(factory, repositoryConfigParams, credentials);
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

        new CsvReport().generateReport(testData);
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
        final AbstractPerformanceTestSuite testSuite = testSuiteClass.getConstructor(SuiteConfiguration.class)
                                                                     .newInstance(suiteConfiguration);

        if (isSuiteExcluded(testSuiteClass)) {
            return;
        }

        if (!testSuite.isCompatibleWithCurrentRepository()) {
            LOGGER.warn("Test suite {} not compatible with {}",
                        new Object[] {testSuite.getClass().getSimpleName(), testData.getRepositoryName()});
            return;
        }

        LOGGER.info("Starting suite: {}[warmup #:{}, repeat#{}]", new Object[] {testSuiteClass.getSimpleName(),
            runnerConfig.warmupCount, runnerConfig.repeatCount});
        testSuite.setUp();
        // warm up the suite
        RecordableOperation<Void> testSuiteRun = new RecordableOperation<Void>(testSuiteClass.getSimpleName(),
                                                                               true,
                                                                               runnerConfig.warmupCount) {
            @Override
            public Void call() throws Exception {
                testSuite.run();
                return null;
            }
        };
        testSuiteRun.run();

        // run and record
        testSuiteRun.setWarmup(false).setRepeatCount(runnerConfig.repeatCount).run();
        testSuite.tearDown();

    }

    private boolean isSuiteExcluded( Class<? extends AbstractPerformanceTestSuite> testSuiteClass ) {
        // first search excluded list
        if (patternMatchesSuiteName(testSuiteClass, runnerConfig.excludeTestsRegExp)) {
            return true;
        }
        // then search included list
        return !runnerConfig.includeTestsRegExp.isEmpty()
               && !patternMatchesSuiteName(testSuiteClass, runnerConfig.includeTestsRegExp);
    }

    private boolean patternMatchesSuiteName( Class<? extends AbstractPerformanceTestSuite> suiteClass,
                                             List<String> patternsList ) {
        for (Iterator<String> iterator = patternsList.iterator(); iterator.hasNext();) {
            String pattern = iterator.next();
            try {
                if (Pattern.matches(pattern, suiteClass.getName()) || Pattern.matches(pattern, suiteClass.getSimpleName())) {
                    return true;
                }
            } catch (PatternSyntaxException e) {
                LOGGER.warn("Invalid regex {}", pattern);
                iterator.remove();
            }
        }
        return false;
    }

    private Repository initRepository( final RepositoryFactory repositoryFactory,
                                       final Map<?, ?> repositoryConfigParams,
                                       final Credentials credentials ) throws Exception {

        return new RecordableOperation<Repository>("Initialization", false, 1) {
            @Override
            public Repository call() throws Exception {
                Repository repository = repositoryFactory.getRepository(repositoryConfigParams);
                if (repository == null) {
                    return null;
                }
                repository.login(credentials).logout();
                return repository;
            }
        }.run();
    }

    private Set<Class<? extends AbstractPerformanceTestSuite>> loadPerformanceTestSuites() {
        ConfigurationBuilder builder = new ConfigurationBuilder().addUrls(ClasspathHelper.forPackage("org.modeshape"))
                                                                 .setScanners(new TypesScanner())
                                                                 .useParallelExecutor();
        Reflections reflections = new Reflections(builder);
        return reflections.getSubTypesOf(AbstractPerformanceTestSuite.class);
    }

    /**
     * Class which represents a recordable operation, depending on whether the warmup parameters is true or not. In case of
     * warmup, no data is recorded.
     * 
     * @param <V> the result type of the operation
     */
    private abstract class RecordableOperation<V> implements Callable<V> {
        private String name;
        private boolean warmup;
        private int repeatCount;

        RecordableOperation( String name,
                             boolean warmup,
                             int repeatCount ) {
            this.name = name;
            this.warmup = warmup;
            this.repeatCount = repeatCount;
        }

        RecordableOperation<V> setWarmup( boolean warmup ) {
            this.warmup = warmup;
            return this;
        }

        RecordableOperation<V> setRepeatCount( int repeatCount ) {
            this.repeatCount = repeatCount;
            return this;
        }

        V run() throws Exception {
            V result = null;
            try {
                for (int i = 0; i < repeatCount; i++) {
                    long start = System.nanoTime();
                    result = call();
                    long duration = System.nanoTime() - start;
                    if (!warmup) {
                        getTestData().recordSuccess(name, duration);
                    }
                }
            } catch (Throwable throwable) {
                if (!warmup) {
                    getTestData().recordFailure(name, throwable);
                }
            }
            return result;
        }
    }
}
