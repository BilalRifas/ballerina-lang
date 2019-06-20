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

import ballerina/socket;
import ballerina/io;
import ballerina/log;
import ballerina/system;

# Configurations related to a STOMP connection.
#
# + socketClient - socketConnection.
# + endOfFrame - End of frame used a null octet (^@ = \u0000).
# + config - ConnectionConfiguration.

public type Sender client object {
    private socket:Client socketClient;
    public string endOfFrame = "\u0000";

    public ConnectionConfiguration config = { };

    public function __init(ConnectionConfiguration stompConfig) {
        self.config = stompConfig;
        self.socketClient = new({
                host: stompConfig.host,
                port: stompConfig.port,
                callbackService: ClientService
            });
        var connection = self->connect(stompConfig);
    }

    public remote function connect(ConnectionConfiguration stompConfig) returns error?;

    public remote function send(string message, string destination, map<string> customHeaderMap) returns error?;

    public remote function disconnect() returns error?;

    public remote function readReceipt() returns error?;

    public remote function readCustomHeader(map<string> customHeaderMap) returns string;
};

public type ConnectionConfiguration record {
    string host = "";
    int port = 0;
    string username  = "";
    string password = "";
    string vhost = "";
    string acceptVersion = "";
};

public remote function Sender.connect(ConnectionConfiguration stompConfig) returns error?{
    socket:Client socketClient = self.socketClient;
    io:println("Starting up the Ballerina Stomp Service\n");

    // CONNECT frame to get connected.
    string connect = "CONNECT" + "\n" +
        "accept-version:" + stompConfig.acceptVersion + "\n" +
        "login:" + stompConfig.username + "\n" +
        "passcode:" + stompConfig.password + "\n" +
        "host:" + stompConfig.vhost + "\n" +
        "\n" + self.endOfFrame;

    byte[] payloadByte = connect.toByteArray("UTF-8");
    // Send desired content to the server using the write function.
    var writeResult = socketClient->write(payloadByte);
    if (writeResult is error) {
        io:println("Unable to write the connect frame", writeResult);
    }
    log:printInfo("Successfully connected to stomp broker");

    var readReceipt = self->readReceipt();
    return;
}

public remote function Sender.send(string message, string destination, map<string> customHeaderMap) returns error?{
    socket:Client socketClient = self.socketClient;

    // Generating unique id for message receipt.
    string messageId = system:uuid();

    string customHeaders = self->readCustomHeader(customHeaderMap);

    // SEND frame to send message.
    string send = "SEND" + "\n" + "destination:" + destination + "\n" + "receipt:" + messageId + "\n" + customHeaders + "\n" + "redelivered:" + "false" + "\n" + "content-type:"+"text/plain" + "\n" + "\n" + message + "\n" + self.endOfFrame;

    byte[] payloadByte = send.toByteArray("UTF-8");
    // Send desired content to the server using the write function.
    var writeResult = socketClient->write(payloadByte);
    if (writeResult is error) {
        io:println("Unable to write the send frame", writeResult);
    }

    var readReceipt = self->readReceipt();
    return;
}

public remote function Sender.disconnect() returns error?{
    socket:Client socketClient = self.socketClient;

    string disconnectId = system:uuid();
    // DISCONNECT frame to disconnect.
    string disconnect = "DISCONNECT" + "\n" + "receipt:" + disconnectId + "\n" + "\n" + self.endOfFrame;

    byte[] payloadByte = disconnect.toByteArray("UTF-8");
    // Send desired content to the server using the write function.
    var writeResult = socketClient->write(payloadByte);
    if (writeResult is error) {
        io:println("Unable to write the disconnect frame", writeResult);
    }

    var readReceipt = self->readReceipt();
    io:println("Disconnected from stomp broker successfully");
    // Close the connection between the server and the client.
    var closeResult = socketClient->close();
    if (closeResult is error) {
        io:println(closeResult);
    } else {
        io:println("Client connection closed successfully.");
    }
    return;
}

public remote function Sender.readCustomHeader(map<string> customHeaderMap) returns string{
        string headerValue = "";
        int headerCount = customHeaderMap.length();
        string[] cHeader = [] ;
        if (headerCount > 0) {
            string[] mapKeys = customHeaderMap.keys();
            int arrayKeyLength = mapKeys.length();
            int arrayCount = 0;
                foreach var header in mapKeys {
                    headerValue = <string>customHeaderMap[header];
                    headerValue = headerValue + "\n";
                    cHeader[arrayCount] = headerValue;
                    arrayCount = arrayCount + 1;
                }
        }

        int arrayLength = cHeader.length();
        int arrayCount = 0;
        string element = "";
        while (arrayCount < arrayLength){
            element = element + cHeader[arrayCount];
            arrayCount = arrayCount +1;
        }
        return element;
}

public remote function Sender.readReceipt() returns error?{
    socket:Client socketClient = self.socketClient;
    var result = socketClient->read();
    if (result is (byte[], int)) {
        var (content, length) = result;
        if (length > 0) {
            io:ReadableByteChannel byteChannel =
                io:createReadableChannel(content);
            io:ReadableCharacterChannel characterChannel =
                new io:ReadableCharacterChannel(byteChannel, "UTF-8");
            var str = characterChannel.read(300);
            if (str is string) {
                io:println(untaint str);
            } else {
                io:println("Error while reading characters ", str);
            }
        } else {
            io:println("Client close: ", socketClient.remotePort);
        }
    } else {
        io:println(result);
    }
    return;
}

// Callback service for the TCP client. The service needs to have four predefined resources.
service ClientService = service {
    // This is invoked once the client connects to the TCP server.
    resource function onConnect(socket:Caller caller) {
        io:println("Connect to: ", caller.remotePort);
    }

    // This is invoked when the server sends any content.
    resource function onReadReady(socket:Caller caller) {
    }

    // This resource is invoked for the error situation
    // if it happens during the `onConnect`, `onReadReady`, and `onClose` functions.
    resource function onError(socket:Caller caller, error err) {
        io:println(err);
    }
};
