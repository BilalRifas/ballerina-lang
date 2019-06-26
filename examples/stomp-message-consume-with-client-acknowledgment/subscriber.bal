// This is the service implementation for STOMP protocol.
import ballerina/stomp;
import ballerina/log;
import ballerina/io;

int limitCount = 1;

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
        ackMode: stomp:CLIENT
}

// This binds the created consumer to the listener service.
service stompListenerSports on consumerEndpoint  {
    // This resource is invoked when a message is received.
    // Message object only gives us the string message.
    resource function onMessage(stomp:Message message) {
        var messageId = message.getMessageId();
        var content = message.getContent();
        log:printInfo("Message: " + content + "\n" + "Message Id: " + messageId + "\n");
        if (limitCount < 5) {
            limitCount= limitCount + 1;
        } else {
            log:printInfo("----------- Limit count exceeded. Acknowledged messages -----------");
            limitCount = 0;
            var messageAck = message.ack();
        }
    }

    // // This resource is invoked when the connection is interrupted.
    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
}
