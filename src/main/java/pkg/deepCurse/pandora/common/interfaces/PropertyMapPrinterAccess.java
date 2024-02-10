package pkg.deepCurse.pandora.common.interfaces;

import java.util.Map;
import java.util.function.Function;

import net.minecraft.state.property.Property;

public interface PropertyMapPrinterAccess {
	Function<Map.Entry<Property<?>, Comparable<?>>, String> pandora_getPropertyMapPrinter();
}
