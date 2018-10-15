package com.bachecubano.nautabackgroundlibrary.exceptions;

class NotNetworkException extends RuntimeException {

    public NotNetworkException() {
        super("NotNetworkException, you need internet connection to send the email");
    }

    public NotNetworkException(String detailMessage) {
        super(detailMessage);
    }
}
