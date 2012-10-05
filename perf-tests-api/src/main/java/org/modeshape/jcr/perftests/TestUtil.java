/*
 * ModeShape (http://www.modeshape.org)
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * See the AUTHORS.txt file in the distribution for a full listing of 
 * individual contributors.
 *
 * ModeShape is free software. Unless otherwise indicated, all code in ModeShape
 * is licensed to you under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * ModeShape is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.modeshape.jcr.perftests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 */
public class TestUtil {

    /**
     * Delete the file or directory given by the supplied reference. This method works on a directory that is not empty, unlike
     * the {@link File#delete()} method.
     * 
     * @param fileOrDirectory the reference to the Java File object that is to be deleted
     * @return true if the supplied file or directory existed and was successfully deleted, or false otherwise
     */
    public static boolean delete( File fileOrDirectory ) {
        if (fileOrDirectory == null) return false;
        if (!fileOrDirectory.exists()) return false;

        // The file/directory exists, so if a directory delete all of the contents ...
        if (fileOrDirectory.isDirectory()) {
            for (File childFile : fileOrDirectory.listFiles()) {
                delete(childFile); // recursive call (good enough for now until we need something better)
            }
            // Now an empty directory ...
        }
        // Whether this is a file or empty directory, just delete it ...
        return fileOrDirectory.delete();
    }

    /**
     * Write the entire contents of the supplied string to the given stream. This method always flushes and closes the stream when
     * finished.
     * 
     * @param input the content to write to the stream; may be null
     * @param stream the stream to which the content is to be written
     * @throws IOException
     * @throws IllegalArgumentException if the stream is null
     */
    public static void write( InputStream input,
                              OutputStream stream ) throws IOException {
        write(input, stream, 1024);
    }

    /**
     * Write the entire contents of the supplied string to the given stream. This method always flushes and closes the stream when
     * finished.
     * 
     * @param input the content to write to the stream; may be null
     * @param stream the stream to which the content is to be written
     * @param bufferSize the size of the buffer; must be positive
     * @throws IOException
     * @throws IllegalArgumentException if the stream is null
     */
    public static void write( InputStream input,
                              OutputStream stream,
                              int bufferSize ) throws IOException {
        assert stream != null;
        assert input != null;
        boolean error = false;
        try {
            if (input != null) {
                byte[] buffer = new byte[bufferSize];
                try {
                    int numRead = 0;
                    while ((numRead = input.read(buffer)) > -1) {
                        stream.write(buffer, 0, numRead);
                    }
                } finally {
                    input.close();
                }
            }
        } catch (IOException e) {
            error = true; // this error should be thrown, even if there is an error flushing/closing stream
            throw e;
        } catch (RuntimeException e) {
            error = true; // this error should be thrown, even if there is an error flushing/closing stream
            throw e;
        } finally {
            try {
                stream.flush();
            } catch (IOException e) {
                if (!error) throw e;
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    if (!error) throw e;
                }
            }
        }
    }
}
