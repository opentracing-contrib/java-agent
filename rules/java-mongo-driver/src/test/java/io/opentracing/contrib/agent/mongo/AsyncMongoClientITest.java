/*
 * Copyright 2018 The OpenTracing Authors
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
package io.opentracing.contrib.agent.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.mongodb.Block;
import com.mongodb.ServerAddress;
import com.mongodb.MongoClientSettings;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.connection.ClusterSettings;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.bson.Document;
import org.junit.Test;

import io.opentracing.contrib.agent.common.OTAgentTestBase;
import io.opentracing.mock.MockSpan;

/**
 * @author rmfitzpatrick
 */
public class AsyncMongoClientITest extends OTAgentTestBase {

    @Test
    public void testAsyncMongoClient() throws IOException, InterruptedException {
        MongoServer server = new MongoServer(new MemoryBackend());
        final InetSocketAddress serverAddress = server.bind();

        try {
            MongoClientSettings clientSettings = MongoClientSettings.builder()
                    .applyToClusterSettings(new Block<ClusterSettings.Builder>() {
                            @Override
                            public void apply(ClusterSettings.Builder builder) {
                                    builder.hosts(Arrays.asList(new ServerAddress(serverAddress)));
                            }
                    })
                    .build();

            MongoClient mongoClient = MongoClients.create(clientSettings);

            final MongoCollection<Document> collection = mongoClient.getDatabase("MyDB").getCollection("MyCollection");
            final CountDownLatch latch = new CountDownLatch(2);

            Document myDocument = new Document("name", "MyDocument");
            collection.insertOne(myDocument, new SingleResultCallback<Void>() {
                @Override
                public void onResult(final Void result, final Throwable t) {
                    latch.countDown();

                    SingleResultCallback<Document> doc = new SingleResultCallback<Document>() {
                        @Override
                        public void onResult(final Document document, final Throwable t) {
                            latch.countDown();
                        }
                    };

                    collection.find().first(doc);
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            mongoClient.close();

            List<MockSpan> spans = getTracer().finishedSpans();
            assertEquals(2, spans.size());
            assertEquals("insert", spans.get(0).operationName());
            assertEquals("find", spans.get(1).operationName());

        } finally {
            server.shutdown();
        }
    }

}
