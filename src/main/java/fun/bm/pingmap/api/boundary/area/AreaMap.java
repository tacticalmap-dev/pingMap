package fun.bm.pingmap.api.boundary.area;

import fun.bm.pingmap.boundary.area.Area;
import net.minecraft.nbt.CompoundTag;

public interface AreaMap {
    void addArea(Area area);

    CompoundTag toNbt();

    static AreaMap fromNbt(CompoundTag tag) {
        return null;
    }
}
