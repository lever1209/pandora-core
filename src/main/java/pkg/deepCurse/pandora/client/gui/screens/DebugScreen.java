package pkg.deepCurse.pandora.client.gui.screens;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import pkg.deepCurse.pandora.client.config.ClientConfig;
import pkg.deepCurse.pandora.common.CommonTools;
import pkg.deepCurse.pandora.common.Pandora;
import pkg.deepCurse.pandora.common.config.CommonConfig;
import pkg.deepCurse.pandora.common.config.DebugConfig;
import pkg.deepCurse.pandora.common.util.ConfigUtils;

@Environment(EnvType.CLIENT)
public class DebugScreen extends Screen {

	private Screen parent;
	private int pageNum = 1;
	private boolean enablePageBackground = true;
	private Text pageName = Text.translatable("pandora.menu.debug.page" + pageNum);

	private static Logger log = LoggerFactory.getLogger(DebugScreen.class);

	public DebugScreen(Screen parent) {
		super(Text.translatable("pandora.menu.debug.title"));
		this.parent = parent;
	}

	public DebugScreen(Screen parent, int i) {
		super(Text.translatable("pandora.menu.debug.title"));
		this.parent = parent;
		this.pageNum = i;
	}

	@Override
	public void init() {

		var buttonWidthPadding = 4;
		var buttonHeightPadding = 2;

		var buttonWidth = 120; // this.width / 6;
		var buttonHeight = 24;// this.height / 12;
		var usable_screen_width_offset = ((this.width % buttonWidth) / 2) + this.width / buttonWidth;
		var usable_screen_height_offset = ((this.height % buttonHeight) / 2) + this.height / buttonHeight;
		var usable_screen_width = this.width - (this.width % buttonWidth) - this.width / buttonWidth;
		var usable_screen_height = this.height - (this.height % buttonHeight) - this.height / buttonHeight;

		var center_pos_x = (usable_screen_width / 2) + usable_screen_width_offset;
		var center_pos_y = (usable_screen_height / 2) + usable_screen_height_offset;

		var columnCount = 3;
		var rowCount = 8;

		var centerOffsetHorizontalPosition = center_pos_x - ((buttonWidth / 2) - buttonHeightPadding) * columnCount;
		var centerOffsetVerticalPosition = center_pos_y - ((buttonHeight / 2) - buttonHeightPadding) * rowCount;

		var buttonPosX = 0;
		var buttonPosY = 0;

		pageName = Text.translatable("pandora.menu.debug.page" + pageNum);

		var previousButton = new ButtonWidget(
				buttonPosX++ * buttonWidth - buttonWidthPadding + centerOffsetHorizontalPosition,
				buttonPosY * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
				buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
				Text.translatable("pandora.menu.debug.previous.page"), (ButtonWidget var1) -> {
					pageNum--;
					enablePageBackground = true;
					this.clearAndInit();
				}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
					renderOrderedTooltip(matricies,
							textRenderer
									.wrapLines(
											Text.translatable(button.active ? "pandora.menu.debug.previous.page.tooltip"
													: "pandora.menu.debug.previous.page.tooltip.disabled"),
											buttonWidth * 2),
							mouseX, mouseY);
				});

		var pageNameField = new TextFieldWidget(textRenderer,
				buttonPosX++ * buttonWidth - buttonWidthPadding + centerOffsetHorizontalPosition,
				buttonPosY * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
				buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2), pageName);
		pageNameField.setEditable(false);
		pageNameField.setUneditableColor(0x00_FF_FF_FF);
		pageNameField.setText(pageName.getString());
		// TODO center the text within the field

		var nextButton = new ButtonWidget(
				buttonPosX * buttonWidth - buttonWidthPadding + centerOffsetHorizontalPosition,
				buttonPosY * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
				buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
				Text.translatable("pandora.menu.debug.next.page"), (ButtonWidget var1) -> {
					pageNum++;
					enablePageBackground = true;
					this.clearAndInit();
				}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
					renderOrderedTooltip(matricies,
							textRenderer
									.wrapLines(
											Text.translatable(button.active ? "pandora.menu.debug.next.page.tooltip"
													: "pandora.menu.debug.next.page.tooltip.disabled"),
											buttonWidth * 2),
							mouseX, mouseY);
				});

		buttonPosX = 0;
		buttonPosY++;

		// TODO allow page skipping when eventually there is a page that requires a
		// world

		switch (pageNum) {
		case 0:
			previousButton.active = false;
			enablePageBackground = false;
			break;
		case 1:
			firstPage(buttonPosX, buttonPosY, buttonWidthPadding, buttonHeightPadding, buttonWidth, buttonHeight,
					centerOffsetHorizontalPosition, centerOffsetVerticalPosition - usable_screen_height_offset);
			break;
		case 2:
			enablePageBackground = false;
			secondPage(buttonPosX, buttonPosY, buttonWidthPadding, buttonHeightPadding, buttonWidth, buttonHeight,
					centerOffsetHorizontalPosition, centerOffsetVerticalPosition - usable_screen_height_offset);
			break;
		case 3:
			nextButton.active = false;
			enablePageBackground = false;
			thirdPage(buttonPosX, buttonPosY, buttonWidthPadding, buttonHeightPadding, buttonWidth, buttonHeight,
					centerOffsetHorizontalPosition, centerOffsetVerticalPosition - usable_screen_height_offset);
			break;
		default:
			log.info(
					"[PandoraDebug] Switching page to 0 and reinitializing the DebugScreen since the page number has somehow gone out of bounds. Dont mind me. :)");
			pageNum = 1;
			this.clearAndInit();
			return;
		}

		this.addDrawableChild(pageNameField);
		this.addDrawableChild(previousButton);
		this.addDrawableChild(nextButton);

		buttonPosX = 2;
		buttonPosY = 8;

		this.addDrawableChild(
				new ButtonWidget(buttonPosX++ * buttonWidth - buttonWidthPadding + centerOffsetHorizontalPosition,
						buttonPosY * buttonHeight - buttonHeightPadding
								+ (centerOffsetVerticalPosition - usable_screen_height_offset),
//						Math.min(buttonPosY++ * buttonHeight - buttonHeightPadding + centerOffsetVerticalPosition,
//								height - buttonHeight),
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.return"), (ButtonWidget var1) -> {
							this.client.setScreen(this.parent);
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));

	}

	public static ButtonWidget plotPoints;

	public static Vec3d posA;
	public static Vec3d posB;

	private void thirdPage(int buttonPosX, int buttonPosY, int buttonWidthPadding, int buttonHeightPadding,
			int buttonWidth, int buttonHeight, int centerOffsetHorizontalPosition, int centerOffsetVerticalPosition) {
		if (this.client.player == null) {
			return;
		}
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + centerOffsetHorizontalPosition,
						buttonPosY++ * buttonHeight - buttonHeightPadding + centerOffsetVerticalPosition,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.set.pos.a"), (ButtonWidget var1) -> {

							posA = this.client.player.getPos();

						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));

		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + centerOffsetHorizontalPosition,
						buttonPosY++ * buttonHeight - buttonHeightPadding + centerOffsetVerticalPosition,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.set.pos.b"), (ButtonWidget var1) -> {

							posB = this.client.player.getPos();

						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));

		plotPoints = new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + centerOffsetHorizontalPosition,
				buttonPosY++ * buttonHeight - buttonHeightPadding + centerOffsetVerticalPosition,
				buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
				Text.translatable("pandora.menu.debug.place.ray.marched.diamonds"), (ButtonWidget var1) -> {

					var set = new HashSet<Vec3d>();

					double distance = posA.distanceTo(posB);

					// given the positions A and B, run the code under this function call distance *
					// 1.15f times with every interval running the code below
					CommonTools.rayMarch(posA, posB, (float) (distance * 1.15f), (pos) -> {

						// round off the block position so that the check for if we already placed the
						// block works (if we dont and the two positions we are checking with the set
						// are even a single 0.0000000001 off the check will fail and it will attempt to
						// place the block anyway)
						pos = new Vec3d(Math.round(pos.x), Math.round(pos.y), Math.round(pos.z));

						// i discovered this after fully implementing my version
//						this.client.world.getWorldChunk(posA).raycastBlock(pos, pos, posA, null, null); // WTF

						// check if we have already placed this block
						if (!set.contains(pos)) {
							// set the block
							this.client.player.sendCommand(String.format("setblock %s %s %s minecraft:gold_block",
									(int) pos.x, (int) pos.y, (int) pos.z));
							// add the position to the set so we do not attempt to place this block again
							set.add(pos);
						}

						// should stop after running this block
						return false;
					});

					this.client.player.sendCommand(String.format("setblock %s %s %s minecraft:diamond_block",
							(int) posA.getX(), (int) posA.getY(), (int) posA.getZ()));
					this.client.player.sendCommand(String.format("setblock %s %s %s minecraft:diamond_block",
							(int) posB.getX(), (int) posB.getY(), (int) posB.getZ()));

				}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
				});

		this.addDrawableChild(plotPoints);

		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + centerOffsetHorizontalPosition,
						buttonPosY++ * buttonHeight - buttonHeightPadding + centerOffsetVerticalPosition,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.set.sandbox"), (ButtonWidget var1) -> {

							log.info("{} {} {}", posA, posB, posA.distanceTo(posB));

							log.info("{} {} {}", CommonTools.iLerp(0, 99, 0.5));

						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));

		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + centerOffsetHorizontalPosition,
						buttonPosY++ * buttonHeight - buttonHeightPadding + centerOffsetVerticalPosition,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.testing.angles"), (ButtonWidget var1) -> {

//							log.info("{} {} {}", posA, posB, Math.sqrt(posA.getSquaredDistance(posB)));
//
//							log.info("{} {} {}", PandoraTools.InverseLerp(0, 99, 0.5));

							var blockPos = this.client.player.getBlockPos();
							var eyePosY = this.client.player.getPos().y
									+ this.client.player.getEyeHeight(this.client.player.getPose());
//							var mulValsAverage = 1f;

							var blockPosSet = new HashSet<Vec3d>();

//							for (var i2 = 0; i2 < 360; i2++) {
////			break;
//								for (var i3 = 0; i3 < 360; i3++) {
//				break;
//				var headPitch = MathHelper.wrapDegrees(player.getPitch()) * -1;
//				var headYaw = MathHelper.wrapDegrees(player.getHeadYaw()) * -1;

//							var headPitch = i2;
//							var headYaw = i3;

							var headPitch = this.client.player.getPitch();
							var headYaw = this.client.player.getHeadYaw();

//			headPitch = (headPitch >= 0 ? headPitch : Math.abs(headPitch));
//			headYaw = (headYaw >= 0 ? headYaw : Math.abs(headYaw));

							var dist = 45;

							var phi = (float) Math.toRadians(headPitch);
							var theta = (float) Math.toRadians(headYaw);

//			var posZ = MathHelper.sin(phi) * MathHelper.cos(theta) * dist;
//			var posY = Math.sin(phi) * dist; // try isine b // r is distance
//			var posX = MathHelper.sin(phi) * MathHelper.sin(theta) * dist;

//			var posZ = MathHelper.sin(phi) * MathHelper.cos(theta) * dist;
//			var posY = Math.sin(phi) * Math.sinh(theta) * dist; // try isine b // r is distance
//			var posX = MathHelper.sin(phi) * MathHelper.sin(theta) * dist;

//									var posX = Math.sin(theta) * dist;
//									var posY = Math.sin(phi) * dist;
//									var posZ = Math.cos(theta) * dist;

//			var posX = Math.sin(theta) * dist;
//			var posY = Math.sin(phi)   * dist * MathHelper.cos(theta);
//			var posZ = Math.cos(theta) * dist;

							var posX = dist * Math.sin(theta) * MathHelper.cos(phi);
							var posY = dist * Math.sin(phi);
							var posZ = dist * Math.cos(theta) * MathHelper.cos(phi);

//			posY = posY * PandoraTools.InverseLerp(posY, eyePosY, eyePosY + dist);

							Vec3d newPos = new Vec3d(Math.round(blockPos.getX() + posX), Math.round(eyePosY + posY),
									Math.round(blockPos.getZ() + posZ));

							if (!blockPosSet.contains(newPos)) {
								this.client.player
										.sendCommand(String.format("setblock %s %s %s minecraft:glowstone keep",
												(int) newPos.getX(), (int) newPos.getY(), (int) newPos.getZ()));
								blockPosSet.add(newPos);
							}
//								}
//							}
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));

	}

	@Override
	public void tick() {
		if (plotPoints != null) {
			if (posA == null || posB == null) {
				plotPoints.active = false;
			} else {
				plotPoints.active = true;
			}
		}

		super.tick();
	}

	private void secondPage(int buttonPosX, int buttonPosY, int buttonWidthPadding, int buttonHeightPadding,
			int buttonWidth, int buttonHeight, int usable_screen_width_offset, int usable_screen_height_offset) {

		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.print.modified.block.lights"), (ButtonWidget var1) -> {
							for (var i : CommonConfig.COMMON.BlockLightLevelSettings.entrySet()) {
								for (var entry : Registry.BLOCK.get(i.getKey()).getStateManager().getStates()) {
									log.info("[PandoraDebug] {}={}", NbtHelper.fromBlockState(entry).toString(),
											i.getValue().LightLevel.applyAsInt(entry, entry.getLuminance()));
								}
							}
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));

		var world = this.client.world;
		if (world != null) {
			if (ClientConfig.CLIENT.clientDimensionConfigMap.get(world.getRegistryKey().getValue()) != null) {
				var fogLevel = ClientConfig.CLIENT.clientDimensionConfigMap.get(world.getRegistryKey().getValue())
						.getFogLevel();

				this.addDrawableChild(
						new SliderWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
								buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
								buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
								Text.literal("fog factor: " + fogLevel), fogLevel) {

							@Override
							protected void updateMessage() {
								super.setMessage(Text.literal("fog factor: " + this.value));
							}

							@Override
							protected void applyValue() {
								ClientConfig.CLIENT.clientDimensionConfigMap.get(world.getRegistryKey().getValue())
										.setFogLevel(Float.parseFloat(String.valueOf(this.value)));
							}
						});
				this.addDrawableChild(new ButtonWidget(
						buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.print.dimension.settings"), (ButtonWidget var1) -> {
							var settings = ClientConfig.CLIENT.clientDimensionConfigMap
									.get(world.getRegistryKey().getValue());
							log.info(settings.toString());
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
			}

			this.addDrawableChild(
					new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
							buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
							buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
							Text.translatable("pandora.menu.debug.chunk.stuff"), (ButtonWidget var1) -> {
								BlockModelRenderer.disableBrightnessCache();
								BlockModelRenderer.enableBrightnessCache();

								this.client.world.getChunkManager().getLightingProvider()
										.doLightUpdates(Integer.MAX_VALUE, true, true);

							}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
								renderOrderedTooltip(matricies,
										textRenderer.wrapLines(
												Text.translatable("pandora.menu.debug.chunk.stuff.tooltip"),
												buttonWidth * 2),
										mouseX, mouseY);
							}));
		}
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.full.resource.reload"), (ButtonWidget var1) -> {

							this.client.reloadResourcesConcurrently();
							this.client.getBlockRenderManager().reload(this.client.getResourceManager());

						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
							renderOrderedTooltip(matricies,
									textRenderer.wrapLines(
											Text.translatable("pandora.menu.debug.full.resource.reload.tooltip"),
											buttonWidth * 2),
									mouseX, mouseY);
						}));
	}

	private void firstPage(int buttonPosX, int buttonPosY, int buttonWidthPadding, int buttonHeightPadding,
			int buttonWidth, int buttonHeight, int usable_screen_width_offset, int usable_screen_height_offset) {

		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.initialize"), (ButtonWidget var1) -> {
							new Pandora().onInitialize();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.modify.registries"), (ButtonWidget var1) -> {
							Pandora.modifyRegistries();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));

		buttonPosY++;

		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.register.callbacks"), (ButtonWidget var1) -> {
							Pandora.registerCallbacks();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));

		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.save.config.client"), (ButtonWidget var1) -> {
							ClientConfig.saveClientConfig();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.save.config.common"), (ButtonWidget var1) -> {
							CommonConfig.saveCommonConfig();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.save.config.debug"), (ButtonWidget var1) -> {
							DebugConfig.saveDebugConfig();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.save.config.all"), (ButtonWidget var1) -> {
							ConfigUtils.saveConfigs();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));

		buttonPosX++;
		buttonPosY = 1;

		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.load.config.client"), (ButtonWidget var1) -> {
							ClientConfig.loadClientConfig();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.load.config.common"), (ButtonWidget var1) -> {
							CommonConfig.loadCommonConfig();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.load.config.debug"), (ButtonWidget var1) -> {
							DebugConfig.loadDebugConfig();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.load.config.all"), (ButtonWidget var1) -> {
							ConfigUtils.loadConfigs();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));

		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.delete.config.client"), (ButtonWidget var1) -> {
							ConfigUtils.getConfigFile("pandora.client.yaml").delete();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.delete.config.common"), (ButtonWidget var1) -> {
							ConfigUtils.getConfigFile("pandora.common.yaml").delete();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.delete.config.debug"), (ButtonWidget var1) -> {
							ConfigUtils.getConfigFile("pandora.debug.properties").delete();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.delete.config.all"), (ButtonWidget var1) -> {
							ConfigUtils.getConfigFile("pandora.client.yaml").delete();
							ConfigUtils.getConfigFile("pandora.common.yaml").delete();
							ConfigUtils.getConfigFile("pandora.debug.properties").delete();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));

		buttonPosX++;
		buttonPosY = 1;

		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.unpack.config.client"), (ButtonWidget var1) -> {
							ConfigUtils.unpackageFile("pandora.client.yaml");
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.unpack.config.common"), (ButtonWidget var1) -> {
							ConfigUtils.unpackageFile("pandora.common.yaml");
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.unpack.config.debug"), (ButtonWidget var1) -> {
							ConfigUtils.unpackageFile("pandora.debug.properties");
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.unpack.config.all"), (ButtonWidget var1) -> {
							ConfigUtils.unpackageFile("pandora.client.yaml");
							ConfigUtils.unpackageFile("pandora.common.yaml");
							ConfigUtils.unpackageFile("pandora.debug.properties");
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));

//		var iter = buttons.iterator();
//
//		for (var i = 0; i != usable_screen_width / widthMultiple; i++) {
//			for (var j = 0; j != usable_screen_height / heightMultiple; j++) {
//				if (!iter.hasNext()) {
//					break;
//				}
//
//				var button = iter.next();
//
//				if (button != null) {
//					var thing = usable_screen_width_offset + (widthMultiple * i);
//					var thing2 = usable_screen_height_offset + (heightMultiple * j);
//					
//					if (button.getLeft().getString().contentEquals("Return")) {
////						log.info("{} {} {} {}", usable_screen_width_offset, widthMultiple, i, widthMultiple * i);
////						log.info("{} {} {} {}", usable_screen_height_offset, heightMultiple, j, heightMultiple * j);
//						
//						log.info("X{} Y{} W{} H{}",thing + buttonWidthPadding, thing2 + buttonHeightPadding,
//							widthMultiple - (buttonWidthPadding * 2), heightMultiple - (buttonHeightPadding * 2));
//					}
//					
//					this.addDrawableChild(new ButtonWidget(thing + buttonWidthPadding, thing2 + buttonHeightPadding,
//							widthMultiple - (buttonWidthPadding * 2), heightMultiple - (buttonHeightPadding * 2),
//							button.getLeft(), button.getMiddle(), button.getRight()));
//				}
////				currentButtonCount++;
//			}
//		}
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {

		if (enablePageBackground || this.client.world == null) {
			float vOffset = 0f; // vertical UV positioning, use 1048569f to find the edge
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferBuilder = tessellator.getBuffer();
			RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
			RenderSystem.setShaderTexture(0, new Identifier("minecraft", "textures/block/deepslate.png"));
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
			bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
			bufferBuilder.vertex(0.0D, (double) this.height, 0.0D).texture(0.0F, (float) this.height / 32.0F + vOffset)
					.color(64, 64, 64, 255).next();
			bufferBuilder.vertex((double) this.width, (double) this.height, 0.0D)
					.texture((float) this.width / 32.0F, (float) this.height / 32.0F + vOffset).color(64, 64, 64, 255)
					.next();
			bufferBuilder.vertex((double) this.width, 0.0D, 0.0D).texture((float) this.width / 32.0F, vOffset)
					.color(64, 64, 64, 255).next();
			bufferBuilder.vertex(0.0D, 0.0D, 0.0D).texture(0.0F, vOffset).color(64, 64, 64, 255).next();
			tessellator.draw();

			this.fillGradient(matrices, 0, 0, this.width, this.height, 0x00_1a_1a_1a, 0xff_00_00_00);
			this.fillGradient(matrices, 0, 0, this.width, this.height, 0x1f_ff_ff_ff, 0x00_00_00_00);
		}
		drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, this.height / 128, 0x00_ff_ff_ff);

		super.render(matrices, mouseX, mouseY, delta);
	}

}
