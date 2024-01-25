package pkg.deepCurse.pandora.core.util.interfaces;

@FunctionalInterface
public interface ConditionalToIntFunction<T> {
	int applyAsInt(T value, int oldValue);
}
