package cn.heshiqian.database.impl;

import cn.heshiqian.database.Column;
import cn.heshiqian.database.exception.ColumnTypeIllegalException;

import java.io.Serializable;

public class ColumnImpl implements Column, Serializable {

    private static final long serialVersionUID = 298230781123529238L;
    private String columnName;
    private Object data;

    public ColumnImpl(String columnName, Object data) {
        this.columnName = columnName;
        this.data = data;
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    @Override
    public String getString() {
        return String.valueOf(data);
    }

    @Override
    public int getInt() {
        try {
            return (int) data;
        }catch (Exception e){
            throw new ColumnTypeIllegalException("数据不是Int类型！",e);
        }
    }

    @Override
    public double getDouble() {
        try {
            return (double) data;
        }catch (Exception e){
            throw new ColumnTypeIllegalException("数据不是Double类型！",e);
        }
    }

    @Override
    public long getLong() {
        try {
            return (long) data;
        }catch (Exception e){
            throw new ColumnTypeIllegalException("数据不是Long类型！",e);
        }
    }

    @Override
    public Object getOrigin() {
        return data;
    }

    @Override
    public String toString() {
        return String.valueOf(data);
    }
}
