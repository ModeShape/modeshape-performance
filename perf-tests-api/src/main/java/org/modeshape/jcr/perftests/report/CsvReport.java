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

package org.modeshape.jcr.perftests.report;

import org.modeshape.jcr.perftests.TestData;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Properties;

/**
 * Class which generates a csv file with [testName=value1,value2,value3...] entries.
 *
 * @author Horia Chiorean
 */
public final class CsvReport extends TestReportGenerator {

    public static final String REPOSITORY_PROPERTY = "Repository";
    public static final String REPORT_PARENT_DIR = "org.modeshape.report";

    @Override
    public void generateReport( TestData testData ) throws Exception {
        Properties performanceProperties = new Properties();
        performanceProperties.put(REPOSITORY_PROPERTY, testData.getRepositoryName());
        for (String testName : testData.getSuccessfulTestNames()) {
            performanceProperties.setProperty(testName, getTestPerfData(testData, testName));
        }
        performanceProperties.store(new FileWriter(getReportFile(testData.getRepositoryName())), null);
    }

    private File getReportFile(String repositoryName) {
        File rootDir = getRootReportDir();
        File reportDir = new File(rootDir, REPORT_PARENT_DIR.replaceAll("\\.", "/"));
        if (!reportDir.exists()) {
            if (!reportDir.mkdir()) {
                throw new IllegalStateException("Cannot create report directory");
            }
        }
        String fileName = repositoryName.toLowerCase().replaceAll(" ", "-") + ".csv";
        return new File(reportDir, fileName);
    }

    private String getTestPerfData( TestData testData, String testName ) {
        StringBuilder outputBuilder = new StringBuilder();
        for (Iterator<Long> it = testData.getTestDurationsNanos(testName).iterator(); it.hasNext();) {
            outputBuilder.append(it.next().toString());
            if (it.hasNext()) {
                outputBuilder.append(",");
            }
        }
        return outputBuilder.toString();
    }
}
