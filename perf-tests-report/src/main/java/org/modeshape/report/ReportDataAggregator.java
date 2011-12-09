/*
 * JBoss, Home of Professional Open Source
 * Copyright [2011], Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.modeshape.report;

import org.modeshape.jcr.perftests.report.CsvReport;
import org.modeshape.jcr.perftests.util.DurationsConverter;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Class which loads all the .csv files from the classpath which are expected to contain performance reports.
 *
 * @author Horia Chiorean
 */
public final class ReportDataAggregator {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReportDataAggregator.class);

    /**
     * Loads all the performance data by scanning the classpath for csv files under the {@link CsvReport#REPORT_PARENT_DIR} location.
     * @return a map of the form - [test, [repository name, (duration ns 1, duration ns 2...)]]
     *
     *@param convertToUnit the unit to which the performance data should be converted
     * @throws Exception if anything fails
     */
     Map<String, Map<String, List<Double>>> loadPerformanceData(TimeUnit convertToUnit) throws Exception {
        Map<String, Map<String, List<Long>>> testToRepositoryDurationsMap = new TreeMap<String, Map<String, List<Long>>>();
        ConfigurationBuilder builder = new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(CsvReport.REPORT_PARENT_DIR))
                .setScanners(new ResourcesScanner())
                .useParallelExecutor();
        Reflections reflections = new Reflections(builder);
        Set<String> reportFiles = reflections.getResources(new FilterBuilder().include(".*\\.csv"));

        for (String reportFileName : reportFiles) {
            processReport(reportFileName, testToRepositoryDurationsMap);
        }

        return convertToTimeUnit(testToRepositoryDurationsMap, convertToUnit);
    }

    private void processReport( String reportFileName, Map<String, Map<String, List<Long>>> testToRepositoryDurationsMap ) throws IOException {
        Properties reportProperties = new Properties();
        reportProperties.load(getClass().getClassLoader().getResourceAsStream(reportFileName));
        String repositoryName = reportProperties.getProperty(CsvReport.REPOSITORY_PROPERTY);
        if (repositoryName == null) {
            LOGGER.warn(reportFileName + " is not a valid test data file. Ignoring it");
            return;
        }

        for (String test : reportProperties.stringPropertyNames()) {
            String durationsString = reportProperties.getProperty(test);
            loadDataForTest(durationsString, repositoryName, test, testToRepositoryDurationsMap);
        }
    }

    private void loadDataForTest( String durationsString, String repositoryName, String test,
                                  Map<String, Map<String, List<Long>>> testToRepositoryDurationsMap ) {
        if (test.equals(CsvReport.REPOSITORY_PROPERTY)) {
            return;
        }
        Map<String, List<Long>> repositoryDurationsMap = testToRepositoryDurationsMap.get(test);
        if (repositoryDurationsMap == null) {
            repositoryDurationsMap = new TreeMap<String, List<Long>>();
            testToRepositoryDurationsMap.put(test, repositoryDurationsMap);
        }

        List<Long> durations = repositoryDurationsMap.get(repositoryName);
        if (durations == null) {
            durations = new ArrayList<Long>();
            repositoryDurationsMap.put(repositoryName, durations);
        }

        for (String duration : durationsString.split(",")) {
            durations.add(Long.valueOf(duration));
        }
    }

    private Map<String, Map<String, List<Double>>> convertToTimeUnit( Map<String, Map<String, List<Long>>> aggregateDataMap, TimeUnit timeUnit ) {
        Map<String, Map<String, List<Double>>> convertedMap = new TreeMap<String, Map<String, List<Double>>>();
        for (String testName : aggregateDataMap.keySet()) {
            Map<String, List<Long>> repositoriesValuesMap = aggregateDataMap.get(testName);
            Map<String, List<Double>> convertedRepositoriesValuesMap = new TreeMap<String, List<Double>>();
            for (String repositoryName : repositoriesValuesMap.keySet()) {
                List<Double> convertedDurations = DurationsConverter.convertFromNanos(repositoriesValuesMap.get(repositoryName), timeUnit);
                convertedRepositoriesValuesMap.put(repositoryName, convertedDurations);
            }
            convertedMap.put(testName, convertedRepositoriesValuesMap);
        }
        return convertedMap;
    }
}
