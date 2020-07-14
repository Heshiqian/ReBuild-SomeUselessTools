package cn.heshiqian.database.exception;

public class ColumnIllegalException extends IllegalArgumentException {
    private static final long serialVersionUID = 8959810027135317148L;

    public ColumnIllegalException(String s) {
        super(s);
    }

    public ColumnIllegalException(String message, Throwable cause) {
        super(message, cause);
    }

    public ColumnIllegalException(Throwable cause) {
        super(cause);
    }
}
