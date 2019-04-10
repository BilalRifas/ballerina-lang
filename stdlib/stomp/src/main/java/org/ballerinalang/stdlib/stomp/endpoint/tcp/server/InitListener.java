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
import org.ballerinalang.connector.api.Resource;
// import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.stomp.DefaultStompClient;
import org.ballerinalang.stdlib.stomp.StompConstants;
//stomp library imports
// import org.ballerinalang.stdlib.stomp.StompClient;
import org.ballerinalang.stdlib.stomp.StompException;
import org.ballerinalang.stdlib.stomp.StompClient;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

// import static org.ballerinalang.stdlib.stomp.StompConstants.CONFIG_FIELD_CLIENT_OBJ;
// import static org.ballerinalang.stdlib.stomp.StompConstants.CONFIG_FIELD_PASSCODE;
// import static org.ballerinalang.stdlib.stomp.StompConstants.CONFIG_FIELD_LOGIN;
// import static org.ballerinalang.stdlib.stomp.StompConstants.CONFIG_FIELD_HOST;
// import static org.ballerinalang.stdlib.stomp.StompConstants.CONFIG_FIELD_PORT;
import static org.ballerinalang.stdlib.stomp.StompConstants.*;

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
        Struct clientEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        Struct clientEndpointConfig = clientEndpoint.getStructField(CLIENT_CONFIG);
        BMap<String, BValue> endpointConfig = (BMap<String, BValue>) context.getRefArgument(1);
        Map<String, Resource> resourceMap = null;
        clientEndpoint.addNativeData(CLIENT_CONFIG, endpointConfig);

        BMap<String, BValue> config = (BMap<String, BValue>) clientEndpoint.getNativeData(CLIENT_CONFIG);

        BString login = (BString) config.get(StompConstants.CONFIG_FIELD_LOGIN);
        BString passcode = (BString) config.get(StompConstants.CONFIG_FIELD_PASSCODE);
        BString host = (BString) config.get(StompConstants.CONFIG_FIELD_HOST);
        BInteger port = (BInteger) config.get(StompConstants.CONFIG_FIELD_PORT);

        String connectionURI = "tcp://" + login + ":" + passcode + "@" + host + ":" + port;
        clientEndpoint.addNativeData(CONFIG_FIELD_URI, connectionURI);
        this.run(new URI(connectionURI), context);

        context.setReturnValues();
        } catch (StompException e) {
            // context.setReturnValues("Unable to bind the stomp exception");
        } catch (URISyntaxException e) {
            // context.setReturnValues("Unable to bind the stomp URISyntaxException");
        }
}

    public void run(URI uri, Context context) throws StompException, URISyntaxException {
        Struct clientEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        Struct clientEndpointConfig = clientEndpoint.getStructField(CLIENT_CONFIG);
        BMap<String, BValue> endpointConfig = (BMap<String, BValue>) context.getRefArgument(1);
        Map<String, Resource> resourceMap = null;
        DefaultStompClient client = new DefaultStompClient(uri);
        clientEndpoint.addNativeData(StompConstants.CONFIG_FIELD_CLIENT_OBJ, client);
        clientEndpoint.addNativeData(IS_CLIENT, true);
    }
}

