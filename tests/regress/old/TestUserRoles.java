package com.rehivetech.beeeon;
import java.io.IOException;

import android.graphics.Rect;
import android.os.RemoteException;

import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.core.UiWatcher;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;


/**
 * Tests for BeeeOn application.
 * User Roles.
 * @uthor Martina Kůrová
 */
public class TestUserRoles extends UiAutomatorTestCase {

	// current test name
    private String currentTestName;
    
    // info about testing application
    private static final String TEST_APP_PKG = "com.rehivetech.beeeon.debug";
    private static final String START_LOGIN_ACTIVITY = "com.rehivetech.beeeon.activity.LoginActivity";
    
    // class with object ant methods definitions
    Base base = new Base();

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
        
        // register and run ANR watcher
        getUiDevice().registerWatcher("ANR WATCHER", base.anrWatcher);
        getUiDevice().runWatchers();
        
        // instrument
        launchApp();
    	intro();
    }
    
	/**
  	 * @Override(non-Javadoc)
	 * @see com.android.uiautomator.testrunner.UiAutomatorTestCase#tearDown()
	 * */
    protected void tearDown() throws Exception {
        
    	logout();
    	exitApp();
        super.tearDown();
    }

    /************************************************************************************
     * Actual Tests starts here
     ***********************************************************************************/ 
    
    /**
     *    
     * @throws Exception
     */
    public void test_AddUser() throws Exception{
    	
    	setCurrentTestName("AddUserToAdapter");
    	base.trace_start(currentTestName);
    	
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
         base.myGoogle.click();
         
         UiObject emailForTesting = new UiObject(new UiSelector().className(
         		android.widget.CheckedTextView.class.getName())
                 .text("android.test.regress.1@gmail.com"));

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
	 		sleep(7000);
	 		getUiDevice().getInstance().click(530, 1110);

	 	}
	         
	    sleep(3000);

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
	    	.resourceId("com.rehivetech.beeeon.debug:id/add_adapter_next")
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
        
        sleep(3000);
        
        if(base.b_close_rid.exists()){
	    	base.b_close_rid.click();
	    
        
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
        }
        
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
	        
	        // guide
	        if(base.b_close_rid.exists()){
		    	base.b_close_rid.click();
	        }
	        
	        /*
	        if(base.ib_navDrawer_i.exists()){
	        	base.ib_navDrawer_i.click();
	        }*/
	        
	        // verify list of gateways
	        base.ib_navDrawer_i.click();
	        UiObject at10 = new UiObject(new UiSelector().className(
	        		android.widget.TextView.class.getName())
	        		.text(name));
	        assertTrue(".added gateway not found in list of adapters", at10.exists());
	        base.ib_navDrawer_i.click();
	        
	    	// open menu - nav drawer
	    	if(!base.ib_navDrawer_i.exists()){
	    		sleep(3000);
	    	}
	    	assertTrue("NavDrawer not clickable", base.ib_navDrawer_i.click());

	    	at10.swipeRight(100);
			
	    	base.trace("swiping");
	    	sleep(2000);
	    	base.trace("searching users");
	    	
	    	base.tw_adausers_rid.waitForExists(3000);
	    	base.tw_adausers_rid.clickAndWaitForNewWindow();
			
			// add user icon
	    	base.b_fabAddUser_rid.clickAndWaitForNewWindow();
			
			// fill user email
			if(!base.et_userEmail_rid.exists()){
	    		sleep(3000);
	    		base.trace("sleeping - waiting for editText");
	    	}
			base.et_userEmail_rid.clearTextField();
			base.et_userEmail_rid.setText("android.test.resgress.2@gmail.com");
			base.trace("after settext");
			
			// choose user role
			base.spi_userRole_rid.click();
			base.chtw_admin_t.click();
			
			// confirm
			base.b_addUser_rid.clickAndWaitForNewWindow();
			
			sleep(3000);

    	base.trace_end(currentTestName); 	
    }
    
    /************************************************************************************
     * Methods definition
     ***********************************************************************************/  
    
    /**
     * Launch BeeeOn Application and validate BeeeOn App by it's package name
     * @throws Exception 
     */
    public void launchApp() throws Exception {

        setCurrentTestName("testLaunchTestApp");
        base.trace_start(this.currentTestName);      
        // good to start with this
        getUiDevice().pressHome();
        Runtime.getRuntime().exec("am start -n com.rehivetech.beeeon.debug/com.rehivetech.beeeon.activity.LoginActivity");
        
        if(!base.beeeonValidation.exists()){
        	sleep(5000);
        }
        assertTrue("Unable to detect BeeeOn", base.beeeonValidation.exists());
        base.trace_end(this.currentTestName);
    }
    
    /**
     * Intro
     * @throws UiObjectNotFoundException
     * @throws Exception
     */
    public void intro() throws UiObjectNotFoundException, Exception {
    	
    	setCurrentTestName("testIntro");
    	base.trace_start(this.currentTestName);
    	
    	if(!base.nextButton.exists()) {
    		sleep(3000);
    	}

    	// verify intro screen
        assertTrue("No intro started.", base.nextButton.exists());
        
        // go through into guide
        base.nextButton.clickAndWaitForNewWindow();
        assertTrue("NEXT button not found.", base.nextButton.exists());
        base.nextButton.clickAndWaitForNewWindow();
        assertTrue("NEXT button not found.", base.nextButton.exists());
        base.nextButton.clickAndWaitForNewWindow();
        assertTrue("NEXT button not found.", base.nextButton.exists());
        base.nextButton.clickAndWaitForNewWindow();
        assertTrue("START Button not found", base.startIntroButton.exists());
        base.startIntroButton.clickAndWaitForNewWindow();

        base.trace_end(this.currentTestName);
    }
    /**
     * Logout
     * @throws UiObjectNotFoundException
     */
    public void logout() throws UiObjectNotFoundException {
    	
    	setCurrentTestName("testLogout");
    	base.trace_start(this.currentTestName);
    	
    	// open menu - nav drawer
    	if(!base.ib_navDrawer_i.exists()){
    		sleep(4000);
    	}
        assertTrue("NavDrawer not clickable", base.ib_navDrawer_i.click());
        
        if(!base.logoutText.exists()){
	        // scroll to logout
	    	assertTrue("Logout is not found", 
	        	new UiScrollable(
	        		new UiSelector().scrollable(true)
	            ).scrollIntoView(base.logoutText));
        }
        base.logoutText.clickAndWaitForNewWindow();
        
        // verify
        if(!base.demoImageButton.exists()){
    		sleep(5000);
    	}
        assertTrue("Failed logout from BeeeOn application!", base.demoImageButton.exists());
        
        base.trace_end(this.currentTestName);
    }
    
    /**
     * Minimize
     * @throws UiObjectNotFoundException
     */
    public void minimize() throws UiObjectNotFoundException {
    
    	setCurrentTestName("testMinimize");
    	base.trace_start(this.currentTestName);
       // minimize
    	base.myDevice.getInstance().pressHome();
    	// base.myDevice.pressMenu();

        // verify
    	UiObject Applications = new UiObject(new UiSelector()
		.description("Apps"));
    	Applications.exists();
    	
    	base.trace_end(this.currentTestName);
    }
    
    /**
     * Maximize
     * @throws UiObjectNotFoundException
     * @throws IOException
     * @throws RemoteException
     */
    public void maximize() throws UiObjectNotFoundException, IOException, RemoteException {
    
    	setCurrentTestName("testMaximize");
    	base.trace_start(this.currentTestName);
    	
        // maximize
    	base.myDevice.getInstance().pressRecentApps();
    	
    	if(!base.tw_beeeOn_t.exists()){
    		sleep(5000);
    	}
    	
    	if(!base.tw_beeeOn_t.exists()){
    		sleep(5000);
    	}
    	
    	base.tw_beeeOn_t.clickAndWaitForNewWindow();
    	
    	if(!base.beeeonValidation.exists()){
    		sleep(5000);
    	}
    	
        // verify
    	assertTrue("Unable to detect BeeeOn", base.beeeonValidation.exists());
    	
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
     * Print current test name
     * @param testName
     */
    private void setCurrentTestName(String testName) {
        this.currentTestName = testName;
        //takeScreenshot("start");
    }
}
