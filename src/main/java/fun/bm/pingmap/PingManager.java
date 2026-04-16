package fun.bm.pingmap;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PingManager {
    private static final String DATA_FILE = "pingmap_data.dat";
    private static PingManager instance;
    private final List<Ping> pings = new ArrayList<>();

    public static class Ping {
        public double x;
        public double y;
        public double z;
        public long timestamp;
        public String dimension;

        public Ping(double x, double y, double z, String dimension) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.dimension = dimension;
            this.timestamp = System.currentTimeMillis();
        }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("x", x);
            tag.putDouble("y", y);
            tag.putDouble("z", z);
            tag.putString("dimension", dimension);
            tag.putLong("timestamp", timestamp);
            return tag;
        }

        public static Ping fromNBT(CompoundTag tag) {
            Ping ping = new Ping(
                    tag.getDouble("x"),
                    tag.getDouble("y"),
                    tag.getDouble("z"),
                    tag.getString("dimension")
            );
            ping.timestamp = tag.getLong("timestamp");
            return ping;
        }
    }

    public static synchronized PingManager get(Minecraft minecraft) {
        if (instance == null) {
            instance = new PingManager();
            instance.load(minecraft);
        }
        return instance;
    }

    public static synchronized void reset() {
        instance = null;
    }

    private void load(Minecraft minecraft) {
        File dataFile = minecraft.gameDirectory.toPath().resolve(DATA_FILE).toFile();
        if (dataFile.exists()) {
            try {
                CompoundTag tag = net.minecraft.nbt.NbtIo.read(dataFile);
                if (tag != null) {
                    ListTag listTag = tag.getList("pings", Tag.TAG_COMPOUND);
                    for (int i = 0; i < listTag.size(); i++) {
                        pings.add(Ping.fromNBT(listTag.getCompound(i)));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void save(Minecraft minecraft) {
        try {
            File dataFile = minecraft.gameDirectory.toPath().resolve(DATA_FILE).toFile();
            CompoundTag tag = new CompoundTag();
            ListTag listTag = new ListTag();
            for (Ping ping : pings) {
                listTag.add(ping.toNBT());
            }
            tag.put("pings", listTag);
            net.minecraft.nbt.NbtIo.write(tag, dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPing(double x, double y, double z, String dimension) {
        Minecraft minecraft = Minecraft.getInstance();
        pings.clear();
        pings.add(new Ping(x, y, z, dimension));
        save(minecraft);
    }

    public void cancelPing() {
        Minecraft minecraft = Minecraft.getInstance();
        pings.clear();
        save(minecraft);
    }

    public void removePing(int index) {
        Minecraft minecraft = Minecraft.getInstance();
        if (index >= 0 && index < pings.size()) {
            pings.remove(index);
            save(minecraft);
        }
    }

    public List<Ping> getPings() {
        return pings;
    }

    public Ping getCurrentPing() {
        return pings.isEmpty() ? null : pings.get(0);
    }

    public List<Ping> getPingsForDimension(String dimension) {
        List<Ping> result = new ArrayList<>();
        for (Ping ping : pings) {
            if (ping.dimension.equals(dimension)) {
                result.add(ping);
            }
        }
        return result;
    }
}
