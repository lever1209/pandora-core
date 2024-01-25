package pkg.deepCurse.pandora.core.util.screens;

import org.slf4j.*;

import com.mojang.blaze3d.systems.*;

import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.*;
import net.minecraft.client.util.math.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import pkg.deepCurse.pandora.core.*;

public class DebugScreen extends Screen {

	public static double factor;
	private Screen parent;

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(DebugScreen.class);

	public DebugScreen(Screen parent) {
		super(Text.translatable("pandora.menu.debug.title"));
		this.parent = parent;
	}

	@Override
	public void init() {
		this.addDrawableChild(
				new ButtonWidget(0, 0, 49, 10, Text.translatable("pandora.menu.return"), (buttonWidget) -> {
					this.client.setScreen(this.parent);
				}));
		this.addDrawableChild(
				new ButtonWidget(0, 10, 79, 10, Text.translatable("pandora.menu.debug.save.config"), (buttonWidget) -> {
					PandoraConfig.saveConfigs();
				}));
		this.addDrawableChild(new ButtonWidget(0, 20, 59, 10, Text.translatable("pandora.menu.debug.reload.config"),
				(buttonWidget) -> {
					try {
						PandoraConfig.loadConfig();
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
				}));
		this.addDrawableChild(new ButtonWidget(60, 20, 59, 10, Text.translatable("pandora.menu.debug.register.hooks"),
				(buttonWidget) -> {
					try {
						Pandora.registerHooks();
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
				}));

		// this.addDrawableChild(new ButtonWidget(60, 20, 59, 10,
		// Text.translatable("pandora.menu.debug.new.config"),
		// (buttonWidget) -> {
		// PandoraConfig.newConfig();
		// })); LEGACY unset method
		this.addDrawableChild(new ButtonWidget(0, 30, 69, 10, Text.translatable("pandora.menu.debug.delete.config"),
				(buttonWidget) -> {
					PandoraConfig.deleteConfig();
				}));
		this.addDrawableChild(new ButtonWidget(0, 40, 79, 10, Text.translatable("pandora.menu.debug.unpack.config"),
				(buttonWidget) -> {
					try {
						PandoraConfig.unpackageConfig();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}));
		this.addDrawableChild(
				new ButtonWidget(101, 100, 20, 10, Text.translatable("print modified block lights"), (buttonWidget) -> {
					log.info("{}\n{}", PandoraConfig.General.BlockLightLevelSettings.entrySet(),
							PandoraConfig.General.BlockLightLevelSettings.values());
				}));
		this.addDrawableChild(new SliderWidget(0, 80, 100, 20, Text.literal("fog factor: " + factor), 1.0D) {

			@Override
			protected void updateMessage() {
				super.setMessage(Text.literal("fog factor: " + this.value));
			}

			@Override
			protected void applyValue() {
				DebugScreen.factor = this.value;
			}
		});
		// this.addDrawableChild(new )
		// this.addDrawableChild(new ButtonWidget(0, 50, 79, 10,
		// Text.translatable("pandora.menu.debug.new.config.instance"),
		// (buttonWidget) -> {
		// try {
		// PandoraConfig.newConfig();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }));
		// this.addDrawableChild(new ButtonWidget(0, 60, 79, 10,
		// Text.translatable("pandora.menu.debug.new.config.instance"),
		// (buttonWidget) -> {
		// try {
		// Pandora.LOGGER
		// .info(PandoraConfig.getSimpleDimensions());
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }));
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		MatrixStack mats = new MatrixStack();
		int vOffset = 0;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, new Identifier("minecraft", "textures/block/deepslate.png"));
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		bufferBuilder.vertex(0.0D, (double) this.height, 0.0D)
				.texture(0.0F, (float) this.height / 32.0F + (float) vOffset).color(64, 64, 64, 255).next();
		bufferBuilder.vertex((double) this.width, (double) this.height, 0.0D)
				.texture((float) this.width / 32.0F, (float) this.height / 32.0F + (float) vOffset)
				.color(64, 64, 64, 255).next();
		bufferBuilder.vertex((double) this.width, 0.0D, 0.0D).texture((float) this.width / 32.0F, (float) vOffset)
				.color(64, 64, 64, 255).next();
		bufferBuilder.vertex(0.0D, 0.0D, 0.0D).texture(0.0F, (float) vOffset).color(64, 64, 64, 255).next();
		tessellator.draw();
		drawCenteredText(mats, this.textRenderer, this.title, this.width / 2, 15, 16777215);
		super.render(mats, mouseX, mouseY, delta);
	}

}
