package space.cutekitten.commandhelper.mixin;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import space.cutekitten.commandhelper.client.ClientDB;

import java.util.Collection;
import java.util.List;

@Mixin(Scoreboard.class)
public class ScoreboardMixin {
    @Inject(method = "getAllPlayerScores", at = @At(value = "INVOKE", target = "Ljava/util/List;sort(Ljava/util/Comparator;)V"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void getAllPlayerScores(ScoreboardObjective objective, CallbackInfoReturnable<Collection<ScoreboardPlayerScore>> cir, List<ScoreboardPlayerScore> list) {
        if (ClientDB.customScoreboardActive) {
            cir.setReturnValue(list);
        }
    }
}
