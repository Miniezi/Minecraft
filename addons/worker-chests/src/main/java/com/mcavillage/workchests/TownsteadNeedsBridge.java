package com.mcavillage.workchests;

import net.minecraft.world.entity.Mob;
import net.minecraftforge.fml.ModList;
import java.lang.reflect.Method;

/**
 * Reflective read-only bridge for Townstead 0.7.x. Does not import/link MCA or
 * Townstead classes. Absent or changed API means no real-needs reading.
 */
public final class TownsteadNeedsBridge {
    private TownsteadNeedsBridge() {}
    public record State(int hunger, int thirst, int fatigue, boolean collapsed,
                        float hungerMoodDrift, boolean available) {}

    private static Object invoke(Object receiver, String name, Object... args) throws Exception {
        if (receiver == null) throw new IllegalStateException("Missing Townstead snapshot");
        Class<?> type = receiver instanceof Class<?> c ? c : receiver.getClass();
        for (Method m: type.getMethods()) {
            if (!m.getName().equals(name) || m.getParameterCount()!=args.length) continue;
            boolean valid=true;
            for(int i=0;i<args.length;i++) {
                if(args[i]!=null && !m.getParameterTypes()[i].isInstance(args[i]))valid=false;
            }
            if(!valid)continue;
            return m.invoke(receiver instanceof Class<?> ? null : receiver,args);
        }
        throw new NoSuchMethodException(type.getName()+"#"+name);
    }
    private static int num(Object o) { return ((Number)o).intValue(); }

    public static State read(Mob worker) {
        if(!ModList.get().isLoaded("townstead"))return new State(0,0,0,false,0,false);
        try {
            Class<?> api=Class.forName("com.aetherianartificer.townstead.api.TownsteadAPI");
            Object villager=invoke(api,"entity",worker);
            Object needs=invoke(villager,"needs");
            int hunger=num(invoke(needs,"hunger"));
            int thirst=num(invoke(needs,"thirst"));
            int fatigue=num(invoke(needs,"fatigue"));
            boolean collapsed=(Boolean)invoke(needs,"collapsed");
            // Mood: 0.7.x snapshots do not expose a universal MCA mood score.
            return new State(hunger,thirst,fatigue,collapsed,0,true);
        } catch(ReflectiveOperationException | LinkageError | IllegalArgumentException e) {
            return new State(0,0,0,false,0,false);
        }
    }
}
