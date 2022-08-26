package space.cutekitten.commandhelper.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.util.math.Matrix4f;
import space.cutekitten.commandhelper.mixin.HandledScreenAccessor;

public class ScoreboardRenderer {
    public static void renderScore(CreativeInventoryScreen screen, MatrixStack matrices, ScoreboardPlayerScore score, int index) {
        TextRenderer textRenderer = ClientDB.client.textRenderer;

        int x = ((HandledScreenAccessor) screen).getX() + 9;
        int y = ((HandledScreenAccessor) screen).getY() + 18 + index * 18;

//        main background
        fill(matrices.peek().getPositionMatrix(),
                x - 1, y - 1, x + 18 * 9 - 2 + 1, y + 18 - 2 + 1, // rect bounds
                0xFFFFFFFF, // argb color
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
}