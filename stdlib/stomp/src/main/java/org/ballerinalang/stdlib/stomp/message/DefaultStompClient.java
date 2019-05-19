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

import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.stdlib.stomp.StompConstants;
import org.ballerinalang.stdlib.stomp.StompUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Extended DefaultStompClient of StompClient.
 *
 * @since 0.995.0
 */
public class DefaultStompClient extends StompClient {
    private static final Logger log = LoggerFactory.getLogger(DefaultStompClient.class);
    private CallableUnitCallback callableUnit;
    private Map<String, Service> serviceRegistry = new HashMap<>();
    private boolean connected;
    private Resource onMessageResource;
    private Resource onErrorResource;
    private String destination;
    private CountDownLatch connectDownLatch;

    public DefaultStompClient(URI uri) {
        super(uri);
    }

    @Override
    public void onConnected(String sessionId) {
        // This is for testing: make the client connection delay. Thread.sleep(15000);

        if (callableUnit != null) {
            callableUnit.notifySuccess();
        }

        log.debug("Client connected");
        connectDownLatch.countDown();
    }

    public void getCountDownLatch(CountDownLatch countDownLatch) {
        this.connectDownLatch = countDownLatch;
    }

    public void setCallableUnit(CallableUnitCallback callableUnit) {
        this.callableUnit = callableUnit;
    }

    // When broker is disconnected onDisconnected will be triggered.
    @Override
    public void onDisconnected() {
    }

    @Override
    public void onMessage(String messageId, String body, String destination) {
        Service service = this.serviceRegistry.get(destination);
        this.destination = destination;
        extractResource(service);
        ProgramFile programFile = this.onMessageResource.getResourceInfo().getPackageInfo().getProgramFile();

        BMap<String, BValue> msgObj = BLangConnectorSPIUtil.createBStruct(programFile,
                StompConstants.STOMP_PACKAGE, StompConstants.MESSAGE_OBJ);
        List<ParamDetail> paramDetails = this.onMessageResource.getParamDetails();
        String callerType = paramDetails.get(0).getVarType().toString();
        if (callerType.equals("string")) {
            Executor.submit(this.onMessageResource, new ResponseCallback(),
                    new HashMap<>(), null, new BString(body));
        } else if (callerType.equals("ballerina/stomp:Message")) {
            msgObj.addNativeData(StompConstants.STOMP_MSG, body);
            msgObj.put(StompConstants.MSG_CONTENT_NAME, new BString(body));
            msgObj.put(StompConstants.MSG_DESTINATION, new BString(destination));
            msgObj.put(StompConstants.MSG_ID, new BString(messageId));
            msgObj.addNativeData(StompConstants.CONFIG_FIELD_CLIENT_OBJ, this);
            Executor.submit(this.onMessageResource, new ResponseCallback(),
                    new HashMap<>(), null, msgObj);
        }
    }

    private static class ResponseCallback implements CallableUnitCallback {

        @Override
        public void notifySuccess() {
            log.debug("Successful completion");
        }

        @Override
        public void notifyFailure(BError error) {
            ErrorHandlerUtils.printError("error: " + BLangVMErrors.getPrintableStackTrace(error));
        }
    }

    // When STOMP broker sends an acknowledgement receipt onReceipt will get triggered.
    @Override
    public void onReceipt(String receiptId) {
    }

    @Override
    public void onError(String message, String description) {
        Service service = this.serviceRegistry.get(destination);
        extractResource(service);
        try {
            Executor.submit(this.onErrorResource, new ResponseCallback(),
                    null, null, getErrorSignatureParameters(this.onErrorResource, description));
        } catch (BallerinaConnectorException c) {
            log.error("Error while executing onError resource", c);
        }
    }

    @Override
    public void onCriticalError(Exception e) {
        log.error("Error: ", e);
    }

    public void registerService(Service service, String destination) {
        this.serviceRegistry.put(destination, service);
    }

    public Map getServiceRegistryMap() {
        return this.serviceRegistry;
    }

    public void extractResource(Service service) {
        int count;
        for (count = 0; count < service.getResources().length; count++) {
            // Accessing each element of array
            String resourceName = service.getResources()[count].getName();
            if (resourceName.equals("onMessage")) {
                this.onMessageResource = service.getResources()[count];
            }
            if (resourceName.equals("onError")) {
                this.onErrorResource = service.getResources()[count];
            }
        }
    }

    private BValue getErrorSignatureParameters(Resource onErrorResource, String errorMessage) {
        ProgramFile programFile = onErrorResource.getResourceInfo().getPackageInfo().getProgramFile();
        BMap<String, BValue> messageObj = BLangConnectorSPIUtil.createBStruct(
                programFile, StompConstants.STOMP_PACKAGE, StompConstants.MESSAGE_OBJ);
        BError error = StompUtils.createStompError(programFile, errorMessage);
        messageObj.addNativeData(StompConstants.STOMP_MESSAGE, errorMessage);
        return error;
    }
}
