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

package org.ballerinalang.stdlib.stomp;

import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.*;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.stdlib.stomp.StompConstants.*;

/**
 * Register stomp listener service.
 *
 * @since 0.990.2
 */
public class DefaultStompClient extends StompClient {
    private CallableUnitCallback callableUnit;
    private Map<String, Service> serviceRegistry = new HashMap<>();
    private boolean connected;
    private static final Logger log = LoggerFactory.getLogger(DefaultStompClient.class);

    public DefaultStompClient(URI uri) {
        super(uri);
    }

    @Override
    public void onConnected(String sessionId) {
        this.connected = true;

        if (callableUnit != null) {
            callableUnit.notifySuccess();
        }
    }

    public boolean isConnected(){
        return this.connected;
    }

    public void setCallableUnit(CallableUnitCallback callableUnit) {
        this.callableUnit = callableUnit;
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onMessage(String messageId, String body, String destination) {
            Resource messageResource = getMessageResource();
            ProgramFile programFile = messageResource.getResourceInfo().getPackageInfo().getProgramFile();
            BMap<String, BValue> msgObj = BLangConnectorSPIUtil.createBStruct(programFile, STOMP_PACKAGE, MESSAGE_OBJ);
            List<ParamDetail> paramDetails = messageResource.getParamDetails();
            String callerType = paramDetails.get(0).getVarType().toString();

            if (callerType.equals("string")) {
                    Executor.submit(messageResource, new ResponseCallback(body, messageId), new HashMap<>(), null, new BString(body));
            } else if (callerType.equals("ballerina/stomp:Message")) {
                    msgObj.addNativeData(STOMP_MSG, body);
                    msgObj.put(MSG_CONTENT_NAME, new BString(body));
                    msgObj.put(MSG_DESTINATION, new BString(destination));
                    msgObj.put(MSG_ID, new BString(messageId));
                    msgObj.addNativeData(CONFIG_FIELD_CLIENT_OBJ, this);
                    Executor.submit(messageResource, new ResponseCallback(body, messageId), new HashMap<>(), null, msgObj);
            }

    }

    private class ResponseCallback implements CallableUnitCallback {
        private String message;
        private String messageId;

        ResponseCallback(String message, String messageId) {
            this.message = message;
            this.messageId = messageId;
        }

        @Override
        public void notifySuccess() {
            log.debug("Success notification");
        }

        @Override
        public void notifyFailure(BError error) {
            ErrorHandlerUtils.printError("error: " + BLangVMErrors.getPrintableStackTrace(error));
        }
    }

    @Override
    public void onReceipt(String receiptId) {
    }

    @Override
    public void onError(String message, String description) {
        Resource errorResource = getErrorResource();
        try {
            Executor.submit(errorResource, new ResponseCallback(message, description),
                        null, null, getErrorSignatureParameters(errorResource, message));
        } catch (BallerinaConnectorException c) {
                log.error("Error while executing onError resource", c);
        }
    }

    @Override
    public void onCriticalError(Exception e) {
        System.out.println("Error: " + e);
    }

    public void registerService(Service service, String destination) {
        this.serviceRegistry.put(destination, service);
    }

    public Map getDestinationValue() {
        return this.serviceRegistry;
    }

    public Resource getMessageResource(){
        Service subscriberService = this.serviceRegistry.get("destination");
        Resource msgResource = null;
        int count;
        for (count = 0; count < subscriberService.getResources().length; count++) {
            // Accessing each element of array
            String resourceName = subscriberService.getResources()[count].getName();
            if (resourceName.equals("onMessage")) {
                Resource onMessageResource = subscriberService.getResources()[count];
                if (onMessageResource != null) {
                    msgResource = onMessageResource;
                }
            }
        }
        return msgResource;
    }

    public Resource getErrorResource(){
        // Get service resources by iterating
        Service subService = this.serviceRegistry.get("destination");
        Resource errResource = null;
        int count;
        for (count = 0; count < subService.getResources().length; count++) {
            // Accessing each element of array
            String resourceName = subService.getResources()[count].getName();
            if (resourceName.equals("onError")) {
                Resource onErrorResource = subService.getResources()[count];
                if (onErrorResource != null) {
                    errResource = onErrorResource;
                }
            }
        }
        return errResource;
    }

    private BValue getErrorSignatureParameters(Resource onErrorResource, String errorMessage) {
        ProgramFile programFile = onErrorResource.getResourceInfo().getPackageInfo().getProgramFile();
        BMap<String, BValue> messageObj = BLangConnectorSPIUtil.createBStruct(
                programFile, STOMP_PACKAGE, MESSAGE_OBJ);
        BError error = StompUtils.createStompError(programFile, errorMessage);
        messageObj.addNativeData(STOMP_MESSAGE, errorMessage);
        return error;
    }
}
