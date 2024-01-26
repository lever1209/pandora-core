package pkg.deepCurse.pandora.core;

import net.minecraft.entity.damage.DamageSource;

public class GrueDamageSource extends DamageSource {
	protected boolean fire;
	public static final DamageSource GRUE = new GrueDamageSource("pandora.darkness");

	protected GrueDamageSource(String name) {
		super(name);
	}

	protected GrueDamageSource setFire() {
		this.fire = true;
		return this;
	}
}