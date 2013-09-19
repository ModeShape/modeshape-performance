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
package org.modeshape.jcr.perftests.output;

import org.modeshape.jcr.perftests.TestData;

/**
 * Base class which should be extended by classes which generate test reports based on a
 * {@link org.modeshape.jcr.perftests.TestData} object
 *
 * @author Horia Chiorean
 */
public abstract class TestDataOutput {

    /**
     * Generates a report based on the provided test data
     *
     * @param testData a <code>TestData</code> instance which must be non-null.
     * @throws Exception if anything goes wrong during the report generation.
     */
    public abstract void generateOutput( TestData testData ) throws Exception;
}
