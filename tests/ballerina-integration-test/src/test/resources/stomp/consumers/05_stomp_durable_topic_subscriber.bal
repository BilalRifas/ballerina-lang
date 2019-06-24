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
import ballerina/runtime;

string msgVal = "";
stomp:Listener consumerEndpointDurable = new({
        host: "localhost",
        port: 61613,
        username: "guest",
        password: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

@stomp:ServiceConfig{
        destination:"/topic/my-durable",
        ackMode: stomp:AUTO,
        durable: true,
        durableId: "d1234"
}

service stompListenerDurable = @stomp:ServiceConfig {} service {
    resource function onMessage(stomp:Message message) {
        var content = message.getContent();
        msgVal = untaint content;
        io:println("Message received :" + content);
    }

    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
};

@stomp:ServiceConfig{
        destination:"/topic/my-durable",
        ackMode: stomp:AUTO,
        durable: true,
        durableId: "e1234"
}

service stompListenerDurableSecond = @stomp:ServiceConfig {} service {
    resource function onMessage(stomp:Message message) {
        var content = message.getContent();
        msgVal = untaint content;
        io:println("Message received :" + content);
    }

    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
};

public function invokeListener(){
    var callAttach = consumerEndpointDurable.__attach(stompListenerDurable, name = "stompService");
    var callStart = consumerEndpointDurable.__start();
}

public function invokeListener2(){
    var callAttach = consumerEndpointDurable.__attach(stompListenerDurableSecond, name = "stompService2");
    var callStart = consumerEndpointDurable.__start();
}

public function shutDownListener(){
    var disconnect = consumerEndpointDurable.__stop();
}

public function getMessage() returns string{
    io:println("Message received :"+ msgVal);
    return msgVal;
}
