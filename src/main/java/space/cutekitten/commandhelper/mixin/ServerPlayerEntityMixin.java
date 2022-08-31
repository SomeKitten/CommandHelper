package space.cutekitten.commandhelper.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.cutekitten.commandhelper.client.ClientDB;
import space.cutekitten.commandhelper.client.PinnedScore;
import space.cutekitten.commandhelper.client.ScoreboardRenderer;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Shadow @Final public MinecraftServer server;

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtList list = PinnedScore.toNbt(ClientDB.pinnedScores);
        nbt.put("pinnedScores", list);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        PinnedScore.fromNbt(nbt.getList("pinnedScores", 10), ClientDB.pinnedScores);

        ScoreboardRenderer.updateScoreboard(this.server.getScoreboard());
    }
}
