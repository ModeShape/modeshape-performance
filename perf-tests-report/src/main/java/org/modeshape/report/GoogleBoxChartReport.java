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

import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.modeshape.jcr.perftests.StatisticalData;
import org.modeshape.jcr.perftests.util.DurationsConverter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Class which generates a report with box charts for each of the ran tests, comparing the different values for the different
 * repositories.
 *
 * @author Horia Chiorean
 */
public final class GoogleBoxChartReport {

    private static final Random RANDOM = new Random();
    private static final Configuration FREEMARKER_CONFIG;
    private static final String REPORT_NAME = "google-box-chart.html";
    private static final String TEMPLATE_NAME = "google-box-chart-template.ftl";

    private final TimeUnit timeUnit;

    static {
        FREEMARKER_CONFIG = new Configuration();
        FREEMARKER_CONFIG.setDefaultEncoding("UTF-8");
        FREEMARKER_CONFIG.setTemplateLoader(new URLTemplateLoader() {
            @Override
            protected URL getURL( String name ) {
                return this.getClass().getClassLoader().getResource(name);
            }
        });
        FREEMARKER_CONFIG.setWhitespaceStripping(true);
    }

    public GoogleBoxChartReport( TimeUnit timeUnit ) {
        this.timeUnit = timeUnit;
    }

    public void generateReport() throws Exception {
        Map<String, String> chartsMap = createChartsMap();
        generateReport(chartsMap);
    }

    private void generateReport( Map<String, String> chartsMap ) throws IOException, TemplateException {
        Template reportTemplate = FREEMARKER_CONFIG.getTemplate(TEMPLATE_NAME);
        Map<String, Object> templateMap = new HashMap<String, Object>();
        templateMap.put("chartsMap", chartsMap);
        templateMap.put("timeUnit", timeUnit);
        reportTemplate.process(templateMap, new PrintWriter(getReportFile()));
    }

    private Map<String, String> createChartsMap() throws Exception {
        Map<String, String> chartsMap = new HashMap<String, String>();

        Map<String, Map<String, List<Long>>> testDataMap = new ReportDataAggregator().loadPerformanceData();
        for (String testName : testDataMap.keySet()) {
            GoogleBoxChart chart = createChartForTest(testName, testDataMap.get(testName));
            chartsMap.put(testName, chart.toUrl());
        }
        return chartsMap;
    }

    private GoogleBoxChart createChartForTest( String testName, Map<String, List<Long>> repositoriesWithDurations ) {
        GoogleBoxChart boxChart = new GoogleBoxChart(400, 500, testName + "(" + timeUnit.toString().toLowerCase() + ")");
        for (String repositoryName : repositoriesWithDurations.keySet()) {
            List<Double> convertedDurations = DurationsConverter.convertFromNanos(repositoriesWithDurations.get(repositoryName), timeUnit);
            boxChart.addDataForRepository(repositoryName, convertedDurations);
        }
        boxChart.generateChartValues();
        return boxChart;
    }

    private File getReportFile() {
        return new File(getRootReportDir(), REPORT_NAME);
    }

    private File getRootReportDir() {
        try {
            File reportDir = new File(getClass().getClassLoader().getResource(".").toURI());
            if (!reportDir.exists() || !reportDir.isDirectory()) {
                throw new IllegalStateException("Cannot locate target folder for performance report");
            }
            return reportDir;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static String colorHex( Color color ) {
        return Integer.toHexString((color.getRGB() & 0xffffff) | 0x1000000).substring(1).toUpperCase();
    }

    private static String randomColorHex() {
        return colorHex(new Color(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256)));
    }

    private static class GoogleBoxChart {

        private static final Map<String, String> REPOSITORY_COLOR_MAP = new HashMap<String, String>();
        private static final String MULTI_VALUE_SEPARATOR = ",";
        private static final String SERIES_SEPARATOR = "|";
        private static final String CHART_BOUNDARY_GUARD = "-1";
        private static final String BAR_WIDTH = "30";

        private String size;
        private String title;
        private StringBuilder minSeries = new StringBuilder(CHART_BOUNDARY_GUARD).append(MULTI_VALUE_SEPARATOR);
        private StringBuilder lowerQuartileSeries = new StringBuilder(CHART_BOUNDARY_GUARD).append(MULTI_VALUE_SEPARATOR);
        private StringBuilder medianSeries = new StringBuilder(CHART_BOUNDARY_GUARD).append(MULTI_VALUE_SEPARATOR);
        private StringBuilder upperQuartileSeries = new StringBuilder(CHART_BOUNDARY_GUARD).append(MULTI_VALUE_SEPARATOR);
        private StringBuilder maxSeries = new StringBuilder(CHART_BOUNDARY_GUARD).append(MULTI_VALUE_SEPARATOR);
        private StringBuilder seriesDisplay = new StringBuilder();
        private StringBuilder labels = new StringBuilder();
        private StringBuilder labelColors = new StringBuilder();

        private Map<String, StatisticalData> repositoryData = new HashMap<String, StatisticalData>();

        GoogleBoxChart( int width, int height, String title ) {
            this.size = String.valueOf(width) + "x" + String.valueOf(height);
            this.title = title;
        }

        void addDataForRepository( String repositoryName, List<Double> values ) {
            repositoryData.put(repositoryName, new StatisticalData(values));
        }

        private void generateChartValues() {
            int seriesIndex = 1;
            for (Iterator<String> it = repositoryData.keySet().iterator(); it.hasNext(); ) {
                String repositoryName = it.next();
                StatisticalData statisticalData = repositoryData.get(repositoryName);

                appendSeriesValue(minSeries, statisticalData.min(), !it.hasNext());
                appendSeriesValue(lowerQuartileSeries, statisticalData.lowerQuartile(), !it.hasNext());
                appendSeriesValue(medianSeries, statisticalData.median(), !it.hasNext());
                appendSeriesValue(upperQuartileSeries, statisticalData.upperQuartile(), !it.hasNext());
                appendSeriesValue(maxSeries, statisticalData.max(), !it.hasNext());

                String repositoryColor = getRepositoryColor(repositoryName);

                appendMultipleValues(seriesDisplay, true, "F", repositoryColor, "0", String.valueOf(seriesIndex), BAR_WIDTH);

                labels.append(repositoryName).append(SERIES_SEPARATOR);
                labelColors.append(repositoryColor).append(MULTI_VALUE_SEPARATOR);

                seriesIndex++;
            }

            String seriesRange = "1:" + String.valueOf(repositoryData.size());
            String markerWidth = "1:" + BAR_WIDTH;

            String minColor = colorHex(Color.CYAN);
            appendMultipleValues(seriesDisplay, true, "H", minColor, "0", seriesRange, markerWidth);
            labels.append("Min").append(SERIES_SEPARATOR);
            labelColors.append(minColor).append(MULTI_VALUE_SEPARATOR);

            String maxColor = colorHex(Color.BLUE);
            appendMultipleValues(seriesDisplay, true, "H", maxColor, "3", seriesRange, markerWidth);
            labels.append("Max").append(SERIES_SEPARATOR);
            labelColors.append(maxColor).append(MULTI_VALUE_SEPARATOR);

            String medianColor = colorHex(Color.BLACK);
            appendMultipleValues(seriesDisplay, false, "H", medianColor, "4", seriesRange, markerWidth);
            labels.append("Median");
            labelColors.append(medianColor);
        }

        private String getRepositoryColor( String repositoryName ) {
            String repositoryColor = REPOSITORY_COLOR_MAP.get(repositoryName);
            if (repositoryColor == null) {
                repositoryColor = randomColorHex();
                REPOSITORY_COLOR_MAP.put(repositoryName, repositoryColor);
            }
            return repositoryColor;
        }

        private void appendSeriesValue( StringBuilder seriesBuilder, double value, boolean isLastValue ) {
            if (Double.NaN == value) {
                seriesBuilder.append("-1");
            } else {
                seriesBuilder.append(String.valueOf(value));
            }
            seriesBuilder.append(MULTI_VALUE_SEPARATOR);
            if (isLastValue) {
                seriesBuilder.append(CHART_BOUNDARY_GUARD);
            }
        }

        private void appendMultipleValues( StringBuilder holder, boolean hasNextSeries, String... values ) {
            for (int i = 0; i < values.length; i++) {
                holder.append(values[i]);
                if (i < values.length - 1) {
                    holder.append(MULTI_VALUE_SEPARATOR);
                }
            }
            if (hasNextSeries) {
                holder.append(SERIES_SEPARATOR);
            }
        }

        String toUrl() {
            StringBuilder urlBuilder = new StringBuilder("https://chart.googleapis.com/chart?");
            urlBuilder.append("chtt=").append(title);//chart title
            urlBuilder.append("&chs=").append(size);//chart size
            urlBuilder.append("&chds=a");//autoscale axis
            urlBuilder.append("&cht=ls");//don't show axis lines
            urlBuilder.append("&chd=t0:")
                    .append(minSeries).append(SERIES_SEPARATOR)
                    .append(upperQuartileSeries).append(SERIES_SEPARATOR)
                    .append(lowerQuartileSeries).append(SERIES_SEPARATOR)
                    .append(maxSeries).append(SERIES_SEPARATOR)
                    .append(medianSeries);//chart series
            urlBuilder.append("&chm=").append(seriesDisplay); //ui settings for each series (color, width etc)
            urlBuilder.append("&chxt=y"); //axes to display
            urlBuilder.append("&chdl=").append(labels); //labels
            urlBuilder.append("&chco=").append(labelColors); //label colors

            return urlBuilder.toString();
        }
    }
}
