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
    string message = "Hello World From Ballerina";
    string destination = "/queue/test";

    // Multiple message sending
    foreach int i in 0 ... 20 {
        var broadcast = stompSender->send(message,destination);
    }


    // If multiple messages are sent then at the end disconnect is sent to the broker
    // to close the socket connection from broker.
    var disconnect = stompSender->disconnect();
}
