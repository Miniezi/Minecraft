package io.github.mcatinkers.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.Optional;

/**
 * Patches the concrete MCA EquipmentTask call site, not the InventoryUtils
 * interface. Includes standard sword items plus modular Tinkers weapons.
 */
@Mixin(targets = "forge.net.mca.entity.ai.brain.tasks.EquipmentTask", remap = false)
public abstract class McaSwordSelectionMixin {
    @Redirect(method = "equipBestWeapon",
              at = @At(value = "INVOKE",
                target = "Lforge/net/mca/util/InventoryUtils;getBestSword(Lnet/minecraft/world/Container;)Ljava/util/Optional;"),
              remap = false)
    private Optional<ItemStack> mcatinkers$findModularSword(Container inventory) {
        ItemStack chosen = ItemStack.EMPTY;
        double best = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof SwordItem) && !isTinkersMelee(stack)) continue;
            double score = damage(stack);
            if (chosen.isEmpty() || score > best) {
                chosen = stack;
                best = score;
            }
        }
        return chosen.isEmpty() ? Optional.empty() : Optional.of(chosen);
    }

    private static boolean isTinkersMelee(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !id.getNamespace().equals("tconstruct")) return false;
        String name = id.getPath();
        return name.equals("sword") || name.equals("dagger") ||
               name.equals("cleaver") || name.equals("battlesign") ||
               name.equals("flint_and_brick");
    }

    private static double damage(ItemStack stack) {
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
