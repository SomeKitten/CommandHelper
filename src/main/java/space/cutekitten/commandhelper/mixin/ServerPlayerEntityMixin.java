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
        NbtList list = new NbtList();

        list.addAll(ClientDB.pinnedScores.stream().map(score -> {
            NbtCompound compound = new NbtCompound();
            compound.putString("objective", score.objective);
            compound.putString("player", score.player);

            return compound;
        }).toList());

        nbt.put("pinnedScores", list);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        ClientDB.pinnedScores.clear();

        NbtList list = nbt.getList("pinnedScores", 10);
        list.forEach(element -> {
            if (!(element instanceof NbtCompound compound)) return;

            ClientDB.pinnedScores.add(new PinnedScore(
                    compound.getString("objective"),
                    compound.getString("player")
            ));
        });

        ScoreboardRenderer.updateScoreboard(this.server.getScoreboard());
    }
}
