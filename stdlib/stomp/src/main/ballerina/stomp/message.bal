import ballerina/io;
import ballerina/log;

public type Message object {

    //public MessageConfiguration config = {};
    //private Listener? listenerConnection;
    // public string ackMode = "";
    public string message = "";
    public string receiptId = "";
  
    // End of frame used a null octet (^@ = \u0000).
    public string endOfFrame = "\u0000";

    // public MessageConfiguration config = {
    //     ackMode:ackMode
    // };

    public extern function getContent() returns string|error;
    //public function getContent() returns string|error;

    // Acknowledgement related stuffs
    //public remote function ack();

    //extern function test();
};

public type MessageConfiguration record {
    // string destination = "queue/test";
    // string ackMode = "auto";
};

//function Message.getContent() returns string|error{
//
//    io:println(untaint self.message);
//    io:println("Message received is: ", self.message );
//
//    return self.message;
//}

//function Message.getContent() returns string|error{
//    byte[] bytes = content.toByteArray("UTF-8");
//        io:ReadableByteChannel byteChannel = io:createReadableChannel(bytes);
//        io:ReadableCharacterChannel characterChannel =
//        new io:ReadableCharacterChannel(byteChannel, "UTF-8");
//        var str = characterChannel.read(500);
//        if (str is string) {
//            io:println(untaint str);
//        } else {
//            io:println(str);
//        }
//}
//remote function Message.ack() {
//
//    // string getAckType = stompConfig.acknowledgementMode;
//
//    // if (getAckType == "client-individual" ){
//    //     io:println("Ack mode is client individual");
//    //     var clientIndividual = self->clientIndividual();
//    // }
//
//    // stomp:Listener socketListener = self.socketListener;
//    // string ackIndividualFrame = "ACK" + "\n" + "receipt-id:" + "reciept-123" + "\n" + "\n" + self.endOfFrame;
//
//}
