package fun.bm.pingmap.mixin;

import fun.bm.pingmap.event.server.ServerSyncFriendlyPosition;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Scoreboard.class)
public class TeamsUpdate {

    @Inject(method = "addPlayerToTeam", at = @At("RETURN"))
    private void onAddPlayerToTeam(String p_83434_, PlayerTeam p_83435_, CallbackInfoReturnable<Boolean> cir) {
        ServerSyncFriendlyPosition.cachedHandlers.add(new ServerSyncFriendlyPosition.TeamUpdateHandler(p_83434_, true));
    }

    @Inject(method = {"removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V"}, at = @At("RETURN"))
    private void onRemovePlayerFromTeam(String p_83464_, PlayerTeam p_83465_, CallbackInfo ci) {
        ServerSyncFriendlyPosition.cachedHandlers.add(new ServerSyncFriendlyPosition.TeamUpdateHandler(p_83464_, false));
    }
}
