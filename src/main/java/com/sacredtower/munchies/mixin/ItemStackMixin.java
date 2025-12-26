package com.sacredtower.munchies.mixin;

import com.sacredtower.munchies.Config;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void munchies$overrideUseDuration(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        Integer duration = Config.getFoodUseDuration(stack);
        if (duration != null) {
            cir.setReturnValue(duration);
        }
    }
}
