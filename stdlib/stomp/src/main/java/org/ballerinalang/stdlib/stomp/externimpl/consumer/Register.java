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
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.stomp.StompConstants;
import org.ballerinalang.stdlib.stomp.message.DefaultStompClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
/**
 * Register stomp listener service.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = StompConstants.BALLERINA,
        packageName = StompConstants.STOMP,
        functionName = "register",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = "Listener",
                structPackage = StompConstants.STOMP_PACKAGE), isPublic = true)
public class Register extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(Register.class);
    @Override
    public void execute(Context context) {
        BMap<String, BValue> connection = (BMap<String, BValue>) context.getRefArgument(0);

        // Get service config annotation values
        Service service = BLangConnectorSPIUtil.getServiceRegistered(context);
        Annotation serviceAnnotation = getServiceConfigAnnotation(service);
        Struct annotationValue = serviceAnnotation.getValue();
        String destination = annotationValue.getStringField(StompConstants.CONFIG_FIELD_DESTINATION);
        String ackMode = annotationValue.getStringField(StompConstants.CONFIG_FIELD_ACKMODE);
        if (ackMode != null) {
            String strLowerAck = ackMode.toLowerCase(Locale.ENGLISH);
            if (strLowerAck.equals("auto") || strLowerAck.equals("client") || strLowerAck.equals("client-individual")) {
                connection.addNativeData(StompConstants.CONFIG_FIELD_ACKMODE, strLowerAck);
            } else {
                log.error("Ack Mode is not supported");
            }
        } else {
            log.error("Ack Mode is null");
        }

        if (destination != null) {
            connection.addNativeData(StompConstants.CONFIG_FIELD_DESTINATION, destination);
        } else {
            log.error("Destination is null");
        }

        boolean durableFlag = annotationValue.getBooleanField(StompConstants.CONFIG_FIELD_DURABLE);
        if (durableFlag) {
            connection.addNativeData(StompConstants.CONFIG_FIELD_DURABLE, true);
        }

        // Get stompClient object created in intListener
        DefaultStompClient client = (DefaultStompClient)
                connection.getNativeData(StompConstants.CONFIG_FIELD_CLIENT_OBJ);

        client.registerService(service, destination);
        context.setReturnValues();
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
