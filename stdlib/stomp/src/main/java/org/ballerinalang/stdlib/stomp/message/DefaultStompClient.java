/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.stomp.message;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.CountDownLatch;

/**
 * Extended DefaultStompClient of StompClient.
 *
 * @since 0.995.0
 */
public class DefaultStompClient extends StompListener {
    private static final Logger log = LoggerFactory.getLogger(DefaultStompClient.class);
    private CallableUnitCallback callableUnit;
    private CountDownLatch connectDownLatch;
    private Context context;

    public DefaultStompClient(URI uri) {
        super(uri);
    }

    @Override
    public void onConnected(String sessionId) {
        if (callableUnit != null) {
            callableUnit.notifySuccess();
        }
        log.debug("Client connected");
        connectDownLatch.countDown();
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.connectDownLatch = countDownLatch;
    }

    public void setCallableUnit(CallableUnitCallback callableUnit) {
        this.callableUnit = callableUnit;
    }

    @Override
    public void onMessage(String messageId, String body, String destination, String replyToDestination) {
        StompDispatcher.execute(this.context);
        StompDispatcher.executeOnMessage(messageId, body, destination, replyToDestination);
    }

    @Override
    public void onError(String message, String description) {
        StompDispatcher.executeOnError(message, description);
    }

    @Override
    public void onCriticalError(Exception e) {
        // It's not implemented in Ballerina yet.
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
