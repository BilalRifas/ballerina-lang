import ballerina/io;
import ballerina/log;

public type Message object {

    //public string message = "";
    //public string receiptId = "";
  
    // End of frame used a null octet (^@ = \u0000).
    // public string endOfFrame = "\u0000";

    public extern function getContent() returns string|error;
};

public type MessageConfiguration record {

};

