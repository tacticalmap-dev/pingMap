package fun.bm.pingmap.boundary.area;

import fun.bm.pingmap.api.boundary.area.AreaMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Set;

public class AMap implements AreaMap {
    private final Set<Area> areas = new HashSet<>();

    @Override
    public void addArea(Area area) {
        areas.add(area);
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

    public static AMap fromNbt(CompoundTag tag) {
        AMap amap = new AMap();
        ListTag listTag = tag.getList("areas", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag areaTag = listTag.getCompound(i);
            Area area = Area.fromNbt(areaTag.getCompound("area"));
            amap.areas.add(area);
        }
        return amap;
    }
}
