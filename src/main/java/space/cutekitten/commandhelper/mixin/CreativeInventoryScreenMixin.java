package space.cutekitten.commandhelper.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
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

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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

        for (int i = 0; i < Math.min(ClientDB.showScores.size(), 5); i++) {
            ScoreboardPlayerScore score = ClientDB.showScores.get(i);
            ScoreboardRenderer.renderScore((CreativeInventoryScreen) (Object) this, matrices, score, i);
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
        if (selectedTab == CommandHelper.ITEM_GROUP.getIndex()) {
            ClientDB.currentSearch = searchBox.getText();
            String searched = ClientDB.currentSearch.toLowerCase(Locale.ROOT);

            MinecraftServer server = ClientDB.client.getServer();
            if (server == null) return;

            Scoreboard scoreboard = server.getScoreboard();

            ClientDB.scores = new ArrayList<>();

            for (ScoreboardObjective objective : scoreboard.getObjectives()) {
                try {
                    ClientDB.scores.addAll(scoreboard.getAllPlayerScores(objective).stream().filter(
                            score -> score.getPlayerName().toLowerCase(Locale.ROOT).contains(searched)).toList());
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }

//            don't sort if the amount of scores is too high!!!
            if (ClientDB.scores.size() > 10000) {
                return;
            }

//            hopefully faster sorting than just sorting the whole list at once
            HashMap<Character, List<ScoreboardPlayerScore>> scoreByFirstChar = new HashMap<>();
            for (ScoreboardPlayerScore score : ClientDB.scores) {
                char firstChar = score.getPlayerName().charAt(0);
                if (!scoreByFirstChar.containsKey(firstChar)) {
                    scoreByFirstChar.put(firstChar, new ArrayList<>());
                }
                scoreByFirstChar.get(firstChar).add(score);
            }

            ClientDB.scores.clear();

            Collator collator = Collator.getInstance(Locale.ROOT);
            for (char key : scoreByFirstChar.keySet().stream().sorted().toList()) {
                List<ScoreboardPlayerScore> sortedScores = scoreByFirstChar.get(key);
                sortedScores.sort((score1, score2) -> {
                    String score1Name = score1.getPlayerName();
                    String score2Name = score2.getPlayerName();
                    return collator.compare(score1Name, score2Name);
                });

                ClientDB.scores.addAll(sortedScores);
            }
        }
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
}
