package cn.heshiqian.database;

import java.util.HashMap;
import java.util.List;

public interface Row {
    Column columnName(String cname);
    HashMap<String,Column> getColumns();
    int getColumnSize();
}
