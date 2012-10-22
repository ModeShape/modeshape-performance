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

import org.modeshape.common.util.StringUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Base class for report generators which generates a separate report for each test.
 * 
 * @author Horia Chiorean
 * @see BoxPlotReport
 */
public abstract class MultipleAggregatedReport {

    private static final MachineInfo MACHINE_INFO = new MachineInfo();

    public void generate( TimeUnit timeUnit ) throws Exception {
        Map<String, Map<String, List<Double>>> convertedDataMap = new ReportDataAggregator().loadPerformanceData(timeUnit);
        File reportRootDir = ReportsHelper.getRootReportDir();

        for (String testName : convertedDataMap.keySet()) {
            String filename = getReportFilename(testName);
            File reportFile = ReportsHelper.getReportFile(reportRootDir, filename);

            String templateName = getReportTemplate(testName);
            Map<String, Object> templateModel = getTemplateModel(testName, convertedDataMap.get(testName), timeUnit);
            addMachineInformation(templateModel);
            new FreemarkerTemplateProcessor(reportFile, templateName).processTemplate(templateModel);
        }

        String indexFilename = getIndexReportFilename();
        if (indexFilename != null) {
            // Generate the index report ...
            File reportIndexFile = ReportsHelper.getReportFile(reportRootDir, indexFilename);

            String indexTemplateName = getIndexReportTemplate();
            Map<String, Object> indexModel = new HashMap<String, Object>();
            indexModel.put("reportsMap", convertedDataMap);
            new FreemarkerTemplateProcessor(reportIndexFile, indexTemplateName).processTemplate(indexModel);
        }
    }

    protected String getWorkingDir() {
        String resourcesDir = getClass().getClassLoader().getResource(".").toString();
        if (resourcesDir.endsWith("/")) {
            resourcesDir = resourcesDir.substring(0, resourcesDir.lastIndexOf("/"));
        }
        return resourcesDir;
    }

    protected void addMachineInformation(Map<String, Object> templateModel) {
        List<String> machineInfo = new ArrayList<String>();
        machineInfo.add(MACHINE_INFO.jvmInformation());
        machineInfo.add(MACHINE_INFO.osInformation());

        String cpuInformation = MACHINE_INFO.cpuInformation();
        if (!StringUtil.isBlank(cpuInformation)) {
            machineInfo.add(cpuInformation);
        }

        String memoryInformation = MACHINE_INFO.memoryInformation();
        if (!StringUtil.isBlank(memoryInformation)) {
            machineInfo.add(memoryInformation);
        }

        templateModel.put("machineInfo", machineInfo);
    }

    protected abstract Map<String, Object> getTemplateModel( String testName,
                                                        Map<String, List<Double>> repositoryValuesMap,
                                                        TimeUnit timeUnit );

    protected abstract String getReportTemplate( String testName );

    protected abstract String getReportFilename( String testName );

    protected abstract String getIndexReportFilename();

    protected abstract String getIndexReportTemplate();

}
