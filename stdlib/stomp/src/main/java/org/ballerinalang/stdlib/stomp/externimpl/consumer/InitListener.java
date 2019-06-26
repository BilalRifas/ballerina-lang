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

package org.ballerinalang.stdlib.stomp.externimpl.consumer;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.stomp.StompConstants;
import org.ballerinalang.stdlib.stomp.StompUtils;
import org.ballerinalang.stdlib.stomp.message.DefaultStompClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Initialize the server stomp endpoint.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = StompConstants.BALLERINA,
        packageName = StompConstants.STOMP,
        functionName = "initListener",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Listener",
                structPackage = StompConstants.STOMP_PACKAGE),
        isPublic = true
)
public class InitListener extends BlockingNativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(InitListener.class);
    private static String userLogin;
    private static String userPasscode;
    private static String hostName;
    private static Integer stompPort;

    @Override
    public void execute(Context context) {
        try {
            BMap<String, BValue> connection = (BMap<String, BValue>) context.getRefArgument(0);

            this.validateURI(context);
            String connectionURI = "tcp://" + InitListener.userLogin + ":" + InitListener.userPasscode +
                    "@" + InitListener.hostName + ":" + InitListener.stompPort;

            DefaultStompClient client = new DefaultStompClient(new URI(connectionURI));
            connection.addNativeData(StompConstants.CONFIG_FIELD_CLIENT_OBJ, client);

            context.setReturnValues();
        } catch (URISyntaxException e) {
            context.setReturnValues(StompUtils.getError(context, e));
        }
    }

    private void validateURI(Context context) {
        BMap<String, BValue> endpointConfig = (BMap<String, BValue>) context.getRefArgument(1);

        BString login = (BString) endpointConfig.get(StompConstants.CONFIG_FIELD_LOGIN);
        InitListener.userLogin = String.valueOf(login);

        BString passcode = (BString) endpointConfig.get(StompConstants.CONFIG_FIELD_PASSCODE);
        InitListener.userPasscode = String.valueOf(passcode);

        BString host = (BString) endpointConfig.get(StompConstants.CONFIG_FIELD_HOST);
        InitListener.hostName = String.valueOf(host);

        BInteger port = (BInteger) endpointConfig.get(StompConstants.CONFIG_FIELD_PORT);
        InitListener.stompPort = Integer.parseInt(String.valueOf(port));
    }
}
