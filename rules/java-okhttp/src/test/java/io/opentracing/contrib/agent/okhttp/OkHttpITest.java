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
package io.opentracing.contrib.agent.okhttp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import io.opentracing.contrib.agent.common.OTAgentTestBase;
import io.opentracing.mock.MockSpan;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * @author gbrown
 */
public class OkHttpITest extends OTAgentTestBase {

    @Test
    public void testRequest() throws IOException {
        MockWebServer server = new MockWebServer();

        try {
            server.enqueue(new MockResponse().setBody("hello, world!").setResponseCode(200));

            HttpUrl httpUrl = server.url("/hello");

            // TODO: Rule does not currently work when just using the OkHttpClient default constructor
            OkHttpClient client = new OkHttpClient.Builder()
                    .build();

            Request request = new Request.Builder()
                  .url(httpUrl)
                  .build();

            Response response = client.newCall(request).execute();

            assertEquals(200, response.code());

            List<MockSpan> spans = getTracer().finishedSpans();

            assertEquals(1, spans.size());
            assertEquals("GET", spans.get(0).operationName());
        } finally {
            server.shutdown();
            server.close();
        }
    }

}
