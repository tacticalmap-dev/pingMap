package fun.bm.pingmap.boundary;

import fun.bm.pingmap.api.boundary.BoundaryManager;
import fun.bm.pingmap.api.boundary.area.AreaMap;
import fun.bm.pingmap.boundary.area.AMap;
import fun.bm.pingmap.data.ServerDataStoreManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class AreaBoundaryManager implements BoundaryManager {
    private static BoundaryManager instance;
    private final Map<String, AreaMap> maps = new HashMap<>();

    public static synchronized BoundaryManager get(@Nullable MinecraftServer server) {
        if (instance == null) {
            instance = new AreaBoundaryManager();
            instance.load(server);
        }
        return instance;
    }

    public static synchronized void drop(@Nullable MinecraftServer server) {
        if (instance != null) {
            instance.clear(server);
        }
        instance = null;
    }

    public void addAreaMap(String uniqueName, AreaMap areaMap) {
        if (hasAreaMap(uniqueName)) {
            throw new IllegalArgumentException("AreaMap with unique name " + uniqueName + " already exists.");
        }
        maps.put(uniqueName, areaMap);
    }

    public void removeAreaMap(String uniqueName) {
        maps.remove(uniqueName);
    }

    public AreaMap getAreaMap(String uniqueName) {
        return maps.get(uniqueName);
    }

    public boolean hasAreaMap(String uniqueName) {
        return maps.containsKey(uniqueName);
    }

    public void clear(@Nullable MinecraftServer server) {
        save(server);
        maps.clear();
    }

    public void save(@Nullable MinecraftServer server) {
        CompoundTag tag = ServerDataStoreManager.getNbtOrigin(server);

        if (tag == null) return;

        ListTag listTag = new ListTag();
        for (Map.Entry<String, AreaMap> entry : maps.entrySet()) {
            String uniqueName = entry.getKey();
            AreaMap areaMap = entry.getValue();
            CompoundTag areaMapTag = new CompoundTag();
            areaMapTag.putString("uniqueName", uniqueName);
            areaMapTag.put("areaData", areaMap.toNbt());
            listTag.add(areaMapTag);
        }

        tag.put("areaMaps", listTag);

        ServerDataStoreManager.save();
    }

    public void load(@Nullable MinecraftServer server) {
        CompoundTag tag = ServerDataStoreManager.getNbt(server);
        if (tag == null) {
            return;
        }

        ListTag listTag = tag.getList("areaMaps", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag areaMapTag = listTag.getCompound(i);
            String uniqueName = areaMapTag.getString("uniqueName");
            AreaMap areaMap = new AMap().fromNbt(areaMapTag.getCompound("areaData"));
            addAreaMap(uniqueName, areaMap);
        }
    }
}
