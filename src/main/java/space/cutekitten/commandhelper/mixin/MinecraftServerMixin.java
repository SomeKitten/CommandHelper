package space.cutekitten.commandhelper.mixin;

import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.cutekitten.commandhelper.client.ScoreboardRenderer;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow public abstract ServerScoreboard getScoreboard();

    @Inject(method = "initScoreboard", at = @At("RETURN"))
    private void onInitScoreboard(CallbackInfo ci) {
        ScoreboardRenderer.updateScoreboard(this.getScoreboard());
    }
}
