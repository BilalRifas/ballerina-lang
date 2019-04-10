// This is the service implementation for STOMP protocol.
import ballerina/stomp;
import ballerina/log;
import ballerina/io;

// This initializes a stomp listener using the created tcp connection.
listener stomp:Listener consumerEndpoint = new({
        host: "localhost",
        port: 61613,
        login: "guest",
        passcode: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

// Add service config
@stomp:ServiceConfig{  
        destination:"/queue/test",
        ackMode:"AUTO"
}

// This binds the created consumer to the listener service.
service stompListeners on consumerEndpoint  {
    // This resource is invoked when a message is received.
    // Message object only gives us the string message.
    resource function onMessage(stomp:Message message) {
        io:println("Ballerina onMessage triggered");
        var messageContent = message.getContent();

        io:println(messageContent);
        //var messageAck = message->ack();
    }

    // // This resource is invoked when the connection is interrupted.
    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
}