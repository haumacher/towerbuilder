/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package de.haumacher.demo.three4g.towerbuilder.client;

/**
 * TODO
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum Direction {
	TO_FRONT, TO_RIGHT;
	
	Direction next() {
		return Direction.values()[(ordinal() + 1) % 2];
	}
}
