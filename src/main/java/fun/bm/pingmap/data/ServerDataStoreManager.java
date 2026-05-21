package fun.bm.pingmap.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

public class ServerDataStoreManager {
    private static final String DATA_FILE = "pingmap_data.dat";
    private static boolean inited = false;
    private static CompoundTag tag = null;
    private static File saveDir = null;

    public static void drop() {
        tag = null;
        inited = false;
        saveDir = null;
    }

    private static void load(@Nullable MinecraftServer server) {
        if (!inited && server != null) {
            saveDir = server.getWorldPath(LevelResource.ROOT).toFile();
        }

        if (saveDir == null || !saveDir.exists()) {
            return;
        }

        File dataFile = new File(saveDir, DATA_FILE);

        if (dataFile.exists()) {
            try {
                tag = NbtIo.read(dataFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            tag = new CompoundTag();
        }
        inited = true;
    }

    public static void save() {
        if (!inited) return;

        try {
            NbtIo.write(tag, new File(saveDir, DATA_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CompoundTag getNbtOrigin(@Nullable MinecraftServer server) {
        load(server);
        return tag;
    }

    public static CompoundTag getNbt(@Nullable MinecraftServer server) {
        CompoundTag ret = getNbtOrigin(server);
        return ret == null ? ret : ret.copy();
    }
}
