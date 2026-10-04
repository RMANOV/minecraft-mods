package com.erik.medievalconquest;

import com.erik.medievalconquest.registry.ModAnomalyBlocks;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

public final class AnomalyMenuGameTest {
    private static CraftingMenu menu(GameTestHelper h, ServerPlayer player) {
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        h.setBlock(1, 1, 1, Blocks.CRAFTING_TABLE);
        var menu = new CraftingMenu(1, player.getInventory(),
                ContainerLevelAccess.create(h.getLevel(), h.absolutePos(new BlockPos(1, 1, 1))));
        player.containerMenu = menu;
        return menu;
    }

    private static ItemStack book(GameTestHelper h, boolean infinity) {
        var book = new ItemStack(Items.ENCHANTED_BOOK);
        var enchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchants.upgrade(h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(infinity ? Enchantments.INFINITY : Enchantments.MENDING), 1);
        book.set(DataComponents.STORED_ENCHANTMENTS, enchants.toImmutable());
        return book;
    }

    private static long inventoryCount(ServerPlayer player, Item item) {
        long count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }

    private static long inventoryTotal(ServerPlayer player) {
        long count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++)
            count += player.getInventory().getItem(i).getCount();
        return count;
    }

    @GameTest
    public void icyPickupConsumesExactlyThreeFourTwo(GameTestHelper h) {
        var player = h.makeMockServerPlayerInLevel();
        var menu = menu(h, player);
        var ingredients = List.of(Items.OBSIDIAN, Items.DIAMOND, Items.ICE,
                Items.DIAMOND, Items.OBSIDIAN, Items.DIAMOND,
                Items.ICE, Items.DIAMOND, Items.OBSIDIAN);
        var grid = menu.getInputGridSlots();
        for (int i = 0; i < 9; i++) grid.get(i).set(new ItemStack(ingredients.get(i), 2));
        menu.slotsChanged(grid.getFirst().container);
        h.assertTrue(menu.getResultSlot().getItem().is(ModAnomalyBlocks.ICY_OBSIDIAN.asItem())
                && menu.getResultSlot().getItem().getCount() == 1, "real registered icy result preview");
        menu.clicked(0, 0, ClickType.PICKUP, player);
        h.assertTrue(menu.getCarried().is(ModAnomalyBlocks.ICY_OBSIDIAN.asItem())
                && menu.getCarried().getCount() == 1, "exactly one icy result actually granted to cursor");
        h.assertTrue(inventoryTotal(player) == 0, "PICKUP must not also grant an inventory copy");
        for (int i = 0; i < 9; i++) h.assertTrue(grid.get(i).getItem().is(ingredients.get(i))
                && grid.get(i).getItem().getCount() == 1, "one consumed from every literal grid cell");
        h.succeed();
    }

    @GameTest
    public void temporaryQuickMoveConsumesTwoIcyForTwoOutputs(GameTestHelper h) {
        var player = h.makeMockServerPlayerInLevel();
        var menu = menu(h, player);
        var grid = menu.getInputGridSlots();
        grid.getFirst().set(new ItemStack(ModAnomalyBlocks.ICY_OBSIDIAN, 2));
        menu.slotsChanged(grid.getFirst().container);
        h.assertTrue(menu.getResultSlot().getItem().is(ModAnomalyBlocks.TEMPORARY_BARRIER.asItem()),
                "real registered temporary result");
        menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
        h.assertTrue(grid.stream().allMatch(slot -> slot.getItem().isEmpty()), "both icy inputs consumed");
        h.assertTrue(inventoryCount(player, ModAnomalyBlocks.TEMPORARY_BARRIER.asItem()) == 2
                && inventoryTotal(player) == 2, "exactly two temporary grants and no unrelated inventory item");
        h.assertTrue(menu.getCarried().isEmpty() && menu.getResultSlot().getItem().isEmpty(),
                "no cursor copy or further preview after input exhausted");
        h.succeed();
    }

    @GameTest
    public void infinityPickupConsumesOneIcyAndOneBook(GameTestHelper h) {
        var player = h.makeMockServerPlayerInLevel();
        var menu = menu(h, player);
        var grid = menu.getInputGridSlots();
        grid.get(0).set(new ItemStack(ModAnomalyBlocks.ICY_OBSIDIAN, 2));
        grid.get(1).set(book(h, true));
        menu.slotsChanged(grid.getFirst().container);
        h.assertTrue(menu.getResultSlot().getItem().is(ModAnomalyBlocks.PERMANENT_BARRIER.asItem())
                && menu.getResultSlot().getItem().getCount() == 1, "genuine Infinity native result");
        menu.clicked(0, 0, ClickType.PICKUP, player);
        h.assertTrue(menu.getCarried().is(ModAnomalyBlocks.PERMANENT_BARRIER.asItem())
                && menu.getCarried().getCount() == 1, "one permanent grant on cursor");
        h.assertTrue(inventoryTotal(player) == 0, "no unintended inventory grant or returned book");
        h.assertTrue(grid.get(0).getItem().is(ModAnomalyBlocks.ICY_OBSIDIAN.asItem())
                && grid.get(0).getItem().getCount() == 1 && grid.get(1).getItem().isEmpty(),
                "one icy and one real book consumed");
        for (int i = 2; i < 9; i++) h.assertTrue(grid.get(i).getItem().isEmpty(), "other inputs remain empty");
        h.assertTrue(menu.getResultSlot().getItem().is(ModAnomalyBlocks.TEMPORARY_BARRIER.asItem())
                && menu.getResultSlot().getItem().getCount() == 1
                && !menu.getResultSlot().getItem().is(ModAnomalyBlocks.PERMANENT_BARRIER.asItem()),
                "book consumed; remaining icy previews one ordinary temporary barrier, not permanent");
        h.succeed();
    }

    @GameTest
    public void wrongStoredEnchantmentCannotCraftOrConsume(GameTestHelper h) {
        var player = h.makeMockServerPlayerInLevel();
        var menu = menu(h, player);
        var grid = menu.getInputGridSlots();
        var icy = new ItemStack(ModAnomalyBlocks.ICY_OBSIDIAN, 2);
        var mending = book(h, false);
        var expectedIcy = icy.copy();
        var expectedBook = mending.copy();
        grid.get(0).set(icy);
        grid.get(1).set(mending);
        menu.slotsChanged(grid.getFirst().container);
        h.assertTrue(menu.getResultSlot().getItem().isEmpty(), "Mending-only book must not craft Infinity recipe");
        menu.clicked(0, 0, ClickType.PICKUP, player);
        h.assertTrue(menu.getResultSlot().getItem().isEmpty() && menu.getCarried().isEmpty()
                && inventoryTotal(player) == 0, "invalid click cannot grant any item");
        h.assertTrue(ItemStack.isSameItemSameComponents(grid.get(0).getItem(), expectedIcy)
                && grid.get(0).getItem().getCount() == 2, "invalid recipe must preserve icy input");
        h.assertTrue(ItemStack.isSameItemSameComponents(grid.get(1).getItem(), expectedBook)
                && grid.get(1).getItem().getCount() == 1, "invalid recipe must preserve book components/count");
        for (int i = 2; i < 9; i++) h.assertTrue(grid.get(i).getItem().isEmpty(), "no fabricated other input");
        h.succeed();
    }
}
