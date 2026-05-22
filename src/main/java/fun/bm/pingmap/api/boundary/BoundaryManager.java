package fun.bm.pingmap.api.boundary;

import fun.bm.pingmap.api.boundary.area.AreaMap;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;

public interface BoundaryManager {
    void addAreaMap(String uniqueName, AreaMap areaMap);

    void removeAreaMap(String uniqueName);

    AreaMap getAreaMap(String uniqueName);

    boolean hasAreaMap(String uniqueName);

    void clear(@Nullable MinecraftServer server);

    void save(@Nullable MinecraftServer server);

    void load(@Nullable MinecraftServer server);
}
