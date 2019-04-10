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
import org.ballerinalang.connector.api.*;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.stomp.StompConstants;
import org.ballerinalang.stdlib.stomp.StompClient;
import org.ballerinalang.stdlib.stomp.DefaultStompClient;

//stomp library imports
import org.ballerinalang.stdlib.stomp.Ack;
import org.ballerinalang.stdlib.stomp.StompClient;
import org.ballerinalang.stdlib.stomp.StompFrame;
import org.ballerinalang.stdlib.stomp.StompException;

import java.io.InputStream;
import java.net.URI;

import org.ballerinalang.util.codegen.ProgramFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.listener.ResponseCallback;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.ServerSocketChannel;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.stdlib.stomp.StompConstants.*;

/**
 * Register stomp listener service.
 *
 * @since 0.990.2
 */
@BallerinaFunction(orgName = "ballerina",
 packageName = "stomp",
  functionName = "register",
  receiver = @Receiver(type = TypeKind.OBJECT,
   structType = "Listener",
    structPackage = STOMP_PACKAGE), isPublic = true)
public class Register extends BlockingNativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(Register.class);

    @Override
    public void execute(Context context) {
        try {
            Struct clientEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
            BMap<String, BValue> config = (BMap<String, BValue>) clientEndpoint.getNativeData(CLIENT_CONFIG);

            // Get service config annotation values
            Service service = BLangConnectorSPIUtil.getServiceRegistered(context);

            Annotation serviceAnnotation = getServiceConfigAnnotation(service);
            Struct annotationValue = serviceAnnotation.getValue();
            String destination = annotationValue.getStringField(StompConstants.CONFIG_FIELD_DESTINATION);
            String ackMode = annotationValue.getStringField(StompConstants.CONFIG_FIELD_ACKMODE);

            // String strLowerAck = ackMode.toLowerCase();

            Resource onMessageResource = service.getResources()[1];
            Resource onErrorResource = service.getResources()[0];

            Struct clientEndpointConfig = clientEndpoint.getStructField(CLIENT_CONFIG);
            BMap<String, BValue> endpointConfig = (BMap<String, BValue>) context.getRefArgument(1);
            Map<String, Resource> resourceMap = null;

            clientEndpoint.addNativeData(RESOURCE_ON_MESSAGE, onMessageResource);

            // get stompClient object created in intListener
            DefaultStompClient client = (DefaultStompClient) clientEndpoint.getNativeData(StompConstants.CONFIG_FIELD_CLIENT_OBJ);

            if (onMessageResource != null) {
                client.getOnMessageResource(onMessageResource);
            }

            if (onErrorResource != null) {
                client.getOnErrorResource(onErrorResource);
            }

            // connect to STOMP server, send CONNECT command and wait CONNECTED answer
            client.connect();

            // subscribe on queue
            client.subscribe(destination, Ack.auto);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
            }

            // wait
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }

            // unsubscribe
            client.unsubscribe(destination);

            // disconnect
            client.disconnect();

            context.setReturnValues();
        } catch (StompException e) {
            // context.setReturnValues("Unable to bind the stomp exception");
        }
    }

    private Annotation getServiceConfigAnnotation(Service service) {
        List<Annotation> annotationList = service
                .getAnnotationList(StompConstants.STOMP_PACKAGE,
                        "ServiceConfig");

        if (annotationList == null) {
            return null;
        }
        return annotationList.isEmpty() ? null : annotationList.get(0);
    }
}