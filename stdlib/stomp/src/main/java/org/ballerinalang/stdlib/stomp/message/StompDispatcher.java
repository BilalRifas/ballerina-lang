package org.ballerinalang.stdlib.stomp.message;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.stdlib.stomp.StompConstants;
import org.ballerinalang.util.codegen.ProgramFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stomp Dispatcher.
 *
 * @since 0.995.0
 */
public class StompDispatcher {
    private static final Logger log = LoggerFactory.getLogger(StompDispatcher.class);
    private static Map<String, Service> serviceRegistry = new HashMap<>();
    private static Map<String, Resource> resourceRegistry = new HashMap<>();
    private static Resource onMessageResource;
    private static Resource onErrorResource;
    private static DefaultStompClient client;

    public static void execute(Context context) {
        BMap<String, BValue> connection = (BMap<String, BValue>) context.getRefArgument(0);
        // Get stompClient object created in intListener.
        DefaultStompClient stompClient = (DefaultStompClient)
                connection.getNativeData(StompConstants.CONFIG_FIELD_CLIENT_OBJ);
        StompDispatcher.client = stompClient;
    }

    public static void registerService(Service service, String destination) {
       serviceRegistry.put(destination, service);
    }

    public static Map getServiceRegistryMap() {
        return serviceRegistry;
    }

    public static void extractResource(Service service) {
        int count;
        if (service.getResources().length == 2) {
            for (count = 0; count < service.getResources().length; count++) {
                // Accessing each element of array
                String resourceName = service.getResources()[count].getName();
                if (resourceName.equals("onMessage")) {
                    onMessageResource = service.getResources()[count];
                    resourceRegistry.put("onMessage", onMessageResource);
                }

                if (resourceName.equals("onError")) {
                    onErrorResource = service.getResources()[count];
                    resourceRegistry.put("onError", onErrorResource);
                }
            }
        } else {
            log.error("We can have, only 2 resources");
        }
    }

    public static void executeOnMessage(String messageId, String body, String destination, String replyToDestination) {
        Service service = serviceRegistry.get(destination);
        extractResource(service);
        onMessageResource = resourceRegistry.get("onMessage");

        Annotation serviceAnnotation = getServiceConfigAnnotation(service);
        Struct annotationValue = serviceAnnotation.getValue();
        String ackMode = annotationValue.getStringField(StompConstants.CONFIG_FIELD_ACKMODE);

        if (onMessageResource != null) {
            ProgramFile programFile = onMessageResource.getResourceInfo().getPackageInfo().getProgramFile();
            BMap<String, BValue> msgObj = BLangConnectorSPIUtil.createBStruct(programFile,
                    StompConstants.STOMP_PACKAGE, StompConstants.MESSAGE_OBJ);
            List<ParamDetail> paramDetails = onMessageResource.getParamDetails();
            if (paramDetails.get(0) != null) {
                String callerType = paramDetails.get(0).getVarType().toString();

                if (callerType.equals("string")) {
                    Executor.submit(onMessageResource, new ResponseCallback(),
                            new HashMap<>(), null, new BString(body));
                } else if (callerType.equals("ballerina/stomp:Message")) {
                    msgObj.addNativeData(StompConstants.STOMP_MSG, body);
                    msgObj.addNativeData(StompConstants.MSG_ID, new BString(messageId));
                    msgObj.addNativeData(StompConstants.REPLY_TO_DESTINATION, replyToDestination);
                    msgObj.put(StompConstants.MSG_CONTENT_NAME, new BString(body));
                    msgObj.put(StompConstants.REPLY_TO_DESTINATION, new BString(replyToDestination));
                    msgObj.put(StompConstants.MSG_ID, new BString(messageId));
                    msgObj.put(StompConstants.ACK_MODE, new BString(ackMode));
                    msgObj.addNativeData(StompConstants.ACK_MODE, ackMode);
                    msgObj.addNativeData(StompConstants.CONFIG_FIELD_CLIENT_OBJ, client);
                    Executor.submit(onMessageResource, new ResponseCallback(),
                            new HashMap<>(), null, msgObj);
                }
            } else {
                log.error("onMessage resource doesn't not have any parameter");
            }
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

    public static void executeOnError(String message, String description) {
        onErrorResource = resourceRegistry.get("onError");
        ProgramFile programFile = onErrorResource.getResourceInfo().getPackageInfo().getProgramFile();
        BMap<String, BValue> messageObj = BLangConnectorSPIUtil.createBStruct(
                programFile, StompConstants.STOMP_PACKAGE, StompConstants.MESSAGE_OBJ);
        messageObj.addNativeData(StompConstants.STOMP_MESSAGE, message);
        messageObj.put(StompConstants.STOMP_MESSAGE, new BString(message));

        if (onErrorResource != null) {
            try {
                Executor.submit(onErrorResource, new ResponseCallback(),
                        new HashMap<>(), null, messageObj);
            } catch (BallerinaConnectorException c) {
                log.error("Error while executing onError resource", c);
            }
        }
    }

    private static Annotation getServiceConfigAnnotation(Service service) {
        List<Annotation> annotationList = service
                .getAnnotationList(StompConstants.STOMP_PACKAGE,
                        "ServiceConfig");
        if (annotationList == null) {
            return null;
        }
        return annotationList.isEmpty() ? null : annotationList.get(0);
    }
}
