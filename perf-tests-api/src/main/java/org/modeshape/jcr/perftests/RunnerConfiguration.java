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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holder for the configuration options of a <code>PerformanceTestSuiteRunner</code>. Normally this class holds the values read
 * from a configuration file. (e.g. runner.properties)
 * 
 * @author Horia Chiorean
 */
public final class RunnerConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(RunnerConfiguration.class);

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

    int repeatCount = DEFAULT_REPEAT_COUNT;
    int warmupCount = DEFAULT_WARMUP_COUNT;

    RunnerConfiguration( String fileName ) {
        try {
            Properties configParams = new Properties();
            configParams.load(getClass().getClassLoader().getResourceAsStream(fileName));
            initRunner(configParams);
        } catch (IOException e) {
            LOGGER.warn("Cannot load config file. Will use defaults ", e);
        }
    }

    /**
     * Creates a new instance by reading the {@link RunnerConfiguration#DEFAULT_CONFIG_FILE}
     */
    public RunnerConfiguration() {
        this(DEFAULT_CONFIG_FILE);
    }

    /**
     * Adds regexp patterns to exclude tests from running.
     * 
     * @param excludeTestsRegExp the regular expressions defining the tests that should be excluded
     * @return this runner configuration (for method chaining purposes)
     */
    public RunnerConfiguration addTestsToExclude( String... excludeTestsRegExp ) {
        this.excludeTestsRegExp.addAll(Arrays.asList(excludeTestsRegExp));
        return this;
    }

    /**
     * Adds regexp patterns to include tests to run.
     * 
     * @param includeTestsRegExp the regular expressions defining the tests that should be included
     * @return this runner configuration (for method chaining purposes)
     */
    public RunnerConfiguration addTestsToInclude( String... includeTestsRegExp ) {
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
    public RunnerConfiguration setRepeatCount( int repeatCount ) {
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
    public RunnerConfiguration setWarmupCount( int warmupCount ) {
        this.warmupCount = warmupCount;
        return this;
    }

    private void initRunner( Properties configParams ) {
        parseMultiValuedString(configParams.getProperty("tests.exclude"), excludeTestsRegExp);
        parseMultiValuedString(configParams.getProperty("tests.include"), includeTestsRegExp);
        repeatCount = Integer.valueOf(configParams.getProperty("repeat.count"));
        warmupCount = Integer.valueOf(configParams.getProperty("warmup.count"));
    }

    private void parseMultiValuedString( String multiValueString,
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
}
