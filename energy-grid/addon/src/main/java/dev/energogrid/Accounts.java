package dev.energogrid;

import com.rhille.money.api.AccountId;
import com.rhille.money.api.MoneyAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

final class Accounts {
    static void record(MinecraftServer server, AccountId account, long amount, String reason) {
        if (!account.isPlayer()) return;
        var p=server.getPlayerList().getPlayer(account.id());
        if (p==null) return;
        var data=p.getPersistentData();
        long second=server.overworld().getGameTime()/20;
        String key="eg_ledger_"+Math.floorMod(second,300);
        var bucket=data.getCompound(key);
        if (bucket.getLong("second")!=second) bucket=new CompoundTag();
        bucket.putLong("second",second);
        bucket.putLong("net",bucket.getLong("net")+amount);
        data.put(key,bucket);
        ListTag trades=data.getList("eg_history",Tag.TAG_COMPOUND);
        var trade=new CompoundTag();
        trade.putString("reason",reason==null?"Money":reason);
        trade.putLong("amount",amount);
        trade.putLong("second",second);
        trades.add(trade);
        if (trades.size()>12) trades.remove(0);
        data.put("eg_history",trades);
    }
    static int dashboard(ServerPlayer p) {
        var data=p.getPersistentData();
        long now=p.server.overworld().getGameTime()/20;
        long net=0;
        for (int i=0;i<300;i++) {
            var entry=data.getCompound("eg_ledger_"+i);
            if (entry.getLong("second")>now-300 && entry.getLong("second")<=now) net+=entry.getLong("net");
        }
        Supply.say(p,"Изменение счёта за последние 5 игровых минут: "+MoneyAPI.format(net));
        var trades=data.getList("eg_history",Tag.TAG_COMPOUND);
        for (int i=Math.max(0,trades.size()-4);i<trades.size();i++) {
            var t=trades.getCompound(i);
            Supply.say(p,t.getString("reason")+"  "+MoneyAPI.format(t.getLong("amount")));
        }
        return 1;
    }
}
