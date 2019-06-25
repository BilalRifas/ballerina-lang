import ballerina/stomp;
import ballerina/io;
import ballerina/log;
import ballerina/runtime;

string msgVal = "Bilal";

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
        destination:"/topic/bilalDurable",
        ackMode: stomp:AUTO,
        durable: true
}

// This binds the created consumer to the listener service.
service stompListenerSports on consumerEndpoint  {
    // This resource is invoked when a message is received.
    // Message object only gives us the string message.
    resource function onMessage(stomp:Message message) {
        var messageId = message.getMessageId();
        var content = message.getContent();
        log:printInfo("Message: " + content + "\n" + "Message Id: " + messageId + "\n");
        msgVal = untaint content;

    }

    // This resource is invoked when the connection is interrupted.
    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
}

// This initializes a STOMP connection with the STOMP broker.
stomp:Sender stompSender = new({
        host: "localhost",
        port: 61613,
        username: "guest",
        password: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

function getMsgVal() returns string {
    return msgVal;
}

public function main() {
        // This sends the Ballerina message to the stomp broker.
        sendMessage();
        //runtime:sleep(10000);
        string msg = getMsgVal();
        log:printInfo("Message received: " + msg);
        //var disconnect = stompSender->disconnect();
        //var publish2 = stompSender->send(message,destination,customHeaderMap);
        //var msg2 = getMsgVal();
        //log:printInfo("Message received: " + msg2);
}

function sendMessage(){
        string message = "Hello World From Ballerina";
        string destination = "/topic/bilalDurable";
        map<string> customHeaderMap = {};
        customHeaderMap["persistent"] = "persistent:true";

        var publish = stompSender->send(message,destination,customHeaderMap);

}
