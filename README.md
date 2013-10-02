AndroidNativeDriver By Theo
============================

## Introduce

This project is automation tool for android testing.
Original project please reference : https://code.google.com/p/nativedriver/
It was plan to supprot three platform at the same time.
But it stopped on 2012/03/07.
See: https://groups.google.com/forum/#!searchin/nativedriver-devs/down/nativedriver-devs/WC0GopaDMIo/jjIljf0UOjoJ

Here's project is only for android .
If you try to use something like this on ios, I recommend this project:
https://github.com/ios-driver/ios-driver

It can merge with AndroidNativeDriver together perfectly.

I'm still looking for similar tool on windows.

## Main Improved

* Update to selenium library to 2.35 with jetty 7
* Update android.jar to SDK 17
* Use android ddmlib to capture screenshot
* Support scroll screen,double & long click Element ,drag Element.
* Find all elements in (or not) current activity.

## Usage

* Make app testable:
  https://code.google.com/p/nativedriver/wiki/AndroidMakeAnAppTestable

* Start:
  https://code.google.com/p/nativedriver/wiki/GettingStartedAndroid
  
  ##Sample:
    
    package android;
    import java.io.File;
    import java.util.List;
    import org.openqa.selenium.OutputType;
    import com.google.android.testing.nativedriver.client.AdbConnectionBuilder;
    import com.google.android.testing.nativedriver.client.AndroidNativeDriver;
    import com.google.android.testing.nativedriver.client.AndroidNativeDriverBuilder;
    import com.google.android.testing.nativedriver.client.AndroidNativeElement;
    import com.google.android.testing.nativedriver.client.ScreenShotTaker.RotateDegree;
    import com.google.android.testing.nativedriver.common.AndroidNativeBy;
    public class Sample {
    	public static void main(String[] args) {
    		
    		//Build driver
    		AndroidNativeDriver driver = 
    			new AndroidNativeDriverBuilder().withDefaultServer()
    					.withAdbConnection(
    							new AdbConnectionBuilder()
    								.findAdbFromAndroidSdk("path/to/android/SDK.").build(), 
    								"@Nullable(will find first device) android-device-id")
    						.build();
    		
    		// Get all elements
    		List<AndroidNativeElement> elementList = 
    			driver.findAndroidNativeElements(AndroidNativeBy.all(true/*true=currentActivity,false=all alive.*/));
    		
    		//Scroll screen
    		driver.Scroll(50/*startX*/, 50/*startY*/, 100/*endX*/, 100/*endY*/);
    		
    		//Drag element
    		driver.findElement(AndroidNativeBy.id("elementID")).drag(100/*endX*/, 100/*endY*/);
    		
    		//Double click element
    		driver.findElement(AndroidNativeBy.id("elementID")).doubleClick();
    		
    		//Long click element
    		driver.findElement(AndroidNativeBy.id("elementID")).longClick();
    		
    		//Capture screen shot
    		File screenshot = driver.getScreenshotAs(OutputType.FILE, true, RotateDegree.Degree_90/*0 90 180 270*/);
    		File targetPath = new File("/path/to/save/screenshot.png");
    		screenshot.renameTo(targetPath);
    		
    	}
    
    }

## Advance

* Combine this with RobotFrameWork: https://code.google.com/p/robotframework/
* Demo: http://youtu.be/NH0iZScfLaM

 
## To do

* Simple grid just like selenium grid.
* Support web view.
 

## Others

* Group: https://groups.google.com/forum/?pli=1#!forum/nativedriver-users
