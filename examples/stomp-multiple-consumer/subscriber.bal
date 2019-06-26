// This is the service implementation for STOMP protocol.
import ballerina/stomp;
import ballerina/log;

// This initializes a stomp listener using the created tcp connection.
listener stomp:Listener consumerEndpoint = new({
        host: "localhost",
        port: 61613,
        username: "guest",
        password: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

// Add service config
@stomp:ServiceConfig{
    destination:"/queue/sports",
    ackMode: stomp:AUTO
}

// This binds the created consumer to the listener service.
service stompListenerSports on consumerEndpoint  {
    // This resource is invoked when a message is received.
    // Message object only gives us the string message.
    resource function onMessage(stomp:Message message) {
        var messageId = message.getMessageId();
        var content = message.getContent();
        log:printInfo("Message: " + content + "\n" + "Message Id: " + messageId + "\n");
        log:printInfo("StompListener Sports");
    }

    // This resource is invoked when the connection is interrupted.
    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
}

@stomp:ServiceConfig{
    destination:"/queue/news",
    ackMode: stomp:AUTO
}

service stompListenerNews on consumerEndpoint  {
    // This resource is invoked when a message is received.
    // Message object only gives us the string message.
    resource function onMessage(stomp:Message message) {
        var messageId = message.getMessageId();
        var content = message.getContent();
        log:printInfo("Message: " + content + "\n" + "Message Id: " + messageId + "\n");
        log:printInfo("StompListener News");
    }

    // // This resource is invoked when the connection is interrupted.
    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
}
