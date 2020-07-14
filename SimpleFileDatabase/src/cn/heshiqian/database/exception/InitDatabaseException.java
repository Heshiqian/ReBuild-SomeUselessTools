package cn.heshiqian.database.exception;

public class InitDatabaseException extends RuntimeException{

    public InitDatabaseException(String message) {
        super(message);
    }

    public InitDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public InitDatabaseException(Throwable cause) {
        super(cause);
    }
}
