package fun.bm.pingmap.config;

import fun.bm.pingmap.enums.PingType;
import net.minecraftforge.common.ForgeConfigSpec;

public final class PingmapConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue POINT_PING_LIFETIME_SECONDS = BUILDER
            .comment("Point ping lifetime in seconds. -1 means never expire.")
            .defineInRange("pingLifetime.pointSeconds", 30, -1, 86400);

    public static final ForgeConfigSpec.IntValue ENEMY_PING_LIFETIME_SECONDS = BUILDER
            .comment("Enemy ping lifetime in seconds. -1 means never expire.")
            .defineInRange("pingLifetime.enemySeconds", 10, -1, 86400);

    public static final ForgeConfigSpec.IntValue FRIENDLY_PING_LIFETIME_SECONDS = BUILDER
            .comment("Friendly ping lifetime in seconds. -1 means never expire.")
            .defineInRange("pingLifetime.friendlySeconds", 10, -1, 86400);

    public static final ForgeConfigSpec.IntValue SERVER_PING_LIFETIME_SECONDS = BUILDER
            .comment("Server ping lifetime in seconds. -1 means never expire.")
            .defineInRange("pingLifetime.serverSeconds", -1, -1, 86400);

    public static final ForgeConfigSpec.IntValue LABEL_SCALE_SPLIT_DISTANCE = BUILDER
            .comment("Distance (blocks) used to switch near/far label scale multipliers.")
            .defineInRange("labelScale.splitDistance", 80, 1, 10000);

    public static final ForgeConfigSpec.DoubleValue NEAR_LABEL_SCALE_MULTIPLIER = BUILDER
            .comment("Label scale multiplier when ping distance is less than or equal to splitDistance.")
            .defineInRange("labelScale.nearMultiplier", 1.5, 0.1, 10.0);

    public static final ForgeConfigSpec.DoubleValue FAR_LABEL_SCALE_MULTIPLIER = BUILDER
            .comment("Label scale multiplier when ping distance is greater than splitDistance.")
            .defineInRange("labelScale.farMultiplier", 1.5, 0.1, 10.0);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private PingmapConfig() {
    }

    public static int getPingLifetimeSeconds(PingType type) {
        if (type == null) {
            return POINT_PING_LIFETIME_SECONDS.get();
        }
        return switch (type) {
            case Point -> POINT_PING_LIFETIME_SECONDS.get();
            case Enemy -> ENEMY_PING_LIFETIME_SECONDS.get();
            case Friendly -> FRIENDLY_PING_LIFETIME_SECONDS.get();
            case Server -> SERVER_PING_LIFETIME_SECONDS.get();
        };
    }

    public static float getLabelScaleMultiplier(double distance) {
        double splitDistance = LABEL_SCALE_SPLIT_DISTANCE.get();
        if (distance <= splitDistance) {
            return NEAR_LABEL_SCALE_MULTIPLIER.get().floatValue();
        }
        return FAR_LABEL_SCALE_MULTIPLIER.get().floatValue();
    }
}
