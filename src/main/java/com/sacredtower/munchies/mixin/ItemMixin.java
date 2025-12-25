package com.sacredtower.munchies.mixin;

import com.sacredtower.munchies.Config;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void munchies$overrideUseDuration(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        Integer duration = Config.getFoodUseDuration(stack);
        if (duration != null) {
            cir.setReturnValue(duration);
        }
    }
}
