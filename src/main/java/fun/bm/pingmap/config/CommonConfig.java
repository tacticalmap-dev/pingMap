package fun.bm.pingmap.config;

import fun.bm.pingmap.enums.PingType;
import net.minecraftforge.common.ForgeConfigSpec;

public final class CommonConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue POINT_PING_LIFETIME_SECONDS = BUILDER
            .comment("Point ping lifetime in seconds. -1 means never expire.")
            .defineInRange("pingLifetime.pointSeconds", 30, -1, 86400);

    public static final ForgeConfigSpec.IntValue ENEMY_PING_LIFETIME_SECONDS = BUILDER
            .comment("Enemy ping lifetime in seconds. -1 means never expire.")
            .defineInRange("pingLifetime.enemySeconds", 10, -1, 86400);

    public static final ForgeConfigSpec.IntValue FRIENDLY_PING_LIFETIME_SECONDS = BUILDER
            .comment("Friendly ping lifetime in seconds. -1 means never expire.")
            .defineInRange("pingLifetime.friendlySeconds", -1, -1, 86400);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int getPingLifetimeSeconds(PingType type) {
        return switch (type) {
            case Point -> POINT_PING_LIFETIME_SECONDS.get();
            case Enemy -> ENEMY_PING_LIFETIME_SECONDS.get();
            case Friendly -> FRIENDLY_PING_LIFETIME_SECONDS.get();
            default -> -1;
        };
    }
}
