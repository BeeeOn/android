package com.rehivetech.beeeon;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.RemoteException;
import android.os.RemoteException;
import android.content.ComponentName;
import android.content.Intent;

import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.core.UiWatcher;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

//import org.testng.Assert;
//import org.testng.annotations.Test;

/**
 * Tests for BeeeOn application.
 * Failures
 * @uthor Martina Kůrová
 */
public class TestFailures extends UiAutomatorTestCase {

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
        
    }
    
	/**
  	 * @Override(non-Javadoc)
	 * @see com.android.uiautomator.testrunner.UiAutomatorTestCase#tearDown()
	 * */
    protected void tearDown() throws Exception {
        
        super.tearDown();
    }

    /************************************************************************************
     * Actual Tests starts here
     ***********************************************************************************/ 
    
    /**
     * @test
     * Minimize application immediately after click on google image button po kliknuti na tlacitko pro prihlaseni pres google ucet
     */
    public void test_01_MinimizeAfterSignIn() throws UiObjectNotFoundException, Exception {
    	
    	setCurrentTestName("testsignInViaGoogleAccount");
    	base.trace_start(this.currentTestName);
    	
        launchApp();
    	intro();
    	
    	UiObject emailForTesting = new UiObject(new UiSelector().className(
        		android.widget.CheckedTextView.class.getName())
                .text("android.test.regress.1@gmail.com"));
    	
        // log in: clickAndWaitForNewWindow() -> click()
        // NO: clickAndWaitForNewWindow()
        base.googleImageButton.click();
        if(emailForTesting.exists()){
    		if(emailForTesting.isCheckable()){
    			emailForTesting.click();
    			base.b_ok_t.click();
    		}
        }
        
        getUiDevice().pressHome();
        
        maximize();  
        
        // guide
        if(base.cancelButton.exists()){
        	base.cancelButton.click();
        }
        
        // guide
        if(base.b_close_rid.exists()){
	        base.b_close_rid.click();
        }
    
    	logout();
    	exitApp();
        base.trace_end(this.currentTestName);
    }
    
    /**
     * @test
     * Different screen orientations
     */
    public void test_02_switchScreenOrientation() throws UiObjectNotFoundException, Exception {
    	
    	setCurrentTestName("testswitchScreenOrientation");
    	base.trace_start(this.currentTestName);
    	
    	base.myDevice.setOrientationLeft();
	
        // good to start with this
        getUiDevice().pressHome();
        Runtime.getRuntime().exec("am start -n com.rehivetech.beeeon.debug/com.rehivetech.beeeon.activity.LoginActivity");
        
        if(!base.beeeonValidation.exists()){
        	sleep(5000);
        }
        assertTrue("Unable to detect BeeeOn", base.beeeonValidation.exists());
    	
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

        // click on demo button
        assertTrue("Demo Button not found", base.demoImageButton.exists());
        base.demoImageButton.clickAndWaitForNewWindow(2000);
        sleep(2000);
        assertTrue("No demo started", base.demoImageButton.exists());
    	
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
	    	.text("SKIP")
	    	.index(0));
        
        // b: cancel
        UiObject b_cancel_u =  new UiObject(new UiSelector()
	    	.className(android.widget.Button.class.getName())
	    	.resourceId("com.rehivetech.beeeon.debug:id/add_adapter_cancel")
	    	.text("Cancel"));

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
                .index(5));

        // .add gateway
        assertTrue("Fab menu item - .add adapter - not found", ib_fabaddAdapter_i.exists());
        ib_fabaddAdapter_i.clickAndWaitForNewWindow(); 
        
        // 1A: next
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
	        assertTrue(".add button dos not exists.", base.add3.exists());
	        base.add3.click();
	        
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
    	
    	base.myDevice.setOrientationLeft();
    	
        // open fab menu
    	assertTrue("Fab menu not found", ib_fabMenu_rid.exists());
    	ib_fabMenu_rid.click();

        // .add gateway
    	// ib: must be here ! its index
    	UiObject ib_fabaddAdapter_i2 = new UiObject(new UiSelector().className(
                android.widget.ImageButton.class.getName())
                .index(4));
    	
        assertTrue("Fab menu item - .add adapter - not found", ib_fabaddAdapter_i2.exists());
        ib_fabaddAdapter_i2.clickAndWaitForNewWindow();    
	        
        // 1B: skip
	    b_skip_u.click();

	        // input name and id
	        String name2 = "at 11";
	        String id2 = "111111";
	        nameAdapterEditText.setText(name2);
	        new UiScrollable(
	                new UiSelector().scrollable(true)
	            ).scrollIntoView(idAdapterEditText);
	        idAdapterEditText.setText(id2);
        
	        // add 
	        base.add3.click();
	        base.trace("1");
	        
	        base.addButton.click();
	        base.trace("2");

	        base.add2.click();
	        base.trace("3");
	        
	        sleep(3000);
	        
	        // verify list of gateways
	        base.ib_navDrawer_i.click();
	        UiObject at11 = new UiObject(new UiSelector().className(
	        		android.widget.TextView.class.getName())
	        		.text(name2));
	        assertTrue("Added gateway not found in list of adapters", at11.exists());
	        base.ib_navDrawer_i.click();	        
   	    	
        // sometimes a big fullscreen TextView appears ? emulator
    	if(!ib_fabMenu_rid.exists()){ 
	    	sleep(3000);
    	}
    	
    	base.myDevice.setOrientationNatural();

        // open fab menu
    	assertTrue("Fab menu not found", ib_fabMenu_rid.exists());
    	ib_fabMenu_rid.click();
    	
    	// ib: must be here ! its index
    	UiObject ib_fabaddAdapter_i3 = new UiObject(new UiSelector().className(
                android.widget.ImageButton.class.getName())
                .index(4));
    	
        // .add gateway
        assertTrue("Fab menu item - .add adapter - not found", ib_fabaddAdapter_i3.exists());
        ib_fabaddAdapter_i3.clickAndWaitForNewWindow();
        
        // 1C: cancel
	    base.cancelButton.click();
	    
    	base.myDevice.setOrientationRight();

	    // verify default screen
	    assertTrue("No deafult screen after click on 'Cancel'", base.ib_navDrawer_i.waitForExists(2));
        
    	 // sometimes a big fullscreen TextView appears
    	if(!ib_fabMenu_rid.exists()){ 
	    	sleep(3000);
    	}
    	
        // open fab menu<
    	assertTrue("Fab menu not found", ib_fabMenu_rid.exists());
    	ib_fabMenu_rid.click();
    	
    	base.trace("Fab menu click.");
    	
    	UiObject addDevice_i = new UiObject(new UiSelector().className(
                android.widget.ImageButton.class.getName())
                .index(5));

        // .add adapter
        assertTrue("Fab menu item - .add device - not found", addDevice_i.exists());
       addDevice_i.clickAndWaitForNewWindow();
        
        base.trace("Fab device click.");
        
    	base.myDevice.setOrientationLeft();
        
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
        
   	 // sometimes a big fullscreen TextView appears
   	if(!ib_fabMenu_rid.exists()){ 
	    	sleep(3000);
   	}
   	
    // open fab menu<
   	assertTrue("Fab menu not found", ib_fabMenu_rid.exists());
   	ib_fabMenu_rid.click();
   	
   	base.trace("Fab menu click.");

       // .add adapter
       assertTrue("Fab menu item - .add device - not found", addDevice_i.exists());
      addDevice_i.clickAndWaitForNewWindow();
       
       base.trace("Fab device click.");

       if(base.add.exists()){
       	base.add.click();
       	base.add.click();
       }
       else{
       	base.swipe_through_add_device_guide();
       }
       
       // wa
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
    	
    	// open nav Drawer
    	assertTrue("Menu (NavDrawer) Button not found", base.ib_navDrawer_i.exists());
        base.ib_navDrawer_i.click();
    	
    	//
        base.firstAdapterInMenu.click();  
    	
    	// close Nav Drawer
    	assertTrue("Menu (NavDrawer) Button not found", base.ib_navDrawer_i.exists());
        base.ib_navDrawer_i.click();
    	
    	// scroll to device name
        assertTrue("Device text filed named " + base.senzorNameText.toString() + " is not found", 
                new UiScrollable(
                    new UiSelector().scrollable(true)
                ).scrollIntoView(base.senzorNameText));
    	
        // show device detail
        base.senzorNameText.clickAndWaitForNewWindow();
        
        if(!base.ib_editDevice_i.exists()){
        	sleep(5000);
        }
        base.ib_editDevice_i.click();
        
        // rename device
        if(!base.et_renameDevice_rid.exists()){
        	sleep(5000);
        }
        assertTrue("Cannot rename device.", base.et_renameDevice_rid.exists());
        base.et_renameDevice_rid.clearTextField();
        assertTrue("Cannot rename device.", base.et_renameDevice_rid.setText("Renamed Karma"));
        
        // change device location
        base.spi_changeDevicelocation_rid.click();
        base.chtw_gardenDevicelocation_t.click();
        
        // change refresh time
        UiScrollable refreshTime = new UiScrollable(new UiSelector().resourceId("com.rehivetech.beeeon.debug:id/sen_edit_refreshtime"));
        refreshTime.swipeRight(2);
        
        // save
        assertTrue("Cannot find save button.", base.tw_save_t.exists());
        base.tw_save_t.clickAndWaitForNewWindow();
        
        // back
        base.ib_navDrawer_i.click();

    	// scroll to device name
    	// actor: "Televizor"
        assertTrue("Device text filed named " + base.actorNameText.toString() + " is not found", 
                new UiScrollable(
                    new UiSelector().scrollable(true)
                ).scrollIntoView(base.actorNameText));
    	
        // show device detail
        base.actorNameText.clickAndWaitForNewWindow();
        
        sleep(3000);
        
        String actorState = new UiObject(new UiSelector().className(
        		android.widget.TextView.class.getName()).index(0)).getText();
        
        // change aktor state
        base.sc_actor_rid.click();
        
        sleep(3000);
        
    	UiScrollable v_deviceSwipeBox_rid =  new UiScrollable(new UiSelector()
			.className(android.view.View.class.getName())
			.resourceId("com.rehivetech.beeeon.debug:id/swipe_container"));
			//.scrollable(true)
    	
    	v_deviceSwipeBox_rid.swipeLeft(5);
    	v_deviceSwipeBox_rid.swipeLeft(5);
    	v_deviceSwipeBox_rid.swipeLeft(5);
    	v_deviceSwipeBox_rid.swipeRight(4);
    	v_deviceSwipeBox_rid.swipeRight(4);
    	v_deviceSwipeBox_rid.swipeRight(4);
    	v_deviceSwipeBox_rid.swipeRight(4);
    	
        // back
        base.ib_navDrawer_i.clickAndWaitForNewWindow();
        
    	
    	UiObject my_textView = new UiObject(new UiSelector()
		.className(android.widget.ListView.class.getName())
		.index(0)).getChild(new UiSelector()
		.className(android.view.View.class.getName())
		.index(0)).getChild(new UiSelector()
		.className(android.widget.LinearLayout.class.getName())
		.index(1)).getChild(new UiSelector()
		.className(android.widget.RelativeLayout.class.getName())
		.index(1)).getChild(new UiSelector()
		.className(android.widget.TextView.class.getName())
		.index(0));
    	
    	String removed = my_textView.getText().toString();
    	
    	UiObject removed_device_text = new UiObject(new UiSelector()
		.className(android.widget.ListView.class.getName())
		.index(0)).getChild(new UiSelector()
		.className(android.view.View.class.getName())
		.index(0)).getChild(new UiSelector()
		.className(android.widget.LinearLayout.class.getName())
		.index(1)).getChild(new UiSelector()
		.className(android.widget.RelativeLayout.class.getName())
		.index(1)).getChild(new UiSelector()
		.className(android.widget.TextView.class.getName())
		.text(removed));
    	
    	base.trace("Trying to remove device named: " + removed);
    	
    	my_textView.swipeLeft(400);
     	
    	// click on wastebin
    	if(!base.tw_bin_rid.exists()){
    		base.tw_bin_rid.waitForExists(2000);
    	}
    	
    	base.tw_bin_rid.click();
            
    	assertFalse("Device '" + removed + "' has not been removed! Still in list.", removed_device_text.exists());

        // open NavDrawer menu
    	if(!base.ib_navDrawer_i.exists()){
        	sleep(5000);
        }
    	base.ib_navDrawer_i.click();
    	
    	// switch adapter
    	base.graphs.click();
    	
        // open NavDrawer menu
    	if(!base.ib_navDrawer_i.exists()){
        	sleep(5000);
        }
    	base.ib_navDrawer_i.click();
    	
    	// switch adapter
    	base.secondAdapterInMenu.click();
    	
    	// back to overview
    	base.overview.clickAndWaitForNewWindow();
    	
        // open NavDrawer menu
    	if(!base.ib_navDrawer_i.exists()){
        	sleep(5000);
        }
    	base.ib_navDrawer_i.click();
    	
    	// switch adapter
    	base.firstAdapterInMenu.click();
    	
    	// open NavDrawer menu
    	if(!base.ib_navDrawer_i.exists()){
        	sleep(5000);
        }
    	base.ib_navDrawer_i.click();

    	// TOOD comment after EditGateway implemeted
    	// open menu - nav drawer
    	if(!base.ib_navDrawer_i.exists()){
    		sleep(3000);
    	}
    	assertTrue("NavDrawer not clickable", base.ib_navDrawer_i.click());

		// adapter users
		// adapter long press
    	if(!base.firstAdapterInMenu.exists()){
    		sleep(3000);
    	}
    	base.firstAdapterInMenu.swipeRight(100);
		
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
		base.et_userEmail_rid.setText("kurovamartina@gmail.com");
		base.trace("after settext");
		
		// choose user role
		base.spi_userRole_rid.click();
		base.chtw_admin_t.click();
		
		// confirm
		base.b_addUser_rid.clickAndWaitForNewWindow();
		
		sleep(8000);
		
		// back
		base.ib_navDrawer_i.click();
    	
		// unregister adapter
		// adapter long press
		base.firstAdapterInMenu.swipeRight(100);
		if(!base.tw_adabin_rid.exists()){
    		sleep(3000);
    	}
		base.tw_adabin_rid.click();
		
		// back
		base.ib_navDrawer_i.click();

    	// open menu - nav drawer
    	if(!base.ib_navDrawer_i.exists()){
    		sleep(3000);
    	}
        assertTrue("NavDrawer not clickable", base.ib_navDrawer_i.click());
        
        base.iw_gamification_rid.clickAndWaitForNewWindow();
    	
    	// back
    	base.ib_navDrawer_i.click();
      	
    	// open menu - nav drawer
    	if(!base.ib_navDrawer_i.exists()){
    		sleep(3000);
    	}
        assertTrue("NavDrawer not clickable", base.ib_navDrawer_i.click());
        
        // scroll to watchdog and click on it
        base.tw_watchdog_t.clickAndWaitForNewWindow();
    
    	
    	// open menu - nav drawer
    	if(!base.ib_navDrawer_i.exists()){
    		sleep(3000);
    	}
        assertTrue("NavDrawer not clickable", base.ib_navDrawer_i.click());
        
        if(!base.tw_settings_t.exists()){
	        // scroll to settings and click on it
	    	assertTrue("Settings is not found", 
	        	new UiScrollable(
	        		new UiSelector().scrollable(true)
	            ).scrollIntoView(base.tw_settings_t));
        }
        base.tw_settings_t.clickAndWaitForNewWindow();
    	
    	// back
    	base.ib_navDrawer_i.click();
    	
    	// close
    	base.ib_navDrawer_i.click();
    	
    	// open menu - nav drawer
    	if(!base.ib_navDrawer_i.exists()){
    		sleep(3000);
    	}
        assertTrue("NavDrawer not clickable", base.ib_navDrawer_i.click());
        
        // scroll to about and click on it
    	assertTrue("About is not found", 
        	new UiScrollable(
        		new UiSelector().scrollable(true)
            ).scrollIntoView(base.tw_about_t));
    	base.tw_about_t.click();
       	// OK
    	
    	UiObject ok = new UiObject(new UiSelector().className(
    	            android.widget.Button.class.getName())
    	            .resourceId("android:id/button1")
    	            .text("OK")
    	            .index(1));
    	ok.click();
    	base.trace("OK");
    	this.getUiDevice().getInstance().pressBack();
    	base.trace("BACK");
       	
        // scroll to about and click on it
       	if(!base.tw_about_t.exists()){
	    	assertTrue("About is not found", 
	        	new UiScrollable(
	        		new UiSelector().scrollable(true)
	            ).scrollIntoView(base.tw_about_t));
       	}

    	base.tw_about_t.click();
    	
       	// wwww
       	assertTrue("Cannout find www", base.tw_www_t.exists());
    	base.tw_www_t.clickAndWaitForNewWindow();
    	
       	sleep(5000);
       	//assertTrue("fit.vutbr.com has not loaded", base.tw_browser_t.exists());
       	base.myDevice.getInstance().pressRecentApps();
       	base.tw_beeeOn_t.clickAndWaitForNewWindow();
       	base.b_ok_t.click();
 
       	base.trace("Skip email - not supported yet.");
       	
       	base.ib_navDrawer_i.click();
    	
      	// minimize
    	base.myDevice.getInstance().pressHome();
    	
    	// maximize
    	UiObject Applications = new UiObject(new UiSelector()
    		.description("Apps"));
        Applications.clickAndWaitForNewWindow();
        UiObject apps = new UiObject(new UiSelector()
        	.text("Apps"));
        //apps.click();
        UiScrollable ListOfapplications = new UiScrollable(new UiSelector().scrollable(true));
        UiObject BeeeOnApp = ListOfapplications.getChildByText(new UiSelector().className(
        		android.widget.TextView.class.getName()), "BeeeOn (debug)");
        BeeeOnApp.clickAndWaitForNewWindow();
    	
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
        
       // minimize
    	base.myDevice.getInstance().pressHome();
    	// base.myDevice.pressMenu();

        // verify
    	Applications.exists();

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
    	
    	// start BeeeOn app
        Applications.clickAndWaitForNewWindow();
        
        // emulator 
        //UiObject apps = new UiObject(new UiSelector().text("Apps"));
        //apps.click();
        
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
