// This is the publisher implementation for STOMP protocol.
import ballerina/stomp;

// This initializes a STOMP connection with the STOMP broker.
stomp:Sender stompSender = new({
        host: "localhost",
        port: 61613,
        login: "guest",
        passcode: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

public function main() {
        // This sends the Ballerina message to the stomp broker.
        string message1 = "Hello World From Ballerina - 1";
        string message2 = "Hello World From Ballerina - 2";
        string destination1 = "/queue/test";
        string destination2 = "/queue/new";
        var publish1 = stompSender->send(message1,destination1);
        var publish2 = stompSender->send(message2,destination2);
}
