package pkg.deepCurse.pandora.common.util;

import java.util.HashMap;

import pkg.deepCurse.pandora.common.CommonTools;

public class CooldownTracker<T> {

	private final HashMap<T, Entry> entries = new HashMap<>();

	public boolean isCoolingDown(T trackerObject) {
		return this.entries.containsKey(trackerObject);
	}

	// TODO use this for a screen effect when winding down to 0
	public float getCooldownProgress(T trackerObject, float f) {
		Entry entry = this.entries.get(trackerObject);
		if (entry == null) {
			return 0.0f;
		}

		return CommonTools.clamp(CommonTools.iLerp(entry.startTick, entry.endTick, entry.currentTick + f), 0.0f, 1.0f);
	}

	public void update(T trackerObject) {
		var entry = this.entries.get(trackerObject);
		if (entry != null && entry.endTick < ++entry.currentTick) {
			this.remove(trackerObject);
		}
	}

	public void set(T trackerObject, int i) {
		this.entries.putIfAbsent(trackerObject, new Entry(i));
	}

	public void remove(T trackerObject) {
		this.entries.remove(trackerObject);
	}

	private class Entry {
		private final long startTick = 0;
		private final long endTick;
		private long currentTick = 0;

		private Entry(int j) {
			this.endTick = j;
		}
	}
}
