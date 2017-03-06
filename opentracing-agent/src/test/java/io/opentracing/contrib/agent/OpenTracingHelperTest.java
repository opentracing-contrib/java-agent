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

import static org.junit.Assert.*;

import org.junit.Test;

import io.opentracing.Span;
import io.opentracing.mock.MockTracer;

public class OpenTracingHelperTest {

    @Test
    public void testActivateDeactivateSpan() {
        OpenTracingHelper helper = new OpenTracingHelper(null);

        Span span = new MockTracer().buildSpan("Test").start();

        assertNull(helper.currentSpan());

        helper.activateSpan(span);

        assertEquals(span, helper.currentSpan());

        helper.deactivateCurrentSpan();

        assertNull(helper.currentSpan());
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
}
