package pkg.deepCurse.pandora.core.mixins.shared;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import pkg.deepCurse.pandora.core.util.interfaces.PropertyMapPrinterAccess;

@Mixin(State.class)
public class StateMixin implements PropertyMapPrinterAccess {

	@Shadow
	public static Function<Map.Entry<Property<?>, Comparable<?>>, String> PROPERTY_MAP_PRINTER;

	@Override
	public Function<Entry<Property<?>, Comparable<?>>, String> pandora_getPropertyMapPrinter() {
		return PROPERTY_MAP_PRINTER;
	}

}
