package com.rehivetech.beeeon;
import android.os.RemoteException;
import android.os.RemoteException;
import android.content.ComponentName;
import android.content.Intent;

import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;

import com.android.uiautomator.testrunner.UiAutomatorTestCase;

//import org.testng.Assert;
//import org.testng.annotations.Test;

/**
 * Tests
 * @uthor Martina Kůrová
 */
public class TestMinimize extends UiAutomatorTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //trace("In setup, Launching the application");
        //Runtime.getRuntime().exec("am start -n com.rehivetech.beeeon/com.rehivetech.beeeon.activity.LoginActivity");
        Runtime.getRuntime().exec("am start -n com.rehivetech.beeeon.debug/com.rehivetech.beeeon.activity.LoginActivity");
        sleep(3);
        sleep(3000);
        //testFailed = true;
    }

    @Override
    protected void tearDown() throws Exception {
        getUiDevice().pressHome();
        super.tearDown();
    }

    /**
     * Objects definition
     */
    // Get the device properties
    UiDevice myDevice = getUiDevice();
    // All App Tray Button
    UiObject AppTrayButton = new UiObject(new UiSelector().description("Apps"));

    // Get AppTray container
    UiScrollable appView = new UiScrollable(new UiSelector().className(
            "android.view.View").scrollable(true));
    // Apps Tab
    UiObject AppsTab = new UiObject(new UiSelector().className(
            "android.widget.TextView").description("Apps"));
    // Verify the launched application by it's Package name
    UiObject beeeonValidation = new UiObject(new UiSelector().packageName("com.rehivetech.beeeon.debug"));
    // for beta testers
    //UiObject beeeonValidation = new UiObject(new UiSelector().packageName("com.rehivetech.beeeon"));
    // for alpha tester
    //UiObject beeeonValidation = new UiObject(new UiSelector().packageName("com.rehivetech.beeeon"));
    UiObject currentPackage = new UiObject(
            new UiSelector());
    // demo button
    UiObject googleImageButton = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .index(0));
    // demo button
    UiObject mojeIDImageButton = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .index(1));
    // demo button
    UiObject demoImageButton = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .index(2));
    // menu - nav drawer
    UiObject menuNavDrawerButton = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .index(0));
    // overview
    UiObject overview =  new UiObject(new UiSelector()
            .className(android.widget.TextView.class.getName())
            .text("Overview"));
    // device name text (TextView)
    UiObject deviceNameText =  new UiObject(new UiSelector()
            .className(android.widget.TextView.class.getName())
            .text("Televizor"));
    // logout (TextView)
    UiObject logoutText =  new UiObject(new UiSelector()
            .className(android.widget.TextView.class.getName())
            .text("Logout"));
    // NEXT 
    UiObject nextButton =  new UiObject(new UiSelector()
            .className(android.widget.Button.class.getName())
            .text("NEXT"));
    // SKIP 
    UiObject skipButton =  new UiObject(new UiSelector()
            .className(android.widget.Button.class.getName())
            .text("SKIP"));
    // Cancel 
    UiObject cancelButton =  new UiObject(new UiSelector()
            .className(android.widget.Button.class.getName())
            .text("Cancel"));
    // START 
    UiObject startIntroButton =  new UiObject(new UiSelector()
            .className(android.widget.Button.class.getName())
            .text("START"));
    // START 
    UiObject addButton =  new UiObject(new UiSelector()
            .className(android.widget.Button.class.getName())
            .text("ADD"));
    // OK
    UiObject okButton =  new UiObject(new UiSelector()
            .className(android.widget.Button.class.getName())
            .text("OK"));
    // Close
    UiObject closeButton =  new UiObject(new UiSelector()
            .className(android.widget.Button.class.getName())
            .text("Close"));
    // fab button - menu
    UiObject fabMenu = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .index(2));
    // fab add adapter
    UiObject fabAddAdapterButton = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .index(6));
    // fab add device
    UiObject fabAddDeviceButton = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .index(5));

    UiObject nameAdaptereditText = new UiObject(new UiSelector().className(
            android.widget.EditText.class.getName())
            .index(1));
    UiObject idAdaptereditText = new UiObject(new UiSelector().className(
            android.widget.EditText.class.getName())
            .index(2));

    UiObject logout = new UiObject(new UiSelector().description("Logout"));


    /************************************************************************************
     * Actual Tests starts here
     ***********************************************************************************/ 

    /**
     * Validate BeeeOn App by it's package name
     */
    // Launch BeeeOn Application

    //@Test	// for testng
    /*public void test_00_LaunchApp() throws RemoteException, UiObjectNotFoundException {

        //setCurrentTestName("testLaunchTestApp");
        assertTrue("Unable to detect BeeeOn application.", beeeonValidation.exists());

    }*/

    /**
     * Minimize application (hned) po kliknuti na tlacitko pro prihlaseni pres google ucet
     */
    //@Test	// for testng
    public void test_01_MinimizeAfterSignIn() throws UiObjectNotFoundException, Exception {
    	// check intro
    	check_intro();
        // log in: clickAndWaitForNewWindow() -> click()
        // NO: clickAndWaitForNewWindow()
        googleImageButton.click();
        getUiDevice().pressHome();
        // NO: sleep(10);
        check_no_account();
        skipp_addAdapterGuidePlus();
        maximize();
        logout();
    }

    // test_02_ 
    // test_03_ 
    // ...

    // - behem editace senzoru, pri ukladani dat, pri pidavani adapteru, pri pridavani sernzoru

    public void check_intro() throws UiObjectNotFoundException, Exception {
    	if(nextButton.exists()){
    		// clicking through the intro
	        assertTrue("No intro started.", nextButton.exists());
	        nextButton.clickAndWaitForNewWindow();
	        assertTrue("NEXT button not found.", nextButton.exists());
	        nextButton.clickAndWaitForNewWindow();
	        assertTrue("NEXT button not found.", nextButton.exists());
	        nextButton.clickAndWaitForNewWindow();
	        assertTrue("NEXT button not found.", nextButton.exists());
	        nextButton.clickAndWaitForNewWindow();
	        assertTrue("START Button not found", startIntroButton.exists());
	        startIntroButton.clickAndWaitForNewWindow();
    	}
    }

    public void check_no_account() throws UiObjectNotFoundException, Exception {
        if(nextButton.exists()){
            okButton.clickAndWaitForNewWindow();
            nextButton.clickAndWaitForNewWindow();
            nextButton.clickAndWaitForNewWindow();
            nextButton.clickAndWaitForNewWindow();
            cancelButton.clickAndWaitForNewWindow();
        }
    }

    public void skipp_addAdapterGuidePlus() throws UiObjectNotFoundException, Exception {
        if(nextButton.exists()){
            closeButton.clickAndWaitForNewWindow();
        }
    }

    public void maximize() throws UiObjectNotFoundException{
        UiObject Applications = new UiObject(new UiSelector().description("Apps"));
        Applications.clickAndWaitForNewWindow();
        UiObject apps = new UiObject(new UiSelector().text("Apps"));
        apps.click();
        UiScrollable ListOfapplications = new UiScrollable(new UiSelector().scrollable(true));
        UiObject BeeeOnApp = ListOfapplications.getChildByText(new UiSelector().className(android.widget.TextView.class.getName()), "BeeeOn (debug)");
        BeeeOnApp.clickAndWaitForNewWindow();
    }

    public void logout() throws UiObjectNotFoundException, Exception {
        // open menu - nav drawer
        if(menuNavDrawerButton.exists()){
            
            menuNavDrawerButton.click();
            
            if(logoutText.exists()){
                logoutText.clickAndWaitForNewWindow();
            }
            else{
                // scroll to logout
                assertTrue("Logout is not found", new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(logoutText));
                logoutText.clickAndWaitForNewWindow();
            }
        }
    }
}