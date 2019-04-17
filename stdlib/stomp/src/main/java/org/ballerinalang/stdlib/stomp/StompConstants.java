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

    public static final String STOMP_PACKAGE = "ballerina/stomp";
    public static final String RESOURCE_ON_MESSAGE = "onMessage";
    public static final String RESOURCE_ON_ERROR = "onError";

    // Stomp configs fields
    public static final String CONFIG_FIELD_HOST = "host";
    public static final String CONFIG_FIELD_PORT = "port";
    public static final String CONFIG_FIELD_LOGIN = "login";
    public static final String CONFIG_FIELD_PASSCODE = "passcode";
    public static final String CONFIG_FIELD_ACKMODE = "ackMode";
    public static final String CONFIG_FIELD_DESTINATION = "destination";

    public static final String CLIENT_CONFIG = "config";
    public static final String CONFIG_FIELD_CLIENT_OBJ = "client";

    public static final String STOMP_MESSAGE = "stomp-message";
    public static final String MSG_CONTENT_NAME = "content";
    public static final String MESSAGE_OBJ = "Message";

    // Error related constants
    static final String STOMP_ERROR_CODE = "{ballerina/stomp}StompError";
    static final String STOMP_ERROR_RECORD = "StompError";
    static final String STOMP_ERROR_MESSAGE = "message";
}
