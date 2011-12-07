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

import org.junit.Test;
import java.util.concurrent.TimeUnit;

/**
 * Test which invokes the {@link GoogleBoxChartReport} class in order to generate an aggregated report of all the tests.
 *
 * @author Horia Chiorean
 */
public class AggregatedReportTest {

    @Test
    public void generateReport() throws Exception {
        new GoogleBoxChartReport(TimeUnit.MILLISECONDS).generateReport();
    }
}
