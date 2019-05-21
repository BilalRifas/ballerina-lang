package org.ballerinalang.stdlib.stomp.message;

/**
 * Each Stomp commands.
 *
 * @since 0.995.0
 */
enum StompCommand {
// client-commands
CONNECT, SEND, SUBSCRIBE, UNSUBSCRIBE, BEGIN, COMMIT, ABORT, ACK, DISCONNECT,

// server-commands
CONNECTED, MESSAGE, RECEIPT, ERROR, DISCONNECTED
}
