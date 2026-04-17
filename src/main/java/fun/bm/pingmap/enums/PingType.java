package fun.bm.pingmap.enums;

import fun.bm.pingmap.api.pingmanager.ping.Ping;
import fun.bm.pingmap.pingmanager.ping.EntityPing;
import fun.bm.pingmap.pingmanager.ping.PointPing;
import fun.bm.pingmap.pingmanager.ping.ServerPing;
import org.jetbrains.annotations.NotNull;

public enum PingType {
    Point(PointPing.class, "●", 0xFFFFFF00, true, 1),
    Enemy(EntityPing.class, "●", 0xFFFF0000, true, -1),
    Friendly(EntityPing.class, "●", 0x8000FFFF, false, -1),
    Server(ServerPing.class, null, null, null, -1);

    private final Class<? extends Ping> origin;
    private final String icon;
    private final Integer color;
    private final int maxPings;
    private final Boolean showDistance;

    PingType(Class<? extends Ping> clazz, String icon, Integer color, Boolean showDistance, int maxPings) {
        this.origin = clazz;
        this.icon = icon;
        this.color = color;
        this.showDistance = showDistance;
        this.maxPings = maxPings;
    }

    public static PingType fromOrdinal(int ordinal) {
        for (PingType type : values()) {
            if (type.ordinal() == ordinal) {
                return type;
            }
        }
        return null;
    }

    @NotNull
    public Ping newInstance() {
        try {
            return this.origin.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getIcon() {
        return icon;
    }

    public int getColor() {
        return color;
    }

    public int getMaxPings() {
        return maxPings == -1 ? Integer.MAX_VALUE : maxPings;
    }

    public boolean showDistance() {
        return showDistance;
    }
}
