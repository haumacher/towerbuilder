package de.haumacher.demo.three4g.towerbuilder.client;

/**
 * One slice of the Tower being built.
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public class Platform {

	private float _x1;

	private float _x2;

	private float _y1;

	private float _y2;

	/**
	 * Creates a {@link Platform}.
	 */
	public Platform(float cubeX1, float cubeX2, float cubeY1, float cubeY2) {
		_x1 = cubeX1;
		_x2 = cubeX2;
		_y1 = cubeY1;
		_y2 = cubeY2;
	}

	/**
	 * The X coordinate of the {@value Side#LEFT} side.
	 */
	public float getX1() {
		return _x1;
	}

	/**
	 * The X coordinate of the {@value Side#RIGHT} side.
	 */
	public float getX2() {
		return _x2;
	}

	/**
	 * The Y coordinate of the {@value Side#FRONT} side.
	 */
	public float getY1() {
		return _y1;
	}

	/**
	 * The Y coordinate of the {@link Side#BACK} side.
	 */
	public float getY2() {
		return _y2;
	}

	/**
	 * The width of the {@link Platform} in X direction.
	 */
	public float width() {
		return _x2 - _x1;
	}

	/**
	 * The dimension of the {@link Platform} in Y direction.
	 */
	public float height() {
		return _y2 - _y1;
	}

	/**
	 * Computes the part of the given block that stays on top of this
	 * {@link Platform}.
	 */
	public Platform intersect(Platform block) {
		float matchX1 = Math.max(_x1, block.getX1());
		float matchX2 = Math.min(_x2, block.getX2());
		float matchY1 = Math.max(_y1, block.getY1());
		float matchY2 = Math.min(_y2, block.getY2());

		return new Platform(matchX1, matchX2, matchY1, matchY2);
	}

	/**
	 * The center in X direction.
	 */
	public float centerX() {
		return (_x1 + _x2) / 2;
	}

	/**
	 * The center in Y direction.
	 */
	public float centerY() {
		return (_y1 + _y2) / 2;
	}

	/**
	 * Creates a copy of this {@link Platform}.
	 */
	public Platform copy() {
		return new Platform(_x1, _x2, _y1, _y2);
	}

	/**
	 * The border coordinate at the given {@link Side}.
	 */
	public float position(Side side) {
		switch (side) {
		case FRONT:
			return _y1;
		case BACK:
			return _y2;
		case LEFT:
			return _x1;
		case RIGHT:
			return _x2;
		default:
			throw new IllegalArgumentException("No such side: " + side);
		}
	}

	/**
	 * Updates the border coordinate at the given {@link Side}.
	 * 
	 * @param side
	 *        The {@link Side} to update.
	 * @param position
	 *        The new border coordinate value.
	 * @return The given value.
	 */
	public float setPosition(Side side, float position) {
		switch (side) {
		case FRONT:
			return _y1 = position;
		case BACK:
			return _y2 = position;
		case LEFT:
			return _x1 = position;
		case RIGHT:
			return _x2 = position;
		default:
			throw new IllegalArgumentException("No such side: " + side);
		}
	}

	/**
	 * Increments the border coordinate on the given {@link Side} by the given delta.
	 */
	public void incPosition(Side side, float delta) {
		setPosition(side, position(side) + delta);
	}
}
