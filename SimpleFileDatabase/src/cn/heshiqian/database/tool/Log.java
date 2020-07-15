package cn.heshiqian.database.tool;

import cn.heshiqian.database.Database;

public final class Log {

    private static final boolean silence = Database.getDbProperties().isSilenceMode();

    public static void info(String msg){
        if (silence) return;
        System.out.println(msg);
    }
    public static void error(String msg){
        if (silence) return;
        System.err.println(msg);
    }
}
