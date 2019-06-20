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
 * Includes tests for a Client Acknowledge consumer and producer.
 */
@Test(groups = {"stomp-test"})
public class ClientAckTest extends StompTestCommons {
    private CompileResult result;

    @BeforeClass
    public void setup() throws URISyntaxException {
        TestUtils.prepareBalo(this);
        Path sourcePath = Paths.get("src", "test", "resources", "stomp", "producers");
        result = BCompileUtil.compile(sourcePath.resolve("stomp_publisher_client_ack.bal").toAbsolutePath().toString());
    }

    @Test(description = "Tests the sending of a string message to a queue")
    public void testClientAckSend() {
        String log1 = "received: Hello World with Client Ack";
        String log2 = "received: Hello World with Client Ack";
        String log3 = "received: Hello World with Client Ack";
        String log4 = "received: Hello World with Client Ack";
        String log5 = "received: Hello World with Client Ack";
        String functionName = "testClientAckSend";
        testSend(result, log1, functionName, serverInstance);
        testSend(result, log2, functionName, serverInstance);
        testSend(result, log3, functionName, serverInstance);
        testSend(result, log4, functionName, serverInstance);
        testSend(result, log5, functionName, serverInstance);
    }
}
