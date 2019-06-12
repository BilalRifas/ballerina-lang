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
// specific language governing permissions and limitationsc
// under the License.

import ballerina/stomp;

stomp:Sender stompSender = new({
        host: "localhost",
        port: 61613,
        login: "admin",
        passcode: "admin",
        vhost: "/",
        acceptVersion: "1.1"
    });

public function testMultipleSend() {
        string message1 = "Hello World From Ballerina - Sports";
        string message2 = "Hello World From Ballerina - News";
        string destination1 = "/queue/sports";
        string destination2 = "/queue/news";
        var publish1 = stompSender->send(message1,destination1);
        var publish2 = stompSender->send(message2,destination2);
        var disconnect = stompSender->disconnect();
}
