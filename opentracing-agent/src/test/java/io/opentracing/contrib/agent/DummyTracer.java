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

import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

/**
 * This dummy tracer only exists to be service loaded for test purposes.
 *
 */
public class DummyTracer implements Tracer {

    public DummyTracer() {
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        // For test purposes, simply through a specific exception to indicate that
        // this tracer was called.
        throw new DummyCalled();
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        return null;
    }

    public static class DummyCalled extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    @Override
    public ActiveSpan activeSpan() {
        return null;
    }

    @Override
    public ActiveSpan makeActive(Span arg0) {
        return null;
    }
    
}
