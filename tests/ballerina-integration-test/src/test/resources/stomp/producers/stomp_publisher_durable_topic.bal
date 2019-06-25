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

stomp:Sender stompSender = new({
        host: "localhost",
        port: 61613,
        username: "guest",
        password: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

public function publishMessage(){
        string message = "Hello World with durable topic subscription 1 ";
        string destination = "/topic/my-durable";
        map<string> customHeaderMap = {};
        customHeaderMap["persistent"] = "true";
        var publish = stompSender->send(message,destination,customHeaderMap);
        var disconnect = stompSender->disconnect();

}

public function publishMessage2(){
        string message2 = "Hello World with durable topic subscription 1 ";
        string destination2 = "/topic/my-durable";
        map<string> customHeaderMap2 = {};
        customHeaderMap2["persistent"] = "true";
        var publish2 = stompSender->send(message2,destination2,customHeaderMap2);
        var disconnect2 = stompSender->disconnect();

}
