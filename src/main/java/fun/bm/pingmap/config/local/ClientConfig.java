package fun.bm.pingmap.config.local;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue LABEL_SCALE_SPLIT_DISTANCE = BUILDER
            .comment("Distance (blocks) used to switch near/far label scale multipliers.")
            .defineInRange("labelScale.splitDistance", 80, 1, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue NEAR_LABEL_SCALE_MULTIPLIER = BUILDER
            .comment("Label scale multiplier when ping distance is less than or equal to splitDistance.")
            .defineInRange("labelScale.nearMultiplier", 1.5, 0.1, 10.0);

    public static final ForgeConfigSpec.DoubleValue FAR_LABEL_SCALE_MULTIPLIER = BUILDER
            .comment("Label scale multiplier when ping distance is greater than splitDistance.")
            .defineInRange("labelScale.farMultiplier", 1.5, 0.1, 10.0);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static float getLabelScaleMultiplier(double distance) {
        double splitDistance = LABEL_SCALE_SPLIT_DISTANCE.get();
        if (distance <= splitDistance) {
            return NEAR_LABEL_SCALE_MULTIPLIER.get().floatValue();
        }
        return FAR_LABEL_SCALE_MULTIPLIER.get().floatValue();
    }
}
