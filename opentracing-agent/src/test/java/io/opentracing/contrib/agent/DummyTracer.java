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

import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import org.jboss.byteman.rule.Rule;

/**
 * This dummy tracer only exists to be service loaded for test purposes.
 *
 */
public class DummyTracer implements Tracer {
    /**
     * Whether or not the tracer was initialized with triggering enabled.
     */
    private boolean triggeringEnabled;

    public DummyTracer() {
        triggeringEnabled = Rule.isTriggeringEnabled();
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        // For test purposes, simply throw a specific exception to indicate that
        // this tracer was initialized, and whether instrumentation was enabled
        // when it was created.
        throw new DummyCalled(triggeringEnabled);
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

        public boolean triggeringEnabled;

        private DummyCalled(boolean triggeringEnabled) {
            this.triggeringEnabled = triggeringEnabled;
        }
    }

    @Override
    public Span activeSpan() {
        return null;
    }

    @Override
    public ScopeManager scopeManager() {
        return null;
    }
    
}
