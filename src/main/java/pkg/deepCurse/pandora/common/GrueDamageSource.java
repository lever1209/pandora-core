package pkg.deepCurse.pandora.common;

import net.minecraft.entity.damage.DamageSource;

public class GrueDamageSource extends DamageSource {

	public GrueDamageSource(boolean gruesBypassArmor, boolean gruesBypassProtection) {
		super("pandora.darkness");
		if (gruesBypassArmor) {
			this.setBypassesArmor();
		}
		if (gruesBypassProtection) {
			this.setBypassesProtection();
		}
	}
}