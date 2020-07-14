package cn.heshiqian.database.impl;


import cn.heshiqian.database.Column;
import cn.heshiqian.database.Query;
import cn.heshiqian.database.Row;
import cn.heshiqian.database.Table;

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
        return table.getRows().get(idx);
    }

    @Override
    public Row[] getRowsByWhere(Where where) {
        ArrayList<Row> rows = new ArrayList<>();
        QueryWhereImpl queryWhere = (QueryWhereImpl)where;
        HashMap<String, String> kv = queryWhere.getKv();
        Iterator<Map.Entry<String, String>> iterator;
        int whereCount = kv.size();
        int shootCount;
        for (Row row : table.getRows()) {
            shootCount = 0;
            iterator = kv.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                String key = next.getKey();
                String value = next.getValue();
                Column column = row.getColumns().get(key);
                if (column != null && column.getString().equals(value))
                    shootCount++;
            }
            //如果一行中，判断条件重合次数与where长度相同则代表该Row为要寻找的Row
            if (shootCount==whereCount)
                rows.add(row);
        }
        return rows.toArray(new Row[]{});
    }


}
