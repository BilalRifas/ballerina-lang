// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/stomp;
import ballerina/log;
import ballerina/io;

int limitCount = 1;

listener stomp:Listener consumerEndpointClientAck = new({
        host: "localhost",
        port: 61613,
        username: "guest",
        password: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

@stomp:ServiceConfig{
        destination:"/queue/sports",
        ackMode: stomp:CLIENT,
        durableId: "e12345"
}
// TODO try to find out for client acknowledgement retry for batch message subscribe
service stompListenerClientAck on consumerEndpointClientAck  {
    resource function onMessage(stomp:Message message) {
        var content = message.getContent();
        io:println("received: " + content);
        if (limitCount < 5) {
            limitCount= limitCount + 1;
        } else {
            limitCount = 0;
            var messageAck = message.ack();
        }
    }

    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
}