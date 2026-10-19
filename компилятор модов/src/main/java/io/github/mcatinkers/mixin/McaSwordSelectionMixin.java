package io.github.mcatinkers.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

/**
 * MCA inventory AI rejects modular Tinkers melee equipment because it
 * checks vanilla SwordItem. This redirects only the weapon selection call
 * inside MCA's EquipmentTask, leaving its fishing behavior unchanged.
 */
@Mixin(targets = "forge.net.mca.entity.ai.brain.tasks.EquipmentTask", remap = false)
public abstract class McaSwordSelectionMixin {
    @Redirect(method = "equipBestWeapon",
              at = @At(value = "INVOKE",
                 target = "Lforge/net/mca/util/InventoryUtils;getBestSword(Lnet/minecraft/world/Container;)Ljava/util/Optional;"),
              remap = false)
    private Optional<ItemStack> mcatinkers$findModularWeapon(Container inventory) {
        ItemStack selected = ItemStack.EMPTY;
        double strongest = Double.NEGATIVE_INFINITY;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty() || !isWeapon(candidate)) continue;
            double strength = attackDamageBonus(candidate);
            if (selected.isEmpty() || strength > strongest) {
                selected = candidate;
                strongest = strength;
            }
        }
        return selected.isEmpty() ? Optional.empty() : Optional.of(selected);
    }

    private static boolean isWeapon(ItemStack stack) {
        if (stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem) return true;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return false;
        String namespace = id.getNamespace();
        if (!namespace.equals("tconstruct") && !namespace.equals("tinkers_thinking")) return false;
        // Modular tools expose their effective damage through stack attributes.
        // This catches material-dependent sabres/broadswords and Tinkers axes.
        // Do not classify fishing rods, bows, casting components or broken tools
        // as melee equipment.
        String path = id.getPath();
        if (path.contains("bow") || path.contains("fishing") || path.contains("staff")
              || path.contains("arrow") || path.contains("cast") || path.contains("armor")
              || path.contains("helmet") || path.contains("chestplate")
              || path.contains("leggings") || path.contains("boots")) return false;
        return attackDamageBonus(stack) > 0;
    }

    private static double attackDamageBonus(ItemStack stack) {
        final double[] result = {0.0d};
        stack.getAttributeModifiers(EquipmentSlot.MAINHAND)
            .get(Attributes.ATTACK_DAMAGE)
            .forEach(attribute -> {
                switch (attribute.getOperation()) {
                    case ADDITION -> result[0] += attribute.getAmount();
                    case MULTIPLY_BASE, MULTIPLY_TOTAL -> result[0] += attribute.getAmount() * 3.0d;
                }
            });
        return result[0];
    }
}
