/*
 * JBoss, Home of Professional Open Source
 * Copyright [2011], Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.modeshape;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import org.apache.jackrabbit.commons.JcrUtils;
import org.modeshape.jcr.perftests.AbstractImplementationTest;
import org.modeshape.jcr.perftests.TestUtil;

/**
 * Test which runs the performance suite against a Jackrabbit in memory repository.
 */
public class JRPerformanceTest extends AbstractImplementationTest {

    /**
     * Before each test, we need to copy the correct configuration file into the test directory where all of the repository data
     * will be stored. Jackrabbit expects the "repository.xml" file to be in this directory.
     */
    @Override
    protected void initializeBeforeRunningTest() throws Exception {
        super.initializeBeforeRunningTest();

        // Write the repository.xml file into the (one) test directory ...
        final File testDir = getTestDirectory();
        final String testConfig = runnerConfig.getProperty("configuration.file");

        InputStream stream = getClass().getClassLoader().getResourceAsStream(testConfig);
        OutputStream configStream = new FileOutputStream(new File(testDir, "repository.xml"));
        TestUtil.write(stream, configStream);
    }

    /**
     * Jackrabbit requires a single URI parameter that points to the directory where the repository is persisted, and in which
     * should appear the "repository.xml" configuration file.
     */
    @Override
    protected void initializeRepositoryFactoryProperties( Map<String, Object> parameters ) throws Exception {
        final File testDir = getTestDirectory();
        parameters.put(JcrUtils.REPOSITORY_URI, testDir.toURI().toURL());
    }

    /**
     * Utility method to get the first test directory, which is where we'll put all the repository data.
     * 
     * @return the directory object; never null
     */
    protected File getTestDirectory() {
        File testDir = testDirectories.get(0);
        assert testDir != null;
        // Note the directory may not exist yet, since it is created every time the tests are run while this method
        // might be called *before* any of the tests are actually run
        return testDir;
    }

}
