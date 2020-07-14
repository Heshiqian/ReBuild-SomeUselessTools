package cn.heshiqian.database.exception;

import java.util.Arrays;

public class ColumnNotExistException extends RuntimeException {
    private static final long serialVersionUID = -8097894101148623561L;

    public ColumnNotExistException(String[] strings){
        super("列:"+ Arrays.toString(strings)+" 不存在！");
    }

    public ColumnNotExistException(String message) {
        super(message);
    }

    public ColumnNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public ColumnNotExistException(Throwable cause) {
        super(cause);
    }

    public ColumnNotExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
