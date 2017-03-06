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

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.opentracing.contrib.agent.common.OTAgentTestBase;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author gbrown
 */
public class TomcatServletITest extends OTAgentTestBase {

    private int serverPort = 9786;

    protected Tomcat tomcatServer;

    @Before
    public void beforeTest() throws Exception {
        tomcatServer = new Tomcat();
        tomcatServer.setPort(serverPort);
        
        File baseDir = new File("tomcat");
        tomcatServer.setBaseDir(baseDir.getAbsolutePath());

        File applicationDir = new File(baseDir + "/webapps", "/ROOT");
        if (!applicationDir.exists()) {
            applicationDir.mkdirs();
        }

        Context appContext = tomcatServer.addWebapp("", applicationDir.getAbsolutePath());
        Tomcat.addServlet(appContext, "helloWorldServlet", new TestServlet());
        appContext.addServletMappingDecoded("/hello", "helloWorldServlet");

        tomcatServer.start();
        System.out.println("Tomcat server: http://" + tomcatServer.getHost().getName() + ":" + serverPort + "/");
    }

    @After
    public void afterTest() throws Exception {
        tomcatServer.stop();
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

        assertEquals(1, getTracer().finishedSpans().size());
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
