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
service stompListener1 on consumerEndpoint  {
    // This resource is invoked when a message is received.
    // Message object only gives us the string message.
    resource function onMessage(stomp:Message message) {
        var content = message.getContent();
        //log:printInfo(payload);
        log:printInfo("stompListener 1");
        log:printInfo(content);

        var messageAck = message.ack();
    }

    // // This resource is invoked when the connection is interrupted.
    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
}

@stomp:ServiceConfig{
    destination:"/queue/new",
    ackMode:"AUTO"
}

service stompListener2 on consumerEndpoint  {
    // This resource is invoked when a message is received.
    // Message object only gives us the string message.
    resource function onMessage(stomp:Message message) {
        var content = message.getContent();
        //log:printInfo(payload);
        log:printInfo("stompListener 2");
        log:printInfo(content);
        var messageAck = message.ack();
    }

    // // This resource is invoked when the connection is interrupted.
    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
}
