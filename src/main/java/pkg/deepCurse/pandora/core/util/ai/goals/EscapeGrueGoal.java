package pkg.deepCurse.pandora.core.util.ai.goals;

import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import pkg.deepCurse.pandora.core.PandoraConfig;
import pkg.deepCurse.pandora.core.util.tools.PandoraTools;

/**
 * recovered from old pandora, 2021/10/21
 */
public class EscapeGrueGoal extends Goal /* MoveToTargetPosGoal? */ {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(EscapeGrueGoal.class);

	protected final MobEntity mob;

	private double targetX;

	private double targetY;

	private double targetZ;

	private final double speed;

	private final World world;

	private int minimumLightLevel;

	public EscapeGrueGoal(MobEntity mob, double speed, int minimumLightLevel) {
		this.mob = mob;
		this.speed = speed;
		this.world = mob.getWorld();
		this.minimumLightLevel = minimumLightLevel;
		setControls(EnumSet.of(Goal.Control.MOVE));
	}

	public boolean canStart() {
		if (!PandoraConfig.General.MobSettings.get(Registry.ENTITY_TYPE.getId(this.mob.getType())).FearDarkness) {
//			log.info("MobSettings {}", this.mob);
			return false;
		}
		if (this.mob.getTarget() != null) {
//			log.info("hasTarget {}", this.mob);
			return false;
		}
		if (PandoraTools.isNearLight(this.world, this.mob.getBlockPos(), this.minimumLightLevel)) {
//			log.info("isNearLight {}", this.mob);
			return false;
		}
		if (targetLightPos()) {
//			log.info("noValidPos {}", this.mob);
			return false;
		}
//		log.info("validPos {}", this.mob);
		return true;
	}

	protected boolean targetLightPos() {
		Vec3d vec3d = locateBrightPos();
		if (vec3d == null)
			return false;
		this.targetX = vec3d.getX();
		this.targetY = vec3d.getY();
		this.targetZ = vec3d.getZ();
		return true;
	}

	public boolean shouldContinue() {
		boolean shouldContinue = !this.mob.getNavigation().isIdle();
		return shouldContinue;
	}

	public void start() {
		this.mob.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, this.speed);
	}

	@Nullable
	protected Vec3d locateBrightPos() {
		Random random = this.mob.getRandom();
		BlockPos blockPos = this.mob.getBlockPos();
		for (int i = 0; i < 10; i++) {
			BlockPos blockPos2 = blockPos.add(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
			if (PandoraTools.isNearLight(this.world, blockPos2, this.minimumLightLevel)) {
				if (this.mob instanceof PathAwareEntity) {
					if (((PathAwareEntity) this.mob).getPathfindingFavor(blockPos2) < 0.0F)
						;
					return Vec3d.ofBottomCenter((Vec3i) blockPos2);
				}
			}
		}
		return null;
	}
}
