package pkg.deepCurse.pandora.common.mixins;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pkg.deepCurse.pandora.common.Pandora;
import pkg.deepCurse.pandora.common.config.CommonConfig;
import pkg.deepCurse.pandora.common.interfaces.PlayerGrueDataInterface;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements PlayerGrueDataInterface {

	private long lastTutorialEncounterTime; // PandoraGrueLastTutorialEncounterTime
	private short tutorialEncountersLeft; // PandoraGrueTutorialEncountersLeft
	private boolean skipTimeCheck; // PandoraGrueTutorialSkipTimeCheck

	@Inject(method = "<init>*", at = @At(value = "RETURN"))
	private void pandora_constructorReturn(World world, BlockPos pos, float yaw, GameProfile gameProfile,
			@Nullable PlayerPublicKey publicKey, CallbackInfo ci) {
		var pandoraDifficulty = CommonConfig.DifficultySettingsEnum.getFromDifficulty(world.getDifficulty());

		if (CommonConfig.COMMON.Player_UsesHardcoreDifficulty) {
			pandoraDifficulty = CommonConfig.DifficultySettingsEnum.Hardcore;
		}

		var difficultySettings = CommonConfig.COMMON.DifficultySettingsMap.get(pandoraDifficulty);

		this.tutorialEncountersLeft = difficultySettings.grueTutorialCount;
		this.lastTutorialEncounterTime = 0;
		this.skipTimeCheck = true;

		Pandora.log.info("CINIT {} {}", this.tutorialEncountersLeft, this.lastTutorialEncounterTime);
	}

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

	@Override
	public boolean skipTimeCheck() {
		return this.skipTimeCheck;
	}

	@Override
	public void setSkipTimeCheck(boolean skipTimeCheck) {
		this.skipTimeCheck = skipTimeCheck;
	}

	@Inject(method = "readCustomDataFromNbt", at = @At(value = "RETURN"))
	public void pandora_readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {

		var world = ((PlayerEntity) (Object) this).getWorld();
		var pandoraDifficulty = CommonConfig.DifficultySettingsEnum.getFromDifficulty(world.getDifficulty());

		if (CommonConfig.COMMON.Player_UsesHardcoreDifficulty) {
			pandoraDifficulty = CommonConfig.DifficultySettingsEnum.Hardcore;
		}

		var difficultySettings = CommonConfig.COMMON.DifficultySettingsMap.get(pandoraDifficulty);

		this.tutorialEncountersLeft = nbt.contains("PandoraGrueTutorialEncountersLeft", NbtElement.SHORT_TYPE)
				? nbt.getShort("PandoraGrueTutorialEncountersLeft")
				: difficultySettings.grueTutorialCount;
		this.lastTutorialEncounterTime = nbt.contains("PandoraGrueLastTutorialEncounterTime", NbtElement.LONG_TYPE)
				? nbt.getLong("PandoraGrueLastTutorialEncounterTime")
				: 0;
		this.skipTimeCheck = nbt.getBoolean("PandoraGrueTutorialSkipTimeCheck");
	}

	@Inject(method = "writeCustomDataToNbt", at = @At(value = "RETURN"))
	public void pandora_writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
		nbt.putShort("PandoraGrueTutorialEncountersLeft", tutorialEncountersLeft);
		nbt.putLong("PandoraGrueLastTutorialEncounterTime", lastTutorialEncounterTime);
		nbt.putBoolean("PandoraGrueTutorialSkipTimeCheck", this.skipTimeCheck);
	}
}
