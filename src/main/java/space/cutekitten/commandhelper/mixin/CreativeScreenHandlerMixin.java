package space.cutekitten.commandhelper.mixin;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.cutekitten.commandhelper.CommandHelper;
import space.cutekitten.commandhelper.client.ClientDB;
import space.cutekitten.commandhelper.client.ScoreboardRenderer;

import java.util.ArrayList;
import java.util.List;

@Mixin(CreativeInventoryScreen.CreativeScreenHandler.class)
public class CreativeScreenHandlerMixin {
    @Inject(method = "scrollItems", at = @At("HEAD"), cancellable = true)
    private void scrollItems(float position, CallbackInfo ci) {
        if (CreativeInventoryScreenAccessor.getSelectedTab() != CommandHelper.ITEM_GROUP.getIndex()) {
            return;
        }

        int startingIndex = (int) (position * (ClientDB.scores.size() - 5));
        startingIndex = Math.max(0, startingIndex);

        ClientDB.showScores.clear();
        for (int i = 0; i < 5; i++) {
            if (startingIndex + i == ClientDB.scores.size()) break;

            ClientDB.showScores.add(i, ClientDB.scores.get(startingIndex + i));
        }

        ci.cancel();
    }
}
