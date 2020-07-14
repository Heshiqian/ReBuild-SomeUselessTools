package cn.heshiqian.database;

import cn.heshiqian.database.exception.ColumnIllegalException;
import cn.heshiqian.database.exception.InitDatabaseException;
import cn.heshiqian.database.exception.TableNotExistException;
import cn.heshiqian.database.tool.ExecuteTimer;
import cn.heshiqian.database.tool.Log;
import cn.heshiqian.database.tool.Utils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Database {

    public static class Property{
        private boolean enabledBigDataMode = false;
        private boolean objectStreamUseOffset = false;
        private int offsetSize = 0x13;
        public boolean isEnabledBigDataMode() {
            return enabledBigDataMode;
        }
        public void setEnabledBigDataMode(boolean enabledBigDataMode) {
            this.enabledBigDataMode = enabledBigDataMode;
        }
        public boolean isObjectStreamUseOffset() {
            return objectStreamUseOffset;
        }
        public void setObjectStreamUseOffset(boolean objectStreamUseOffset) {
            this.objectStreamUseOffset = objectStreamUseOffset;
        }
        public int getOffsetSize() {
            return offsetSize;
        }
        public void setOffsetSize(int offsetSize) {
            this.offsetSize = offsetSize;
        }
    }

    private static final Property dbProperties = new Property();

    private static final byte[] TABLE_FLAG_BG = {0x0, 0x0, 0x0, 0x0, 0x33, 0x34};
    private static final byte[] TABLE_FLAG_EOF = {0x0, 0x0, 0x0, 0x0, 0x35, 0x36};
    private static final byte[] MODE_BIG = {0x0, 0x55, 0x55, 0x55};
    private static final byte[] MODE_NORMAL = {0x0, 0x56, 0x56, 0x56};
    public static final int MODE_HEAD_LENGTH = 4;
    public static final String DATABASE_ENCODE = "UTF-8";
    private static final Object lock = new Object();

    private File databaseFile;
    private HashMap<Table, TableIndex> tableIndexMap = new HashMap<>();
    private HashMap<String, Table> tables = new HashMap<>();

    private static boolean ROLL_BACK_FLAG = false;

    /**
     * 文件大小大约为10MB以上，视文件系统返回值为准
     */
    private static final int DB_TOO_BIG = 1024 * 1024 * 10;

    /**
     * 初始化数据库表和数据
     */
    private void initDatabaseData() {
        try {
            FileInputStream dfis = new FileInputStream(databaseFile);
            byte[] headCheck = new byte[4];
            int read = dfis.read(headCheck);
            if (read!=4) throw new InitDatabaseException("文件头错误");
            if (dbProperties.isEnabledBigDataMode()){
                if (!Utils.checkFileHead(MODE_BIG,headCheck)) throw new InitDatabaseException("不是BIG模式文件");
                initDatabaseDataPlanB(dfis);
            }else {
                if (!Utils.checkFileHead(MODE_NORMAL,headCheck)) throw new InitDatabaseException("不是NORMAL模式文件");
                initDatabaseDataPlanA(dfis);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            Log.error("初始化数据库失败！");
        }
    }

    private void initDatabaseDataPlanA(FileInputStream fis) throws IOException, ClassNotFoundException {
        long size = fis.getChannel().size() - MODE_HEAD_LENGTH;
        //证明为新库，没必要继续执行下面的代码
        if (size == 0) return;
        if (size > DB_TOO_BIG){
            Log.info("[waring] 数据量过大，在保存或加载时可能过慢，建议使用大数据量模式存储");
        }
        tableIndexMap.clear();
        tables.clear();
        byte[] memoryByte = new byte[(int) size];
        int read = fis.read(memoryByte);
        if (size > read) {
            Log.error("数据库长度过长！无法读取数据库");
            return;
        }
        buildTableLinked(size, memoryByte);
        fis.close();
    }

    private void initDatabaseDataPlanB(FileInputStream fis) throws IOException, ClassNotFoundException {
        long size = fis.getChannel().size() - MODE_HEAD_LENGTH;
        //证明为新库，没必要继续执行下面的代码
        if (size == 0) return;


        tableIndexMap.clear();
        tables.clear();




        fis.close();
    }

    private void buildTableLinked(long size, byte[] memoryByte) throws IOException, ClassNotFoundException {
        tableIndexMap.clear();
        tables.clear();
        int readed = 0;
        while (readed < size) {
            int tableStart = Utils.getByteIndexOf(memoryByte, TABLE_FLAG_BG, readed, memoryByte.length);
            int tableEnd = Utils.getByteIndexOf(memoryByte, TABLE_FLAG_EOF, readed, memoryByte.length);
            if (tableStart != -1 && tableEnd != -1) {
                //发现表
                byte[] bytes = Arrays.copyOfRange(memoryByte, tableStart + 6, tableEnd);
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bais);
                Table table = (Table) ois.readObject();
                tableIndexMap.put(table, new TableIndex(tableStart, tableEnd));
                tables.put(table.getTableName(), table);
                ois.close();
            }
            readed = tableEnd + 6;
        }
    }

    private AtomicBoolean saving = new AtomicBoolean(false);

    /**
     * 将当前内存中的数据，覆盖至文件中
     * 此操作会导致原文件数据丢失！
     * 此操作为异步，且无备份，若在运行过程中强制退出，会导致数据丢失！
     */
    public void saveAll(){
        if (!saving.get()){
            saving.set(true);
            Thread savingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.info("开始保存数据...");
                    ExecuteTimer.Timer timer = ExecuteTimer.getNewTimerAndBeginCount();
                    BufferedOutputStream bos = null;
                    try {
                        bos = new BufferedOutputStream(new FileOutputStream(databaseFile));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (bos == null) {
                        Log.error("打开文件失败");
                        return;
                    }
                    //写入文件标识头
                    try{
                        if (dbProperties.isEnabledBigDataMode()) {
                            bos.write(MODE_BIG);
                        }else {
                            bos.write(MODE_NORMAL);
                        }
                        bos.flush();
                    }catch (IOException e){
                        e.printStackTrace();
                        Log.error("标识头写入失败");
                        return;
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 1024 * 10);
                    for (Map.Entry<String, Table> next : tables.entrySet()) {
                        Table table = next.getValue();
                        try {
                            baos.write(TABLE_FLAG_BG);
                            ObjectOutputStream oos = new ObjectOutputStream(baos);
                            oos.writeObject(table);
                            baos.write(TABLE_FLAG_EOF);
                            baos.writeTo(bos);
                            bos.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            baos.reset();
                        }
                    }
                    Log.info("保存完毕... " + timer.stop() + "ms");
                }
            });
            savingThread.setDaemon(false);
            savingThread.start();
        }
    }

    /**
     * 保存某个表
     * 若不为大数量存储方式则此方法为阻塞方法
     * @param table 即将保存的表
     */
    public void save(Table table) {
        backupOriginDatabase();
        try {
            if (!dbProperties.isEnabledBigDataMode()) {
                //默认不使用大数据量存储方式
                if (!tableIndexMap.containsKey(table)) {
                    //不存在该表的索引，证明该表为新表，追加到文件中
                    saveTableToAfterFileEnd(table);
                }else {
                    //存在该表，需要移动偏移
                    //耗时

                }
            } else {
                //大数量存储
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.error("保存表失败");
            synchronized (lock) {
                setRollBackFlag();
            }
        } finally {
            recoverOriginDatabase();
        }

        /*if (tableIndexMap.containsKey(table)) {
            //已存在则替换，需要前后合并
            try {
                backupOriginDatabase();
                ExecuteTimer.Timer timer = ExecuteTimer.getNewTimerAndBeginCount();
                TableIndex tableIndex = tableIndexMap.get(table);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(table);
                oos.flush();
                FileInputStream dfis = new FileInputStream(databaseFile);
                long size = dfis.getChannel().size();
                byte[] memoryByte = new byte[(int) size];
                dfis.read(memoryByte);
                byte[] objBytes = baos.toByteArray();
                byte[] backBytes = Arrays.copyOfRange(memoryByte, tableIndex.end, memoryByte.length);
                byte[] frontBytes = Arrays.copyOfRange(memoryByte, 0, tableIndex.start + 6);
                FileOutputStream fos = new FileOutputStream(databaseFile);
                fos.write(frontBytes);
                fos.write(objBytes);
                fos.write(backBytes);
                fos.close();
                rebuildTableLinked();
                Log.info("已保存表");
                Log.info("保存时间："+timer.stop()+"ms");
            } catch (IOException e) {
                e.printStackTrace();
                Log.error("保存表失败");
                synchronized (lock){
                    setRollBackFlag();
                }
            } finally {
                recoverOriginDatabase();
            }*/

    }

    protected void saveTableToAfterFileEnd(Table table) throws IOException{
        ExecuteTimer.Timer timer = ExecuteTimer.getNewTimerAndBeginCount();
        // 应该没有人文件大小大于int范围还在用我这种"数据库"吧
        // A:你能再表演一下那个吗?
        // B:这个吗?
        // B:不会吧，不会吧，不会还有人不知道MySQL吧.
        // A:哈哈哈哈!
        int oldLength = (int) databaseFile.length();
        // 写入文件
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(databaseFile,true));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        bufferedOutputStream.write(TABLE_FLAG_BG);
        oos.writeObject(table);
        oos.flush();
        bufferedOutputStream.write(baos.toByteArray());
        bufferedOutputStream.write(TABLE_FLAG_EOF);
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
        oos.close();
        // 重新计算该表所占位置
        int size = baos.size();
        tableIndexMap.put(table, new TableIndex(oldLength + 6, oldLength + size));
        Log.info("保存完毕..."+timer.stop()+"ms");
    }

    private void rebuildTableLinked() {
        ExecuteTimer.Timer timer = ExecuteTimer.getSingletonTimer();
        timer.start();
        try {
            FileInputStream dfis = new FileInputStream(databaseFile);
            long size = dfis.getChannel().size();
            byte[] memoryByte = new byte[(int) size];
            dfis.read(memoryByte);
            buildTableLinked(size, memoryByte);
            dfis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            Log.error("初始化数据库失败！");
        }
        long stop = timer.stop();
        Log.info("重新建立映射花费 --> "+stop+"ms");
    }

    private void recoverOriginDatabase() {
        if (ROLL_BACK_FLAG){
            clearRollBackFlag();
            try {
                if (databaseFile.exists()) databaseFile.delete();
                FileInputStream fis = new FileInputStream("DB.temp");
                FileChannel channel = fis.getChannel();
                FileOutputStream fos = new FileOutputStream(databaseFile.getName());
                channel.transferTo(0, channel.size(), fos.getChannel());
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        deleteTempDBFile();
    }

    private void deleteTempDBFile() {
        new File("DB.temp").delete();
    }

    private void backupOriginDatabase() {
        ExecuteTimer.Timer time = ExecuteTimer.getNewTimerAndBeginCount();
        try {
            FileInputStream fis = new FileInputStream(databaseFile);
            FileChannel channel = fis.getChannel();
            FileOutputStream fos = new FileOutputStream("DB.temp");
            channel.transferTo(0, channel.size(), fos.getChannel());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            Log.info("备份源库耗时："+time.stop()+"ms");
        }
    }

    private void setRollBackFlag(){
        ROLL_BACK_FLAG = true;
    }
    private void clearRollBackFlag(){
        ROLL_BACK_FLAG = false;
    }

    public Table getTable(String tableName){
        Table table = tables.get(tableName);
        if (table==null) throw new TableNotExistException("表："+tableName+"没有找到！");
        Utils.setInternalObjectField(table,"parentDB",this,true);
        return table;
    }

    public Table createNewTable(String tableName, String[] columnNames){
        if (tables.containsKey(tableName)){
            throw new IllegalStateException("已存在表:"+tableName);
        }
        ExecuteTimer.Timer timer = ExecuteTimer.getNewTimerAndBeginCount();
        Table table = new Table(this);
        table.setTableName(tableName);
        table.setRows(new ArrayList<>());
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i]==null||columnNames[i].equals("")){
                throw new ColumnIllegalException("列:"+i+" 内容:'"+ columnNames[i]+"' |why: 名称不合法");
            }
        }
        table.setColumnNames(columnNames);
//        table.save();
        Log.info("新建表:"+tableName+" 列:"+ Arrays.toString(columnNames));
        tables.put(tableName,table);
        long stop = timer.stop();
        Log.info("新建表花费:"+stop+"ms");
        return table;
    }

    public void setDatabaseFile(File databaseFile) {
        this.databaseFile = databaseFile;
    }

    public static Property getDbProperties() {
        return dbProperties;
    }

    //单例模式
    private Database() {
    }

    private static class DatabaseHolder {
        private static final HashMap<String,Database> dholder = new HashMap<>();

        public static Database singleton(String dbName) {
            Database database = dholder.get(dbName);
            if (database == null){
                database = loadDatabaseIntoHolder(dbName);
            }
            return database;
        }

        private static Database loadDatabaseIntoHolder(String dbName) {
            Database database = new Database();
            File file = new File(dbName);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    FileOutputStream fos = new FileOutputStream(file);
                    if (Database.dbProperties.isEnabledBigDataMode()){
                        fos.write(Database.MODE_BIG);
                    }else {
                        fos.write(Database.MODE_NORMAL);
                    }
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.error("新建数据库时，发生错误！");
                }
            }
            database.setDatabaseFile(file);
            ExecuteTimer.Timer timer = ExecuteTimer.getSingletonTimer();
            timer.start();
            database.initDatabaseData();
            Log.info("数据库加载花费:" + timer.stop() + "ms");
            dholder.put(dbName,database);
            return database;
        }
    }

    public static Database getInstance(String dbName){
        return Database.DatabaseHolder.singleton(dbName);
    }

    public static Database getInstance() {
        return getInstance("DATABASE.dat");
    }

    private static class TableIndex {
        int start;
        int end;

        public TableIndex(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
