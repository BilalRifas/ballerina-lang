package org.ballerinalang.stdlib.stomp.message;

/**
 * Each Stomp commands.
 *
 * @since 0.995.0
 */
enum StompCommand {
// Client-commands
CONNECT, SEND, SUBSCRIBE, UNSUBSCRIBE, BEGIN, COMMIT, ABORT, ACK, DISCONNECT,

// Server-commands
CONNECTED, MESSAGE, RECEIPT, ERROR, DISCONNECTED
}
