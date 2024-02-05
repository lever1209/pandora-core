package pkg.deepCurse.pandora.common.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import pkg.deepCurse.pandora.common.util.interfaces.PlayerGrueDataInterface;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements PlayerGrueDataInterface {

	private long lastTutorialEncounterTime; // PandoraGrueLastTutorialEncounterTime
	private short tutorialEncountersLeft; // PandoraGrueTutorialEncountersLeft

	@Override
	public short getTutorialEncountersLeft() {
		return tutorialEncountersLeft;
	}

	@Override
	public void setTutorialEncountersLeft(short tutorialEncountersLeft) {
		this.tutorialEncountersLeft = tutorialEncountersLeft;
	}

	@Override
	public long getLastTutorialEncounterTime() {
		return lastTutorialEncounterTime;
	}

	@Override
	public void setLastTutorialEncounterTime(long lastTutorialEncounterTime) {
		this.lastTutorialEncounterTime = lastTutorialEncounterTime;
	}

	@Inject(method = "readCustomDataFromNbt", at = @At(value = "RETURN"))
	public void pandora_readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
//		Pandora.log.info("READING {}", nbt);
		this.tutorialEncountersLeft = nbt.contains("PandoraGrueTutorialEncountersLeft", NbtElement.SHORT_TYPE)
				? nbt.getShort("PandoraGrueTutorialEncountersLeft")
				: 7 /* TODO config this */ ;
		this.lastTutorialEncounterTime = nbt.contains("PandoraGrueLastTutorialEncounterTime", NbtElement.LONG_TYPE)
				? nbt.getLong("PandoraGrueLastTutorialEncounterTime")
				: 0 /* TODO config this */ ;
	}

	@Inject(method = "writeCustomDataToNbt", at = @At(value = "RETURN"))
	public void pandora_writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
		nbt.putShort("PandoraGrueTutorialEncountersLeft", tutorialEncountersLeft);
		nbt.putLong("PandoraGrueLastTutorialEncounterTime", lastTutorialEncounterTime);
//		Pandora.log.info("WRITING {}", nbt);
	}
}
/*
 *  
 */
