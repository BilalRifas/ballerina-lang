/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.stomp.endpoint.tcp.server;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.stomp.Ack;
import org.ballerinalang.stdlib.stomp.DefaultStompClient;
import org.ballerinalang.stdlib.stomp.StompException;
import org.ballerinalang.stdlib.stomp.StompUtils;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.ballerinalang.stdlib.stomp.StompConstants.*;
import static org.ballerinalang.stdlib.stomp.StompConstants.CONFIG_FIELD_ACKMODE;

/**
 * Start server stomp listener.
 *
 * @since 0.990.2
 */
@BallerinaFunction(orgName = "ballerina",
        packageName = "stomp",
        functionName = "start",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = "Listener",
                structPackage = STOMP_PACKAGE),
        isPublic = true
)
public class Start implements NativeCallableUnit {
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {
        try {
            BMap<String, BValue> start = (BMap<String, BValue>) context.getRefArgument(0);
            start.addNativeData(COUNTDOWN_LATCH, countDownLatch);

            String ackMode = (String) start.getNativeData(CONFIG_FIELD_ACKMODE);

            Ack ack = new Ack();
            ack.setAckMode(ackMode);

            // get stompClient object created in intListener
            DefaultStompClient client = (DefaultStompClient) start.getNativeData(CONFIG_FIELD_CLIENT_OBJ);

            client.setCallableUnit(callableUnitCallback);

            // connect to STOMP server, send CONNECT command and wait CONNECTED answer
            client.connect();

            // Change variable name to destination Map or something
            Map<String, Service> subMapDestination = client.getDestinationValue();
            for (Map.Entry<String, Service> entry : subMapDestination.entrySet()) {
                String subscribeDestination = entry.getKey();

                // Keeps on waiting for the connected flag, once connected is made it should subscribe on queue.
                int count = 0;
                int maxTries = 3;
                while (client.isConnected()) {
                    {
                        try {
                            client.subscribe(subscribeDestination, ackMode);
                        } catch (StompException e) {
                            // handle exception
                            if (++count == maxTries) throw e;
                        }
                    }

                }
            }

            // It is essential to keep a non-daemon thread running in order to avoid the java program or the
            // Ballerina service from exiting
            new Thread(() -> {
                try {
                    countDownLatch.await();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            context.setReturnValues();
        } catch (StompException e) {
            context.setReturnValues(StompUtils.getError(context, e));
            callableUnitCallback.notifySuccess();
        }
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}