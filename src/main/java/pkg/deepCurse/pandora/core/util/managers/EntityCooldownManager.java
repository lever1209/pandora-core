package pkg.deepCurse.pandora.core.util.managers;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class EntityCooldownManager {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(EntityCooldownManager.class);

	private final Map<Entity, Entry> entries = Maps.newHashMap();

	public boolean isCoolingDown(Entity entity) {
		return this.getCooldownProgress(entity, 0.0f) > 0.0f;
	}

	public float getCooldownProgress(Entity entity, float f) {
		Entry entry = this.entries.get(entity);
		if (entry != null) {
			float g = entry.endTick - entry.startTick;
			float h = (float) entry.endTick - ((float) entry.currentTick + f);
			return MathHelper.clamp(h / g, 0.0f, 1.0f);
		}
		return 0.0f;
	}

	public void update(Entity entity) {
		var entry = this.entries.get(entity);

		if (entry != null && entry.endTick < ++entry.currentTick) {
			this.entries.remove(entity);
			this.onCooldownUpdate(entity);
		}

	}

	public void set(Entity entity, int i) {
		this.entries.putIfAbsent(entity, new Entry(i));
		this.onCooldownUpdate(entity);
	}

	public void remove(Entity entity) {
		this.entries.remove(entity);
		this.onCooldownUpdate(entity);
	}

	protected void onCooldownUpdate(Entity entity, int i) {
	}

	protected void onCooldownUpdate(Entity entity) {
	}

	private static class Entry {
		private final int startTick = 0;
		private final int endTick;
		public int currentTick;

		public Entry(int j) {
//			this.startTick = i;
			this.endTick = j;
		}
	}
}
