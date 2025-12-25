package com.sacredtower.munchies;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.food.FoodProperties;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = Munchies.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue AUTO_ASSIGN_FROM_NUTRITION = BUILDER.comment("Automatically set food use duration to 5 * nutrition points when not in foodUseDurations.")
            .define("autoAssignFromNutrition", false);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> FOOD_USE_DURATION_STRINGS = BUILDER.comment("List of food items with custom use durations in ticks, formatted as item_id=ticks.")
            .defineListAllowEmpty("foodUseDurations", List.of("minecraft:apple=20", "minecraft:carrot=20", "minecraft:beetroot=10", "minecraft:sweet_berries=5", "minecraft:glow_berries=5, minecraft:cookie=5"), Config::validateFoodUseDuration);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    private static final Map<Item, Integer> FOOD_USE_DURATIONS = new HashMap<>();
    private static boolean autoAssignFromNutrition;

    private static boolean validateFoodUseDuration(final Object obj) {
        if (!(obj instanceof final String entry)) {
            return false;
        }

        int separatorIndex = entry.indexOf('=');
        if (separatorIndex <= 0 || separatorIndex == entry.length() - 1) {
            return false;
        }

        String itemId = entry.substring(0, separatorIndex).trim();
        String ticksRaw = entry.substring(separatorIndex + 1).trim();
        ResourceLocation itemKey = ResourceLocation.tryParse(itemId);
        if (itemKey == null || !ForgeRegistries.ITEMS.containsKey(itemKey)) {
            return false;
        }

        Item item = ForgeRegistries.ITEMS.getValue(itemKey);
        if (item == null || !item.isEdible()) {
            return false;
        }

        try {
            return Integer.parseInt(ticksRaw) > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        autoAssignFromNutrition = AUTO_ASSIGN_FROM_NUTRITION.get();
        FOOD_USE_DURATIONS.clear();
        for (String entry : FOOD_USE_DURATION_STRINGS.get()) {
            int separatorIndex = entry.indexOf('=');
            if (separatorIndex <= 0 || separatorIndex == entry.length() - 1) {
                continue;
            }

            String itemId = entry.substring(0, separatorIndex).trim();
            String ticksRaw = entry.substring(separatorIndex + 1).trim();
            ResourceLocation itemKey = ResourceLocation.tryParse(itemId);
            if (itemKey == null) {
                continue;
            }

            Item item = ForgeRegistries.ITEMS.getValue(itemKey);
            if (item == null || !item.isEdible()) {
                continue;
            }

            try {
                int ticks = Integer.parseInt(ticksRaw);
                if (ticks > 0) {
                    FOOD_USE_DURATIONS.put(item, ticks);
                }
            } catch (NumberFormatException ignored) {
                // Validation keeps most bad entries out, but skip any remaining invalid values.
            }
        }
    }

    public static Integer getFoodUseDuration(ItemStack stack) {
        if (!stack.isEdible()) {
            return null;
        }

        Integer configured = FOOD_USE_DURATIONS.get(stack.getItem());
        if (configured != null) {
            return configured;
        }

        if (!autoAssignFromNutrition) {
            return null;
        }

        FoodProperties foodProperties = stack.getFoodProperties(null);
        if (foodProperties == null) {
            return null;
        }

        int nutrition = foodProperties.getNutrition();
        return nutrition > 0 ? nutrition * 5 : null;
    }
}
