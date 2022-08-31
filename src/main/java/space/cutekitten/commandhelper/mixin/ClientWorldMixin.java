package space.cutekitten.commandhelper.mixin;

import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.cutekitten.commandhelper.client.ClientDB;
import space.cutekitten.commandhelper.client.PinnedScore;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Inject(method = "disconnect", at = @At("HEAD"))
    private void disconnect(CallbackInfo ci) {
        if (ClientDB.client.isIntegratedServerRunning()) return;
        ServerInfo entry = ClientDB.client.getCurrentServerEntry();
        if (entry == null) return;

        PinnedScore.savePins(ServerAddress.parse(entry.address).hashCode(), ClientDB.pinnedScores);
    }
}
