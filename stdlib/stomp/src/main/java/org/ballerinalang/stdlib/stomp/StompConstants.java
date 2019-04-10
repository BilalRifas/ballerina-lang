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

/**
 * Initialize the server stomp endpoint.
 *
 * @since 0.990.2
 */
public class StompConstants {
    private StompConstants() {
    }

    public static final String SERVER_STOMP_KEY = "ServerStomp";
    public static final String STOMP_KEY = "Stomp";
    public static final String STOMP_PACKAGE = "ballerina/stomp";
    // public static final String RESOURCE_ON_CLOSE = "onClose";
    public static final String RESOURCE_ON_MESSAGE = "onMessage";
    // public static final String RESOURCE_ON_CONNECT = "onConnect";
    public static final String RESOURCE_ON_ERROR = "onError";
    public static final String CLIENT = "Client";
    public static final String CONFIG_FIELD_INTERFACE = "interface";

    public static final String CONFIG_FIELD_HOST = "host";
    public static final String CONFIG_FIELD_PORT = "port";
    public static final String CONFIG_FIELD_LOGIN = "login";
    public static final String CONFIG_FIELD_PASSCODE = "passcode";
    public static final String CONFIG_FIELD_URI = "connectionURI";
    public static final String CONFIG_FIELD_ACKMODE = "ackMode";

    // subscribe destination
    public static final String CONFIG_FIELD_DESTINATION = "destination";
    public static final String DESTINATION_CONFIG = "destinationConfig";
    public static final String DESTINATION_NAME = "destinationName";
    public static final String CONFIG_FIELD_CONFIG = "";
    public static final String CLIENT_CONFIG = "";

    public static final String CONFIG_FIELD_ACCEPTVERSION = "acceptVersion";
    public static final String CONFIG_FIELD_ACKTYPE = "ackType";
    public static final String CONFIG_FIELD_ENDOFFRAME = "endOfFrame";

    public static final String LISTENER_CONFIG = "config";

    public static final String CONFIG_FIELD_CLIENT_OBJ = "client";

    // public static final String CLIENT_SERVICE_CONFIG = "callbackService";
    public static final String STOMP_SERVICE = "stompService";
    public static final String SERVICE_CONFIG = "serviceConfig";
    public static final String IS_CLIENT = "isClient";
    public static final String MESSAGE_ID = "messageId";
    public static final String MESSAGE_BODY = "messageBody";
    public static final String STOMP_MESSAGE = "stomp-message";
    public static final String MESSAGE_OBJ = "Message";

    // Native objects
    public static final String STOMP_CONNECTION = "stomp_connection_object";
}
