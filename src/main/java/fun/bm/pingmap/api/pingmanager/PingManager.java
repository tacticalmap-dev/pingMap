package fun.bm.pingmap.api.pingmanager;

import fun.bm.pingmap.api.pingmanager.ping.Ping;
import fun.bm.pingmap.enums.PingType;
import fun.bm.pingmap.pingmanager.ping.EntityPing;
import fun.bm.pingmap.pingmanager.ping.PointPing;
import fun.bm.pingmap.pingmanager.ping.ServerPing;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PingManager {
    void save(MinecraftServer server);

    Ping addPing(CompoundTag tag);

    Ping addPing(CompoundTag tag, MinecraftServer server);

    @Deprecated
    Ping addPing(CompoundTag tag, PingType type);

    @Deprecated
    Ping addPing(CompoundTag tag, PingType type, MinecraftServer server);

    void cancelPing(Ping ping);

    // if you edit pings here, make sure to call resent request to update the client
    Collection<Ping> getPings();

    List<Ping> getPingsForDimension(String dimension);

    PointPing addPointPing(double x, double y, double z, String dimension, UUID generatorId, MinecraftServer server);

    EntityPing addEntityPing(Entity entity, String dimension, UUID generatorId, PingType type, MinecraftServer server);

    ServerPing addServerPing(String name, String dimension, double x, double y, double z, int color, boolean showDistance, int expireAfter, MinecraftServer server);
}
