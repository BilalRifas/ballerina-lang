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

import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Includes common functionality for Stomp test cases.
 */
public class StompTestCommons extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(StompTestCommons.class);

    private EmbeddedActiveMQ embeddedBroker;

    protected static BServerInstance serverInstance;

    @BeforeGroups(value = "stomp-test", alwaysRun = true)
    public void start() throws BallerinaTestException {
        Path path = Paths.get("src", "test", "resources", "stomp");

        // Start broker
        embeddedBroker = new EmbeddedActiveMQ();
        String brokerXML = path.resolve("configfiles").resolve("broker.xml").toUri().toString();
        embeddedBroker.setConfigResourcePath(brokerXML);
        try {
            embeddedBroker.start();
        } catch (Exception ex) {
            Assert.fail("Cannot start ActiveMQ Stomp broker", ex);
        }

        // Start Ballerina server
        serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(path.toAbsolutePath().toString(), "consumers", new String[]{"--experimental"},
                                   new int[]{});
    }
    // TODO find-out to stop the broker
    @AfterGroups(value = "stomp-test", alwaysRun = true)
    public void stop() throws Exception {
        serverInstance.removeAllLeechers();
        serverInstance.shutdownServer();
    }

}