package xzeroair.trinkets.capabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

/**
 * 通用能力提供者。
 *
 * 移植说明：1.12 的 IStorage（原 CapabilityStorage）在 1.20.1 已从 Forge 移除——
 * 序列化不再经 Capability.writeNBT/readNBT 中转，改由本提供者直接调用 handler 的 saveToNBT/loadFromNBT。
 * 另外 hasCapability + getCapability 两个方法合并为返回 LazyOptional 的单方法。
 *
 * 只以 ITrinketCapability 为界：提供者仅负责存取与失效，不关心宿主是实体还是物品栈。
 *
 * @param <H> 能力实现类型
 */
public class CapabilityProviderBase<H extends ITrinketCapability<?>> implements ICapabilitySerializable<CompoundTag> {

    protected final Capability<H> capability;
    protected final H handler;
    protected LazyOptional<H> optional;

    public CapabilityProviderBase(final Capability<H> capability, final H handler) {
        this.capability = capability;
        this.handler = handler;
        this.optional = LazyOptional.of(() -> handler);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap != this.capability) {
            return LazyOptional.empty();
        }

        /*
         * ServerPlayer 跨维度时 Forge 会先 invalidateCaps()，随后对同一个玩家实例
         * 调用 reviveCaps()。Forge 只恢复宿主 CapabilityProvider 的 valid 标记，不会
         * 重建 AttachCapabilitiesEvent 中 provider 自己持有的 LazyOptional。
         * 因此这里在宿主 revive 后首次重新查询时，使用原 handler 重建包装。
         * 实体真正失效时宿主本身 valid=false，不会进入到这里。
         */
        if (!this.optional.isPresent()) {
            this.optional = LazyOptional.of(() -> this.handler);
        }
        return this.optional.cast();
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.handler.saveToNBT(new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.handler.loadFromNBT(nbt);
    }

    /** 宿主失效时调用，避免持有者泄漏 */
    public void invalidate() {
        this.optional.invalidate();
    }

    public H getInstance() {
        return this.handler;
    }
}
