package org.ballerinalang.stdlib.stomp.message;

/**
 * Each Stomp commands.
 *
 * @since 0.995.0
 */

enum StompCommand {
// Client-commands
CONNECT, SUBSCRIBE, ACK,

// Server-commands
CONNECTED, MESSAGE, ERROR
}
