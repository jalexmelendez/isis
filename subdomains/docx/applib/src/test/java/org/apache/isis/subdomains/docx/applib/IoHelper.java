/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.subdomains.docx.applib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class IoHelper {

    private final Class<?> baseClass;

    public byte[] asBytes(String fileName) throws IOException {
        final ByteArrayOutputStream baos = asBaos(fileName);
        return baos.toByteArray();
    }

    public byte[] asBytes(File file) throws IOException {
        final FileInputStream fis = new FileInputStream(file);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteStreams.copy(fis, baos);
        return baos.toByteArray();
    }


    public ByteArrayOutputStream asBaos(String fileName) throws IOException {
        final ByteArrayInputStream bais = openInputStream(fileName);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteStreams.copy(bais, baos);
        return baos;
    }

    public String readFileAsString(final String fileName) throws IOException {
        final URL fileUrl = asUrl(fileName);
        return Resources.toString(fileUrl, Charset.forName("UTF-8"));
    }

    public ByteArrayInputStream openInputStream(final String fileName) throws IOException {
        final URL fileUrl = asUrl(fileName);
        return new ByteArrayInputStream(Resources.toByteArray(fileUrl));
    }

    public URL asUrl(final String fileName) {
        return Resources.getResource(baseClass, fileName);
    }

    public File asFile(String fileName) {
        return toFile(asUrl(fileName));
    }

    public File asFileInSameDir(final String existingFileName, String newFile) {
        final File existingFile = asFile(existingFileName);
        return asFileInSameDir(existingFile, newFile);
    }

    public File asFileInSameDir(File existingFile, String newFile) {
        final File dir = existingFile.getParentFile();
        return new File(dir, newFile);
    }


    public void write(byte[] bytes, File file) throws IOException {
        final FileOutputStream targetFos = new FileOutputStream(file);
        ByteStreams.copy(new ByteArrayInputStream(bytes), targetFos);
    }


    static File toFile(URL url) {
        File file;
        String path;

        try {
            path = url.toURI().getSchemeSpecificPart();
            if ((file = new File(path)).exists()) return file;
        } catch (URISyntaxException e) {
        }

        try {
            path = url.toExternalForm();
            if (path.startsWith("file:")) path = path.substring("file:".length());
            if ((file = new File(path)).exists()) return file;

        } catch (Exception e) {
        }

        return null;
    }
}
