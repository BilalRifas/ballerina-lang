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
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.stomp.DefaultStompClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

    private static final Logger log = LoggerFactory.getLogger(DefaultStompClient.class);

    @Override
    public void execute(Context context) {
        BMap<String, BValue> connection = (BMap<String, BValue>) context.getRefArgument(0);

        // Get service config annotation values
        Service service = BLangConnectorSPIUtil.getServiceRegistered(context);
        Annotation serviceAnnotation = getServiceConfigAnnotation(service);
        Struct annotationValue = serviceAnnotation.getValue();
        String destination = annotationValue.getStringField(CONFIG_FIELD_DESTINATION);
        String ackMode = annotationValue.getStringField(CONFIG_FIELD_ACKMODE);
        String strLowerAck = ackMode.toLowerCase();

        if (destination != null) {
            connection.addNativeData(CONFIG_FIELD_DESTINATION, destination);
        } else {
            log.error("Destination is null");
        }

        if (strLowerAck != null) {
            if (strLowerAck.equals("auto") | strLowerAck.equals("client") | strLowerAck.equals("client-individual")) {
                connection.addNativeData(CONFIG_FIELD_ACKMODE, strLowerAck);
            } else {
                log.error("Ack Mode is not supported");
            }
        } else {
            log.error("Ack Mode is null");
        }

        // Get stompClient object created in intListener
        DefaultStompClient client = (DefaultStompClient)
                connection.getNativeData(CONFIG_FIELD_CLIENT_OBJ);

        client.registerService(service, destination);

        context.setReturnValues();
    }

    private Annotation getServiceConfigAnnotation(Service service) {
        List<Annotation> annotationList = service
                .getAnnotationList(STOMP_PACKAGE,
                        "ServiceConfig");

        if (annotationList == null) {
            return null;
        }
        return annotationList.isEmpty() ? null : annotationList.get(0);
    }
}
