package cn.heshiqian.database.tool;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public final class Utils {

    public static boolean checkFileHead(byte[] head,byte[] src){
        return Arrays.equals(head,src);
    }

    public static boolean setInternalObjectField(Object source,String fieldName,Object obj,boolean access){
        try{
            Field declaredField = source.getClass().getDeclaredField(fieldName);
            declaredField.setAccessible(access);
            if (declaredField.get(source)==null) {
                declaredField.set(source,obj);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isFlag(byte[] flag, byte[] target, int length){
        for (int i = 0; i < length; i++) {
            if (flag[i]!=target[i]) return false;
        }
        return true;
    }

    public static int getByteIndexOf(byte[] sources, byte[] src, int startIndex, int endIndex) {
        if (sources == null || src == null || sources.length == 0 || src.length == 0) {
            return -1;
        }
        if (endIndex > sources.length) {
            endIndex = sources.length;
        }
        int i, j;
        for (i = startIndex; i < endIndex; i++) {
            if (sources[i] == src[0] && i + src.length-1 < endIndex) {
                for (j = 1; j < src.length; j++) {
                    if (sources[i + j] != src[j]) {
                        break;
                    }
                }
                if (j == src.length) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static Object invoke(Object o, String method, Object[] args,boolean access,Class<?>... parameterTypes) {
        try {
            Method declaredMethod = o.getClass().getDeclaredMethod(method, parameterTypes);
            declaredMethod.setAccessible(access);
            return declaredMethod.invoke(o, args);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static void sleepWithoutException(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
        }
    }
}
