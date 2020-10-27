package de.haumacher.demo.three4g.towerbuilder.client;

/**
 * Direction in which boxes move.
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public enum Direction {
	
	BACK_TO_FRONT, LEFT_TO_RIGHT;
	
	Direction next() {
		return Direction.values()[(ordinal() + 1) % 2];
	}
}
