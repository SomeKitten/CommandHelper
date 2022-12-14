package space.cutekitten.commandhelper;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class CommandHelper implements ModInitializer {
    public static final ItemStack iconStack = new ItemStack(Blocks.COMMAND_BLOCK, 69);
    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
            new Identifier("commandhelper", "scoreboard"),
            () -> iconStack).setTexture("item_search.png");

    @Override
    public void onInitialize() {

    }
}
