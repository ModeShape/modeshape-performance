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
 * Base class for report generators which generate just 1 report for all the test classes.
 *
 * @author Horia Chiorean
 * @see GoogleBoxChartReport
 */
public abstract class SingleAggregatedReport {

    public void generate(TimeUnit timeUnit) throws Exception {
        Map<String, ?> templateModel = getTemplateModel(new ReportDataAggregator().loadPerformanceData(timeUnit), timeUnit);
        new FreemarkerTemplateProcessor(getReportFilename(), getTemplateFilename()).processTemplate(templateModel);
    }

    abstract String getReportFilename();
    abstract String getTemplateFilename();
    abstract Map<String, ?> getTemplateModel( Map<String, Map<String, List<Double>>> aggregateDataMap, TimeUnit timeUnit );
}
