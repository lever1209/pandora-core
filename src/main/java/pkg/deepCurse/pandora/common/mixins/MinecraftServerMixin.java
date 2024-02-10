package pkg.deepCurse.pandora.common.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import pkg.deepCurse.pandora.common.config.CommonConfig;
import pkg.deepCurse.pandora.common.interfaces.PlayerGrueDataInterface;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

	@Inject(method = "setDifficulty", at = @At(value = "RETURN"))
	private void pandora_setDifficulty(Difficulty difficulty, boolean forceUpdate, CallbackInfo ci) {
		var isHardcore = ((MinecraftServer) (Object) this).isHardcore();
		var playerList = ((MinecraftServer) (Object) this).getPlayerManager().getPlayerList();

		for (var serverPlayerEntity : playerList) {
			var playerEntity = (PlayerEntity) serverPlayerEntity;
			var playerGrueDataInterface = (PlayerGrueDataInterface) playerEntity;

			var difficultySettings = CommonConfig.COMMON.DifficultySettingsMap
					.get(CommonConfig.DifficultySettingsEnum.getFromDifficulty(difficulty));
			if (isHardcore && CommonConfig.COMMON.Player_UsesHardcoreDifficulty) {
				difficultySettings = CommonConfig.COMMON.DifficultySettingsMap
						.get(CommonConfig.DifficultySettingsEnum.Hardcore);
			}

			playerGrueDataInterface.setLastTutorialEncounterTime(0);
			playerGrueDataInterface.setSkipTimeCheck(true);
			playerGrueDataInterface.setTutorialEncountersLeft(difficultySettings.grueTutorialCount);
		}
	}

}
