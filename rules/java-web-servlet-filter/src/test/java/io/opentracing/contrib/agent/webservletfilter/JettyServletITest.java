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
package io.opentracing.contrib.agent.webservletfilter;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.opentracing.contrib.agent.common.OTAgentTestBase;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author gbrown
 */
public class JettyServletITest extends OTAgentTestBase {

    // jetty starts on random port
    private int serverPort;

    protected Server jettyServer;

    @Before
    public void beforeTest() throws Exception {
        ServletContextHandler servletContext = new ServletContextHandler();
        servletContext.setContextPath("/");
        servletContext.addServlet(TestServlet.class, "/hello");

        jettyServer = new Server(0);
        jettyServer.setHandler(servletContext);
        jettyServer.start();
        serverPort = ((ServerConnector)jettyServer.getConnectors()[0]).getLocalPort();
    }

    @After
    public void afterTest() throws Exception {
        jettyServer.stop();
        jettyServer.join();
    }

    public String localRequestUrl(String path) {
        return "http://localhost:" + serverPort + path;
    }

    @Test
    public void testHelloRequest() throws IOException {
        {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(localRequestUrl("/hello"))
                    .build();

            Response response = client.newCall(request).execute();
            
            assertEquals(HttpServletResponse.SC_ACCEPTED, response.code());
        }

        Assert.assertEquals(1, getTracer().finishedSpans().size());
    }

    @SuppressWarnings("serial")
    public static class TestServlet extends HttpServlet {

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
    }

}
