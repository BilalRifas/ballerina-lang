import ballerina/http;
import ballerina/system;
import ballerina/log;
import ballerina/io;
import ballerina/stomp;
import ballerina/runtime;

//map<string> contentMap = {};
string msgVal = "";

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
    destination:"/queue/SMSReceiveNotificationStore",
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
        msgVal = untaint content;
        log:printInfo("Message: " + content + "\n" + "Message Id: " + messageId + "\n");
        log:printInfo("HTTP request service receiver");
        //globalContent = untaint content;
        //forwardContent(content, messageId);
    }

    // This resource is invoked when the connection is interrupted.
    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
}

stomp:Sender stompSender = new({
        host: "localhost",
        port: 61613,
        username: "guest",
        password: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

listener http:Listener sendListener = new(8080);

@http:ServiceConfig {
    basePath: "/stomp"
}

service SMSSenderProxy on sendListener {
    @http:ResourceConfig {
        path: "/test",
        methods: ["POST"]
    }
    resource function send(http:Caller caller, http:Request request) returns error? {
        log:printInfo("Inside send");
        json requestPayload = {};
        var payload = request.getJsonPayload();
        if (payload is json) {
            requestPayload = payload;
            log:printInfo("Payload is json");
            string newPayload = requestPayload.toString();

            map<string> customHeaderMap = {};
            customHeaderMap["reply-to"] = "reply-to:/queue/SMSReceiveNotificationStore";

            var sendMessage = stompSender->send(newPayload, "/queue/SMSStore", customHeaderMap);
            log:printInfo("Payload is sent");

            //TODO add a separate method to get the global content
            string readContent = getContent();
            log:printInfo("readContent: " + readContent);
            runtime:sleep(5000);
            check caller->respond(untaint readContent);
        }
    }
}

function getContent() returns string{
    return msgVal;
}

//function forwardContent(string content, string messageId){
//    contentMap[messageId] = content;
//    io:println("Forward: Content Map :");
//    io:println(contentMap);
//    io:println("Content: " + content);
//    io:println("MessageId: " + messageId);
//}

//function getContent() returns string{
//    string[] mapKeys = contentMap.keys();
//    int arrayLength = mapKeys.length();
//    int count = 0;
//    string currentContent = "";
//        while(arrayLength > count){
//            currentContent = mapKeys[count];
//            count = count +1;
//        }
//    log:printInfo("Current Content :" + currentContent);
//    return currentContent;
//}