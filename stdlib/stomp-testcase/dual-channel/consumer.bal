import ballerina/stomp;
import ballerina/io;
import ballerina/filepath;
import ballerina/http;
import ballerina/log;

stomp:Sender stompSender = new({
        host: "localhost",
        port: 61613,
        username: "guest",
        password: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

listener stomp:Listener consumerEndpoint = new({
        host: "localhost",
        port: 61613,
        username: "guest",
        password: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

// Add service config.
@stomp:ServiceConfig{
    destination:"/queue/SMSStore",
    ackMode: stomp:AUTO,
    durableId: "e12345"
}

// This binds the created consumer to the listener service.
service stompHttpRequestService on consumerEndpoint  {
    // This resource is invoked when a message is received.
    // Message object only gives us the string message.
    resource function onMessage(stomp:Message message) {
        var messageId = message.getMessageId();
        var content = message.getContent();
        log:printInfo("Message: " + content + "\n" + "Message Id: " + messageId + "\n");
        var replyDestination = message.getReplyToDestination();
        string replyTo = replyDestination;
        log:printInfo("HTTP request service");
        string payload = content;
        map<string> customHeaderMap = {};
        io:println(payload);

        http:Client remoteClient = new("http://localhost:9091");
        var response = remoteClient->post("/remote", untaint payload);
        if (response is http:Response) {
            var requestPayload = untaint response.getJsonPayload();
            if (requestPayload is json){
                string textPayload = requestPayload.toString() + messageId;
                var forwardMsg = stompSender->send(textPayload, replyTo, customHeaderMap);
                io:println("Payload sent successfully",textPayload);
            }
        }
    }

    // This resource is invoked when the connection is interrupted.
    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
}
