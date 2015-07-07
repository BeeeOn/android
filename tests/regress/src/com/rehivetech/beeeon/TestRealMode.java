package com.rehivetech.beeeon;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Rect;
import android.os.RemoteException;
import android.os.SystemClock;

import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.core.UiWatcher;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

import com.rehivetech.beeeon.Base;

// import org.testng.Assert;
// import org.testng.annotations.Test;

/**
 * Tests for BeeeOn application.
 * Real mode.
 * @uthor Martina Kůrová
 */
public class TestRealMode extends UiAutomatorTestCase {

	// current test name
    private String currentTestName;
    
    // info about testing application
    private static final String TEST_APP_PKG = "com.rehivetech.beeeon.debug";
    private static final String START_LOGIN_ACTIVITY = "com.rehivetech.beeeon.gui.activity.LoginActivity";
    
    // class with object ant methods definitions
    Base base = new Base();
    TestDemoMode demo = new TestDemoMode();

    /**
     * @Override(non-Javadoc)
     * @see com.android.uiautomator.testrunner.UiAutomatorTestCase#setUp()
     * */
	protected void setUp() throws Exception {

        super.setUp();
        
        // unlock screen
		try {
			if (!getUiDevice().isScreenOn()){
				getUiDevice().wakeUp();
				getUiDevice().getInstance().swipe(540, 1600, 540, 400, 20);
			}
		} catch (RemoteException e) {
			//e.printStackbase.trace();
		}
    }
    
	/**
  	 * @Override(non-Javadoc)
	 * @see com.android.uiautomator.testrunner.UiAutomatorTestCase#tearDown()
	 * */
    protected void tearDown() throws Exception {
        
        base.takeScreenShot("endTest");
        
        // press HOME button
        getUiDevice().pressHome();
        
        // TODO remove google accounts to phone
        // removedAccounts();
        
        super.tearDown();
    }
    
  
    /************************************************************************************
     * Actual Tests starts here
     ***********************************************************************************/ 
    
    /**
     * 
     * @throws Throwable
     */
    public void test() throws Throwable {

        // register and run ANR watcher
        getUiDevice().registerWatcher("ANR WATCHER", base.anrWatcher);
        getUiDevice().runWatchers();
	         
        // run test cases
        try {  
	    	setCurrentTestName("test suite for real part");
	        base.trace("Start " + this.currentTestName + ".");
	    	this.launchApp();
	    	this.intro();
	    	this.signInViaGoogleAccount();
	    	this.addAdapter();
	    	this.addDevice();
	    	this.unregisterFacility();
	    	this.unregisterGateway();
	    	this.logOut();
	    	this.exitApp();
	    	this.introForTheSecondTime();
	    	base.trace("End " + this.currentTestName + ".");

	    } catch (UiObjectNotFoundException e) {
	        //e.printStackbase.trace();
	    }
	}
	      
    
    /**
     * Launch BeeeOn Application and validate BeeeOn App by it's package name
     * @throws IOException 
     */
    //@Test	// for testNG
    public void launchApp() throws RemoteException, UiObjectNotFoundException, IOException {

        setCurrentTestName("testLaunchTestApp");
        base.trace_start(this.currentTestName);
        
        // good to start with this
        getUiDevice().pressHome();
        Runtime.getRuntime().exec("am start -n com.rehivetech.beeeon.debug/com.rehivetech.beeeon.gui.activity.LoginActivity");
        
        if(!base.beeeonValidation.exists()){
        	sleep(5000);
        }
        assertTrue("Unable to detect BeeeOn", base.beeeonValidation.exists());
        base.trace_end(this.currentTestName);
    }
    
    //@Test	// for testNG
    public void intro() throws UiObjectNotFoundException, Exception {
    	
    	setCurrentTestName("testIntro");
    	base.trace_start(this.currentTestName);
    	
        //if(base.nextButton.exists()) {
        	
            // clicking through the intro
            assertTrue("No intro started.", base.nextButton.exists());
            base.nextButton.clickAndWaitForNewWindow();
            assertTrue("NEXT button not found.", base.nextButton.exists());
            base.nextButton.clickAndWaitForNewWindow();
            assertTrue("NEXT button not found.", base.nextButton.exists());
            base.nextButton.clickAndWaitForNewWindow();
            assertTrue("NEXT button not found.", base.nextButton.exists());
            base.nextButton.clickAndWaitForNewWindow();
            assertTrue("START Button not found", base.startIntroButton.exists());
            base.startIntroButton.clickAndWaitForNewWindow();
       // }
        //else{
       // 	base.trace("No intro started.");
       // }
        base.trace_end(this.currentTestName);
    }

    //@Test	// for testNG
    public void signInViaGoogleAccount() throws UiObjectNotFoundException {
    	
    	setCurrentTestName("testsignInViaGoogleAccount");
    	base.trace_start(this.currentTestName);
        // click on demo button
    	UiObject googleImageButton = new UiObject(new UiSelector().className(
                android.widget.ImageButton.class.getName())
                .index(0));
    	
    	if(!googleImageButton.exists()){
    		sleep(3000);
    	}
    	if(!googleImageButton.exists()){
    		sleep(3000);
    	}
    	if(!googleImageButton.exists()){
    		sleep(3000);
    	}
    	
        assertTrue("Google Button not found", googleImageButton.exists());
        //googleImageButton.clickAndWaitForNewWindow();
        base.ib_loginGoogle_ridi.click();
        
        UiObject emailForTesting = new UiObject(new UiSelector().className(
        	android.widget.CheckedTextView.class.getName())
                .text("android.test.regress.1@gmail.com")
		.resourceId("android:id/text1")
	        .index(0));


	UiObject et_email_i = new UiObject(new UiSelector().className(
                android.widget.EditText.class.getName())
                .index(1));

	UiObject et_passwd_i = new UiObject(new UiSelector().className(
                android.widget.EditText.class.getName())
                .index(2));

	UiObject b_singin_i = new UiObject(new UiSelector().className(
                android.widget.Button.class.getName())
                .index(0));

	UiObject b_singin_t = new UiObject(new UiSelector().className(
                android.widget.Button.class.getName())
                .text("Sign in"));


        if(emailForTesting.exists()){
		if(emailForTesting.isCheckable()){
			emailForTesting.click();
			base.b_ok_t.click();
		}
	}
	else{
 		assertTrue("Email for testing not found.", emailForTesting.exists());
	}
	/*else{
		sleep(7000);
		getUiDevice().getInstance().click(530, 1110);
	}*/
        
        sleep(3000);
        
        base.trace_end(this.currentTestName);
    }
    
    //@Test	// for testNG
    public void addAdapter() throws Throwable {

    	setCurrentTestName("testaddAdapter");
    	base.trace_start(this.currentTestName);
    	
    	// ib: fab menu
    	UiObject ib_fabMenu_rid = new UiObject(new UiSelector().className(
                android.widget.ImageButton.class.getName())
                .resourceId("com.rehivetech.beeeon.debug:id/fab"));
    	
        // et: gateway name
        UiObject nameAdapterEditText = new UiObject(new UiSelector().className(
                android.widget.EditText.class.getName())
                .index(1));
        
        // et: gateway id
        UiObject idAdapterEditText = new UiObject(new UiSelector().className(
                android.widget.EditText.class.getName())
                .index(2));
        
        // b: next
        UiObject b_next_u =  new UiObject(new UiSelector()
	    	.className(android.widget.Button.class.getName())
	    	.resourceId("com.rehivetech.beeeon.debug:id/.add_adapter_next")
	    	.text("NEXT")
	    	.text("Next")
	    	.text("next")
	    	.index(2));
        
        // b: next
        UiObject add_u =  new UiObject(new UiSelector()
	    	.className(android.widget.Button.class.getName())
	    	.resourceId("com.rehivetech.beeeon.debug:id/add_adapter_next")
	    	.text("ADD")
	    	.index(1));
        
        // b: skip
        UiObject b_skip_u =  new UiObject(new UiSelector()
	    	.className(android.widget.Button.class.getName())
	    	.resourceId("com.rehivetech.beeeon.debug:id/add_adapter_skip")
	    	.text("SKIP"));
        
        // b: cancel
        UiObject b_cancel_u =  new UiObject(new UiSelector()
	    	.className(android.widget.Button.class.getName())
	    	.resourceId("com.rehivetech.beeeon.debug:id/add_adapter_cancel")
	    	.text("Cancel"));

        // 1A: next
        //click_through_.add_gateway_guide();
        base.swipe_through_add_gateway_guide();

	        // input name and id
	        String name = "at 10";
	        String id = "101010";
	        nameAdapterEditText.setText(name);
	        new UiScrollable(
	                new UiSelector().scrollable(true)
	            ).scrollIntoView(idAdapterEditText);
	        idAdapterEditText.setText(id);
        
	        // .add 
	        assertTrue(".add button dos not exists.", add_u.exists());
	        add_u.click();
	        
	        sleep(2000);
	        
	        // guide
	        if(base.b_close_rid.exists()){
		        base.b_close_rid.click();
	        }
        
	        // verify list of gateways
	        base.ib_navDrawer_i.click();
	        UiObject at10 = new UiObject(new UiSelector().className(
	        		android.widget.TextView.class.getName())
	        		.text(name));
	        assertTrue(".added gateway not found in list of adapters", at10.exists());
	        base.ib_navDrawer_i.click();
	        
        // sometimes a big fullscreen TextView appears ? emulator
    	if(!ib_fabMenu_rid.exists()){ 
	    	sleep(3000);
    	}
    	
        // open fab menu
    	assertTrue("Fab menu not found", ib_fabMenu_rid.exists());
    	ib_fabMenu_rid.click();
    	
    	// ib: must be here ! its index
    	UiObject ib_fabaddAdapter_i = new UiObject(new UiSelector().className(
                android.widget.ImageButton.class.getName())
                .index(4));

        // .add gateway
        assertTrue("Fab menu item - .add adapter - not found", ib_fabaddAdapter_i.exists());
        ib_fabaddAdapter_i.clickAndWaitForNewWindow();    
	        
        // 1B: skip
	    b_skip_u.click();
	    
	    	// try to .add already registered gateway
		nameAdapterEditText.setText(name);
	        new UiScrollable(
	                new UiSelector().scrollable(true)
	            ).scrollIntoView(idAdapterEditText);
	        idAdapterEditText.setText(id);
	        // .add 
	        assertTrue(".add button dos not exists.", add_u.exists());
	        add_u.click();
	        
	    	// try to .add gateway not connected to the cloud yet
	        String notName = "Home";
	        String notID = "1919191";
		new UiScrollable(
	                new UiSelector().scrollable(true)
	            ).scrollIntoView(nameAdapterEditText);
		nameAdapterEditText.setText(notName);
	        new UiScrollable(
	                new UiSelector().scrollable(true)
	            ).scrollIntoView(idAdapterEditText);
	        idAdapterEditText.setText(notID);
	        // .add 
	        assertTrue(".add button dos not exists.", add_u.exists());
	        add_u.click();

	        // input name and id
	        String name2 = "at 11";
	        String id2 = "111111";
		new UiScrollable(
	                new UiSelector().scrollable(true)
	            ).scrollIntoView(nameAdapterEditText);
	        nameAdapterEditText.setText(name2);
	        new UiScrollable(
	                new UiSelector().scrollable(true)
	            ).scrollIntoView(idAdapterEditText);
	        idAdapterEditText.setText(id2);
        
	        // .add 
	        assertTrue(".add button dos not exists.", add_u.exists());
	        add_u.click();
	        
	        // verify list of gateways
	        base.ib_navDrawer_i.click();
	        UiObject at11 = new UiObject(new UiSelector().className(
	        		android.widget.TextView.class.getName())
	        		.text(name2));
	        assertTrue(".added gateway not found in list of adapters", at11.exists());
	        base.ib_navDrawer_i.click();	        
   	    	
        // sometimes a big fullscreen TextView appears ? emulator
    	if(!ib_fabMenu_rid.exists()){ 
	    	sleep(3000);
    	}
    	
        // open fab menu
    	assertTrue("Fab menu not found", ib_fabMenu_rid.exists());
    	ib_fabMenu_rid.click();
    	
    	// ib: must be here ! its index
    	UiObject ib_fabaddAdapter_i2 = new UiObject(new UiSelector().className(
                android.widget.ImageButton.class.getName())
                .index(4));
    	
        // .add gateway
        assertTrue("Fab menu item - .add adapter - not found", ib_fabaddAdapter_i2.exists());
        ib_fabaddAdapter_i2.clickAndWaitForNewWindow();
        
        // 1C: cancel
	    b_cancel_u.click();
	    
	    sleep(3000);
	    // guide appears
	    if(base.b_close_rid.exists()){
	    	base.b_close_rid.click();
	    }

    	// verify default screen
        assertTrue("No deafult screen after click on 'Cancel'", base.ib_navDrawer_i.waitForExists(2));
        
        base.trace_end(this.currentTestName);
    }
 
    //@Test	// for testNG
    public void addDevice() throws UiObjectNotFoundException {
    	
    	setCurrentTestName("test.addDevice");
    	base.trace_start(this.currentTestName);
    	
        UiObject ib_fabMenu_rid = new UiObject(new UiSelector().className(
                android.widget.ImageButton.class.getName())
                .resourceId("com.rehivetech.beeeon.debug:id/fab"));
        
    	 // sometimes a big fullscreen TextView appears
    	if(!ib_fabMenu_rid.exists()){ 
	    	sleep(3000);
    	}
    	
        // open fab menu
    	assertTrue("Fab menu not found", ib_fabMenu_rid.exists());
    	ib_fabMenu_rid.click();
    	
    	//base.trace("Fab menu click.");
    	
    	UiObject addDevice_i = new UiObject(new UiSelector().className(
                android.widget.ImageButton.class.getName())
                .index(5));

        // .add adapter
        assertTrue("Fab menu item - add device - not found", addDevice_i.exists());
        addDevice_i.clickAndWaitForNewWindow();
        
        //base.trace("Fab device click.");

        if(base.add.exists()){
        	base.add.click();
        	base.add.click();
        }
        else{
        	base.skipButton.clickAndWaitForNewWindow();
        }

        UiObject tw_pairOK_t = new UiObject(new UiSelector().className(
                android.widget.TextView.class.getName())
                .text("Device Setup"));
        
        if(!tw_pairOK_t.exists()){
        	sleep(3000);
        }
        if(!tw_pairOK_t.exists()){
        	sleep(3000);
        }
        if(!tw_pairOK_t.exists()){
        	sleep(5000);
        }
        if(!tw_pairOK_t.exists()){
        	sleep(10000);
        }
        if(!tw_pairOK_t.exists()){
        	sleep(5000);
        }
        if(!tw_pairOK_t.exists()){
        	sleep(5000);
        }

        // search sezor name atd...
        if(base.et_deviceName_rid.exists()){
        	    	
        	// set location
        	new UiScrollable(
                    new UiSelector().scrollable(true)
                ).scrollIntoView(base.spi_newDevicelocation_rid);
        	base.spi_newDevicelocation_rid.click();
        	base.chtw_bedroomDevicelocation_t.click();
        	// save
        	assertTrue("Cant find 'Save' button :(", base.b_save_t.exists());
        	base.b_save_t.click();

        }
        else{
	        base.ibBackGuide.click();
	        sleep(2000);
        }

        // confirm
    	base.trace_end(this.currentTestName);
    }

    /**
     * Unregister facility
     * @throws UiObjectNotFoundException
     */
    public void unregisterFacility() throws UiObjectNotFoundException {

    	setCurrentTestName("testUnregisterFacility");
    	base.trace_start(this.currentTestName);
    	
    	UiObject my_textView = new UiObject(new UiSelector()
			.className(android.widget.TextView.class.getName())
			.text("Teplota"));
    	
	if(my_textView.exists()){
		String removed = my_textView.getText().toString();
	    	
	    	base.trace("Trying to remove device named: " + removed);
	    	my_textView.swipeLeft(80);

		UiObject tw_bin_rid =  new UiObject(new UiSelector()
			    .className(android.widget.TextView.class.getName())
			    .resourceId("com.rehivetech.beeeon.debug:id/sensor_menu_del"));
		
		UiObject tw_bell_rid =  new UiObject(new UiSelector()
		    .className(android.widget.TextView.class.getName())
		    .resourceId("com.rehivetech.beeeon.debug:id/action_notification"));
	    	
	    	// click on wastebin
	    	if(!tw_bin_rid.exists()){
	    		tw_bell_rid.click();
	    	}
	    	else tw_bin_rid.click();
	//
	//        new UiScrollable(
	//                new UiSelector().scrollable(true)
	//            ).scrollIntoView(removed_device_text);
		    
	    	assertFalse("Device '" + removed + "' has not been removed! Still in list.", my_textView.exists());
	    	base.trace("OK '" + removed + "' removed :)");
	}
    	
    	base.trace_end(this.currentTestName);
    }    
  
    /**
     * unregister gateway
     * @throws UiObjectNotFoundException
     */
    public void unregisterGateway() throws UiObjectNotFoundException {
    
		setCurrentTestName("testUnregisterGateway");
		base.trace_start(this.currentTestName);
	  	  		
		// open menu - nav drawer
    	if(!base.ib_navDrawer_i.exists()){
    		sleep(3000);
    	}
    	assertTrue("Cannot access 'NavDrawer'.", base.ib_navDrawer_i.click());
    	
    	// tw: at10
		UiObject at10 = new UiObject(new UiSelector()
			.className(android.widget.TextView.class.getName())
			.text("at 10"));
		
		// tw: at11
		UiObject at11 = new UiObject(new UiSelector()
			.className(android.widget.TextView.class.getName())
			.text("at 11"));
		
		// unregister adapter - adapter long press
		at10.swipeRight(100);
		if(!base.tw_adabin_rid.exists()){
    		sleep(3000);
    	}
		base.tw_adabin_rid.click();
		
		// unregister adapter - adapter long press
		at11.swipeRight(100);
		if(!base.tw_adabin_rid.exists()){
    		sleep(3000);
    	}
		base.tw_adabin_rid.click();
		
		// back
		base.ib_navDrawer_i.click();
		
		base.trace_end(this.currentTestName);
	}
    
    /**
     * Logout
     * @throws UiObjectNotFoundException
     */
    public void logOut() throws UiObjectNotFoundException {
    	
    	setCurrentTestName("testLogout");
    	base.trace_start(this.currentTestName);
    	
    	// open menu - nav drawer
    	if(!base.ib_navDrawer_i.exists()){
    		sleep(4000);
    	}
        assertTrue("Could not log out from the application (NavDrawer not clickable).", base.ib_navDrawer_i.click());
        
        if(!base.logoutText.exists()){
	        // scroll to logout
	    	assertTrue("Logout is not found", 
	        	new UiScrollable(
	        		new UiSelector().scrollable(true)
	            ).scrollIntoView(base.logoutText));
        }
        base.logoutText.clickAndWaitForNewWindow();
        
        UiObject googleImageButton = new UiObject(new UiSelector().className(
                android.widget.ImageButton.class.getName())
                .index(0));
        
        // verify
        if(!googleImageButton.exists()){
    		sleep(5000);
    	}
        assertTrue("Failed logout from BeeeOn application!", googleImageButton.exists());
        
        base.trace_end(this.currentTestName);
    }
 
    /**
     * Test exit application.
     * @throws UiObjectNotFoundException
     */
    public void exitApp() throws UiObjectNotFoundException {
    
    	setCurrentTestName("testExitApp");
    	base.trace_start(this.currentTestName);
    	
        // exit
    	base.myDevice.getInstance().pressBack();
    	
    	if(!base.AppTrayButton.exists()){
    		base.myDevice.getInstance().pressBack();
    		sleep(5000);
    	}
    	
    	if(!base.AppTrayButton.exists()){
    		base.myDevice.getInstance().pressBack();
    		sleep(5000);
    	}

        // verify that application exits
    	assertTrue("BeeOn app doesnt exit properly.", base.AppTrayButton.exists());
    	
    	base.trace_end(this.currentTestName);
    }
   
    /**
     * Check intro again
     * @throws IOException
     * @throws UiObjectNotFoundException
     */
    public void introForTheSecondTime() throws IOException, UiObjectNotFoundException{
    	
    	setCurrentTestName("testIntroForTheSecondTime");
    	
    	// start BeeeOn app
    	UiObject Applications = new UiObject(new UiSelector().description("Apps"));
        Applications.clickAndWaitForNewWindow();
        // emulator
        //UiObject apps = new UiObject(new UiSelector().text("Apps"));
        //apps.click();
        UiScrollable ListOfapplications = new UiScrollable(new UiSelector().scrollable(true));
        UiObject BeeeOnApp = ListOfapplications.getChildByText(new UiSelector().className(android.widget.TextView.class.getName()), "BeeeOn (debug)");
        BeeeOnApp.clickAndWaitForNewWindow();

        // assert that no intro appear this time
        assertFalse("Intro started for the second time.", base.nextButton.exists());
        
        // exit
    	getUiDevice().pressBack();
    	
    	base.trace_end(this.currentTestName);
    }
    
    /************************************************************************************
     * Methods definition
     ***********************************************************************************/    
    
    /**
     * Set current test name
     * @param testName
     */
    private void setCurrentTestName(String testName) {
        this.currentTestName = testName;
        //takeScreenshot("start");
    }
}
