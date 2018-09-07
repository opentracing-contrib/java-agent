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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import io.opentracing.contrib.tracerresolver.TracerResolver;
import io.opentracing.util.GlobalTracerTestUtil;
import org.junit.Before;
import org.junit.Test;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.GlobalTracer;

import java.lang.reflect.Field;

public class OpenTracingHelperTest {
    private static void _clearHelperTracer() {
        try {
            Field tracerField = OpenTracingHelper.class.getDeclaredField("tracer");
            tracerField.setAccessible(true);
            tracerField.set(null, null);
            tracerField.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException("Error reflecting OpenTracingHelper: " + e.getMessage(), e);
        }
    }

    @Before
    public void clearTracers() {
        GlobalTracerTestUtil.resetGlobalTracer();
        _clearHelperTracer();
    }

    @Before
    public void reloadResolverProviderCaches() {
        TracerResolver.reload();
    }

    @Test
    public void testAssociateSpan() {
        OpenTracingHelper helper = new OpenTracingHelper(null);

        Object obj = new Object();
        Span span = new MockTracer().buildSpan("Test").start();

        helper.associateSpan(obj, span);

        assertEquals(span, helper.retrieveSpan(obj));
    }

    @Test
    public void testState() {
        OpenTracingHelper helper = new OpenTracingHelper(null);

        Object obj = new Object();

        helper.setState(obj, 5);

        assertEquals(5, helper.getState(obj));
    }

    @Test
    public void testGetTracerResolved() {
        OpenTracingHelper helper = new OpenTracingHelper(null);
        Tracer tracer = helper.getTracer();

        assertNotNull(tracer);

        try {
            tracer.buildSpan("Test");
        } catch (DummyTracer.DummyCalled e) {
            assertFalse(e.triggeringEnabled);
            return;
        }
        fail("DummyTracer did not initialize properly");
    }

    @Test
    public void testGetTracerWithRulesEnabled() {
        System.setProperty("io.opentracing.contrib.agent.allowInstrumentedTracer", "true");
        try {
            OpenTracingHelper helper = new OpenTracingHelper(null);
            Tracer tracer = helper.getTracer();

            assertNotNull(tracer);

            tracer.buildSpan("test2");
        } catch(DummyTracer.DummyCalled e) {
            assertTrue(e.triggeringEnabled);
            return;
        } finally {
            System.clearProperty("io.opentracing.contrib.agent.allowInstrumentedTracer");
        }
        fail("DummyTracer did not initialize properly");
    }

    @Test
    public void testGetTracerExisting() {
        GlobalTracer.register(new MockTracer());

        OpenTracingHelper helper = new OpenTracingHelper(null);
        Tracer tracer = helper.getTracer();

        assertNotNull(tracer);

        assertTrue(tracer.buildSpan("Test").start() instanceof MockSpan);
    }
}
