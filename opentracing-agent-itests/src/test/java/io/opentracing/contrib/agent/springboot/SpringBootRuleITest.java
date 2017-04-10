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
package io.opentracing.contrib.agent.springboot;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.springframework.boot.loader.LaunchedURLClassLoader;

/**
 * @author gbrown
 */
public class SpringBootRuleITest {

    @Test
    public void testFindOpenTracingAgentRules() throws Exception {
        File f = new File(System.getProperty("rules.path"));
        URL[] urls = new URL[1];
        urls[0] = f.toURI().toURL();

        try (LaunchedURLClassLoader loader = new LaunchedURLClassLoader(urls,
                ClassLoader.getSystemClassLoader())) {

            assertNotNull(loader.getResource("test.rule"));
        }
    }

}
