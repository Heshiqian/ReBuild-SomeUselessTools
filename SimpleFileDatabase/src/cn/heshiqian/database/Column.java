package cn.heshiqian.database;


import cn.heshiqian.database.impl.ColumnImpl;

public interface Column {
    String getColumnName();
    String getString();
    int getInt();
    double getDouble();
    long getLong();
    Object getOrigin();

    public static Column column(String key, Object data){
        return new ColumnImpl(key,data);
    }
}
