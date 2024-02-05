package pkg.deepCurse.pandora.client.gui.screens;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.systems.RenderSystem;

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
import net.minecraft.util.registry.Registry;
import pkg.deepCurse.pandora.common.Pandora;
import pkg.deepCurse.pandora.common.PandoraConfig;
import pkg.deepCurse.pandora.common.PandoraConfig.Client;
import pkg.deepCurse.pandora.common.PandoraConfig.Server;

public class DebugScreen extends Screen {

	private Screen parent;
	private short pageNum = 1;
	private boolean enablePageBackground = true;
	private Text pageName = Text.translatable("pandora.menu.debug.page" + pageNum);

	private static Logger log = LoggerFactory.getLogger(DebugScreen.class);

	public DebugScreen(Screen parent) {
		super(Text.translatable("pandora.menu.debug.title"));
		this.parent = parent;
	}

	@Override
	public void init() {

//		var currentButtonCount = 0;

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

		var centerOffsetHorizontalPosition = center_pos_x - (buttonWidth / 2) * columnCount;
		var centerOffsetVerticalPosition = center_pos_y - (buttonHeight / 2) * rowCount;

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
				(((buttonPosX * buttonWidth) - buttonWidthPadding) + centerOffsetHorizontalPosition),
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
			nextButton.active = false;
			enablePageBackground = false;
			secondPage(buttonPosX, buttonPosY, buttonWidthPadding, buttonHeightPadding, buttonWidth, buttonHeight,
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

		buttonPosX = 1;
		buttonPosY = 9;

		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + centerOffsetHorizontalPosition,
						Math.min(buttonPosY++ * buttonHeight - buttonHeightPadding + centerOffsetVerticalPosition,
								height - buttonHeight),
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.return"), (ButtonWidget var1) -> {
							this.client.setScreen(this.parent);
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));

	}

	private void secondPage(int buttonPosX, int buttonPosY, int buttonWidthPadding, int buttonHeightPadding,
			int buttonWidth, int buttonHeight, int usable_screen_width_offset, int usable_screen_height_offset) {

		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.print.modified.block.lights"), (ButtonWidget var1) -> {
							for (var i : Server.SERVER.BlockLightLevelSettings.entrySet()) {
								for (var entry : Registry.BLOCK.get(i.getKey()).getStateManager().getStates()) {
									log.info("[PandoraDebug] {}={}", NbtHelper.fromBlockState(entry).toString(),
											i.getValue().LightLevel.applyAsInt(entry, entry.getLuminance()));
								}
							}
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));

		var world = this.client.world;
		if (world != null) {
			var fogLevel = Client.CLIENT.DimensionSettings.get(world.getRegistryKey().getValue()).fogLevel;
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
							Client.CLIENT.DimensionSettings.get(world.getRegistryKey().getValue()).fogLevel = Float
									.parseFloat(String.valueOf(this.value));
						}
					});

			this.addDrawableChild(
					new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
							buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
							buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
							Text.translatable("pandora.menu.debug.fix.chunk.lighting"), (ButtonWidget var1) -> {
								this.client.reloadResourcesConcurrently();
								this.client.getBlockRenderManager().reload(this.client.getResourceManager());
								BlockModelRenderer.disableBrightnessCache();
								BlockModelRenderer.enableBrightnessCache();

//						this.client.getModStatus();

								this.client.world.getChunkManager().getLightingProvider()
										.doLightUpdates(Integer.MAX_VALUE, true, true);

							}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
								renderOrderedTooltip(matricies,
										textRenderer.wrapLines(
												Text.translatable("pandora.menu.debug.fix.chunk.lighting.tooltip"),
												buttonWidth * 2),
										mouseX, mouseY);
							}));

			this.addDrawableChild(
					new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
							buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
							buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
							Text.translatable("pandora.menu.debug.print.dimension.settings"), (ButtonWidget var1) -> {
								var settings = Server.SERVER.DimensionSettings.get(world.getRegistryKey().getValue());
								log.info(settings.toString());
							}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
							}));
		}

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
							PandoraConfig.saveClientConfig();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.save.config.server"), (ButtonWidget var1) -> {
							PandoraConfig.saveServerConfig();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.save.config.debug"), (ButtonWidget var1) -> {
							PandoraConfig.saveDebugConfig();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.save.config.all"), (ButtonWidget var1) -> {
							PandoraConfig.saveConfigs();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));

		buttonPosX++;
		buttonPosY = 1;

		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.load.config.client"), (ButtonWidget var1) -> {
							PandoraConfig.loadClientConfig();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.load.config.server"), (ButtonWidget var1) -> {
							PandoraConfig.loadServerConfig();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.load.config.debug"), (ButtonWidget var1) -> {
							PandoraConfig.loadDebugConfig();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.load.config.all"), (ButtonWidget var1) -> {
							PandoraConfig.loadConfigs();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));

		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.delete.config.client"), (ButtonWidget var1) -> {
							PandoraConfig.getConfigFile("pandora.client.yaml").delete();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.delete.config.server"), (ButtonWidget var1) -> {
							PandoraConfig.getConfigFile("pandora.server.yaml").delete();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.delete.config.debug"), (ButtonWidget var1) -> {
							PandoraConfig.getConfigFile("pandora.debug.properties").delete();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.delete.config.all"), (ButtonWidget var1) -> {
							PandoraConfig.getConfigFile("pandora.client.yaml").delete();
							PandoraConfig.getConfigFile("pandora.server.yaml").delete();
							PandoraConfig.getConfigFile("pandora.debug.properties").delete();
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));

		buttonPosX++;
		buttonPosY = 1;

		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.unpack.config.client"), (ButtonWidget var1) -> {
							PandoraConfig.unpackageFile("pandora.client.yaml");
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.unpack.config.server"), (ButtonWidget var1) -> {
							PandoraConfig.unpackageFile("pandora.server.yaml");
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.unpack.config.debug"), (ButtonWidget var1) -> {
							PandoraConfig.unpackageFile("pandora.debug.properties");
						}, (ButtonWidget button, MatrixStack matricies, int mouseX, int mouseY) -> {
						}));
		this.addDrawableChild(
				new ButtonWidget(buttonPosX * buttonWidth - buttonWidthPadding + usable_screen_width_offset,
						buttonPosY++ * buttonHeight - buttonHeightPadding + usable_screen_height_offset,
						buttonWidth - (buttonWidthPadding * 2), buttonHeight - (buttonHeightPadding * 2),
						Text.translatable("pandora.menu.debug.unpack.config.all"), (ButtonWidget var1) -> {
							PandoraConfig.unpackageFile("pandora.client.yaml");
							PandoraConfig.unpackageFile("pandora.server.yaml");
							PandoraConfig.unpackageFile("pandora.debug.properties");
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
		drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, this.height / 64, 0x00_ff_ff_ff);

		super.render(matrices, mouseX, mouseY, delta);
	}

}
