package org.ballerinalang.stdlib.stomp;

import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.*;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.stdlib.stomp.endpoint.tcp.server.GetContent;
import org.ballerinalang.util.codegen.ProgramFile;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Register stomp listener service.
 *
 * @since 0.990.2
 */

public class DefaultStompClient extends StompClient {

    public static Resource messageResource;
    public static Resource errorResource;

    public DefaultStompClient() throws URISyntaxException {
    }

    public DefaultStompClient(String url) throws URISyntaxException {
        super(url);
    }

    public DefaultStompClient(URI uri) {
        super(uri);
    }

    @Override
    public void onConnected(String sessionId) {
        System.out.println("Successfully connected" + " SessionId : " + sessionId);
    }

    @Override
    public void onDisconnected() {

    }

    public void getOnMessageResource(Resource onMessageResource){
        this.messageResource = onMessageResource;
        return;
    }

    public void getOnErrorResource(Resource onErrorResource){
        this.errorResource = onErrorResource;
        return;
    }

    @Override
    public void onMessage(String messageId, String body) {
        Resource msgResource = this.messageResource;
        GetContent payload = new GetContent();
        payload.sendPayload(body);
        try {
            Executor.submit(msgResource, new ResponseCallback(body), null, null, getMsgSignatureParameters(msgResource, body));
//            try {
//                ack(messageId);
//            } catch (StompException e) {
//            }
        } catch (BallerinaConnectorException c) {
        }
    }

    private static class ResponseCallback implements CallableUnitCallback {
        private String message;

        ResponseCallback(String message) {
            this.message = message;
            System.out.println(message);
        }

        @Override
        public void notifySuccess() {
            System.out.println("Success notification");
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
        return messageObj;
    }

    private BValue getErrorSignatureParameters(Resource onErrorResource, String errorMessage) {
        ProgramFile programFile = onErrorResource.getResourceInfo().getPackageInfo().getProgramFile();
        BMap<String, BValue> messageObj = BLangConnectorSPIUtil.createBStruct(
                programFile, StompConstants.STOMP_PACKAGE, StompConstants.MESSAGE_OBJ);
        messageObj.addNativeData(StompConstants.STOMP_MESSAGE, errorMessage);
        return messageObj;
    }
}
