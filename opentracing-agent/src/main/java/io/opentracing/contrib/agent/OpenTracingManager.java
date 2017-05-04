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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.jboss.byteman.agent.Retransformer;

/**
 * This class provides the ByteMan manager implementation for OpenTracing.
 *
 */
public class OpenTracingManager {

    private static final String AGENT_RULES = "otarules.btm";

    private static Retransformer transformer;

    /**
     * This method initializes the manager.
     *
     * @param trans The ByteMan retransformer
     * @throws Exception
     */
    public static void initialize(Retransformer trans) throws Exception {
        transformer = trans;

        loadRules(ClassLoader.getSystemClassLoader());
    }

    /**
     * This method loads any OpenTracing Agent rules (otarules.btm) found as resources
     * within the supplied classloader.
     *
     * @param classLoader The classloader
     */
    public static void loadRules(ClassLoader classLoader) {
        if (transformer == null) {
            AgentLogger.error("Attempt to load OpenTracing agent rules before transformer initialized");
            return;
        }

        List<String> scripts = new ArrayList<>();
        List<String> scriptNames = new ArrayList<>();

        // Load default and custom rules
        try {
            Enumeration<URL> iter = classLoader.getResources(AGENT_RULES);
            while (iter.hasMoreElements()) {
                loadRules(iter.nextElement().toURI(), scriptNames, scripts);
            }

            StringWriter sw=new StringWriter();
            try (PrintWriter writer = new PrintWriter(sw)) {
                try {
                    transformer.installScript(scripts, scriptNames, writer);
                } catch (Exception e) {
                    AgentLogger.error("Failed to install scripts: " + e);
                }
            }

            AgentLogger.debug(sw.toString());

        } catch (IOException | URISyntaxException e) {
            AgentLogger.error("Failed to load OpenTracing agent rules: " + e);
        }
        
        AgentLogger.debug("OpenTracing Agent rules loaded");
    }

    private static void loadRules(URI uri, final List<String> scriptNames,
                    final List<String> scripts) throws IOException {
        AgentLogger.debug("Load rules from URI = " + uri);

        StringBuilder str=new StringBuilder();
        try (InputStream is = uri.toURL().openStream()) {
            byte[] b = new byte[10240];
            int len;
            while ((len = is.read(b)) != -1) {
                str.append(new String(b, 0, len));
            }
        }
        scripts.add(str.toString());
        scriptNames.add(uri.toString());
    }

}
