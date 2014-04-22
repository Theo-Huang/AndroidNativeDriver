package com.google.android.testing.nativedriver.server;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.io.TemporaryFilesystem;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.server.DefaultDriverFactory;
import org.openqa.selenium.remote.server.DefaultSession;
import org.openqa.selenium.remote.server.DriverFactory;
import org.openqa.selenium.remote.server.DriverSessions;
import org.openqa.selenium.remote.server.Session;
import android.os.Environment;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * To implements DriverSessions base on DefaultDriverSessions
 * 
 * @author Theo Huang
 */
public class AndroidNativeDriverSessions implements DriverSessions {
  private final DriverFactory factory;

  private final Map<SessionId, Session> sessionIdToDriver =
      new ConcurrentHashMap<SessionId, Session>();

  @SuppressWarnings("serial")
private static Map<Capabilities, String> defaultDrivers = new HashMap<Capabilities, String>() {
	{
		put(DesiredCapabilities.chrome(), "org.openqa.selenium.chrome.ChromeDriver");
	    put(DesiredCapabilities.firefox(), "org.openqa.selenium.firefox.FirefoxDriver");
	    put(DesiredCapabilities.htmlUnit(), "org.openqa.selenium.htmlunit.HtmlUnitDriver");
	    put(DesiredCapabilities.internetExplorer(), "org.openqa.selenium.ie.InternetExplorerDriver");
	    put(DesiredCapabilities.opera(), "com.opera.core.systems.OperaDriver");
	    put(DesiredCapabilities.safari(), "org.openqa.selenium.safari.SafariDriver");
	    put(DesiredCapabilities.iphone(), "org.openqa.selenium.iphone.IPhoneDriver");
	    put(DesiredCapabilities.ipad(), "org.openqa.selenium.iphone.IPhoneDriver");
	    put(DesiredCapabilities.phantomjs(), "org.openqa.selenium.phantomjs.PhantomJSDriver");
  }};

  public AndroidNativeDriverSessions() {
    this(Platform.getCurrent(), new DefaultDriverFactory());
  }

  protected AndroidNativeDriverSessions(Platform runningOn, DriverFactory factory) {
    this.factory = factory;
    registerDefaults(runningOn);
  }

  private void registerDefaults(Platform current) {
    if (current.equals(Platform.ANDROID)) {
      // AndroidDriver is here for backward-compatibility reasons, it should be removed at some point
      registerDriver(DesiredCapabilities.android(), "org.openqa.selenium.android.AndroidDriver");
      
      //Don't know why need to regist two.
      //Keep observing what will happen
//      registerDriver(DesiredCapabilities.android(), "org.openqa.selenium.android.AndroidApkDriver");
      
      return;
    }
    for (Map.Entry<Capabilities, String> entry : defaultDrivers.entrySet()) {
      Capabilities caps = entry.getKey();
      if (caps.getPlatform() != null && caps.getPlatform().is(current)) {
        registerDriver(caps, entry.getValue());
      } else if (caps.getPlatform() == null) {
        registerDriver(caps, entry.getValue());
      }
    }
  }

  private void registerDriver(Capabilities caps, String className) {
    try {
      registerDriver(caps, Class.forName(className).asSubclass(WebDriver.class));
    } catch (ClassNotFoundException e) {
      // OK. Fall through. We just won't be able to create these
    } catch (NoClassDefFoundError e) {
      // OK. Missing a dependency, which is obviously a Bad Thing
      // TODO(simon): Log this!
    }
  }

  public SessionId newSession(Capabilities desiredCapabilities) throws Exception {
	File tempFile = new File(Environment.getExternalStorageDirectory() + "/androidAutomationTemp");
	TemporaryFilesystem tempFs = null;
	try{
		if(!tempFile.mkdirs()){
			throw new WebDriverException("Cannot create temp directory: " + tempFile);
		}
		tempFs = TemporaryFilesystem.getTmpFsBasedOn(tempFile);
	}catch(Exception e){
		System.err.println("May not support web view. Make sure your SD card can read & write.");
		e.printStackTrace();
	}
    SessionId sessionId = new SessionId(UUID.randomUUID().toString());
    Session session = DefaultSession.createSession(factory,tempFs,sessionId,desiredCapabilities);
    sessionIdToDriver.put(sessionId, session);
    return sessionId;
  }

  public Session get(SessionId sessionId) {
    return sessionIdToDriver.get(sessionId);
  }

  public void deleteSession(SessionId sessionId) {
    final Session removedSession = sessionIdToDriver.remove(sessionId);
    if (removedSession != null) {
      removedSession.close();
    }
  }

  public void registerDriver(Capabilities capabilities, Class<? extends WebDriver> implementation) {
    factory.registerDriver(capabilities, implementation);
  }

  public Set<SessionId> getSessions() {
    return Collections.unmodifiableSet(sessionIdToDriver.keySet());
  }
}

