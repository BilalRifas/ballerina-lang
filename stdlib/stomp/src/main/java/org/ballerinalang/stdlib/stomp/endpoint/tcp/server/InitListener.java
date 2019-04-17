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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.stomp.*;
import static org.ballerinalang.stdlib.stomp.StompConstants.*;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URISyntaxException;

/**
 * Initialize the server stomp endpoint.
 *
 * @since 0.990.2
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "stomp",
        functionName = "initListener",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Listener", structPackage = STOMP_PACKAGE),
        isPublic = true
)
public class InitListener extends BlockingNativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(InitListener.class);

    @Override
    public void execute(Context context) {
        try {
            BMap<String, BValue> endpointConfig = (BMap<String, BValue>) context.getRefArgument(1);
            endpointConfig.addNativeData(CLIENT_CONFIG, endpointConfig);
            BMap<String, BValue> config = (BMap<String, BValue>) endpointConfig.getNativeData(CLIENT_CONFIG);

            BString login = (BString) config.get(StompConstants.CONFIG_FIELD_LOGIN);
            BString passcode = (BString) config.get(StompConstants.CONFIG_FIELD_PASSCODE);
            BString host = (BString) config.get(StompConstants.CONFIG_FIELD_HOST);
            BInteger port = (BInteger) config.get(StompConstants.CONFIG_FIELD_PORT);

            String connectionURI = "tcp://" + login + ":" + passcode + "@" + host + ":" + port;
            this.run(new URI(connectionURI), context);
            context.setReturnValues();
        } catch (URISyntaxException e) {
            context.setReturnValues(StompUtils.getError(context, e));
        }
    }

    public void run(URI uri, Context context) {
        BMap<String, BValue> connection = (BMap<String, BValue>) context.getRefArgument(0);
        DefaultStompClient client = new DefaultStompClient(uri);
        connection.addNativeData(StompConstants.CONFIG_FIELD_CLIENT_OBJ, client);
    }
}

