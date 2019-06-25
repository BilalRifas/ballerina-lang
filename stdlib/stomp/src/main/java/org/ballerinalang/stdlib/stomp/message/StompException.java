package org.ballerinalang.stdlib.stomp.message;

import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Stomp exception.
 *
 * @since 0.995.0
 */
public class StompException extends BallerinaException {

    private static final long serialVersionUID = 5475019401678519895L;
    private String message;

    public StompException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
