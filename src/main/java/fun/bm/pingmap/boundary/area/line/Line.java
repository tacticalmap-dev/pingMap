package fun.bm.pingmap.boundary.area.line;

import net.minecraft.core.BlockPos;

public class Line {
    protected final BlockPos start;
    protected final BlockPos end;
    protected final double k;
    protected final double b;
    protected final boolean isVertical;

    public Line(BlockPos start, BlockPos end) {
        this.start = start;
        this.end = end;
        this.isVertical = (start.getX() == end.getX());
        if (this.isVertical) {
            this.k = Double.MAX_VALUE;
            this.b = start.getX();
        } else {
            this.k = (double) (end.getZ() - start.getZ()) / (end.getX() - start.getX());
            this.b = start.getZ() - k * start.getX();
        }
    }
}
