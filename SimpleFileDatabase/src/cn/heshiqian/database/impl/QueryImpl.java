package cn.heshiqian.database.impl;


import cn.heshiqian.database.Column;
import cn.heshiqian.database.Query;
import cn.heshiqian.database.Row;
import cn.heshiqian.database.Table;
import cn.heshiqian.database.tool.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class QueryImpl implements Query, Serializable {

    private static final long serialVersionUID = 6393764654224988149L;
    private Table table;

    public QueryImpl(Table table) {
        this.table = table;
    }

    @Override
    public Row getRowByIndex(int idx) {
        if (idx >= table.getRows().size())
            throw new IndexOutOfBoundsException("不存在这个数据下标: "+idx);
        return table.getRows().get(idx);
    }

    @Override
    public Row[] getRowsByWhere(Where where) {
        ArrayList<Row> rows = new ArrayList<>();
        String[] syntax = where.getSyntax();
        String[][] ready = new String[syntax.length][];
        for (int i = 0; i < ready.length; i++) {
            ready[i] = Utils.splitByAndSymbol(syntax[i]);
        }
        int whereCount = syntax.length;
        int shootCount;
        for (Row row : table.getRows()) {
            shootCount = 0;
            for (String[] exp : ready) {
                String key = exp[0];
                String name = key.substring(4);
                String value = exp[1];
                Column column = row.getColumns().get(name);
                if (key.startsWith("EQL_")) {
                    if (column != null && column.getString().equals(value))
                        shootCount++;
                } else if (key.startsWith("EXP_")) {
                    if (column != null && column.getString().matches(value))
                        shootCount++;
                } else if (key.startsWith("COT_")) {
                    if (column != null && column.getString().contains(value))
                        shootCount++;
                }
            }
            //如果一行中，判断条件重合次数与where长度相同则代表该Row为要寻找的Row
            if (shootCount == whereCount)
                rows.add(row);
        }
        return rows.toArray(new Row[]{});
    }


}
