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

listener stomp:Listener consumerEndpoint = new({
        host: "localhost",
        port: 61613,
        login: "guest",
        passcode: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

@stomp:ServiceConfig{
    destination:"/queue/sports",
    ackMode: stomp:AUTO
}

service stompListenerSports on consumerEndpoint  {
    // This resource is invoked when a message is received.
    // Message object only gives us the string message.
    resource function onMessage(stomp:Message message) {
        var content = message.getContent();
        io:println("received: " + content);
    }

    // // This resource is invoked when the connection is interrupted.
    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
}

@stomp:ServiceConfig{
    destination:"/queue/news",
    ackMode: stomp:AUTO
}

service stompListenerNews on consumerEndpoint  {
    // This resource is invoked when a message is received.
    // Message object only gives us the string message.
    resource function onMessage(stomp:Message message) {
        var content = message.getContent();
        io:println("received: " + content);
    }

    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
}
