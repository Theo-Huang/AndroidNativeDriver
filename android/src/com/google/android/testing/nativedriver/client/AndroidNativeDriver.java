/*
Copyright 2010 NativeDriver committers
Copyright 2010 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.google.android.testing.nativedriver.client;

import com.android.ddmlib.IDevice;
import com.google.android.testing.nativedriver.client.ScreenShotTaker.RotateDegree;
import com.google.android.testing.nativedriver.common.AndroidCapabilities;
import com.google.android.testing.nativedriver.common.AndroidNativeDriverCommand;
import com.google.android.testing.nativedriver.common.FindsAll;
import com.google.android.testing.nativedriver.common.FindsByText;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Rotatable;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.internal.Base64Encoder;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.remote.internal.JsonToWebElementConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;

/**
 * Represents an Android NativeDriver (AND) client used to drive native Android applications.
 * 
 * @author Matt DeVore
 * @author Dezheng Xu
 * @author Tomohiro Kaizu
 * @author Theo Huang
 */
public class AndroidNativeDriver
		extends RemoteWebDriver implements FindsByText, FindsAll, Rotatable, HasInputDevices, TakesScreenshot {
	private class AndroidKeyboard implements Keyboard {
		@Override
		public void sendKeys(CharSequence... keysToSend) {
			execute(AndroidNativeDriverCommand.SEND_KEYS_TO_SESSION,
					ImmutableMap.of("value", keysToSend));
		}

		@Override
		public void pressKey(CharSequence keyToPress) {
			execute(AndroidNativeDriverCommand.SEND_MODIFIER_KEY_TO_SESSION,
							ImmutableMap.of("value", keyToPress, "isdown", true));
		}

		@Override
		public void releaseKey(CharSequence keyToRelease) {
			execute(AndroidNativeDriverCommand.SEND_MODIFIER_KEY_TO_SESSION,
							ImmutableMap.of("value", keyToRelease, "isdown", false));
		}
	}

	@Nullable
	private final AdbConnection adbConnection;
	@Nullable
	private final IDevice device;
	private static final String IMAGE_TYPE = "png";
	private final ScreenShotTaker screenShotTaker;
	private final AndroidKeyboard androidKeyboard = new AndroidKeyboard();

	/**
	 * A {@code Navigation} class for native Android applications. Provides
	 * {@link #toActivity(String)} in addition to the standard {@code Navigation} methods.
	 */
	public class AndroidNativeNavigation implements Navigation {
		private final Navigation navigation;

		private AndroidNativeNavigation(Navigation navigation) {
			this.navigation = navigation;
		}

		public void toActivity(String activityClass) {
			startActivity(activityClass);
		}

		@Override
		public void back() {
			navigation.back();
		}

		@Override
		public void forward() {
			navigation.forward();
		}

		@Override
		public void to(String url) {
			navigation.to(url);
		}

		@Override
		public void to(URL url) {
			navigation.to(url);
		}

		@Override
		public void refresh() {
			navigation.refresh();
		}
	}

	protected AndroidNativeDriver(CommandExecutor executor, String deviceID) {
		this(executor, null, deviceID);
	}

	/**
	 * Creates an instance which routes all commands to a {@code CommandExecutor}. A mock can be
	 * passed as an argument to help with testing. This constructor also takes an
	 * {@code AdbConnection}, to which all ADB commands will be sent.
	 * 
	 * @param executor
	 *          a command executor through which all commands are routed. Using a mock eliminates the
	 *          need to connect to an HTTP server, where the commands are usually routed.
	 * @param adbConnection
	 *          receives all ADB commands, such as event injections. If {@code null}, this instance
	 *          will not support ADB functionality.
	 * @see AndroidNativeDriverBuilder
	 */
	protected AndroidNativeDriver(
			CommandExecutor executor, @Nullable AdbConnection adbConnection, String deviceID) {
		super(Preconditions.checkNotNull(executor), AndroidCapabilities.get());
		setElementConverter(new JsonToWebElementConverter(this) {
			@Override
			protected RemoteWebElement newRemoteWebElement() {
				return new AndroidNativeElement(AndroidNativeDriver.this);
			}
		});
		this.adbConnection = adbConnection;
		try {
			this.device = (deviceID == null || deviceID.isEmpty()) ?
					adbConnection.getAndroidDebugBridge().getDevices()[0] : getAndroidDevice(deviceID);
			this.screenShotTaker = new ScreenShotTaker(device);
		} catch (AdbException ae) {
			throw ae;
		} catch (Exception e) {
			throw new AdbException("Can't find testable android device.");
		}
	}

	private final IDevice getAndroidDevice(String SerialNumber) {
		for (IDevice device : adbConnection.getAndroidDebugBridge().getDevices()) {
			if (device.getSerialNumber().equals(SerialNumber)) {
				return device;
			}
		}
		throw new AdbException("No such device with serial number:" + SerialNumber);
	}

	/**
	 * @deprecated use {@link AndroidNativeDriverBuilder}
	 */
	@Deprecated
	public static AndroidNativeDriver withDefaultServer() {
		return new AndroidNativeDriverBuilder()
				.withDefaultServer()
				.build();
	}

	/**
	 * @deprecated use {@link AndroidNativeDriverBuilder}
	 */
	@Deprecated
	public static AndroidNativeDriver withExecutor(CommandExecutor executor) {
		return new AndroidNativeDriver(executor, null, null);
	}

	/**
	 * @deprecated use {@link AndroidNativeDriverBuilder}
	 */
	@Deprecated
	public static AndroidNativeDriver withServer(URL remoteAddress) {
		return new AndroidNativeDriverBuilder()
				.withServer(remoteAddress)
				.build();
	}

	@Nullable
	public AdbConnection getAdbConnection() {
		return adbConnection;
	}

	@Override
	public Keyboard getKeyboard() {
		return androidKeyboard;
	}

	/**
	 * Start a new activity either in a new task or the current task. This is done by calling
	 * {@code get()} with a coded URL. When not starting in the current task, the activity will start
	 * in the task of the currently-focused activity.
	 * 
	 * @param activityClass
	 *          The full package and class name of the activity.
	 */
	public void startActivity(String activityClass) {
		get("and-activity://" + activityClass);
	}

	@Override
	public WebElement findElementByPartialText(String using) {
		return findElement(USING_PARTIALTEXT, using);
	}

	@Override
	public WebElement findElementByText(String using) {
		return findElement(USING_TEXT, using);
	}

	@Override
	public List<WebElement> findElementsByPartialText(String using) {
		return findElements(USING_PARTIALTEXT, using);
	}

	@Override
	public List<WebElement> findElementsByText(String using) {
		return findElements(USING_TEXT, using);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<AndroidNativeElement> findAndroidNativeElements(By by) {
		return (List) findElements(by);
	}

	@Override
	public AndroidNativeElement findElement(By by) {
		return (AndroidNativeElement) super.findElement(by);
	}

	@Override
	public List<WebElement> findAllElements() {
		return findElements(CURRENT_ACTIVITY, "false");
	}

	@Override
	public List<WebElement> findAllElementsByCurrentActivity() {
		return findElements(CURRENT_ACTIVITY, "true");
	}

	@Override
	public void rotate(ScreenOrientation orientation) {
		// Refers to org.openqa.selenium.android.AndroidDriver
		execute(DriverCommand.SET_SCREEN_ORIENTATION,
				ImmutableMap.of("orientation", orientation));
	}

	@Override
	public ScreenOrientation getOrientation() {
		// Refers to org.openqa.selenium.android.AndroidDriver
		return ScreenOrientation.valueOf(
				(String) execute(DriverCommand.GET_SCREEN_ORIENTATION).getValue());
	}

	@Override
	public AndroidNativeNavigation navigate() {
		return new AndroidNativeNavigation(super.navigate());
	}

	public void Scroll(int xStart, int yStart, int xEnd, int yEnd) {
		execute(DriverCommand.TOUCH_SCROLL,
					ImmutableMap.of(
							"xStart", xStart,
							"yStart", yStart,
							"xEnd", xEnd,
							"yEnd", yEnd));
	}

	@SuppressWarnings("unused")
	private AdbConnection validateAdbConnection() {
		if (adbConnection == null) {
			throw new AdbException("Attempted to use ADB-dependant functionality "
					+ "without an AdbConnection object.");
		}
		return adbConnection;
	}

	/**
	 * @return {@code false} if PNG-writing is not supported, {@code true} otherwise
	 */
	protected boolean writeImageAsPng(
			BufferedImage image, OutputStream destination) throws IOException {
		return ImageIO.write(image, IMAGE_TYPE, destination);
	}

	protected String imageToBase64Png(BufferedImage image) {
		ByteArrayOutputStream rawPngStream = new ByteArrayOutputStream();
		try {
			if (!writeImageAsPng(image, rawPngStream)) {
				throw new RuntimeException(
						"This Java environment does not support converting to PNG.");
			}
		} catch (IOException exception) {
			// This should never happen because rawPngStream is an in-memory stream.
			Throwables.propagate(exception);
		}
		byte[] rawPngBytes = rawPngStream.toByteArray();
		return new Base64Encoder().encode(rawPngBytes);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * Unlike most {@link RemoteWebDriver} operations, this implementation does not send a request to
	 * the remote server. The screenshot is taken by using ADB on the driver client side to query the
	 * device.
	 * 
	 * @throws AdbException
	 *           if an error occurred while driving the device through the {@code adb} tool
	 */
	@Deprecated
	@Override
	public <X> X getScreenshotAs(OutputType<X> target) throws AdbException {
		// AdbConnection adb = validateAdbConnection();
		// FrameBufferFormat format = FrameBufferFormat.ofDevice(adb);
		// BufferedImage screenImage = new BufferedImage(
		// format.getXResolution(), format.getYResolution(),
		// BufferedImage.TYPE_INT_ARGB);
		// Process pullFrameBuffer = adb.pullFile(FrameBufferFormat.FB_DEVICEFILE);
		// InputStream frameBufferStream = pullFrameBuffer.getInputStream();
		// format.copyFrameBufferToImage(frameBufferStream, screenImage);
		// AdbConnection.exhaustProcessOutput(frameBufferStream);
		// // https://code.google.com/p/guava-libraries/issues/detail?id=1118
		// Closeables.closeQuietly(frameBufferStream);
		// //
		// AdbConnection.confirmExitValueIs(0, pullFrameBuffer);
		// String base64Png = imageToBase64Png(screenImage);
		// return target.convertFromBase64Png(base64Png);
		return getScreenshotAs(target, false, RotateDegree.Degree_0);
	}

	public <X> X getScreenshotAs(OutputType<X> target, boolean landscape, RotateDegree degree) throws AdbException {
		String base64Png = imageToBase64Png(screenShotTaker.getScreenShot(landscape, degree));
		return target.convertFromBase64Png(base64Png);
	}

	public String getDeviceAndroidVersion() {
		return device.getProperty("ro.build.version.release");
	}

	public int getDeviceAndroidSDK() {
		return Integer.valueOf(device.getProperty("ro.build.version.sdk"));
	}

	@Override
	public void close() {
		try {
			super.close();
		} catch (UnreachableBrowserException ignored) {
		}
	}

	@Override
	public void quit() {
		try {
			super.quit();
		} catch (UnreachableBrowserException ignored) {
		}
	}

}
