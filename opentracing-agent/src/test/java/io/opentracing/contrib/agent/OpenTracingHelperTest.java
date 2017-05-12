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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.opentracing.NoopTracerFactory;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.GlobalTracer;

public class OpenTracingHelperTest {

    // Approach used in opentracing-util GlobalTracerTest to reset the global tracer
    private static void _setGlobal(Tracer tracer) {
        try {
            Field globalTracerField = GlobalTracer.class.getDeclaredField("tracer");
            globalTracerField.setAccessible(true);
            globalTracerField.set(null, tracer);
            globalTracerField.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException("Error reflecting globalTracer: " + e.getMessage(), e);
        }
    }

    @Before
    @After
    public void clearGlobalTracer() {
        _setGlobal(NoopTracerFactory.create());
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

    @Test(expected=DummyTracer.DummyCalled.class)
    public void testGetTracerResolved() {
        OpenTracingHelper helper = new OpenTracingHelper(null);
        Tracer tracer = helper.getTracer();

        assertNotNull(tracer);

        tracer.buildSpan("Test");
    }

    @Test
    public void testGetTracerExisting() {
        GlobalTracer.register(new MockTracer());

        OpenTracingHelper helper = new OpenTracingHelper(null);
        Tracer tracer = helper.getTracer();

        assertNotNull(tracer);

        assertTrue(tracer.buildSpan("Test").startManual() instanceof MockSpan);
    }

}
