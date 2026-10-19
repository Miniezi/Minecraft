package io.github.mcatinkers.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * MCA's InventoryUtils.getBestSword only accepts SwordItem, while Tinkers
 * modular weapons are not necessarily subclasses of SwordItem.
 * Preserve vanilla selection, but include TConstruct melee weapons.
 */
@Mixin(targets = "forge.net.mca.util.InventoryUtils", remap = false)
public interface McaSwordSelectionMixin {
    @Inject(method = "getBestSword", at = @At("RETURN"), cancellable = true, remap = false)
    static void mcatinkers$findModularSword(Container inventory,
           CallbackInfoReturnable<Optional<ItemStack>> cir) {
        ItemStack chosen = cir.getReturnValue().orElse(ItemStack.EMPTY);
        double best = damage(chosen);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!isTinkersMelee(stack)) continue;
            double score = damage(stack);
            if (chosen.isEmpty() || score > best) {
                chosen = stack;
                best = score;
            }
        }
        if (!chosen.isEmpty()) cir.setReturnValue(Optional.of(chosen));
    }

    static boolean isTinkersMelee(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !id.getNamespace().equals("tconstruct")) return false;
        String path = id.getPath();
        return path.equals("sword") || path.equals("dagger") ||
               path.equals("cleaver") || path.equals("battlesign") ||
               path.equals("flint_and_brick");
    }

    static double damage(ItemStack stack) {
        if (stack.isEmpty()) return Double.NEGATIVE_INFINITY;
        final double[] total = {0d};
        stack.getAttributeModifiers(EquipmentSlot.MAINHAND)
            .get(Attributes.ATTACK_DAMAGE)
            .forEach(modifier -> {
                switch (modifier.getOperation()) {
                    case ADDITION -> total[0] += modifier.getAmount();
                    case MULTIPLY_BASE, MULTIPLY_TOTAL -> total[0] += modifier.getAmount() * 3d;
                }
            });
        return total[0];
    }
}
