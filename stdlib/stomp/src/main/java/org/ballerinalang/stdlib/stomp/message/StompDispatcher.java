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
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
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
    private static Map<String, ObjectValue> serviceRegistry = new HashMap<>();
    private static Map<String, AttachedFunction> resourceRegistry = new HashMap<>();
    private static Resource onMessageResource;
    private static Resource onErrorResource;
    private static ObjectValue sessionObj;
    private static AttachedFunction onMessageAttachedFunction;
    private static AttachedFunction onErrorAttachedFunction;
    private static DefaultStompClient client;

    public static void execute(MapValue<String, Object> dispatcherConfig) {
//        BMap<String, BValue> connection = (BMap<String, BValue>) context.getRefArgument(0);
        // Get stompClient object created in intListener.
        DefaultStompClient stompClient = (DefaultStompClient)
                dispatcherConfig.getNativeData(StompConstants.CONFIG_FIELD_CLIENT_OBJ);
        StompDispatcher.client = stompClient;
    }

    public static void registerService(ObjectValue service, String destination) {
       serviceRegistry.put(destination, service);
    }

    public static Map getServiceRegistryMap() {
        return serviceRegistry;
    }

    public static void extractResource(ObjectValue service) {
        int count;
        if (service.getType().getAttachedFunctions().length == 2) {
            for (count = 0; count < service.getType().getAttachedFunctions().length; count++) {
                // Accessing each element of array
                String resourceName = service.getType().getAttachedFunctions()[count].getName();
                if (resourceName.equals("onMessage")) {
                    onMessageAttachedFunction = service.getType().getAttachedFunctions()[count];
                    resourceRegistry.put("onMessage", onMessageAttachedFunction);
                }

                if (resourceName.equals("onError")) {
                    onErrorAttachedFunction = service.getType().getAttachedFunctions()[count];
                    resourceRegistry.put("onError", onErrorAttachedFunction);
                }
            }
        } else {
            log.error("We can have, only 2 resources");
        }
    }

    public static void executeOnMessage(String messageId, String body, String destination, String replyToDestination) {
        ObjectValue service = serviceRegistry.get(destination);
        extractResource(service);
        onMessageAttachedFunction = resourceRegistry.get("onMessage");

        ArrayValue annotations = service.getType().getAnnotation(StompConstants.STOMP_PACKAGE,
                StompConstants.SERVICE_CONFIG);
        MapValue<String, Object> messageConfig = (MapValue) annotations.getRefValue(0);
        String ackMode = messageConfig.getStringValue(StompConstants.CONFIG_FIELD_ACKMODE);

        if (onMessageAttachedFunction != null) {

//            Executor.submit(service, RabbitMQConstants.FUNC_ON_MESSAGE, new RabbitMQResourceCallback(countDownLatch),
//                    null, getMessageObjectValue(message, deliveryTag, properties));

//            public void onMessage(ClientMessage clientMessage) {
                BType[] parameterTypes = onMessageAttachedFunction.getParameterType();
//                if (parameterTypes.length > 1) {
//                    dispatchResourceWithDataBinding(clientMessage, parameterTypes);
//                } else {
                    Object[] signatureParams = new Object[parameterTypes.length * 2];
                    signatureParams[0] = createAndGetMessageObj(body, sessionObj);
                    signatureParams[1] = true;
                    dispatchResource(clientMessage, signatureParams);
//                }
            }

//            ProgramFile programFileTest = service.getType();

            ProgramFile programFile = onMessageAttachedFunction.getResourceInfo().getPackageInfo().getProgramFile();
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

    private Object createAndGetMessageObj(String clientMessage,
                                          ObjectValue sessionObj) {
        ObjectValue messageObj = BallerinaValues.createObjectValue(StompConstants.STOMP_PACKAGE,
                StompConstants.MESSAGE_OBJ);
        populateMessageObj(clientMessage, messageObj);
        //Add artemis message
        messageObj.getType(clientMessage);
        return messageObj;
    }

    public static void populateMessageObj(String clientMessage, ObjectValue messageObj) {
        MapValue<String, Object> messageConfigObj = (MapValue<String, Object>) messageObj.get(
                StompConstants.MESSAGE_CONFIG);
        populateMessageConfigObj(clientMessage, messageConfigObj);

        messageObj.addNativeData(ArtemisConstants.ARTEMIS_TRANSACTION_CONTEXT, transactionContext);
        messageObj.addNativeData(ArtemisConstants.ARTEMIS_MESSAGE, clientMessage);
    }

    private void dispatchResource(String clientMessage, Object... bValues) {
        // A CountDownLatch is used to prevent multiple resources executing in parallel and hence preventing the use
        // of the same session in multiple threads concurrently (Error AMQ212051).
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executor.submit(scheduler, service, onMessageResource.getName(),
                new ArtemisResourceCallback(clientMessage, autoAck, sessionObj, countDownLatch), null, bValues);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void populateMessageConfigObj(String clientMessage,
                                                 MapValue<String, Object> messageConfigObj) {
//        messageConfigObj.put(ArtemisConstants.EXPIRATION, clientMessage.getExpiration());
//        messageConfigObj.put(ArtemisConstants.TIME_STAMP, clientMessage.getTimestamp());
//        messageConfigObj.put(ArtemisConstants.PRIORITY, clientMessage.getPriority());
//        messageConfigObj.put(ArtemisConstants.DURABLE, clientMessage.isDurable());

        setRoutingTypeToConfig(messageConfigObj, clientMessage);
        if (clientMessage.getGroupID() != null) {
            messageConfigObj.put(ArtemisConstants.GROUP_ID, clientMessage.getGroupID().toString());
        }
        messageConfigObj.put(ArtemisConstants.GROUP_SEQUENCE, clientMessage.getGroupSequence());
        if (clientMessage.getCorrelationID() != null) {
            messageConfigObj.put(ArtemisConstants.CORRELATION_ID, clientMessage.getCorrelationID().toString());
        }
        if (clientMessage.getReplyTo() != null) {
            messageConfigObj.put(ArtemisConstants.REPLY_TO, clientMessage.getReplyTo().toString());
        }
    }

    private void dispatchResource(String clientMessage, Object... bValues) {
        // A CountDownLatch is used to prevent multiple resources executing in parallel and hence preventing the use
        // of the same session in multiple threads concurrently (Error AMQ212051).
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executor.submit(scheduler, service, onMessageResource.getName(),
                new ArtemisResourceCallback(clientMessage, autoAck, sessionObj, countDownLatch), null, bValues);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
//    private ObjectValue getMessageObjectValue(byte[] message, long deliveryTag, AMQP.BasicProperties properties) {
//        ObjectValue messageObjectValue = BallerinaValues.createObjectValue(RabbitMQConstants.PACKAGE_RABBITMQ,
//                RabbitMQConstants.MESSAGE_OBJECT);
//        messageObjectValue.addNativeData(RabbitMQConstants.DELIVERY_TAG, deliveryTag);
//        messageObjectValue.addNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT, channel);
//        messageObjectValue.addNativeData(RabbitMQConstants.MESSAGE_CONTENT, message);
//        messageObjectValue.addNativeData(RabbitMQConstants.AUTO_ACK_STATUS, autoAck);
//        if (!Objects.isNull(rabbitMQTransactionContext)) {
//            messageObjectValue.addNativeData(RabbitMQConstants.RABBITMQ_TRANSACTION_CONTEXT,
//                    rabbitMQTransactionContext);
//        }
//        messageObjectValue.addNativeData(RabbitMQConstants.BASIC_PROPERTIES, properties);
//        messageObjectValue.addNativeData(RabbitMQConstants.MESSAGE_ACK_STATUS, false);
//        return messageObjectValue;
//    }

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
        onErrorAttachedFunction = resourceRegistry.get("onError");
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
