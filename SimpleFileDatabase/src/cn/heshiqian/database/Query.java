package cn.heshiqian.database;


import cn.heshiqian.database.impl.QueryWhereImpl;

public interface Query {

    Row getRowByIndex(int idx);

    Row[] getRowsByWhere(Where where);


    interface Where {
        Where addWhere(String name, String arg);
        static Where where(String name, String arg){
            QueryWhereImpl queryWhere = new QueryWhereImpl();
            queryWhere.addWhere(name,arg);
            return queryWhere;
        }
    }
}
