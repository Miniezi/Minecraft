package io.github.mcatinkers;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

/** Forge 1.20.1 diagnostics for MCA Reborn + Tinkers' Construct. */
@Mod(McaTinkersCompat.MOD_ID)
public final class McaTinkersCompat {
    public static final String MOD_ID = "mcatinkerscompat";

    public McaTinkersCompat() {
        MinecraftForge.EVENT_BUS.register(new CompatibilityDiagnostics());
    }
}
