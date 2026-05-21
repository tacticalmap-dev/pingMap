package fun.bm.pingmap.boundary;

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

public class AreaBoundaryManager {
    private static final Map<String, AreaMap> maps = new HashMap<>();

    public static void addAreaMap(String uniqueName, AreaMap areaMap) {
        if (hasAreaMap(uniqueName)) {
            throw new IllegalArgumentException("AreaMap with unique name " + uniqueName + " already exists.");
        }
        maps.put(uniqueName, areaMap);
    }

    public static void removeAreaMap(String uniqueName) {
        maps.remove(uniqueName);
    }

    public static AreaMap getAreaMap(String uniqueName) {
        return maps.get(uniqueName);
    }

    public static boolean hasAreaMap(String uniqueName) {
        return maps.containsKey(uniqueName);
    }

    public static void clear() {
        save(null);
        maps.clear();
    }

    public static void save(@Nullable MinecraftServer server) {
        CompoundTag tag = ServerDataStoreManager.getNbtOrigin(server);

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

    public static void load(@Nullable MinecraftServer server) {
        CompoundTag tag = ServerDataStoreManager.getNbt(server);
        if (tag == null) {
            return;
        }

        ListTag listTag = tag.getList("areaMaps", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag areaMapTag = listTag.getCompound(i);
            String uniqueName = areaMapTag.getString("uniqueName");
            AreaMap areaMap = AMap.fromNbt(areaMapTag.getCompound("areaData"));
            addAreaMap(uniqueName, areaMap);
        }
    }
}
