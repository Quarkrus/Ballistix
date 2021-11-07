package ballistix.common.blast;

import java.util.List;

import ballistix.common.block.SubtypeBlast;
import ballistix.common.settings.Constants;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class BlastDebilitation extends Blast {

    public BlastDebilitation(Level world, BlockPos position) {
	super(world, position);
    }

    @Override
    public boolean isInstantaneous() {
	return false;
    }

    @Override
    public boolean doExplode(int callCount) {
	hasStarted = true;
	int radius = (int) Constants.EXPLOSIVE_DEBILITATION_SIZE;
	if (world.isClientSide && world instanceof ClientLevel && callCount % 3 == 0) {
	    for (int x = -radius; x <= radius; x++) {
		for (int y = -radius; y <= radius; y++) {
		    for (int z = -radius; z <= radius; z++) {
			if (x * x + y * y + z * z < radius * radius && world.random.nextDouble() < 1 / 20.0) {
			    world.addParticle(new DustParticleOptions(1, 1, 1, 5), position.getX() + x + 0.5 + world.random.nextDouble() - 1.0,
				    position.getY() + y + 0.5 + world.random.nextDouble() - 1.0,
				    position.getZ() + z + 0.5 + world.random.nextDouble() - 1.0, 0.0D, 0.0D, 0.0D);
			}
		    }
		}
	    }
	}
	if (!world.isClientSide) {
	    float x = position.getX();
	    float y = position.getY();
	    float z = position.getZ();
	    int k1 = Mth.floor(x - (double) radius - 1.0D);
	    int l1 = Mth.floor(x + (double) radius + 1.0D);
	    int i2 = Mth.floor(y - (double) radius - 1.0D);
	    int i1 = Mth.floor(y + (double) radius + 1.0D);
	    int j2 = Mth.floor(z - (double) radius - 1.0D);
	    int j1 = Mth.floor(z + (double) radius + 1.0D);
	    List<Entity> list = world.getEntities(null, new AABB(k1, i2, j2, l1, i1, j1));
	    for (Entity entity : list) {
		if (entity instanceof LivingEntity) {
		    LivingEntity living = (LivingEntity) entity;
		    living.addEffect(new MobEffectInstance(MobEffects.POISON, 360));
		    living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 360));
		    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 360, 2));
		}
	    }
	}
	return callCount > Constants.EXPLOSIVE_DEBILITATION_DURATION;
    }

    @Override
    public SubtypeBlast getBlastType() {
	return SubtypeBlast.debilitation;
    }

}
