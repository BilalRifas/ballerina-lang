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
        Path sourcePath = Paths.get("src", "test", "resources", "stomp", "producers");
        result = BCompileUtil.compile(sourcePath.resolve("stomp_publisher_durable_topic.bal")
                .toAbsolutePath().toString());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        result2 = BCompileUtil.compile(sourcePath.resolve("stomp_publisher_durable_topic_2.bal")
                .toAbsolutePath().toString());
    }

    // TODO fix test-cases with proper flow
    @Test(description = "Tests the sending of a string message to a queue")
    public void testDurableTopicSend() {
        String log = "received: Hello World with durable topic subscription";
        String functionName = "testFirstDurableTopicSend";
        testSend(result, log, functionName, serverInstance);
    }

    @Test(description = "Tests the sending of a string message to a queue")
    public void testDurableTopicSend2() {
        String log = "received: Hello World with durable topic subscription";
        String functionName = "testSecondDurableTopicSend";
        testSend(result2, log, functionName, serverInstance);
    }
}
