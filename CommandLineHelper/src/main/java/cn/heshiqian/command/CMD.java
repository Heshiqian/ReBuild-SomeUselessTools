package cn.heshiqian.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class CMD {

    static class Properties{
        private String charset = "utf-8";
    }

    private static final Runtime runtime = Runtime.getRuntime();
    private static final SysRuntime OS = checkRuntime();
    private static final Properties properties = new Properties();

    public static class properties{
        public static void setCharset(String charset){
            properties.charset = charset;
        }
    }

    public static String run(String command, String... args) throws IOException {
        StringBuilder stringBuilder = new StringBuilder(command);
        for (String arg : args) {
            stringBuilder.append(' ')
                    .append(arg);
        }
        String cmd = stringBuilder.toString();
        System.out.println("execute : "+cmd);
        Process process = runtime.exec(cmd.split(" "));

        InputStream inputStream = process.getInputStream();
        InputStreamReader inputStreamReader;
        switch (OS){
            case WIN:
                inputStreamReader = new InputStreamReader(inputStream, "GBK");
                break;
            case LINUX:
                inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                break;
            default:
                throw new IOException("Unknown OS type!");
        }
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder resultBuilder = new StringBuilder();
        String temp="";
        while ((temp=bufferedReader.readLine())!=null){
            resultBuilder.append(temp)
                    //maybe need change line
                    .append(System.lineSeparator());
        }
        try {
            int exitCode = process.waitFor();
            if (exitCode!=0) throw new RuntimeException("Execution command:{"+cmd+"} return not 0.");
        } catch (InterruptedException e) {
            throw new IOException("Command execute error!",e);
        }
        return resultBuilder.toString();
    }

    public static SysRuntime checkRuntime() {
        String os = System.getenv("OS");
        String osName = System.getProperty("os.name");
        if (os.contains("Win")&&osName.contains("Win"))
            return SysRuntime.WIN;
        else
            return SysRuntime.LINUX;
    }


    public enum SysRuntime {WIN,LINUX}
}
