package dev.energogrid;

import com.example.datacenter.hosting.HostingRackBlockEntity;
import com.rhille.money.api.MoneyAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

final class ServiceOrders {
    static void bind(PlayerInteractEvent.RightClickBlock e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || !p.isShiftKeyDown()
                || !p.level().dimension().equals(Level.OVERWORLD)) return;
        if (e.getLevel().getBlockEntity(e.getPos()) instanceof HostingRackBlockEntity rack && rack.getOwner().isOwnedBy(p)) {
            p.getPersistentData().putLong("eg_service_rack", e.getPos().asLong());
            Supply.say(p,"Стойка назначена для заказов хранения и вычислений.");
        }
    }

    static String sample(ServerPlayer p, int job, boolean advance) {
        var state = p.getPersistentData();
        if (!state.contains("eg_service_rack")) return "Назначьте собственную стойку хостинга в обычном мире: Shift + ПКМ.";
        var pos = BlockPos.of(state.getLong("eg_service_rack"));
        var world = p.server.overworld();
        if (!world.hasChunkAt(pos)) return "Чанк стойки выгружен.";
        if (!(world.getBlockEntity(pos) instanceof HostingRackBlockEntity rack) || !rack.getOwner().isOwnedBy(p))
            return "Стойка снята или сменила владельца.";
        if (rack.getThermal().isCrashZone() || rack.isFaulted()) return "Авария стойки. Проверьте температуру, вентиляторы и износ.";
        if (!rack.netLink().connected()) return "Нет сети. Проверьте коммутатор и сетевой кабель.";
        if (rack.getActiveBayMask()==0 || rack.getLastPowerDemand()<=0) return "Нет активного сервера. Проверьте питание и сборку.";
        long disk=0, ram=0;
        for (int i=0; i<rack.loadout().bayCount(); i++) {
            if ((rack.getActiveBayMask() & (1<<i))==0) continue;
            var bay=rack.loadout().bay(i);
            if (bay!=null) { disk+=bay.storageGb; ram+=bay.ramMb; }
        }
        if (job==2 && disk<30) return "Для хранения требуется 30 ГБ активных дисков. Доступно: " + disk;
        if (job==3 && (ram<512 || rack.getLastComputeOutput()<=0)) return "Для вычислений требуется 512 МБ ОЗУ и ненулевая вычислительная нагрузка.";
        int demand=job==2 ? 200 : 600;
        var energy=world.getCapability(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                pos.below(),net.minecraft.core.Direction.UP);
        if (energy==null || energy.extractEnergy(demand,true)<demand)
            return "Поместите под стойкой заряженный накопитель с выходом вверх.";
        int seconds=job==2 ? 45 : 90;
        if (advance && Supply.score(p,"eg_work")<seconds) {
            energy.extractEnergy(demand,false);
            Supply.add(p,"eg_work",1);
        }
        if (advance && Supply.score(p,"eg_work")>=seconds && MoneyAPI.pay(p,MoneyAPI.dollars(job==2?12:36),"Энергосеть: серверный заказ").success()) {
            Supply.add(p,"eg_deals",1);
            Supply.add(p,job==2?"eg_storage":"eg_compute",1);
            Supply.add(p,"eg_job",-job);
            return "Заказ оплачен.";
        }
        return "Работа: " + Supply.score(p,"eg_work") + "/" + seconds + " с. Диски " + disk + " ГБ, ОЗУ " + ram + " МБ.";
    }
}
