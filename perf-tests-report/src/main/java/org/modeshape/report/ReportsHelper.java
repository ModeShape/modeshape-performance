/*
 * JBoss, Home of Professional Open Source
 * Copyright [2012], Red Hat, Inc., and individual contributors
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.modeshape.jcr.perftests.OutputCfg;

/**
 * Helper class for dealing with report folder & file paths
 *
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
public final class ReportsHelper {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");

    private ReportsHelper() {
    }

    static File getReportFile(String reportName) throws IOException {
        String[] reportPathParts = reportName.split("/");

        File intermParent = getRootReportDir();
        for (int i = 0; i < reportPathParts.length - 1; i++) {
            File intermDir = new File(intermParent, reportPathParts[i]);
            if (!intermDir.exists()) {
                assert intermDir.mkdir();
            }
            intermParent = intermDir;
        }
        File reportFile = new File(intermParent, reportPathParts[reportPathParts.length - 1]);
        assert reportFile.createNewFile();
        return reportFile;
    }

    private static File getRootReportDir() {
        try {
            File reportBaseDir = OutputCfg.reportOutputFolder();
            String reportDirName = DATE_FORMAT.format(new Date());
            File reportDir = new File(reportBaseDir, reportDirName);
            if (!reportDir.exists()) {
                assert reportDir.mkdir();
            }
            return reportDir;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main( String[] args ) throws SigarException {
        Sigar sigar = new Sigar();
        CpuInfo[] cpuInfoList = sigar.getCpuInfoList();
        System.out.println(cpuInfoList[0].toString());
        System.out.println(sigar.getMem().toString());
    }

}
