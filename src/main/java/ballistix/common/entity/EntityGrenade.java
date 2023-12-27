package ballistix.common.entity;

import ballistix.api.entity.IDefusable;
import ballistix.common.blast.Blast;
import ballistix.common.block.subtype.SubtypeBlast;
import ballistix.common.item.ItemGrenade.SubtypeGrenade;
import ballistix.registers.BallistixBlocks;
import ballistix.registers.BallistixEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityGrenade extends ThrowableEntity implements IDefusable {

	private static final DataParameter<Integer> FUSE = EntityDataManager.defineId(EntityGrenade.class, DataSerializers.INT);
	private static final DataParameter<Integer> TYPE = EntityDataManager.defineId(EntityGrenade.class, DataSerializers.INT);
	private int grenadeOrdinal = -1;
	public int fuse = 80;

	public EntityGrenade(EntityType<? extends EntityGrenade> type, World worldIn) {
		super(type, worldIn);
	}

	public EntityGrenade(World worldIn) {
		this(BallistixEntities.ENTITY_GRENADE.get(), worldIn);
	}

	public void setExplosiveType(SubtypeGrenade explosive) {
		grenadeOrdinal = explosive.ordinal();
		fuse = explosive.explosiveType.fuse;
	}

	public SubtypeGrenade getExplosiveType() {
		return grenadeOrdinal == -1 ? null : SubtypeGrenade.values()[grenadeOrdinal];
	}

	@Override
	protected void defineSynchedData() {
		entityData.define(FUSE, 80);
		entityData.define(TYPE, -1);
	}

	@Override
	public void defuse() {
		remove(false);
		if (grenadeOrdinal != -1) {
			SubtypeBlast explosive = SubtypeGrenade.values()[grenadeOrdinal].explosiveType;
			ItemEntity item = new ItemEntity(level, blockPosition().getX() + 0.5, blockPosition().getY() + 0.5, blockPosition().getZ() + 0.5, new ItemStack(BallistixBlocks.SUBTYPEBLOCKREGISTER_MAPPINGS.get(explosive).get()));
			level.addFreshEntity(item);
		}
	}

	@Override
	public boolean isPickable() {
		return !isAlive();
	}

	@Override
	public void tick() {
		if (!level.isClientSide) {
			entityData.set(TYPE, grenadeOrdinal);
			entityData.set(FUSE, fuse);
		} else {
			grenadeOrdinal = entityData.get(TYPE);
			fuse = entityData.get(FUSE);
		}
		if (!isNoGravity()) {
			this.setDeltaMovement(getDeltaMovement().add(0.0D, -0.04D, 0.0D));
		}

		move(MoverType.SELF, getDeltaMovement());
		this.setDeltaMovement(getDeltaMovement().scale(0.98D));
		if (onGround) {
			this.setDeltaMovement(getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
		}
		--fuse;
		if (fuse <= 0) {
			remove(false);
			if (grenadeOrdinal != -1) {
				SubtypeBlast explosive = SubtypeGrenade.values()[grenadeOrdinal].explosiveType;
				Blast b = Blast.createFromSubtype(explosive, level, blockPosition());
				if (b != null) {
					b.performExplosion();
				}
			}
		} else {
			updateInWaterStateAndDoFluidPushing();
			if (level.isClientSide) {
				level.addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.5D, getZ(), 0.0D, 0.0D, 0.0D);
			}
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundNBT compound) {
		compound.putInt("Fuse", fuse);
		compound.putInt("type", grenadeOrdinal);
	}

	@Override
	protected void readAdditionalSaveData(CompoundNBT compound) {
		fuse = compound.getInt("Fuse");
		grenadeOrdinal = compound.getInt("type");
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
