package fun.bm.pingmap.config.local;

import fun.bm.pingmap.config.remote.RemoteCommonConfig;
import fun.bm.pingmap.enums.PingType;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.DistExecutor;

import java.util.concurrent.atomic.AtomicBoolean;

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

    public static boolean hasServerConfig() {
        return RemoteCommonConfig.serverPointPingLifetime != null || RemoteCommonConfig.serverEnemyPingLifetime != null || RemoteCommonConfig.serverFriendlyPingLifetime != null;
    }

    public static int getPingLifetimeSeconds(PingType type) {
        AtomicBoolean flag = new AtomicBoolean();
        DistExecutor.unsafeRunForDist(() -> () -> {
            Minecraft minecraft = Minecraft.getInstance();
            flag.set(minecraft != null && !minecraft.hasSingleplayerServer() && hasServerConfig());
            return null;
        }, () -> () -> {
            flag.set(false);
            return null;
        });
        if (flag.get()) {
            return switch (type) {
                case Point ->
                        RemoteCommonConfig.serverPointPingLifetime != null ? RemoteCommonConfig.serverPointPingLifetime : 0;
                case Enemy ->
                        RemoteCommonConfig.serverEnemyPingLifetime != null ? RemoteCommonConfig.serverEnemyPingLifetime : 0;
                case Friendly ->
                        RemoteCommonConfig.serverFriendlyPingLifetime != null ? RemoteCommonConfig.serverFriendlyPingLifetime : 0;
                default -> -1;
            };
        }

        return switch (type) {
            case Point -> POINT_PING_LIFETIME_SECONDS.get();
            case Enemy -> ENEMY_PING_LIFETIME_SECONDS.get();
            case Friendly -> FRIENDLY_PING_LIFETIME_SECONDS.get();
            default -> -1;
        };
    }
}
