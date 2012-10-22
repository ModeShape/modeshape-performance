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

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Class which uses the Sigar library to provided additional machine (hardware) information for each report
 *
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
public final class MachineInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(MachineInfo.class);

    private static final String JAVA_VENDOR = System.getProperty("java.vendor");
    private static final String JAVA_VERSION = System.getProperty("java.version");
    private static final String OS_NAME = System.getProperty("os.name");
    private static final String OS_VERSION = System.getProperty("os.version");
    private static final String OS_ARCH = System.getProperty("os.arch");

    private static CpuInfo cpuInfo;
    private static Mem memoryInfo;

    static {
        try {
            Sigar sigar = new Sigar();
            CpuInfo[] cpuInfoList = sigar.getCpuInfoList();
            if (cpuInfoList.length > 0) {
                cpuInfo = cpuInfoList[0];
            }

            memoryInfo = sigar.getMem();
        } catch (Exception e) {
            LOGGER.warn("Unable to initialize Sigar library. Machine information will not be available", e);
        }
    }

    String jvmInformation() {
        return spaceSeparated(JAVA_VENDOR, JAVA_VERSION);
    }

    String osInformation() {
        return spaceSeparated(OS_NAME, OS_VERSION, OS_ARCH);
    }

    String cpuInformation() {
        return cpuInfo != null ? cpuInfo.getModel() : "";
    }

    String memoryInformation() {
        return memoryInfo != null ? spaceSeparated(String.valueOf(memoryInfo.getRam()), "MB RAM") : "";
    }

    private String spaceSeparated(String...parts) {
        StringBuilder builder = new StringBuilder();
        for (Iterator<String> partsIt = Arrays.asList(parts).iterator(); partsIt.hasNext(); ) {
            builder.append(partsIt.next());
            if (partsIt.hasNext()) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }
}
