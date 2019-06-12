import ballerina/http;
import ballerina/jms;
import ballerina/log;
import ballerina/stomp;

// Type definition for a owner detail.
type DetailDeliver record {
    string publicService?;
    string branch?;
    string contactNumber?;
    string ownerName?;
};

json[] userDetail = ["Name:John", "Phone:0771234567", "Address:No105", "City:Colombo", "Profession:Doctor"];

// Initialize a stomp sender.
stomp:Sender sensors = new({
        host: "localhost",
        port: 61613,
        login: "guest",
        passcode: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

// This initializes a stomp listener using the created tcp connection.
listener stomp:Listener consumerEndpoint = new({
        host: "localhost",
        port: 61613,
        login: "guest",
        passcode: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

// Add service config.
@stomp:ServiceConfig{
    destination:"/queue/deliveryQueue",
    ackMode: stomp:AUTO
}

// This binds the created consumer to the listener service.
service stompDualChannelService on consumerEndpoint  {
    // This resource is invoked when a message is received.
    // Message object only gives us the string message.
    resource function onMessage(stomp:Message message) {
        var content = message.getContent();
        log:printInfo("Stomp Dual Channel Service");
        log:printInfo(content);
    }
}

// Service endpoint.
listener http:Listener deliveryEP = new(9092);

@http:ServiceConfig {
    basePath: "/deliveryDetails"
}
// Owner detail store service, which allows users to order owner online for delivery.
service ownerDetailDeliveryService on deliveryEP {

    // Resource that allows users to place an order for a owner.
    @http:ResourceConfig {
        consumes: ["application/json"],
        produces: ["application/json"]
    }
    resource function sendDelivery(http:Caller caller, http:Request request) {
        http:Response response = new;
        DetailDeliver newDeliver = {};
        json requestPayload = {};

        log:printInfo("Received order details from the owner store service");

        // Try parsing the JSON payload from the request.
        var payload = request.getJsonPayload();
        if (payload is json) {
            // Valid JSON payload.
            requestPayload = payload;
        } else {
            response.statusCode = 400;
            response.setJsonPayload({ "Message": "Invalid payload - Not a valid JSON payload" });
            checkpanic caller->respond(response);
            return;
        }

        json publicService = requestPayload.PublicService;
        json branch = requestPayload.Branch;
        json contact = requestPayload.ContactNumber;
        json ownerName = requestPayload.Name;

        // If payload parsing fails, send a "Bad Request" message as the response.
        if (publicService == null || branch == null || contact == null || ownerName == null) {
                    response.statusCode = 400;
                    response.setJsonPayload({ "Message": "Bad Request - Invalid payload" });
                    checkpanic caller->respond(response);
                    return;
        }

        // Delivery details.
        newDeliver.publicService = publicService.toString();
        newDeliver.branch = branch.toString();
        newDeliver.contactNumber = contact.toString();
        newDeliver.ownerName = ownerName.toString();

        // Boolean variable to track the availability of a requested owner.
        boolean isOwnerAvailable = false;

        // Check the availability of the requested owner.
        foreach var name in userDetail {
            if (newDeliver.ownerName.equalsIgnoreCase(ownerName.toString())) {
                isOwnerAvailable = true;
                break;
            }
        }
        json responseMessage = {};

        // If the requested owner is available, then add the order to the 'OrderQueue'.
        if (isOwnerAvailable) {
            var ownerDeliverDetails = json.convert(newDeliver);

            // Send a STOMP message.
            if (ownerDeliverDetails is json) {

                    log:printInfo("Order delivery details added to the delivery queue'; CustomerName: '" + newDeliver.
                            publicService +
                            "', OwnerDetailName: '" + newDeliver.ownerName + "';");
                    // Send the message to the STOMP broker.
                    var stompQueueMessage = sensors->send(ownerDeliverDetails.toString(),"/queue/deliveryQueue");

                    // Construct a success message for the response.
                    responseMessage =
                    { "Message": "Your order was successfully placed. Ordered owner detail will be delivered soon" };
            } else {
                responseMessage =
                { "Message": "Failed to place the order, Invalid owner delivery details" };
            }
        }
        else {
            // If owner is not available, construct a proper response message to notify user.
            responseMessage = { "Message": "Requested owner not available" };
        }
        // Send response to the user
        response.setJsonPayload(responseMessage);
        checkpanic caller->respond(response);
    }
}