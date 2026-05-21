package fun.bm.pingmap.boundary.area.line;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public class BoardLine extends Line {

    public BoardLine(BlockPos start, BlockPos end) {
        super(start, end);
    }

    public boolean crossLine(BlockPos s1, BlockPos e1) {
        return this.crossLine(new Line(s1, e1));
    }

    public boolean crossLine(Line other) {
        if (this.isVertical && other.isVertical) {
            return this.start.getX() == other.start.getX() &&
                    Math.min(other.start.getZ(), other.end.getZ()) <= Math.max(this.start.getZ(), this.end.getZ()) &&
                    Math.max(other.start.getZ(), other.end.getZ()) >= Math.min(this.start.getZ(), this.end.getZ());
        }

        if (this.isVertical) {
            double z = other.k * this.start.getX() + other.b;
            return isBetween(this.start.getX(), other.start.getX(), other.end.getX()) &&
                    isBetween(z, this.start.getZ(), this.end.getZ()) &&
                    isBetween(z, other.start.getZ(), other.end.getZ());
        }

        if (other.isVertical) {
            double z = this.k * other.start.getX() + this.b;
            return isBetween(other.start.getX(), this.start.getX(), this.end.getX()) &&
                    isBetween(z, this.start.getZ(), this.end.getZ()) &&
                    isBetween(z, other.start.getZ(), other.end.getZ());
        }

        if (Math.abs(this.k - other.k) < 1e-10) {
            return false;
        }

        double intersectX = (other.b - this.b) / (this.k - other.k);
        double intersectZ = this.k * intersectX + this.b;

        return isBetween(intersectX, this.start.getX(), this.end.getX()) &&
                isBetween(intersectX, other.start.getX(), other.end.getX()) &&
                isBetween(intersectZ, this.start.getZ(), this.end.getZ()) &&
                isBetween(intersectZ, other.start.getZ(), other.end.getZ());
    }

    private boolean isBetween(double value, long a, long b) {
        double min = Math.min(a, b);
        double max = Math.max(a, b);
        return value >= min - 1e-10 && value <= max + 1e-10;
    }

    public long toLong(BlockPos pos) {
        return BlockPos.asLong(pos.getX(), 0, pos.getZ());
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("start", this.toLong(start));
        tag.putLong("end", this.toLong(end));
        return tag;
    }

    public static BoardLine fromNbt(CompoundTag tag) {
        BlockPos start = BlockPos.of(tag.getLong("start"));
        BlockPos end = BlockPos.of(tag.getLong("end"));
        return new BoardLine(start, end);
    }
}
