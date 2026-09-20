package xzeroair.trinkets.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import xzeroair.trinkets.capabilities.Capabilities;

public final class PlayerDataSync {

    public static void sync(ServerPlayer player) {
        player.getCapability(Capabilities.ENTITY_PROPERTIES).ifPresent(data -> {

            CompoundTag tag = data.saveToNBT(new CompoundTag());

            NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncPlayerDataPacket(tag)
            );
        });
    }

    private PlayerDataSync() {
    }
}