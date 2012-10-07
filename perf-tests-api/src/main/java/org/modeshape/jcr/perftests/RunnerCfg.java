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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import javax.jcr.Repository;
import javax.jcr.RepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holder for the configuration options of a <code>PerformanceTestSuiteRunner</code>. Normally this class holds the values read
 * from a configuration file. (e.g. runner.properties)
 * 
 * @author Horia Chiorean
 */
public final class RunnerCfg {
    private static final Logger LOGGER = LoggerFactory.getLogger(RunnerCfg.class);

    /** default config file, loaded from classpath */
    private static final String DEFAULT_CONFIG_FILE = "runner.properties";

    /**
     * The default value for the {@link #setRepeatCount(int) repeat count} is {@value} .
     */
    private static final int DEFAULT_REPEAT_COUNT = 1;

    /**
     * The default value for the {@link #setWarmupCount(int) warmup count} is {@value} .
     */
    private static final int DEFAULT_WARMUP_COUNT = 1;

    final List<String> excludeTestsRegExp = new ArrayList<String>();
    final List<String> includeTestsRegExp = new ArrayList<String>();
    private Callable<?> beforeRunningSuiteRunnable;
    private AfterOperation afterRunningSuiteRunnable;
    final Properties configurationProperties;

    int repeatCount = DEFAULT_REPEAT_COUNT;
    int warmupCount = DEFAULT_WARMUP_COUNT;

    RunnerCfg( String fileName ) {
        configurationProperties = new Properties();
        try {
            configurationProperties.load(getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE));
            configurationProperties.load(getClass().getClassLoader().getResourceAsStream(fileName));
            initRunner(configurationProperties);
        } catch (IOException e) {
            LOGGER.warn("Cannot load config file. Will use defaults ", e);
        }
    }

    /**
     * Creates a new instance by reading the {@link RunnerCfg#DEFAULT_CONFIG_FILE}
     */
    public RunnerCfg() {
        this(DEFAULT_CONFIG_FILE);
    }

    /**
     * Get a configuration property.
     * 
     * @param name the property name
     * @return the property value, or null if there was no such property
     */
    public String getProperty( String name ) {
        return configurationProperties.getProperty(name);
    }

    /**
     * Get a configuration property.
     * 
     * @param name the property name
     * @param defaultValue the default value for the property
     * @return the property value, or the default value if there was no such property
     */
    public String getProperty( String name,
                               String defaultValue ) {
        return configurationProperties.getProperty(name, defaultValue);
    }

    /**
     * Get configuration property as a list of values.
     * 
     * @param name the property name
     * @return a list containing the comma-separated values; never null but possibly empty if there was no property with the given
     *         name
     */
    public List<String> getPropertyAsList( String name ) {
        List<String> result = new ArrayList<String>();
        String value = configurationProperties.getProperty(name);
        parseMultiValuedString(value, result);
        return result;
    }

    /**
     * Adds regexp patterns to exclude tests from running.
     * 
     * @param excludeTestsRegExp the regular expressions defining the tests that should be excluded
     * @return this runner configuration (for method chaining purposes)
     */
    public RunnerCfg addTestsToExclude( String... excludeTestsRegExp ) {
        this.excludeTestsRegExp.addAll(Arrays.asList(excludeTestsRegExp));
        return this;
    }

    /**
     * Adds regexp patterns to include tests to run.
     * 
     * @param includeTestsRegExp the regular expressions defining the tests that should be included
     * @return this runner configuration (for method chaining purposes)
     */
    public RunnerCfg addTestsToInclude( String... includeTestsRegExp ) {
        this.includeTestsRegExp.addAll(Arrays.asList(includeTestsRegExp));
        return this;
    }

    /**
     * Set the number of times the tests should be repeated (after warming up). The value will affect the quality of the
     * statistical results. The default is {@link #DEFAULT_REPEAT_COUNT}.
     * 
     * @param repeatCount the number of times to repeat each test
     * @return this runner configuration (for method chaining purposes)
     */
    public RunnerCfg setRepeatCount( int repeatCount ) {
        this.repeatCount = repeatCount;
        return this;
    }

    /**
     * Set the number of times the tests should be run during the warmup phase, when no timing statistics are measured. The
     * default is {@link #DEFAULT_WARMUP_COUNT}.
     * 
     * @param warmupCount the number of times each test should be run during warmup
     * @return this runner configuration (for method chaining purposes)
     */
    public RunnerCfg setWarmupCount( int warmupCount ) {
        this.warmupCount = warmupCount;
        return this;
    }

    private void initRunner( Properties configParams ) {
        parseMultiValuedString(configParams.getProperty("tests.exclude"), excludeTestsRegExp);
        parseMultiValuedString(configParams.getProperty("tests.include"), includeTestsRegExp);
        repeatCount = Integer.valueOf(configParams.getProperty("repeat.count", Integer.toString(DEFAULT_REPEAT_COUNT)));
        warmupCount = Integer.valueOf(configParams.getProperty("warmup.count", Integer.toString(DEFAULT_WARMUP_COUNT)));
    }

    protected void parseMultiValuedString( String multiValueString,
                                           List<String> collector ) {
        if (multiValueString == null) {
            return;
        }
        String[] values = multiValueString.split(",");
        for (String value : values) {
            if (!value.trim().isEmpty()) {
                collector.add(value.trim());
            }
        }
    }

    public void runBeforeRunningSuite( Callable<?> runnable ) {
        beforeRunningSuiteRunnable = runnable;
    }

    public void runAfterRunningSuite( AfterOperation operation ) {
        afterRunningSuiteRunnable = operation;
    }

    public void beforeRunningSuite() throws Exception {
        if (beforeRunningSuiteRunnable != null) {
            beforeRunningSuiteRunnable.call();
        }
    }

    public void afterRunningSuite( RepositoryFactory repositoryFactory,
                                   Repository repository ) throws Exception {
        if (afterRunningSuiteRunnable != null) {
            afterRunningSuiteRunnable.call(repositoryFactory, repository);
        }
    }

    public static interface AfterOperation {
        void call( RepositoryFactory repositoryFactory,
                   Repository repository ) throws Exception;
    }
}
