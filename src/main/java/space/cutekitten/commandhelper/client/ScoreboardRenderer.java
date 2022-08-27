package space.cutekitten.commandhelper.client;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Matrix4f;
import space.cutekitten.commandhelper.CommandHelper;
import space.cutekitten.commandhelper.mixin.HandledScreenAccessor;

import java.text.Collator;
import java.util.*;

public class ScoreboardRenderer {
    public static void renderScore(CreativeInventoryScreen screen, MatrixStack matrices,
                                   ScoreboardPlayerScore score, int index, boolean pinned) {
        TextRenderer textRenderer = ClientDB.client.textRenderer;

        int x = ((HandledScreenAccessor) screen).getX() + 9;
        int y = ((HandledScreenAccessor) screen).getY() + 18 + index * 18;

        int colour = 0xFFFFFFFF;
        if (pinned) {
            colour = 0xFFFFFF00; // yellow
        }
//        main background
        fill(matrices.peek().getPositionMatrix(),
                x - 1, y - 1, x + 18 * 9 - 2 + 1, y + 18 - 2 + 1, // rect bounds
                colour, // argb color
                screen.getZOffset());
        fill(matrices.peek().getPositionMatrix(),
                x, y, x + 18 * 9 - 2, y + 18 - 2, // rect bounds
                0xFF8B8B8B, // argb color
                screen.getZOffset());

//        name
        int scoreWidth = textRenderer.getWidth(score.getScore() + "") + 3;
        textRenderer.draw(
                matrices,
                textRenderer.trimToWidth(score.getPlayerName(), 18 * 9 - 4 - scoreWidth),
                x + 2,
                y + 5,
                0xFFFFFF);

//        score background
        fill(matrices.peek().getPositionMatrix(),
                x + 18 * 9 - 2 - scoreWidth, y, x + 18 * 9 - 2, y + 18 - 2, // rect bounds
                0xFF7B7B7B, // argb color
                screen.getZOffset());

//        score
        textRenderer.draw(
                matrices,
                score.getScore() + "",
                x + 18 * 9 - 2 - scoreWidth + 2,
                y + 5,
                0xFFFFFF);
    }

//    mostly copied / adapted from DrawableHelper.fill
    public static void fill(Matrix4f matrix, int x1, int y1, int x2, int y2, int color, float z) {
        int i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }

        float f = (float)(color >> 24 & 255) / 255.0F;
        float g = (float)(color >> 16 & 255) / 255.0F;
        float h = (float)(color >> 8 & 255) / 255.0F;
        float j = (float)(color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float)x1, (float)y2, z).color(g, h, j, f).next();
        bufferBuilder.vertex(matrix, (float)x2, (float)y2, z).color(g, h, j, f).next();
        bufferBuilder.vertex(matrix, (float)x2, (float)y1, z).color(g, h, j, f).next();
        bufferBuilder.vertex(matrix, (float)x1, (float)y1, z).color(g, h, j, f).next();
        BufferRenderer.drawWithShader(bufferBuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void updateScoreboard(Scoreboard scoreboard) {
        String searched = ClientDB.currentSearch.toLowerCase(Locale.ROOT);

        ClientDB.scores = new ArrayList<>();

        for (ScoreboardObjective objective : scoreboard.getObjectives()) {
            try {
                ClientDB.scores.addAll(scoreboard.getAllPlayerScores(objective).stream().filter(
                        score -> score.getPlayerName().toLowerCase(Locale.ROOT).contains(searched)).toList());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        Collator collator = Collator.getInstance(Locale.ROOT);
        ClientDB.pinnedScores.sort((score1, score2) -> {
            String score1Name = score1.getPlayerName();
            String score2Name = score2.getPlayerName();
            return collator.compare(score1Name, score2Name);
        });
        List<ScoreboardPlayerScore> sortedPins =
                ClientDB.pinnedScores.stream().filter(score ->
                        ClientDB.scores.contains(score) &&
                                score.getPlayerName().toLowerCase(Locale.ROOT).contains(searched)).toList();

//        if the amount of scores is too high
        if (ClientDB.scores.size() > 10000) {
            ClientDB.scores.removeAll(sortedPins);
            ClientDB.scores.addAll(0, sortedPins);

            CommandHelper.iconStack.setCount(ClientDB.scores.size());

            return;
        }

//        hopefully faster sorting than just sorting the whole list at once
        HashMap<Character, List<ScoreboardPlayerScore>> scoreByFirstChar = new HashMap<>();
        for (ScoreboardPlayerScore score : ClientDB.scores) {
            if (ClientDB.pinnedScores.contains(score)) continue;

            char firstChar = score.getPlayerName().charAt(0);
            if (!scoreByFirstChar.containsKey(firstChar)) {
                scoreByFirstChar.put(firstChar, new ArrayList<>());
            }
            scoreByFirstChar.get(firstChar).add(score);
        }

        ClientDB.scores.clear();
        ClientDB.scores.addAll(sortedPins);

        for (char key : scoreByFirstChar.keySet().stream().sorted().toList()) {
            List<ScoreboardPlayerScore> sortedScores = scoreByFirstChar.get(key);
            sortedScores.sort((score1, score2) -> {
                String score1Name = score1.getPlayerName();
                String score2Name = score2.getPlayerName();
                return collator.compare(score1Name, score2Name);
            });

            ClientDB.scores.addAll(sortedScores);
        }

        CommandHelper.iconStack.setCount(Math.max(ClientDB.scores.size(), 1));
    }

    public static void renderCustomScoreboard(InGameHud instance, int scaledWidth, int scaledHeight, MatrixStack matrices, ScoreboardObjective objective) {
        Scoreboard scoreboard = objective.getScoreboard();

        List<ScoreboardPlayerScore> list =
                new ArrayList<>(ClientDB.scores.subList(0, Math.min(ClientDB.scores.size(), 24)));
        Collections.reverse(list);

        List<Pair<ScoreboardPlayerScore, Text>> list2 = Lists.newArrayListWithCapacity(list.size());
        Text text = Text.of(ClientDB.currentSearch);
        int i = instance.getTextRenderer().getWidth(text);
        int j = i;
        int k = instance.getTextRenderer().getWidth(": ");

        ScoreboardPlayerScore scoreboardPlayerScore;
        MutableText text2;
        for(Iterator<ScoreboardPlayerScore> var11 = list.iterator();
            var11.hasNext(); j = Math.max(j, instance.getTextRenderer().getWidth(text2) + k +
                instance.getTextRenderer().getWidth(Integer.toString(scoreboardPlayerScore.getScore())))) {
            scoreboardPlayerScore = var11.next();
            Team team = scoreboard.getPlayerTeam(scoreboardPlayerScore.getPlayerName());
            text2 = Team.decorateName(team, Text.literal(scoreboardPlayerScore.getPlayerName()));
            list2.add(Pair.of(scoreboardPlayerScore, text2));
        }

        int var10000 = list.size();
        Objects.requireNonNull(instance.getTextRenderer());
        int l = var10000 * 9;
        int m = scaledHeight / 2 + l / 2;
        int o = scaledWidth - j - 3;
        int p = 0;
        int q = ClientDB.client.options.getTextBackgroundColor(0.3F);
        int r = ClientDB.client.options.getTextBackgroundColor(0.4F);

        for (Pair<ScoreboardPlayerScore, Text> pair : list2) {
            ++p;
            ScoreboardPlayerScore scoreboardPlayerScore2 = pair.getFirst();
            Text text3 = pair.getSecond();
            Formatting var31 = Formatting.RED;
            String string = "" + var31 + scoreboardPlayerScore2.getScore();
            Objects.requireNonNull(instance.getTextRenderer());
            int t = m - p * 9;
            int u = scaledWidth - 3 + 2;
            int var10001 = o - 2;
            Objects.requireNonNull(instance.getTextRenderer());
            InGameHud.fill(matrices, var10001, t, u, t + 9, q);
            instance.getTextRenderer().draw(matrices, text3, (float) o, (float) t,
                    ClientDB.pinnedScores.contains(scoreboardPlayerScore2) ? 0xFFFFFF00 : 0xFFFFFFFF);
            instance.getTextRenderer().draw(matrices, string, (float) (u - instance.getTextRenderer().getWidth(string)), (float) t, -1);
            if (p == list.size()) {
                var10001 = o - 2;
                Objects.requireNonNull(instance.getTextRenderer());
                InGameHud.fill(matrices, var10001, t - 9 - 1, u, t - 1, r);
                InGameHud.fill(matrices, o - 2, t - 1, u, t, q);
                TextRenderer var32 = instance.getTextRenderer();
                float var10003 = (float) (o + j / 2 - i / 2);
                Objects.requireNonNull(instance.getTextRenderer());
                var32.draw(matrices, text, var10003, (float) (t - 9), -1);
            }
        }
    }
}
