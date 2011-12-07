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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

/**
 * Class which uses Freemarker (http://fmpp.sourceforge.net/) to process a given template into an output file.
 *
 * @author Horia Chiorean
 */
final class FreemarkerTemplateProcessor {

    private static final Configuration FREEMARKER_CONFIG;

    private final String reportName;
    private final String templateName;

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

    FreemarkerTemplateProcessor( String reportName, String templateName ) {
        this.reportName = reportName;
        this.templateName = templateName;
    }

    void processTemplate( Map<String, ?> templateModel ) throws IOException, TemplateException {
        Template reportTemplate = FREEMARKER_CONFIG.getTemplate(templateName);
        reportTemplate.process(templateModel, new PrintWriter(getReportFile()));
    }

    private File getReportFile() {
        return new File(getRootReportDir(), reportName);
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
}
