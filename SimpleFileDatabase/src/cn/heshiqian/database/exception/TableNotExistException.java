package cn.heshiqian.database.exception;

public class TableNotExistException extends RuntimeException {
    private static final long serialVersionUID = -6468280162605670284L;

    public TableNotExistException(String message) {
        super(message);
    }

    public TableNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public TableNotExistException(Throwable cause) {
        super(cause);
    }
}
