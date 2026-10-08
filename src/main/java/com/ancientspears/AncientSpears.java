package com.ancientspears;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class AncientSpears implements ModInitializer {
    public static final String MOD_ID = "ancient_spears";
    public static final Item WOODEN_SPEAR = register("wooden_spear", new Item(new Item.Settings().maxCount(1).maxDamage(59)));
    public static final Item STONE_SPEAR = register("stone_spear", new Item(new Item.Settings().maxCount(1).maxDamage(131)));
    public static final Item IRON_SPEAR = register("iron_spear", new Item(new Item.Settings().maxCount(1).maxDamage(250)));
    public static final Item GOLDEN_SPEAR = register("golden_spear", new Item(new Item.Settings().maxCount(1).maxDamage(32)));
    public static final Item DIAMOND_SPEAR = register("diamond_spear", new Item(new Item.Settings().maxCount(1).maxDamage(1561)));
    public static final Item NETHERITE_SPEAR = register("netherite_spear", new Item(new Item.Settings().maxCount(1).maxDamage(2031).fireproof()));

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name), item);
    }

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(WOODEN_SPEAR);
            entries.add(STONE_SPEAR);
            entries.add(IRON_SPEAR);
            entries.add(GOLDEN_SPEAR);
            entries.add(DIAMOND_SPEAR);
            entries.add(NETHERITE_SPEAR);
        });
    }
}
