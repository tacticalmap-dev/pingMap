package fun.bm.pingmap.boundary.area;

import fun.bm.pingmap.boundary.area.line.BoardLine;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Area {
    private final List<BlockPos> points;
    private final Set<BoardLine> board;
    private final Set<Area> notContainedArea;
    private final CompoundTag extraData;

    public Area() {
        this(new ArrayList<>(), new CompoundTag(), new HashSet<>());
    }

    public Area(List<BlockPos> boardPoint) {
        this(boardPoint, new CompoundTag(), new HashSet<>());
    }

    public Area(List<BlockPos> points, CompoundTag extraData, Set<Area> notContainedArea) {
        this.points = points;
        this.board = new HashSet<>();
        this.extraData = extraData;
        this.notContainedArea = notContainedArea;

        if (points != null && !points.isEmpty()) {
            generateLinesFromPoints();
        }
    }

    public void addPoint(BlockPos point, int index) {
        points.add(index, point);
    }

    public void addPoint(BlockPos point, BlockPos between1, BlockPos between2) {
        BlockPos lastChecked = points.get(points.size() - 1);
        for (int i = 0; i < points.size(); i++) {
            BlockPos pos = points.get(i);
            if ((between1.equals(pos) && between2.equals(lastChecked))
                    || (between1.equals(lastChecked) && between2.equals(pos))) {
                points.add(i + 1, point);
                generateLinesFromPoints();
                return;
            }
            lastChecked = pos;
        }
        throw new IllegalArgumentException("Points not found between " + between1 + " and " + between2);
    }

    public void removePoint(BlockPos pos) {
        points.remove(pos);
    }

    public void addNotContainedArea(Area area) {
        notContainedArea.add(area);
    }

    public List<BlockPos> getPoints() {
        return points;
    }

    public Set<BoardLine> getBoards() {
        return board;
    }

    public Set<Area> getNotContainedArea() {
        return notContainedArea;
    }

    public CompoundTag getExtraData() {
        return extraData;
    }

    private void generateLinesFromPoints() {
        if (points.size() < 3) {
            throw new IllegalArgumentException("Area must have at least 3 points.");
        }

        board.clear();

        BlockPos lastChecked = points.get(points.size() - 1);
        for (BlockPos point : points) {
            board.add(new BoardLine(lastChecked, point));
            lastChecked = point;
        }
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.put("extraData", extraData);

        ListTag pointsTag = new ListTag();
        for (BlockPos point : points) {
            CompoundTag pointTag = new CompoundTag();
            pointTag.putInt("x", point.getX());
            pointTag.putInt("z", point.getZ());
            pointsTag.add(pointTag);
        }
        tag.put("points", pointsTag);

        ListTag notContainedAreaTag = new ListTag();
        for (Area area : notContainedArea) {
            notContainedAreaTag.add(area.toNbt());
        }
        tag.put("notContainedArea", notContainedAreaTag);
        return tag;
    }

    public static Area fromNbt(CompoundTag tag) {
        List<BlockPos> points = new ArrayList<>();
        if (tag.contains("points")) {
            ListTag pointsTag = tag.getList("points", Tag.TAG_COMPOUND);
            for (int i = 0; i < pointsTag.size(); i++) {
                CompoundTag pointTag = pointsTag.getCompound(i);
                int x = pointTag.getInt("x");
                int z = pointTag.getInt("z");
                points.add(new BlockPos(x, 0, z));
            }
        }

        Set<Area> notContainedArea = new HashSet<>();
        ListTag notContainedAreaTag = tag.getList("notContainedArea", Tag.TAG_COMPOUND);
        for (int i = 0; i < notContainedAreaTag.size(); i++) {
            notContainedArea.add(Area.fromNbt(notContainedAreaTag.getCompound(i)));
        }

        Area area = new Area(points, tag.getCompound("extraData"), notContainedArea);

        if (!points.isEmpty()) {
            area.generateLinesFromPoints();
        }

        return area;
    }
}
