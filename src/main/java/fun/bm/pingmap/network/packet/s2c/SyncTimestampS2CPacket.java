package fun.bm.pingmap.network.packet.s2c;

import fun.bm.pingmap.util.ClientLatencyHelper;
import fun.bm.pingmap.util.TimeUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncTimestampS2CPacket {
    public final long timestamp;

    public SyncTimestampS2CPacket() {
        timestamp = TimeUtil.getServerSideTimeMillis();
    }

    public SyncTimestampS2CPacket(long timestamp) {
        this.timestamp = timestamp;
    }

    public static void encode(SyncTimestampS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.timestamp);
    }

    public static SyncTimestampS2CPacket decode(FriendlyByteBuf buf) {
        return new SyncTimestampS2CPacket(buf.readLong());
    }

    public static void handle(SyncTimestampS2CPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            long diff = packet.timestamp - TimeUtil.getLocalTimeMillis();
            TimeUtil.diffS2C = diff - ClientLatencyHelper.getMyLatency();
        });
        context.setPacketHandled(true);
    }
}
