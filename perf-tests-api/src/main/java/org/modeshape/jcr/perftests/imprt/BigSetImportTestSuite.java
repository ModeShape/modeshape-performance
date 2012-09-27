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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Session;
import org.modeshape.jcr.perftests.AbstractPerformanceTestSuite;
import org.modeshape.jcr.perftests.BigSet;
import org.modeshape.jcr.perftests.SuiteConfiguration;

/**
 * <code>ManyNodesImportTestSuite</code>
 * implements a performance test, which imports
 * repository with many nodes from external file.
 */
public class BigSetImportTestSuite extends AbstractPerformanceTestSuite {

    private static final int NODE_COUNT = 10;
    private static String ROOT_NODE_NAME= "start";

    private Session session;
    private File tempFile;

    public BigSetImportTestSuite( SuiteConfiguration suiteConfiguration ) {
        super(suiteConfiguration);
    }

    @Override
    public void beforeSuite() throws Exception {
        //prepare temp file
        tempFile = File.createTempFile("modeshape-test-repo", "xml");
        FileOutputStream fout = new FileOutputStream(tempFile);

        //create new session and fill the repo with dummy set of nodes
        session = newSession();
        BigSet.fillRepository(session, ROOT_NODE_NAME, NODE_COUNT, 4);
        
        //save session and export repository to external file
        session.save();
        session.exportSystemView("/", fout, false, false);

        //clean up repository
        BigSet.cleanWorkspace(session, ROOT_NODE_NAME);

        //close file
        fout.flush();
        fout.close();
    }

    @Override
    public void runTest() throws Exception {
        //import repository from external file
        FileInputStream fin = new FileInputStream(tempFile);
        session.importXML("/", fin, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
        
        //clean up after import or it can cause Out of memory exception
        BigSet.cleanWorkspace(session, ROOT_NODE_NAME);

        //close file
        fin.close();
    }

    @Override
    public void afterSuite() throws Exception {
        //delete temp file if it was created
        if (tempFile != null) {
            tempFile.delete();
        }

        //save changes (repo must be empty now) and logout
        session.save();
        session.logout();
    }

}
