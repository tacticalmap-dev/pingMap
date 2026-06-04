package fun.bm.pingmap.util;

public class TimeUtil {

    public static long getClientSideTimeMillis() {
        return System.currentTimeMillis();
    }

    public static long getServerSideTimeMillis() {
        return getClientSideTimeMillis() + ClientLatencyHelper.getMyLatency();
    }
}
