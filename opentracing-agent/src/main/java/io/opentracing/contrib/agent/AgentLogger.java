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

/**
 * This class is responsible for logging messages.
 *
 */
class AgentLogger {

    // NOTE: If the agent uses a logging package, then wildfly fails to initialize
    // as it wants to use its own LogManager. This boolean enables debug output
    // from this agent class.
    private static final boolean DEBUG = Boolean.getBoolean("opentracing.agent.debug");

    static void debug(String mesg) {
        if (DEBUG) {
            System.out.println("OTAGENT DEBUG: " + mesg);
        }
    }

    static void error(String mesg) {
        System.out.println("OTAGENT ERROR: " + mesg);
    }

}
