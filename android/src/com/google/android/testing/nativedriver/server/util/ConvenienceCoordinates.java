package com.google.android.testing.nativedriver.server.util;

import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.internal.Coordinates;

public class ConvenienceCoordinates implements Coordinates {
	public int x;
	public int y;

	public ConvenienceCoordinates(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public Object getAuxiliary() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Point inViewPort() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Point onPage() {
		return new Point(x, y);
	}

	@Override
	public Point onScreen() {
		return new Point(x, y);
	}
}
