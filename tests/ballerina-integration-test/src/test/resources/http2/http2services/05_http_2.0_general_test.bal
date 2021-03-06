// Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/http;

listener http:Listener ep = new(9099, config = { httpVersion: "2.0" });

//Backend pointed by these clients should be down.
http:Client priorOn = new("http://localhost:12345", config = { httpVersion: "2.0", http2Settings: {
                http2PriorKnowledge: true }, poolConfig: {} });

http:Client priorOff = new("http://localhost:12345", config = { httpVersion: "2.0", http2Settings: {
                http2PriorKnowledge: false }, poolConfig: {} });

@http:ServiceConfig {
    basePath: "/general"
}
service generalCases on ep {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/serverDown"
    }
    resource function backEndDown(http:Caller caller, http:Request req) {
        http:Request serviceReq = new;
        var result1 = priorOn->submit("GET", "/bogusResource", serviceReq);
        var result2 = priorOff->submit("GET", "/bogusResource", serviceReq);
        string response = handleResponse(result1) + "--" + handleResponse(result2);
        checkpanic caller->respond(untaint response);
    }
}

function handleResponse(http:HttpFuture|error result) returns string {
    string response = "";
    if (result is http:HttpFuture) {
        response = "Call succeeded";
    } else {
        response = "Call to backend failed due to:" + <string>result.detail().message;
    }
    return response;
}
