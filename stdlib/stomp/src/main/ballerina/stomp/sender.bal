import ballerina/socket;
import ballerina/io;
import ballerina/log;
import ballerina/system;

# Configurations related to a STOMP connection
#
# + host - STOMP provider url.
# + port - STOMP port.
# + config - Config.
# + login - STOMP user login.
# + passcode - STOMP passcode.
# + vhost - default stomp vhost.
# + acceptVersion - 1.1.
# + socketClient - socketConnection.
# + endOfFrame - null octet.

public type Sender client object {

    public string host = "";
    public int port = 0;
    public string login = "";
    public string passcode = "";
    public string vhost = "";
    public string acceptVersion = "";
    private socket:Client socketClient;

    // End of frame used a null octet (^@ = \u0000).
    public string endOfFrame = "\u0000";

    public ConnectionConfiguration config = {
        host:host,
        port:port,
        login:login,
        passcode:passcode,
        vhost:vhost,
        acceptVersion:acceptVersion
    };

    public function __init(ConnectionConfiguration stompConfig){
        self.config = stompConfig;
        self.host = stompConfig.host;
        self.port = stompConfig.port;
        self.login = stompConfig.login;
        self.passcode = stompConfig.passcode;
        self.vhost = stompConfig.vhost;
        self.acceptVersion = stompConfig.acceptVersion;
        self.socketClient = new({
                host: self.host,
                port: self.port,
                callbackService: ClientService
            });
        var connection = self->connect(stompConfig);
    }

    public remote function connect(ConnectionConfiguration stompConfig) returns error?;

    public remote function send(string message, string destination) returns error?;

    public remote function disconnect() returns error?;
};

public type ConnectionConfiguration record {
    string host;
    int port;
    string login;
    string passcode;
    string vhost;
    string acceptVersion;
};

public remote function Sender.connect(ConnectionConfiguration stompConfig) returns error?{
    socket:Client socketClient = self.socketClient;
    io:println("Starting up the Ballerina Stomp Service\n");

    // CONNECT frame to get connected.
    string connect = "CONNECT" + "\n" +
        "accept-version:" + stompConfig.acceptVersion + "\n" +
        "login:" + stompConfig.login + "\n" +
        "passcode:" + stompConfig.passcode + "\n" +
        "host:" + stompConfig.vhost + "\n" +
        "\n" + self.endOfFrame;

    byte[] payloadByte = connect.toByteArray("UTF-8");
    // Send desired content to the server using the write function.
    var writeResult = socketClient->write(payloadByte);
    if (writeResult is error) {
        io:println("Unable to write the connect frame", writeResult);
    }
    io:println("Successfully connected to stomp broker");

    return;
}

public remote function Sender.send(string message, string destination) returns error?{
    socket:Client socketClient = self.socketClient;

    // Generating unique id for message receipt.
    string messageId = system:uuid();

    // SEND frame to send message.
    string send = "SEND" + "\n" + "destination:" + destination + "\n" + "receipt:" + messageId + "\n" + "redelivered:" + "false" + "\n" + "content-type:"+"text/plain" + "\n" + "\n" + message + "\n" + self.endOfFrame;

    byte[] payloadByte = send.toByteArray("UTF-8");
    // Send desired content to the server using the write function.
    var writeResult = socketClient->write(payloadByte);
    if (writeResult is error) {
        io:println("Unable to write the connect frame", writeResult);
    }
    io:println("Message: ", message ," is sent successfully");

    return;
}

public remote function Sender.disconnect() returns error?{
    socket:Client socketClient = self.socketClient;
    //string messageId = system:uuid();

    // DISCONNECT frame to disconnect.
    string disconnect = "DISCONNECT" + "\n" + "\n" + self.endOfFrame;

    byte[] payloadByte = disconnect.toByteArray("UTF-8");
    // Send desired content to the server using the write function.
    var writeResult = socketClient->write(payloadByte);
    if (writeResult is error) {
        io:println("Unable to write the connect frame", writeResult);
    }
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

// Callback service for the TCP client. The service needs to have four predefined resources.
service ClientService = service {

    // This is invoked once the client connects to the TCP server.
    resource function onConnect(socket:Caller caller) {
        io:println("Connect to: ", caller.remotePort);
    }

    // This is invoked when the server sends any content.
    resource function onReadReady(socket:Caller caller, byte[] content) {
        io:ReadableByteChannel byteChannel = io:createReadableChannel(content);
        io:ReadableCharacterChannel characterChannel =
        new io:ReadableCharacterChannel(byteChannel, "UTF-8");
        var str = characterChannel.read(300);
        if (str is string) {
            io:println(untaint str);
        } else {
            io:println(str);
        }
    }

    // This is invoked once the connection is closed.
    resource function onClose(socket:Caller caller) {
        io:println("Leave from: ", caller.remotePort);
    }

    // This resource is invoked for the error situation
    // if it happens during the `onConnect`, `onReadReady`, and `onClose` functions.
    resource function onError(socket:Caller caller, error err) {
        io:println(err);
    }
};

