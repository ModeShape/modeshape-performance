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
package org.modeshape.jcr.perftests.imprt;

import javax.jcr.*;
import org.modeshape.jcr.perftests.AbstractPerformanceTestSuite;
import org.modeshape.jcr.perftests.BigSet;
import org.modeshape.jcr.perftests.SuiteConfiguration;

/**
 * <code>ManyNodesImportTestSuite</code>
 * implements a performance test, which imports
 * repository with many nodes from external file.
 */
public class BigSetCloneTestSuite extends AbstractPerformanceTestSuite {

    private static final int NODE_COUNT = 10;
    private static String ROOT_NODE = "ROOT_NODE";

    private Session srcSession, dstSession;
    private int i =0;
    
    public BigSetCloneTestSuite( SuiteConfiguration suiteConfiguration ) {
        super(suiteConfiguration);
    }

    @Override
    public void beforeSuite() throws Exception {
        //create first session and pre-fill the repo
        srcSession = newSession();
        BigSet.fillRepository(srcSession, ROOT_NODE, NODE_COUNT, 4);
        srcSession.save();
        
        //create another workspace and session
        Workspace def = srcSession.getWorkspace();
        def.createWorkspace("test-clone");

        dstSession = srcSession.getRepository().login("test-clone");
        BigSet.cleanWorkspace(dstSession, ROOT_NODE);
        dstSession.save();
    }


    @Override
    public void runTest() throws Exception {
        dstSession.getWorkspace().clone(srcSession.getWorkspace().getName(), "/" + ROOT_NODE, "/" + ROOT_NODE, false);
        BigSet.cleanWorkspace(dstSession, ROOT_NODE);
    }

    @Override
    public void afterSuite() throws Exception {
        BigSet.cleanWorkspace(srcSession, ROOT_NODE);
        BigSet.cleanWorkspace(dstSession, ROOT_NODE);
        dstSession.logout();
        try {
            srcSession.getWorkspace().deleteWorkspace("test-clone");
        } catch (Exception e) {
            //may be not implemented
        }
        srcSession.logout();
    }

}
