package de.haumacher.demo.three4g.towerbuilder.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Model of the tower being built.
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public class Tower {
	
	private final List<Platform> _platforms = new ArrayList<>();

	/** 
	 * Adds a new {@link Platform} on the top of the tower.
	 */
	public void add(Platform platform) {
		_platforms.add(platform);
	}

	/** 
	 * The top-level {@link Platform}.
	 */
	public Platform top() {
		return get(0);
	}

	/** 
	 * The {@link Platform} at the given level. 
	 * 
	 * <p>
	 * Level zero is the top-level {@link Platform}.
	 * </p>
	 */
	public Platform get(int level) {
		return _platforms.get(_platforms.size() - 1 - level);
	}
	
	/**
	 * The number of {@link Platform}s this tower is high.
	 */
	public int height() {
		return _platforms.size();
	}
	
	/** 
	 * The first level this tower is taler that the top-level platform on the given side.
	 */
	public int edgeLevel(Side side) {
		int level = 0;
		float startPos = get(level).position(side);
		while (++level < height()) {
			if (get(level).position(side) != startPos) {
				return level;
			}
		}
		return -1;
	}

}
