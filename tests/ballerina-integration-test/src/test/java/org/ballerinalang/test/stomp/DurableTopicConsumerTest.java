/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.stomp;

import org.awaitility.Awaitility;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.test.util.TestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.test.stomp.StompTestUtils.testSend;

/**
 * Includes tests for a durable consumer and producer.
 */
@Test(groups = {"stomp-test"})
public class DurableTopicConsumerTest extends StompTestCommons {
    private CompileResult result;
    private CompileResult result2;

    @BeforeClass
    public void setup() throws URISyntaxException {
        TestUtils.prepareBalo(this);
        Path sourcePublisherPath = Paths.get("src", "test", "resources", "stomp", "producers");
        Path sourceSubcriberPath = Paths.get("src", "test", "resources", "stomp", "consumers");
        result = BCompileUtil.compile(sourceSubcriberPath.resolve("05_stomp_durable_topic_subscriber.bal")
                .toAbsolutePath().toString());
        result2 = BCompileUtil.compile(sourcePublisherPath.resolve("stomp_publisher_durable_topic.bal")
                .toAbsolutePath().toString());
    }

    // TODO fix test-cases with proper flow
    @Test(description = "Tests the receiving of durable string message from a topic")
    public void testDurableTopicSend() {
        String log1 = "";
        String log2 = "";
        String log3 = "Message received :redelivered:false\n" +
                "content-type:text/plain\n" +
                "\n" +
                "Hello World with durable topic subscription";
        String log4 = "";
        String log5 = "";
        String log6 = "";
        String log7 = "Message received :redelivered:false\n" +
                "content-type:text/plain\n" +
                "\n" +
                "Hello World with durable topic subscription";
        String log8 = "";

        String functionName1 = "invokeListener";
        testSend(result, log1, functionName1, serverInstance);

        String functionName2 = "publishMessage";
        testSend(result2, log2, functionName2, serverInstance);

        Awaitility.await();

        String functionName3 = "getMessage";
        testSend(result, log3, functionName3, serverInstance);

        String functionName4 = "shutDownListener";
        testSend(result, log4, functionName4, serverInstance);

        String functionName5 = "publishMessage2";
        testSend(result2, log5, functionName5, serverInstance);

        Awaitility.await();

        String functionName6 = "invokeListener2";
        testSend(result, log6, functionName6, serverInstance);

        String functionName7 = "getMessage";
        testSend(result, log7, functionName7, serverInstance);

        String functionName8 = "shutDownListener";
        testSend(result, log8, functionName8, serverInstance);
    }
}
