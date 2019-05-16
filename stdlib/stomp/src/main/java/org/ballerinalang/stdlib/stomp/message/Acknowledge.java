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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.stomp.StompConstants;
import org.ballerinalang.stdlib.stomp.StompUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Initialize the Acknowledge.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = StompConstants.BALLERINA,
        packageName = StompConstants.STOMP,
        functionName = "ack",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Message",
                structPackage = StompConstants.STOMP_PACKAGE),
        isPublic = true
)

public class Acknowledge extends BlockingNativeCallableUnit {
    private static String ackMode = "auto";
    private static final Logger log = LoggerFactory.getLogger(Acknowledge.class);

    public static void setAckMode(String ackMode) {
        Acknowledge.ackMode = ackMode;
    }

    @Override
    public void execute(Context context) {
        @SuppressWarnings(StompConstants.UNCHECKED)
        BMap<String, BValue> message = (BMap<String, BValue>) context.getRefArgument(0);
        DefaultStompClient client = (DefaultStompClient)
                message.getNativeData(StompConstants.CONFIG_FIELD_CLIENT_OBJ);
        BValue messageId = message.get(StompConstants.MSG_ID);

        if (Acknowledge.ackMode.equals("auto")) {
            // Do nothing
        } else if (Acknowledge.ackMode.equals("client")) {
            try {
                client.ack(String.valueOf(messageId));
                log.debug("Successfully acknowledged");
            } catch (StompException e) {
                context.setReturnValues(StompUtils.getError(context, e));
            }
        }
    }
}
