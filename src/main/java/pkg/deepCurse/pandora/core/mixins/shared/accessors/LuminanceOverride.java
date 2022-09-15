package pkg.deepCurse.pandora.core.mixins.shared.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.AbstractBlock;

@Mixin(AbstractBlock.AbstractBlockState.class)
public interface LuminanceOverride {
	@Mutable
	@Accessor
	void setLuminance(int value);
}
