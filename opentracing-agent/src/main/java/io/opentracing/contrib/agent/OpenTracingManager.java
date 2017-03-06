/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.opentracing.contrib.agent;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.byteman.agent.Retransformer;

/**
 * This class provides the ByteMan manager implementation for OpenTracing.
 *
 */
public class OpenTracingManager {

    private static Logger log = Logger.getLogger(OpenTracingManager.class.getName());

    private static final String RULE_FILE_EXTENSION = ".btm";
    private static final String ROOT_RULE_FOLDER = "otagent";
    private static final String PATH_JAR_SEPARATOR = "!/";
    private static final String FILE_SCHEME = "file:";

    private static Retransformer transformer;

    /**
     * This method initializes the manager.
     *
     * @param trans The ByteMan retransformer
     * @throws Exception
     */
    public static void initialize(Retransformer trans) throws Exception {
        transformer = trans;

        List<String> scripts = new ArrayList<>();
        List<String> scriptNames = new ArrayList<>();

        // Load default and custom rules
        Enumeration<URL> iter = ClassLoader.getSystemResources(ROOT_RULE_FOLDER);
        while (iter.hasMoreElements()) {
            loadRules(iter.nextElement().toURI(), scriptNames, scripts);
        }

        try (PrintWriter writer = new PrintWriter(new StringWriter())) {
            try {
                transformer.installScript(scripts, scriptNames, writer);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Failed to install scripts", e);
            }
        }
        
        if (log.isLoggable(Level.FINE)) {
            log.fine("OpenTracing Agent rules loaded");
        }
    }

    private static void loadRules(URI uri, final List<String> scriptNames,
                    final List<String> scripts) throws IOException {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Load rules from URI = " + uri);
        }

        FileSystem fs = null;
        String entryName = uri.toString();

        int separator = entryName.indexOf(PATH_JAR_SEPARATOR);
        if (separator != -1) {
            fs = FileSystems.newFileSystem(URI.create(entryName.substring(0, separator)),
                    Collections.<String, Object>emptyMap());
            entryName = entryName.substring(separator + PATH_JAR_SEPARATOR.length());
        } else if (entryName.startsWith(FILE_SCHEME)) {
            fs = FileSystems.getFileSystem(URI.create(FILE_SCHEME + File.separator));
            entryName = entryName.substring(FILE_SCHEME.length());
        }

        if (log.isLoggable(Level.FINE)) {
            log.fine("Filesystem = " + fs + " EntryName = " + entryName);
        }

        if (fs != null) {
            try {
                Path rules = fs.getPath(entryName);

                Files.walkFileTree(rules, new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes arg1) throws IOException {
                        if (path.toString().endsWith(RULE_FILE_EXTENSION)) {
                            try {
                                if (log.isLoggable(Level.FINE)) {
                                    log.fine("Loading rules: " + path.toString());
                                }
                                scripts.add(new String(Files.readAllBytes(path)));
                                scriptNames.add(path.toString());
                            } catch (IOException ioe) {
                                log.log(Level.SEVERE, "Failed to load rule file: " + path.toString(), ioe);
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });

            } finally {
                // If created filesystem, then we need to close it
                if (separator != -1) {
                    fs.close();
                }
            }
        }
    }

}
