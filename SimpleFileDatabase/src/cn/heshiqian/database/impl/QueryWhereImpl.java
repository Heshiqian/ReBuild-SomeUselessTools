package cn.heshiqian.database.impl;

import cn.heshiqian.database.Query;

import java.io.Serializable;
import java.util.HashMap;

public class QueryWhereImpl implements Query.Where, Serializable {

    private static final long serialVersionUID = 6645048893474406641L;

    private HashMap<String,String> kv=new HashMap<>();
    @Override
    public Query.Where addWhere(String name, String arg) {
        kv.put(name,arg);
        return this;
    }
    public HashMap<String, String> getKv() {
        return kv;
    }
}
