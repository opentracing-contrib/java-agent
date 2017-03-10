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
package io.opentracing.contrib.agent.custom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import io.opentracing.contrib.agent.common.OTAgentTestBase;
import io.opentracing.mock.MockSpan;

/**
 * @author gbrown
 */
public class CustomRuleITest extends OTAgentTestBase {

    @Test
    public void testRequest() throws Exception {
        sayHello(false);
        
        List<MockSpan> spans = getTracer().finishedSpans();
        
        // Check that a Span was created with operation name
        // 'TestSpan' and a tag recording the status code.
        assertEquals(1, spans.size());
        assertEquals("TestSpan", spans.get(0).operationName());
        assertEquals("OK", spans.get(0).tags().get("status.code"));
    }

    @Test
    public void testException() throws Exception {
        try {
            sayHello(true);
            fail("Exception expected");
        } catch (Exception e) {
            // Expected
        }

        List<MockSpan> spans = getTracer().finishedSpans();

        // Check that a Span was created with operation name
        // 'TestSpan' and tags recording the status code and error.
        assertEquals(1, spans.size());
        assertEquals("TestSpan", spans.get(0).operationName());
        assertEquals("FAILED", spans.get(0).tags().get("status.code"));
        assertTrue((boolean)spans.get(0).tags().get("error"));
    }

    /**
     * When this method is invoked, the ByteMan rule defined in
     * src/test/resources/otagent/custom.btm will cause a 'TestSpan' span
     * to be started on entry to the method, and then on exit
     * from the method, a tag will be added to the span recording
     * the 'status.code' of "OK" before the span is finished.
     */
    public void sayHello(boolean fail) throws Exception {
        if (fail) {
            throw new Exception("Failed to say hello");
        }
    }

}
