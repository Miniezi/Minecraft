package io.github.mcatinkers.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The MCA fisherman searches its inventory with this predicate.
 * TConstruct rods are not vanilla FishingRodItem subclasses.
 * Returning true here lets MCA's own equipFishingRod and fishing chore run.
 */
@Mixin(targets = "forge.net.mca.entity.ai.brain.tasks.chore.FishingTask", remap = false)
public abstract class McaFishingRodMixin {
    @Inject(method = "lambda$equipFishingRod$4", at = @At("HEAD"),
            cancellable = true, remap = false)
    private static void mcatinkers$acceptRod(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.isEmpty()) return;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id != null && id.getNamespace().equals("tconstruct")
                && id.getPath().equals("fishing_rod")) {
            cir.setReturnValue(true);
        }
    }
}
