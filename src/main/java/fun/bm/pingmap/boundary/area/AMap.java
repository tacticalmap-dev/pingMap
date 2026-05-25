package fun.bm.pingmap.boundary.area;

import fun.bm.pingmap.api.boundary.area.AreaMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Set;

public class AMap implements AreaMap {
    private final Set<Area> areas = new HashSet<>();
    private boolean crossable;
    private boolean visible;
    private int rgba;
    private String name;
    private String description;
    private String icon;

    @Override
    public void addArea(Area area) {
        areas.add(area);
    }

    @Override
    public boolean crossable() {
        return crossable;
    }

    @Override
    public boolean visible() {
        return visible;
    }

    @Override
    public int rgba() {
        return rgba;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String icon() {
        return icon;
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        for (Area area : areas) {
            CompoundTag areaTag = new CompoundTag();
            areaTag.put("area", area.toNbt());
            listTag.add(areaTag);
        }
        tag.put("areas", listTag);
        return tag;
    }

    public AMap fromNbt(CompoundTag tag) {
        ListTag listTag = tag.getList("areas", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag areaTag = listTag.getCompound(i);
            Area area = Area.fromNbt(areaTag.getCompound("area"));
            areas.add(area);
        }
        return this;
    }

    @Override
    public void setCrossable(boolean crossable) {
        this.crossable = crossable;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void setRgba(int rgba) {
        this.rgba = rgba;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setIcon(String icon) {
        this.icon = icon;
    }
}
