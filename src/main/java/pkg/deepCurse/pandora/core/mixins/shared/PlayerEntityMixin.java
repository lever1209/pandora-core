package pkg.deepCurse.pandora.core.mixins.shared;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import pkg.deepCurse.pandora.core.util.interfaces.PlayerGrueDataInterface;

//import net.minecraft.nbt.NbtCompound;
//import net.minecraft.world.PersistentState;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements PlayerGrueDataInterface {

	private long lastEncounterTime; // PandoraGrueLastEncounterTime
	private short trainingWheelEncountersLeft; // PandoraGrueTrainingWheelEncountersLeft

	@Override
	public short getTrainingWheelEncountersLeft() {
		return trainingWheelEncountersLeft;
	}

	@Override
	public void setTrainingWheelEncountersLeft(short trainingWheelEncountersLeft) {
		this.trainingWheelEncountersLeft = trainingWheelEncountersLeft;
	}

	@Override
	public long getLastEncounterTime() {
		return lastEncounterTime;
	}

	@Override
	public void setLastEncounterTime(long lastEncounterTime) {
		this.lastEncounterTime = lastEncounterTime;
	}

	@Inject(method = "readCustomDataFromNbt", at = @At(value = "RETURN"))
	public void pandora_readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
		this.trainingWheelEncountersLeft = nbt.contains("PandoraGrueTrainingWheelEncountersLeft", NbtElement.SHORT_TYPE)
				? 7
				/* TODO config this */ : nbt.getShort("PandoraGrueTrainingWheelEncountersLeft");
		this.lastEncounterTime = nbt.contains("PandoraGrueLastEncounterTime", NbtElement.LONG_TYPE) ? 0
				/* TODO config this */ : nbt.getLong("PandoraGrueLastEncounterTime");
	}

	@Inject(method = "writeCustomDataToNbt", at = @At(value = "RETURN"))
	public void pandora_writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
		nbt.putShort("PandoraGrueTrainingWheelEncountersLeft", trainingWheelEncountersLeft);
		nbt.putLong("PandoraGrueLastEncounterTime", lastEncounterTime);
	}
}
/*
 *  
 */
