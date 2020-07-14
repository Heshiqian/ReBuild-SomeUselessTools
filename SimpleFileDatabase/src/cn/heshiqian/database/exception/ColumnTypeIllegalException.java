package cn.heshiqian.database.exception;

public class ColumnTypeIllegalException extends IllegalArgumentException {
    private static final long serialVersionUID = 2746036291090595946L;

    public ColumnTypeIllegalException(String s) {
        super(s);
    }
    public ColumnTypeIllegalException(String message, Throwable cause) {
        super(message, cause);
    }
    public ColumnTypeIllegalException(Throwable cause) {
        super(cause);
    }
}
