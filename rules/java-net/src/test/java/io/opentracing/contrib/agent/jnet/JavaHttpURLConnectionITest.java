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
package io.opentracing.contrib.agent.jnet;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Test;

import io.opentracing.contrib.agent.common.OTAgentTestBase;
import io.opentracing.mock.MockSpan;
import io.opentracing.tag.Tags;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * This class tests various ways of initiating a HTTP request, using
 * the java.net.HttpURLConnection, against an OkHttp mock server, to
 * verify that the ByteMan rules are correctly creating a Span to
 * represent the client side of the HTTP invocation.
 */
public class JavaHttpURLConnectionITest extends OTAgentTestBase {

    @Test
    public void testHttpURLConnectionConnectGET() throws IOException {
        testHttpRequestConnect("GET", false);
    }

    @Test
    public void testHttpURLConnectionGetStatusCodeGET() throws IOException {
        testHttpRequestGetStatusCode("GET", false);
    }

    @Test
    public void testHttpURLConnectionInputStreamGET() throws IOException {
        sendRequestUsingInputStream("GET", false);
    }

    @Test
    public void testHttpURLConnectionConnectGETWithFault() throws IOException {
        testHttpRequestConnect("GET", true);
    }

    @Test
    public void testHttpURLConnectionInputStreamGETWithFault() throws IOException {
        sendRequestUsingInputStream("GET", true);
    }

    @Test
    public void testHttpURLConnectionConnectPUT() throws IOException {
        testHttpRequestConnect("PUT", false);
    }

    @Test
    public void testHttpURLConnectionInputStreamPUT() throws IOException {
        sendRequestUsingInputStream("PUT", false);
    }

    @Test
    public void testHttpURLConnectionConnectPOST() throws IOException {
        testHttpRequestConnect("POST", false);
    }

    @Test
    public void testHttpURLConnectionInputStreamPOST() throws IOException {
        sendRequestUsingInputStream("POST", false);
    }

    @Test
    public void testHttpURLConnectionRefused() throws MalformedURLException {
        URL url = new URL("http://localhost:9876/invalid");
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);

            connection.getResponseCode();
        } catch (Exception e) {
            // Ignore
        }

        verifyTrace(url, "GET", -1);
    }

    @Test
    public void testHttpURLConnectionConnectGETIgnored() throws IOException {
        sendRequest("GET", false, true, true);
    }

    protected void testHttpRequestConnect(String method, boolean fault) throws IOException {
        sendRequest(method, fault, true, false);
    }

    protected void testHttpRequestGetStatusCode(String method, boolean fault) throws IOException {
        sendRequest(method, fault, false, false);
    }

    protected void sendRequest(String method, boolean fault, boolean connect, boolean ignore) throws IOException {
        MockWebServer server = new MockWebServer();

        try {
            if (fault) {
                server.enqueue(new MockResponse().setResponseCode(401));
            } else {
                server.enqueue(new MockResponse().setBody("hello, world!").setResponseCode(200));
            }

            HttpUrl httpUrl = server.url("/hello");

            HttpURLConnection connection = (HttpURLConnection) httpUrl.url().openConnection();

            if (ignore) {
                connection.setRequestProperty("opentracing.ignore", "True");
            }

            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);

            if (connect) {
                connection.connect();
            }

            int status = connection.getResponseCode();

            if (!fault) {
                assertEquals("Unexpected response code", 200, status);
            } else {
                assertEquals("Unexpected fault response code", 401, status);
            }

            // Call again to make sure does not attempt to finish the span again
            connection.getResponseCode();

            if (!ignore) {
                verifyTrace(httpUrl.url(), method, status);
            } else {
                assertTrue(getTracer().finishedSpans().isEmpty());
            }
        } finally {
            server.shutdown();
            server.close();
        }
    }

    protected void sendRequestUsingInputStream(String method, boolean fault) throws IOException {
        MockWebServer server = new MockWebServer();

        try {
            if (fault) {
                server.enqueue(new MockResponse().setResponseCode(401));
            } else {
                server.enqueue(new MockResponse().setBody("hello, world!").setResponseCode(200));
            }

            HttpUrl httpUrl = server.url("/hello");

            HttpURLConnection connection = (HttpURLConnection) httpUrl.url().openConnection();

            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);

            try (InputStream is = connection.getInputStream()) {
            } catch (IOException ioe) {
                if (!fault) {
                    throw ioe;
                }
            }

            int status = connection.getResponseCode();

            if (!fault) {
                assertEquals("Unexpected response code", 200, status);
            } else {
                assertEquals("Unexpected fault response code", 401, status);
            }

            // Call again to make sure does not attempt to finish the span again
            connection.getResponseCode();

            verifyTrace(httpUrl.url(), method, status);
        } finally {
            server.shutdown();
            server.close();
        }
    }

    protected void verifyTrace(URL url, String method, int statusCode) {
        List<MockSpan> spans = getTracer().finishedSpans();
        assertEquals(1, spans.size());
        assertEquals(method, spans.get(0).operationName());
        assertEquals(Tags.SPAN_KIND_CLIENT, spans.get(0).tags().get(Tags.SPAN_KIND.getKey()));
        assertEquals(url.toString(), spans.get(0).tags().get(Tags.HTTP_URL.getKey()));
        if (statusCode != -1) {
            assertEquals(statusCode, spans.get(0).tags().get(Tags.HTTP_STATUS.getKey()));
        }
        if (statusCode != 200) {
            assertEquals(true, spans.get(0).tags().get(Tags.ERROR.getKey()));
        }
    }

}
