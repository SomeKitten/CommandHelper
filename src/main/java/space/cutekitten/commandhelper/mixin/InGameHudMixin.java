package space.cutekitten.commandhelper.mixin;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import space.cutekitten.commandhelper.client.ClientDB;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static net.minecraft.client.gui.DrawableHelper.fill;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow public abstract TextRenderer getTextRenderer();

    @Shadow private int scaledHeight;

    @Shadow private int scaledWidth;

    @Shadow @Final private MinecraftClient client;

    @Shadow protected abstract void renderScoreboardSidebar(MatrixStack matrices, ScoreboardObjective objective);

    //    adapted from InGameHud.renderScoreboardSidebar
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderScoreboardSidebar(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"))
    private void customRenderScoreboardSidebar(InGameHud instance, MatrixStack matrices, ScoreboardObjective objective) {
        if (!ClientDB.customScoreboardActive) {
            this.renderScoreboardSidebar(matrices, objective);
            return;
        }

        Scoreboard scoreboard = objective.getScoreboard();
        List<ScoreboardPlayerScore> list = new ArrayList<>(ClientDB.scores);

        while (list.size() > 15) {
            list.remove(list.size() - 1);
        }

        List<Pair<ScoreboardPlayerScore, Text>> list2 = Lists.newArrayListWithCapacity(list.size());
        Text text = Text.of(ClientDB.currentSearch);
        int i = this.getTextRenderer().getWidth(text);
        int j = i;
        int k = this.getTextRenderer().getWidth(": ");

        ScoreboardPlayerScore scoreboardPlayerScore;
        MutableText text2;
        for(Iterator<ScoreboardPlayerScore> var11 = list.iterator();
            var11.hasNext(); j = Math.max(j, this.getTextRenderer().getWidth(text2) + k +
                this.getTextRenderer().getWidth(Integer.toString(scoreboardPlayerScore.getScore())))) {
            scoreboardPlayerScore = var11.next();
            Team team = scoreboard.getPlayerTeam(scoreboardPlayerScore.getPlayerName());
            text2 = Team.decorateName(team, Text.literal(scoreboardPlayerScore.getPlayerName()));
            list2.add(Pair.of(scoreboardPlayerScore, text2));
        }

        int var10000 = list.size();
        Objects.requireNonNull(this.getTextRenderer());
        int l = var10000 * 9;
        int m = this.scaledHeight / 2 + l / 3;
        int o = this.scaledWidth - j - 3;
        int p = 0;
        int q = this.client.options.getTextBackgroundColor(0.3F);
        int r = this.client.options.getTextBackgroundColor(0.4F);

        for (Pair<ScoreboardPlayerScore, Text> pair : list2) {
            ++p;
            ScoreboardPlayerScore scoreboardPlayerScore2 = pair.getFirst();
            Text text3 = pair.getSecond();
            Formatting var31 = Formatting.RED;
            String string = "" + var31 + scoreboardPlayerScore2.getScore();
            Objects.requireNonNull(this.getTextRenderer());
            int t = m - p * 9;
            int u = this.scaledWidth - 3 + 2;
            int var10001 = o - 2;
            Objects.requireNonNull(this.getTextRenderer());
            fill(matrices, var10001, t, u, t + 9, q);
            this.getTextRenderer().draw(matrices, text3, (float) o, (float) t, -1);
            this.getTextRenderer().draw(matrices, string, (float) (u - this.getTextRenderer().getWidth(string)), (float) t, -1);
            if (p == list.size()) {
                var10001 = o - 2;
                Objects.requireNonNull(this.getTextRenderer());
                fill(matrices, var10001, t - 9 - 1, u, t - 1, r);
                fill(matrices, o - 2, t - 1, u, t, q);
                TextRenderer var32 = this.getTextRenderer();
                float var10003 = (float) (o + j / 2 - i / 2);
                Objects.requireNonNull(this.getTextRenderer());
                var32.draw(matrices, text, var10003, (float) (t - 9), -1);
            }
        }
    }
}
