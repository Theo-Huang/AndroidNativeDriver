package com.google.android.testing.nativedriver.client;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;

public class ScreenShotTaker {
	public enum RotateDegree {
		Degree_0(0), Degree_90(3), Degree_180(2), Degree_270(1);

		private int num;

		RotateDegree(int num) {
			this.num = num;
		}

		public int getRotateNum() {
			return num;
		}
	}

	private final IDevice device;

	public ScreenShotTaker(IDevice device) {
		this.device = device;
	}

	public BufferedImage getScreenShot() {
		return getScreenShot(false, RotateDegree.Degree_0);
	}

	public BufferedImage getScreenShot(boolean landscape, RotateDegree degree) {
		RawImage rawImage;
		try {
			rawImage = device.getScreenshot();
		} catch (IOException ioe) {
			throw new AdbException("IO error when get Device Image.");
		}
		if (rawImage == null) {
			throw new AdbException("get Device Image error...");
		}
		if (landscape) {
			for (int i = 0, n = degree.getRotateNum(); i < n; i++) {
				rawImage = rawImage.getRotated();
			}
		}
		return rawToBufferedImage(rawImage);
	}

	private BufferedImage rawToBufferedImage(RawImage rawImage) {
		BufferedImage screenImage = new BufferedImage(rawImage.width, rawImage.height,
				BufferedImage.TYPE_INT_ARGB);
		int index = 0;
		int IndexInc = rawImage.bpp >> 3;
		for (int y = 0; y < rawImage.height; y++) {
			for (int x = 0; x < rawImage.width; x++) {
				int value = rawImage.getARGB(index);
				index += IndexInc;
				screenImage.setRGB(x, y, value);
			}
		}
		return screenImage;
	}
}
