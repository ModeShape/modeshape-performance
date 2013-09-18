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

import java.util.Map;
import org.modeshape.jcr.api.RepositoryFactory;
import org.modeshape.jcr.perftests.AbstractImplementationTest;

/**
 * Runs the performance tests against a Modeshape 3.x repo.
 */
public class ModeShapePerformanceTest extends AbstractImplementationTest {

    @Override
    protected void initializeRepositoryFactoryProperties( Map<String, Object> parameters ) {
        final String testConfig = runnerConfig.getProperty("configuration.file");
        parameters.put(RepositoryFactory.URL, getClass().getClassLoader().getResource(testConfig));
    }

    @Override
    protected void cleanupAfterRunningTest( javax.jcr.RepositoryFactory repositoryFactory,
                                            javax.jcr.Repository repository ) throws Exception {
        super.cleanupAfterRunningTest(repositoryFactory, repository);
        // We need to shutdown the ModeShape engine because in the same process
        // we re-create a new engine with the same file system location ...
        if (repositoryFactory instanceof RepositoryFactory) {
            ((RepositoryFactory)repositoryFactory).shutdown().get(); // wait until it is finished shutting down
        }
    }
}
