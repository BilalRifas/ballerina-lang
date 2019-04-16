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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.stomp.DefaultStompClient;
import org.ballerinalang.stdlib.stomp.StompClient;
import org.ballerinalang.stdlib.stomp.StompConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;

import static org.ballerinalang.stdlib.stomp.StompConstants.STOMP_PACKAGE;

/**
 * Initialize the GetPayload.
 *
 * @since 0.990.2
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "stomp",
        functionName = "getContent",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Message", structPackage = STOMP_PACKAGE),
        returnType = {@ReturnType(type = TypeKind.STRING), @ReturnType(type = TypeKind.ERROR)},
        isPublic = true
)

public class GetContent extends BlockingNativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(GetContent.class);
    public static String stompMessage;

    public void sendPayload(String message) {
        this.stompMessage = message;
    }

    // get the native data of message

    @Override
    public void execute(Context context) {
        String stompMessage = this.stompMessage;

        context.setReturnValues(new BString(stompMessage));
    }
}
