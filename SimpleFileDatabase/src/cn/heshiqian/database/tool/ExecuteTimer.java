package cn.heshiqian.database.tool;

public final class ExecuteTimer {

    private static final Timer t = new Timer();

    public static Timer getNewTimerAndBeginCount(){
        Timer timer = new Timer();
        timer.start();
        return timer;
    }

    public static Timer getSingletonTimer(){
        return t;
    }

    public static class Timer{
        @Override
        protected void finalize() throws Throwable {
            super.finalize();
        }

        long time;
        public void start() {
            time=System.currentTimeMillis();
        }
        public long stop(){
            return System.currentTimeMillis()-time;
        }
    }
}
