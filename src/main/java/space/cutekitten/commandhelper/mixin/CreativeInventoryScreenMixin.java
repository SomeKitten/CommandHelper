package space.cutekitten.commandhelper.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import space.cutekitten.commandhelper.CommandHelper;
import space.cutekitten.commandhelper.client.ClientDB;
import space.cutekitten.commandhelper.client.ScoreboardRenderer;

import java.util.List;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin {
    @Shadow private static int selectedTab;

    @Shadow @Nullable private List<Slot> slots;

    @Shadow private TextFieldWidget searchBox;

    @Shadow protected abstract void search();

    @Shadow private float scrollPosition;

    @Inject(method = "render", at = @At("RETURN"))
    private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (selectedTab != CommandHelper.ITEM_GROUP.getIndex()) return;

        MinecraftClient client = ClientDB.client;

        if (client.world == null) return;

        CreativeInventoryScreen screen = (CreativeInventoryScreen) (Object) this;

        for (int i = 0; i < Math.min(ClientDB.showScores.size(), 5); i++) {
            ScoreboardPlayerScore score = ClientDB.showScores.get(i);
            ScoreboardRenderer.renderScore(screen, matrices, score, i, ClientDB.pinnedScores.contains(score));
        }

        for (int i = 0; i < Math.min(ClientDB.showScores.size(), 5); i++) {
            ScoreboardPlayerScore score = ClientDB.showScores.get(i);

            if (((HandledScreenAccessor)this).invokeIsPointWithinBounds(
                    9,
                    18 + 18 * i,
                    18*9, 16, mouseX, mouseY)) {
                screen.renderTooltip(matrices, List.of(
                        Text.of(score.getObjective().getName()),
                        Text.of(score.getPlayerName()),
                        Text.of(score.getScore() + "")
                ), mouseX, mouseY);
            }
        }
    }

    @Inject(method = "hasScrollbar", at = @At("RETURN"), cancellable = true)
    private void hasScrollbar(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(selectedTab == CommandHelper.ITEM_GROUP.getIndex() || cir.getReturnValue());
    }

    @Inject(method = "setSelectedTab", at = @At("RETURN"))
    private void setSelectedTab(ItemGroup group, CallbackInfo ci) {
        HandledScreenAccessor accessor = (HandledScreenAccessor) this;
        CreativeInventoryScreen.CreativeScreenHandler handler =
                (CreativeInventoryScreen.CreativeScreenHandler)accessor.getHandler();

        if (group.getIndex() == ItemGroup.INVENTORY.getIndex()) {
            return;
        }

//        if switching to non-score tab
        if (group.getIndex() != CommandHelper.ITEM_GROUP.getIndex()) {
            if (this.slots != null) {
                handler.slots.clear();
                handler.slots.addAll(this.slots);
            }

            return;
        }

        if (ClientDB.client.player == null) return;

        if (this.slots == null) {
            this.slots = ImmutableList.copyOf(handler.slots);
        }

        handler.itemList.clear();
        handler.slots.clear();

        this.searchBox.setVisible(true);
        this.searchBox.setFocusUnlocked(false);
        this.searchBox.setTextFieldFocused(true);
        this.searchBox.setText(ClientDB.currentSearch);
        this.search();
    }

//    make it think that it is typing on the search screen
    @Redirect(method = "charTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;getIndex()I"))
    private int charTyped(ItemGroup group) {
        if (selectedTab == CommandHelper.ITEM_GROUP.getIndex()) {
            return selectedTab;
        }

        return group.getIndex();
    }

    @Redirect(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;getIndex()I"))
    private int keyPressed(ItemGroup group) {
        if (selectedTab == CommandHelper.ITEM_GROUP.getIndex()) {
            return selectedTab;
        }

        return group.getIndex();
    }

    @Inject(method = "search", at = @At("HEAD"))
    private void search(CallbackInfo ci) {
        if (selectedTab != CommandHelper.ITEM_GROUP.getIndex()) return;

        ClientDB.currentSearch = searchBox.getText();

        Scoreboard scoreboard;

        MinecraftServer server = ClientDB.client.getServer();
        if (server != null) {
            scoreboard = server.getScoreboard();
        } else {
            if (ClientDB.client.world == null) return;
            scoreboard = ClientDB.client.world.getScoreboard();
        }

        ScoreboardRenderer.updateScoreboard(scoreboard);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void mouseScrolled(double mouseX, double mouseY, double amount, CallbackInfoReturnable<Boolean> cir) {
        if (selectedTab != CommandHelper.ITEM_GROUP.getIndex()) return;
        if (ClientDB.client.player == null) return;

//        scroll 5 items at once
        float f = (float)(amount*4 / (double)ClientDB.scores.size());
        this.scrollPosition = MathHelper.clamp(this.scrollPosition - f, 0.0F, 1.0F);
        ((CreativeInventoryScreen.CreativeScreenHandler)ClientDB.client.player.currentScreenHandler).scrollItems(this.scrollPosition);
        cir.setReturnValue(true);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void onMouseClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (selectedTab != CommandHelper.ITEM_GROUP.getIndex()) return;
        if (ClientDB.client.player == null) return;
        if (button != 0) return;

        for (int i = 0; i < Math.min(ClientDB.showScores.size(), 5); i++) {
            ScoreboardPlayerScore score = ClientDB.showScores.get(i);

            if (((HandledScreenAccessor)this).invokeIsPointWithinBounds(
                    9,
                    18 + 18 * i,
                    18*9, 16, mouseX, mouseY)) {
                if (ClientDB.pinnedScores.contains(score)) {
                    ClientDB.pinnedScores.remove(score);
                } else {
                    ClientDB.pinnedScores.add(score);
                }

                this.search();
            }
        }
    }
}
