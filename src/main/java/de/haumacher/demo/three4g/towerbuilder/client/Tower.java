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
		return get(topLevel());
	}

	/** 
	 * The top-most level of this tower.
	 */
	private int topLevel() {
		return height() - 1;
	}

	/** 
	 * The {@link Platform} at the given level. 
	 * 
	 * <p>
	 * Level {@link #height()} - 1 is the top-level {@link Platform}.
	 * </p>
	 */
	public Platform get(int level) {
		return _platforms.get(level);
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
		return edgeLevel(side, topLevel());
	}

	/** 
	 * The next level this tower is taler that the given level's platform on the given side.
	 */
	public int edgeLevel(Side side, int level) {
		float startPos = get(level).position(side);
		
		for (int result = level - 1; result >= 0; result--) {
			if (get(result).position(side) != startPos) {
				return result;
			}
		}
		
		return -1;
	}

}
