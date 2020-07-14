package cn.heshiqian.database;

import cn.heshiqian.database.exception.ColumnNotExistException;
import cn.heshiqian.database.impl.QueryImpl;
import cn.heshiqian.database.impl.QueryWhereImpl;
import cn.heshiqian.database.impl.RowImpl;

import java.io.Serializable;
import java.util.*;

public class Table implements Serializable {
    private static final long serialVersionUID = 2595313727892554415L;

    private transient Database parentDB;
    private String tableName;
    private List<Row> rows;
    private Date updateTime;
    private String[] columnNames;
    private int size;
    private Query query=new QueryImpl(this);

    public Table(){}
    public Table(Database database){
        this.parentDB=database;
    }

    public String getTableName() {
        return tableName;
    }

    public List<Row> getRows() {
        return rows;
    }

    public Table addRow(String[] columnNames, Object... columns){
        if (columnNames==null||columns==null||columns.length==0){
            throw new IllegalArgumentException("列输入不合法，含空或者null.");
        }
        String[] rs = checkColumns(columnNames);
        if (rs!=null) throw new ColumnNotExistException(rs);

        HashMap<String, Column> columnHashMap = new HashMap<>();
        RowImpl row = new RowImpl(columnHashMap);
        for (int c = 0; c < columnNames.length; c++) {
            if (c < columns.length){
                columnHashMap.put(columnNames[c],Column.column(columnNames[c],columns[c]));
            }else {
                columnHashMap.put(columnNames[c],Column.column(columnNames[c],""));
            }
        }
        rows.add(row);
        size = rows.size();//重新计算，防止中间出现问题但是长度却变化了
        return this;
    }

    public Table removeByWhere(Query.Where where){
        QueryWhereImpl queryWhere = (QueryWhereImpl)where;
        HashMap<String, String> kv = queryWhere.getKv();
        Iterator<Map.Entry<String, String>> iterator;
        Iterator<Row> rowIterator = getRows().iterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            iterator = kv.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                String key = next.getKey();
                String value = next.getValue();
                Column column = row.getColumns().get(key);
                if (column != null && column.getString().equals(value))
                    rowIterator.remove();
            }
        }
        return this;
    }

    public Table removeRow(int index){
        rows.remove(index);
        size = rows.size();//重新计算，防止中间出现问题但是长度却变化了
        return this;
    }

    public void sortRows(Comparator<Row> comparator){
        rows.sort(comparator);
    }
    public void sortRows(Comparator<Row> comparator, boolean save){
        rows.sort(comparator);
        if (save){
            save();
        }
    }

    private String[] checkColumns(String[] src) {
        if (Arrays.equals(getColumnNames(), src)) {
            return null;
        }else {
            ArrayList<String> strings = new ArrayList<>();
            List<String> names = Arrays.asList(columnNames);
            for (String s : src) {
                if (!names.contains(s))
                    strings.add(s);
            }
            return strings.toArray(new String[]{});
        }
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public int getColumnCount(){
        return columnNames.length;
    }

    public Query getQuery() {
        return query;
    }

    public void reload(){
        //empty
    }

    public void save(){
        updateTime = new Date();
        parentDB.save(this);
    }

    @Override
    public int hashCode() {
        return tableName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        Table t2 = (Table) obj;
        return t2.getTableName().equals(this.tableName);
    }

    @Override
    public String toString() {
        return "Table{" +
                "tableName='" + tableName + '\'' +
                ", updateTime=" + updateTime +
                '}';
    }


    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Database getParentDB() {
        return parentDB;
    }

}
