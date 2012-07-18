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

import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 * Test which invokes the {@link GoogleBoxChartReport} and the {@link BoxPlotReport} class in order to generate some box charts
 * with the comparative test data for each repository.
 * 
 * @author Horia Chiorean
 */
public class AggregatedReportTest {

    @Test
    public void generateReports() throws Exception {
        // new GoogleBoxChartReport().generate(TimeUnit.MILLISECONDS);
        // new BoxPlotReport().generate(TimeUnit.MILLISECONDS);
        new D3BoxPlotReport().generate(TimeUnit.MILLISECONDS);
    }
}
