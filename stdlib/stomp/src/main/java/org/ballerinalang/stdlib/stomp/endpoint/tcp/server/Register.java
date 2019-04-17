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
import org.ballerinalang.connector.api.*;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.stomp.*;
import static org.ballerinalang.stdlib.stomp.StompConstants.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

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
public class Register implements NativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(Register.class);

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {
        try {
            BMap<String, BValue> connection = (BMap<String, BValue>) context.getRefArgument(0);

            // Get service config annotation values
            Service service = BLangConnectorSPIUtil.getServiceRegistered(context);
            Annotation serviceAnnotation = getServiceConfigAnnotation(service);
            Struct annotationValue = serviceAnnotation.getValue();
            String destination = annotationValue.getStringField(StompConstants.CONFIG_FIELD_DESTINATION);
            String ackMode = annotationValue.getStringField(StompConstants.CONFIG_FIELD_ACKMODE);
            String strLowerAck = ackMode.toLowerCase();

            // Get service resources
            Resource onMessageResource = service.getResources()[1];
            Resource onErrorResource = service.getResources()[0];
            connection.addNativeData(RESOURCE_ON_MESSAGE, onMessageResource);
            connection.addNativeData(RESOURCE_ON_ERROR, onErrorResource);

            // get stompClient object created in intListener
            DefaultStompClient client = (DefaultStompClient) connection.getNativeData(StompConstants.CONFIG_FIELD_CLIENT_OBJ);

            if (onMessageResource != null) {
                client.setOnMessageResource(onMessageResource);
            }

            if (onErrorResource != null) {
                client.setOnErrorResource(onErrorResource);
            }

            // connect to STOMP server, send CONNECT command and wait CONNECTED answer
            client.connect();

            // subscribe on queue
            client.subscribe(destination, strLowerAck);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
            }

            // wait
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }

            context.setReturnValues();
        } catch (StompException e) {
            context.setReturnValues(StompUtils.getError(context, e));
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

    @Override
    public boolean isBlocking() {
        return false;
    }
}