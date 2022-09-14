package pkg.deepCurse.pandora.core;

import net.minecraft.entity.damage.DamageSource;

public class CustomDamageSources extends DamageSource {
	protected boolean fire;
	public static final DamageSource GRUE = new CustomDamageSources(
			"pandora.darkness");

	protected CustomDamageSources(String name) {
		super(name);
	}

	protected CustomDamageSources setFire() {
		this.fire = true;
		return this;
	}
}