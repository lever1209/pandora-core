package pkg.deepCurse.pandora.core.util.managers;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

// stolen from mojang with <3
public class EntityCooldownManager {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(EntityCooldownManager.class);

	private final Map<Entity, Entry> entries = Maps.newHashMap();
	private long tick;

	public boolean isCoolingDown(Entity entity) {
		return this.getCooldownProgress(entity, 0.0f) > 0.0f;
	}

	public float getCooldownProgress(Entity entity, float f) {
		Entry entry = this.entries.get(entity);
		if (entry != null) {
			float g = entry.endTick - entry.startTick;
			float h = (float) entry.endTick - ((float) this.tick + f);
			return MathHelper.clamp(h / g, 0.0f, 1.0f);
		}
		return 0.0f;
	}

	public void update() {
		++this.tick;
		if (!this.entries.isEmpty()) {
			Iterator<Map.Entry<Entity, Entry>> iterator = this.entries.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Entity, Entry> entry = iterator.next();
//				log.info("entry: {}", entry.getKey());
				if (entry.getValue().endTick > this.tick) {
					continue;
				}
				iterator.remove();
				this.remove(entry.getKey());
				this.onCooldownUpdate(entry.getKey());
			}
		}
	}

	public void set(Entity entity, long i) {
		this.entries.putIfAbsent(entity, new Entry(this.tick, this.tick + i));
		this.onCooldownUpdate(entity, i);
	}

	public void remove(Entity entity) {
		this.entries.remove(entity);
		this.onCooldownUpdate(entity);
	}

	protected void onCooldownUpdate(Entity entity, long i) {
	}

	protected void onCooldownUpdate(Entity entity) {
	}

	private static class Entry {
		final long startTick;
		final long endTick;

		Entry(long i, long j) {
			this.startTick = i;
			this.endTick = j;
		}
	}
}
