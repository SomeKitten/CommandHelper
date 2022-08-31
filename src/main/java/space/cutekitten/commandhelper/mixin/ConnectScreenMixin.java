package space.cutekitten.commandhelper.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.cutekitten.commandhelper.client.ClientDB;
import space.cutekitten.commandhelper.client.PinnedScore;

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {
    @Inject(method = "connect(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;)V", at = @At("RETURN"))
    private void connect(MinecraftClient client, ServerAddress address, CallbackInfo ci) {
        if (client.isIntegratedServerRunning()) return;

        PinnedScore.loadPins(address.hashCode(), ClientDB.pinnedScores);
    }
}
