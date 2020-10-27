package de.haumacher.demo.three4g.towerbuilder.client;

/**
 * The side of the {@link Tower}.
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public enum Side {
	
	BACK, FRONT, RIGHT, LEFT;

	/** 
	 * Whether adding the given value to a {@link Platform#position(Side)} makes the tower wider.
	 */
	boolean pointsToOutside(float length) {
		switch (this) {
		case RIGHT:
		case BACK:
			return length > 0;

		default:
			return length < 0;
		}
	}

}
