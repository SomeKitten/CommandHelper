package space.cutekitten.commandhelper.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.cutekitten.commandhelper.client.ClientDB;
import space.cutekitten.commandhelper.client.ScoreboardRenderer;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onScoreboardObjectiveUpdate", at = @At("RETURN"))
    private void onScoreboardObjectiveUpdate(ScoreboardObjectiveUpdateS2CPacket packet, CallbackInfo ci) {
        World world = ClientDB.client.world;
        if (world == null) return;

        if (ClientDB.client.isIntegratedServerRunning())
            world = ClientDB.client.getServer().getWorld(world.getRegistryKey());

        ScoreboardRenderer.updateScoreboard(world.getScoreboard());
    }

    @Inject(method = "onScoreboardPlayerUpdate", at = @At("RETURN"))
    private void onScoreboardPlayerUpdate(ScoreboardPlayerUpdateS2CPacket packet, CallbackInfo ci) {
        World world = ClientDB.client.world;
        if (world == null) return;

        if (ClientDB.client.isIntegratedServerRunning())
            world = ClientDB.client.getServer().getWorld(world.getRegistryKey());

        ScoreboardRenderer.updateScoreboard(world.getScoreboard());
    }
}
