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

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Base class for report generators which generate a separate report for each test.
 *
 * @author Horia Chiorean
 * @see BoxPlotReport
 */
public abstract class MultipleAggregatedReport {
    public void generate(TimeUnit timeUnit) throws Exception {
        Map<String, Map<String, List<Double>>> convertedDataMap = new ReportDataAggregator().loadPerformanceData(timeUnit);

        for (String testName : convertedDataMap.keySet()) {
            String filename = getReportFilename(testName);
            String templateName = getReportTemplate(testName);
            Map<String, ?> templateModel = getTemplateModel(testName, convertedDataMap.get(testName), timeUnit);
            new FreemarkerTemplateProcessor(filename, templateName).processTemplate(templateModel);
        }
    }

    protected abstract Map<String, ?> getTemplateModel( String testName, Map<String, List<Double>> repositoryValuesMap, TimeUnit timeUnit );
    protected abstract String getReportTemplate( String testName );
    protected abstract String getReportFilename( String testName );
}
