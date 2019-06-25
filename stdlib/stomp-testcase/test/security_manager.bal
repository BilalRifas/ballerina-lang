import ballerina/io;
import ballerina/stomp;
import ballerina/log;
import ballerina/http;

string motionDestination = "/queue/ownerMotionDetector";
string smokeDestination = "/queue/ownerSmokeDetector";
string heatDestination = "/queue/ownerHeatDetector";

string policeStationDestination = "/queue/policeStation";
string fireBrigadeDestination = "/queue/fireStation";

boolean motion = false;
boolean smoke = false;
boolean heat = false;

// Type definition for a owner detail.
type DetailOrder record {
    string publicService?;
    string branch?;
    string contactNumber?;
    string ownerName?;
};

// Global variable containing all the available owner.
json[] userDetail = ["Name:John", "Phone:0771234567", "Address:No105", "City:Colombo", "Profession:Doctor"];

@http:WebSocketServiceConfig {
    path: "/securitymanager/firestation"
}

service SimpleProxyService on new http:WebSocketListener(9090) {
    resource function onOpen(http:WebSocketCaller caller) {
        connectionsMap[caller.id] = caller;
    }
}

map<http:WebSocketCaller> connectionsMap = {};

stomp:Sender sensorManager = new({
        host: "localhost",
        port: 61613,
        username: "guest",
        password: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

// This initializes a stomp listener using the created tcp connection.
listener stomp:Listener consumerEndpoint = new({
        host: "localhost",
        port: 61613,
        username: "guest",
        password: "guest",
        vhost: "/",
        acceptVersion: "1.1"
    });

// Service endpoint.
listener http:Listener httpListener = new(9091);

http:Request backendreq = new;

@http:ServiceConfig {
    basePath: "/detailstore"
}
service userStoreService on httpListener {
    // Resource that allows users to place an order for a owner.
    @http:ResourceConfig {
        methods: ["POST"],
        consumes: ["application/json"],
        produces: ["application/json"]
    }

    resource function placeOrder(http:Caller caller, http:Request request) {
        backendreq = untaint request;
        http:Response response = new;
        DetailOrder newOrder = {};
        json requestPayload = {};

        var payload = request.getJsonPayload();
        // Try parsing the JSON payload from the request.
        if (payload is json) {
            // Valid JSON payload.
            requestPayload = payload;
        } else {
            // NOT a valid JSON payload.
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

        // Order details.
        newOrder.publicService = publicService.toString();
        newOrder.branch = branch.toString();
        newOrder.contactNumber = contact.toString();
        newOrder.ownerName = ownerName.toString();

        // Boolean variable to track the availability of a requested owner.
        boolean isOwnerAvailable = false;
        // Check the availability of the requested owner.
        foreach var name in userDetail {
            if (newOrder.ownerName.equalsIgnoreCase(ownerName.toString())) {
                isOwnerAvailable = true;
                break;
            }
        }

        json responseMessage;
        // If the requested owner is available, then add the order to the 'OrderQueue'.
        if (isOwnerAvailable) {
            var ownerOrderDetails = json.convert(newOrder);

            if(ownerOrderDetails is json) {

                    log:printInfo("order will be added to the order  Queue; PublicServiceName: '" + newOrder.ownerName +
                            "', OrderedPhone: '" + newOrder.ownerName + "';");

                    // Send the message to the STOMP queue.
                    var stompQueueMessage = sensorManager->dualChannelSend(ownerOrderDetails.toString(),"/queue/orderQueue","");

                    // Construct a success message for the response.
                    responseMessage = { "Message":
                    "Your order was successfully placed. Ordered owner address will be delivered soon" };
            } else {
                responseMessage = { "Message": "Invalid order delivery details" };
            }
        } else {
            // If owner is not available, construct a proper response message to notify user.
            responseMessage = { "Message": "Requested address not available" };
        }

        // Send response to the user.
        response.setJsonPayload(responseMessage);
        checkpanic caller->respond(response);

        //runtime:sleep(3000);
    }
    // Resource that allows users to get a list of all the available owner.
    @http:ResourceConfig { methods: ["GET"], produces: ["application/json"] }
    resource function getUserDetail(http:Caller httpClient, http:Request request) {
        http:Response response = new;
        // Send json array 'userDetail' as the response, which contains all the available owner.
        response.setJsonPayload(userDetail);
        checkpanic httpClient->respond(response);
    }
}

// Add service config.
@stomp:ServiceConfig{
    destination:"/queue/motionDetector",
    ackMode: stomp:AUTO
}

// This binds the created consumer to the listener service.
service managerMotionDetector on consumerEndpoint  {
    // This resource is invoked when a message is received.
    // Message object only gives us the string message.
    resource function onMessage(stomp:Message message) {
        var messageId = message.getMessageId();
        var content = message.getContent();
        log:printInfo("Message: " + content + "\n" + "Message Id: " + messageId + "\n");
        log:printInfo("Motion detector");
        var motionDetector = sensorManager->send("Motion Sensor has detected", motionDestination);
        var alertPolice = sensorManager->send("Motion Sensor has detected danger. Suspicious activity detected", policeStationDestination);
    }

    // This resource is invoked when the connection is interrupted.
    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
}

@stomp:ServiceConfig{
    destination:"/queue/smokeDetector",
    ackMode: stomp:AUTO
}

service managerSmokeDetector on consumerEndpoint  {
    // This resource is invoked when a message is received.
    // Message object only gives us the string message.
    resource function onMessage(stomp:Message message) {
        string fireMsg = "Smoke Sensor has detected danger. Danger! smoke detected. Fire Brigates please hurry to the destination. ";
        var sendSmoke = broadcast(fireMsg);

        log:printInfo("broadcasted to websocket Client");
        var content = message.getContent();
        log:printInfo("Smoke detector");
        log:printInfo(content);
        var smokeDetector = sensorManager->send("Smoke Sensor has detected", smokeDestination);
        var alertFireBrigade1 = sensorManager->send(fireMsg, fireBrigadeDestination);
    }

    // This resource is invoked when the connection is interrupted.
    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
}

// Add service config.
@stomp:ServiceConfig{
    destination:"/queue/orderQueue",
    ackMode: stomp:AUTO
}

// This binds the created consumer to the listener service.
service stompHttpRequestService on consumerEndpoint  {
    // This resource is invoked when a message is received.
    // Message object only gives us the string message.
    resource function onMessage(stomp:Message message) {
        var content = message.getContent();
        log:printInfo("HTTP request service");
        log:printInfo(content);
        string payload = content;

        // --- http related stuffs --- //
        log:printInfo("New order successfully received from the Order Queue");

        log:printInfo("Order Details: " + payload);

        // Send order queue details to delivery queue.
        http:Request enrichedreq = backendreq;
        var clientResponse = ownerDetailDeliveryServiceEP->forward("/", enrichedreq);
        if (clientResponse is http:Response) {
            log:printInfo("Order details were sent to owner detail delivery service.");
        } else {
            log:printError("Order details were not sent to owner detail delivery service.");
        }
    }

    // This resource is invoked when the connection is interrupted.
    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
}

@stomp:ServiceConfig{
    destination:"/queue/heatDetector",
    ackMode: stomp:AUTO
}

service managerHeatDetector on consumerEndpoint  {
    // This resource is invoked when a message is received.
    // Message object only gives us the string message.
    resource function onMessage(stomp:Message message) {
        var content = message.getContent();
        log:printInfo("Heat detector");
        log:printInfo(content);
        string fireMsg = "Heat Sensor has detected danger. Danger! smoke detected";
        var heatDetector = sensorManager->send("Heat Sensor has detected", heatDestination);
    }

    // This resource is invoked when the connection is interrupted.
    resource function onError(error er) {
        log:printError("An error occured", err = er);
    }
}

// Function to perform the broadcasting of text messages to webSocket client.
function broadcast(string text) {
    http:WebSocketCaller ep;
    foreach var (id, con) in connectionsMap {
        ep = con;
        var err = ep->pushText(text);
        if (err is error) {
            log:printError("Error sending message", err = err);
        }
    }
}

http:Client ownerDetailDeliveryServiceEP = new("http://localhost:9092/deliveryDetails/sendDelivery");
