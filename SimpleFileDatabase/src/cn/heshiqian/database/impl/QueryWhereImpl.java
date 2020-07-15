package cn.heshiqian.database.impl;

import cn.heshiqian.database.Query;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class QueryWhereImpl implements Query.Where, Serializable {

    private static final long serialVersionUID = 6645048893474406641L;

    private HashMap<String,String> kv=new HashMap<>();
    public HashMap<String, String> getKv() {
        return kv;
    }

    @Override
    public Query.Where equals(String name, String arg) {
        kv.put("EQL_"+name,arg);
        return this;
    }

    @Override
    public Query.Where like(String name, String exp) {
        kv.put("EXP_"+name,exp);
        return this;
    }

    @Override
    public Query.Where contain(String name, String arg) {
        kv.put("COT_"+name,arg);
        return this;
    }

    @Override
    public String[] getSyntax() {
        String[] syntax = new String[kv.size()];
        int i=0;
        for (Map.Entry<String, String> entry : kv.entrySet()) {
            syntax[i++]=entry.getKey()+"&"+entry.getValue();
        }
        return syntax;
    }
}
