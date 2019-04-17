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
import org.ballerinalang.stdlib.stomp.endpoint.tcp.server.GetContent;
import org.ballerinalang.util.codegen.ProgramFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Register stomp listener service.
 *
 * @since 0.990.2
 */
public class DefaultStompClient extends StompClient {

    private Resource messageResource;
    private Resource errorResource;
    private static final Logger log = LoggerFactory.getLogger(DefaultStompClient.class);

    public DefaultStompClient(URI uri) {
        super(uri);
    }

    @Override
    public void onConnected(String sessionId) {
        log.debug("Successfully connected" + " SessionId : " + sessionId + "\n");
    }

    @Override
    public void onDisconnected() {
    }

    public void setOnMessageResource(Resource onMessageResource) {
        this.messageResource = onMessageResource;
    }

    public void setOnErrorResource(Resource onErrorResource) {
        this.errorResource = onErrorResource;
    }

    @Override
    public void onMessage(String messageId, String body) {
        Resource msgResource = this.messageResource;

        GetContent payload = new GetContent();
        payload.sendPayload(messageId);

        try {
            Executor.submit(msgResource, new ResponseCallback(body), null, null, getMsgSignatureParameters(msgResource, body));
        } catch (BallerinaConnectorException c) {
            onError(body, c.getMessage());
        }
    }

    private static class ResponseCallback implements CallableUnitCallback {
        private String message;

        ResponseCallback(String message) {
            this.message = message;
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
        Resource errorResource = this.errorResource;
        try {
            Executor.submit(errorResource, new ResponseCallback(message), null, null, getErrorSignatureParameters(errorResource, message));
        } catch (BallerinaConnectorException c) {
            log.error("Error while executing onError resource", c);
        }
    }

    @Override
    public void onCriticalError(Exception e) {
    }

    private BValue getMsgSignatureParameters(Resource onMessageResource, String message) {
        ProgramFile programFile = onMessageResource.getResourceInfo().getPackageInfo().getProgramFile();
        BMap<String, BValue> messageObj = BLangConnectorSPIUtil.createBStruct(
                programFile, StompConstants.STOMP_PACKAGE, StompConstants.MESSAGE_OBJ);
        messageObj.addNativeData(StompConstants.STOMP_MESSAGE, message);
        messageObj.put(StompConstants.MSG_CONTENT_NAME, new BString(message));
        return new BString(message);
    }

    private BValue getErrorSignatureParameters(Resource onErrorResource, String errorMessage) {
        ProgramFile programFile = onErrorResource.getResourceInfo().getPackageInfo().getProgramFile();
        BMap<String, BValue> messageObj = BLangConnectorSPIUtil.createBStruct(
                programFile, StompConstants.STOMP_PACKAGE, StompConstants.MESSAGE_OBJ);
        BError error = StompUtils.createSocketError(programFile, errorMessage);
        messageObj.addNativeData(StompConstants.STOMP_MESSAGE, errorMessage);
        return error;
    }

}
