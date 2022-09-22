package pkg.deepCurse.pandora.core.util.screens;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.lambdaurora.spruceui.screen.SpruceScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class SpruceUIScreen extends SpruceScreen {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(SpruceUIScreen.class);

	@SuppressWarnings("unused")
	private Screen parent;

	public SpruceUIScreen(Screen parent) {
		super(Text.translatable("E"));
		this.parent = parent;
	}

	// private SpruceOptionListWidget list;

	// @Override
	// protected void init() {
	// super.init();

	// // Button list.
	// //this.list = new ButtonListWidget(this.client, this.width, this.height, 43,
	// this.height - 29 - this.getTextHeight(), 25);
	// this.list = SpruceUITest.get().buildOptionList(Position.of(0, 22),
	// this.width, this.height - 35 - 22);
	// SpruceUITest.get().resetConsumer = btn -> {
	// // Re-initialize the screen to update all the values.
	// this.init(this.client, this.client.getWindow().getScaledWidth(),
	// this.client.getWindow().getScaledHeight());
	// };

	// this.addChild(this.list);

	// // Add reset button. You can add option buttons outside a button list widget.
	// GameOptions instance is required because of Vanilla limitations.
	// //this.addButton(this.resetOption.createButton(this.client.options,
	// this.width / 2 - 155, this.height - 29, 150));
	// // Add done button.
	// this.addButton(new SpruceButtonWidget(Position.of(this, this.width / 2 - 155
	// + 160, this.height - 29), 150, 20, SpruceTexts.GUI_DONE,
	// btn -> this.client.openScreen(this.parent)).asVanilla());
	// }

	// @Override
	// public void renderTitle(MatrixStack matrices, int mouseX, int mouseY, float
	// delta) {
	// drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8,
	// 16777215);
	// }

}