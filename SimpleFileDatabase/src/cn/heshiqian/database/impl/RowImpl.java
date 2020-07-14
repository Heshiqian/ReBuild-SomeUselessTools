package cn.heshiqian.database.impl;

import cn.heshiqian.database.Column;
import cn.heshiqian.database.Row;

import java.io.Serializable;
import java.util.HashMap;

public class RowImpl implements Row, Serializable {

    private static final long serialVersionUID = -1148854977806377574L;

    private HashMap<String, Column> columnHashMap = new HashMap<>();

    public RowImpl(HashMap<String, Column> columnHashMap) {
        this.columnHashMap = columnHashMap;
    }

    @Override
    public Column columnName(String cname) {
        return columnHashMap.get(cname);
    }

    @Override
    public HashMap<String, Column> getColumns() {
        return columnHashMap;
    }

    @Override
    public int getColumnSize() {
        return columnHashMap.size();
    }

    @Override
    public String toString() {
        return "{ Data=" + columnHashMap + '}';
    }
}
