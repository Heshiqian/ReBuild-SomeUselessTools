package cn.heshiqian.database;

import cn.heshiqian.database.impl.QueryWhereImpl;

public interface Query {

    Row getRowByIndex(int idx);

    Row[] getRowsByWhere(Where where);


    public static Where where(){
        return new QueryWhereImpl();
    }
    interface Where {
        Where equals(String name, String arg);
        Where like(String name, String exp);
        Where contain(String name, String arg);
        String[] getSyntax();
    }
}
