package fun.bm.pingmap.config.remote;

public class RemoteCommonConfig {
    public static Integer serverPointPingLifetime = null;
    public static Integer serverEnemyPingLifetime = null;
    public static Integer serverFriendlyPingLifetime = null;
    public static Boolean serverMarkEnemyOnly = null;

    public static void setServerConfig(int pointSeconds, int enemySeconds, int friendlySeconds, boolean markEnemyOnly) {
        serverPointPingLifetime = pointSeconds;
        serverEnemyPingLifetime = enemySeconds;
        serverFriendlyPingLifetime = friendlySeconds;
        serverMarkEnemyOnly = markEnemyOnly;
    }

    public static void clearServerConfig() {
        serverPointPingLifetime = null;
        serverEnemyPingLifetime = null;
        serverFriendlyPingLifetime = null;
        serverMarkEnemyOnly = null;
    }
}
