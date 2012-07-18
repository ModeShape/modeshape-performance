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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Class which uses a javascript box plot script (http://informationandvisualization.de/blog/box-plot) to generate a report for
 * each test.
 * 
 * @author Horia Chiorean
 */
public final class BoxPlotReport extends MultipleAggregatedReport {

    @Override
    protected Map<String, ?> getTemplateModel( String testName,
                                               Map<String, List<Double>> repositoryValuesMap,
                                               TimeUnit timeUnit ) {
        Map<String, Object> templateModel = new HashMap<String, Object>();
        templateModel.put("title", testName + "(" + timeUnit.toString().toLowerCase() + ")");
        templateModel.put("repositoryValuesMap", repositoryValuesMap);
        return templateModel;
    }

    @Override
    protected String getReportTemplate( String testName ) {
        return "boxplot/boxplot-template.ftl";
    }

    @Override
    protected String getReportFilename( String testName ) {
        return "boxplot/" + testName + ".html";
    }

    @Override
    protected String getIndexReportFilename() {
        return null;
    }

    @Override
    protected String getIndexReportTemplate() {
        return null;
    }
}
