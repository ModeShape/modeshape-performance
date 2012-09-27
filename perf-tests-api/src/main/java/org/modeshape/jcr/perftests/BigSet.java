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
package org.modeshape.jcr.perftests;

import javax.jcr.Node;
import javax.jcr.Session;

/**
 *
 * @author kulikov
 */
public class BigSet {
    private static int ID = 1;

    public static void fillRepository(Session session, String root,
            int amount, int levels) throws Exception {
        cleanWorkspace(session, root);
        Node node = session.getRootNode().addNode(root);
        createNodes(node, amount, 0, levels);
    }

    public static void cleanWorkspace(Session session, String root) throws Exception {
        if (session.itemExists("/" + root)) {
            Node node = (Node) session.getItem("/" + root);
            node.remove();
            session.save();
        }
    }

    private static void createNodes(Node node, int count, int level, int maxLevel) throws Exception {
        if (level == maxLevel) {
            return;
        }

        for (int i = 0; i < count; i++) {
            Node child = node.addNode("node " + (ID)++, "nt:unstructured");
            child.setProperty("Name", "Value");
            createNodes(child, count, level + 1, maxLevel);
        }
    }

}
