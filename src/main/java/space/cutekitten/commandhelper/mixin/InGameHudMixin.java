package space.cutekitten.commandhelper.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import space.cutekitten.commandhelper.client.ClientDB;
import space.cutekitten.commandhelper.client.ScoreboardRenderer;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow private int scaledHeight;

    @Shadow private int scaledWidth;

    @Shadow protected abstract void renderScoreboardSidebar(MatrixStack matrices, ScoreboardObjective objective);

    //    adapted from InGameHud.renderScoreboardSidebar
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderScoreboardSidebar(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"))
    private void customRenderScoreboardSidebar(InGameHud instance, MatrixStack matrices, ScoreboardObjective objective) {
        if (!ClientDB.customScoreboardActive) {
            this.renderScoreboardSidebar(matrices, objective);
            return;
        }

        ScoreboardRenderer.renderCustomScoreboard(instance, this.scaledWidth, this.scaledHeight, matrices, objective);
    }
}
