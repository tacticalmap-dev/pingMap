package fun.bm.pingmap.api.boundary.area;

import fun.bm.pingmap.boundary.area.Area;
import net.minecraft.nbt.CompoundTag;

public interface AreaMap {
    void addArea(Area area);

    boolean crossable();

    boolean visible();

    int rgba();

    String name();

    String description();

    String icon();

    CompoundTag toNbt();

    AreaMap fromNbt(CompoundTag tag);

    void setCrossable(boolean crossable);

    void setVisible(boolean visible);

    void setRgba(int rgba);

    void setName(String name);

    void setDescription(String description);

    void setIcon(String icon);
}
