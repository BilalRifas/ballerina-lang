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

package org.ballerinalang.stdlib.stomp;

/**
 * Stomp constants.
 *
 * @since 0.995.0
 */
public class StompConstants {

    public static final String STOMP_PACKAGE = "ballerina/stomp";
    public static final String BALLERINA = "ballerina";
    public static final String STOMP = "stomp";

    // Stomp configs fields
    public static final String CONFIG_FIELD_HOST = "host";
    public static final String CONFIG_FIELD_PORT = "port";
    public static final String CONFIG_FIELD_LOGIN = "username";
    public static final String CONFIG_FIELD_PASSCODE = "password";
    public static final String CONFIG_FIELD_ACKMODE = "ackMode";
    public static final String CONFIG_FIELD_DURABLEID = "durableId";
    public static final String CONFIG_FIELD_DURABLE = "durable";
    public static final String CONFIG_FIELD_DESTINATION = "destination";

    public static final String CONFIG_FIELD_CLIENT_OBJ = "client";

    public static final String STOMP_MESSAGE = "stomp-message";
    public static final String MSG_CONTENT_NAME = "content";
    public static final String REPLY_TO_DESTINATION = "destination";
    public static final String STOMP_MSG = "STOMPMSG";
    public static final String MSG_ID = "id";
    public static final String MESSAGE_OBJ = "Message";
    public static final String ACK_MODE = "ack-mode";
    public static final String ACK_AUTO = "auto";
    public static final String ACK_CLIENT = "client";
    public static final String ACK_CLIENT_INDIVIDUAL = "client-individual";

    static final String MESSAGE_OBJ_FULL_NAME = STOMP_PACKAGE + ":" + MESSAGE_OBJ;

    // Warning suppression
    public static final String UNCHECKED = "unchecked";

    public static final String COUNTDOWN_LATCH = "countdown-latch";

    // Error related constants
    static final String STOMP_ERROR_CODE = "{ballerina/stomp}StompError";
    static final String STOMP_ERROR_RECORD = "StompError";
    static final String STOMP_ERROR_MESSAGE = "message";
}
