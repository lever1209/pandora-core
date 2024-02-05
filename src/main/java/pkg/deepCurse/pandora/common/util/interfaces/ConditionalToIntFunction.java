package pkg.deepCurse.pandora.common.util.interfaces;

@FunctionalInterface
public interface ConditionalToIntFunction<T> {
	int applyAsInt(T value, int oldValue);
}
