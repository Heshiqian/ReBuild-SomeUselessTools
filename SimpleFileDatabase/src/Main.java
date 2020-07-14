import cn.heshiqian.database.*;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        Database instance = Database.getInstance();

//        Table test = instance.getTable("test");

        Table test = instance.createNewTable("test1", new String[]{"name", "age", "sex"});

        String[] strings = {"name", "age", "sex"};
        for (int i = 0; i < 120000; i++) {
            test.addRow(strings,"hsq"+i,22+(i%8),i%2==0?"女":"男");
        }
        test.save();

//        test.save();

//        instance.saveAll();

        Row rowByIndex = test.getQuery().getRowByIndex(100);
        System.out.println(rowByIndex);

    }
}
