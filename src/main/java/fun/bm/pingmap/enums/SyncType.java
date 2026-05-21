package fun.bm.pingmap.enums;

public enum SyncType {
    ADD,
    REMOVE,
    RESYNC;

    public static SyncType fromOrdinal(int ordinal) {
        for (SyncType type : values()) {
            if (type.ordinal() == ordinal) {
                return type;
            }
        }
        return null;
    }
}
