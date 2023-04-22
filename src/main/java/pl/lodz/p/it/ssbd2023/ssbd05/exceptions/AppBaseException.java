package pl.lodz.p.it.ssbd2023.ssbd05.exceptions;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class AppBaseException extends Exception {

    public AppBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppBaseException(Throwable cause) {
        super(cause);
    }

    public AppBaseException() {
        super();
    }
}
