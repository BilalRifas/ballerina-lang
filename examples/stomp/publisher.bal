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
    // Message is published within the transaction block.
    // Each message sent should be received from other end else rollback
    // to retry the message along with transaction Id.

        // This sends the Ballerina message to the stomp broker.
        string message1 = "Hello World From Ballerina - 1";
        string message2 = "Hello World From Ballerina - 2";
        string destination1 = "/queue/test";
        string destination2 = "/queue/new";
        var broadcast1 = stompSender->send(message1,destination1);
        var broadcast2 = stompSender->send(message2,destination2);
}