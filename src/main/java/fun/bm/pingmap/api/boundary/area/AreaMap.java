package fun.bm.pingmap.api.boundary.area;

import fun.bm.pingmap.boundary.area.Area;
import net.minecraft.nbt.CompoundTag;

public interface AreaMap {
    void addArea(Area area);

    abstract boolean crossable();

    abstract boolean visible();

    abstract int rgba();

    abstract String name();

    abstract String description();

    abstract String icon();

    CompoundTag toNbt();

    static AreaMap fromNbt(CompoundTag tag) {
        return null;
    }

    void setCrossable(boolean crossable);

    void setVisible(boolean visible);

    void setRgba(int rgba);

    void setName(String name);

    void setDescription(String description);

    void setIcon(String icon);
}
