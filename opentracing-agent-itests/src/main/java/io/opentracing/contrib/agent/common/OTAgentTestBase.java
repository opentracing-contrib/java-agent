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
package io.opentracing.contrib.agent.common;

import org.junit.Before;
import org.junit.BeforeClass;
import io.opentracing.contrib.global.GlobalTracer;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;

/**
 * @author gbrown
 */
public class OTAgentTestBase {

    private static MockTracer tracer = new MockTracer(Propagator.TEXT_MAP);

    @BeforeClass
    public static void initClass() throws Exception {
        GlobalTracer.register(tracer);
    }

    @Before
    public void init() {
        tracer.reset();
    }

    public static MockTracer getTracer() {
        return tracer;
    }

}
