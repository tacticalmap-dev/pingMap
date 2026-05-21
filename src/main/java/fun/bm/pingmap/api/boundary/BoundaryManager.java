package fun.bm.pingmap.api.boundary;

import fun.bm.pingmap.api.boundary.area.AreaMap;

public abstract class BoundaryManager {
    abstract void addAreaMap(AreaMap areaMap);

    abstract void removeAreaMap(AreaMap areaMap);

    abstract boolean crossable();

    abstract boolean visible();

    abstract int rgba();

    abstract String name();

    abstract String description();

    abstract String icon();
}
