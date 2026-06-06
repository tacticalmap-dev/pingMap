package fun.bm.pingmap.util;

public class TimeUtil {
    public static long diffS2C = 0;

    public static long getLocalTimeMillis() {
        return System.currentTimeMillis();
    }

    public static long getServerSideTimeMillis() {
        return getLocalTimeMillis() + diffS2C + ClientLatencyHelper.getMyLatency();
    }
}
