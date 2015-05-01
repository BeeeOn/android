package com.rehivetech.beeeon;
import java.io.IOException;

import android.os.RemoteException;

import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

// import org.testng.Assert;
// import org.testng.annotations.Test;

/**
 * Tests for GUI
 * @uthor Martina Kůrová
 */
public class TestDemo extends UiAutomatorTestCase {

    private String currentTestName;
    protected Boolean testFailed = true;
    private static final String TEST_APP_PKG = "com.rehivetech.beeeon.debug";
    private static final String START_LOGIN_ACTIVITY = "com.rehivetech.beeeon.activity.LoginActivity";
    private boolean intro = false;

    /* TODO nastavit vzdy na Default Screen - v pripade, ze by test objevil chybu
     * @Override(non-Javadoc)
     * @see com.android.uiautomator.testrunner.UiAutomatorTestCase#setUp()
	protected void setUp() throws Exception {
        super.setUp();
    }*/
    
	/* TODO nastavit vzdy na Default Screen - v pripade, ze by test objevi chybu
	 * @Override(non-Javadoc)
	 * @see com.android.uiautomator.testrunner.UiAutomatorTestCase#tearDown()
    protected void tearDown() throws Exception {
        //takeScreenshot("end");
        // Simulate a short press on the HOME button.
        getUiDevice().pressHome();
        super.tearDown();
    }*/

    /************************************************************************************
     * Objects definition
     ***********************************************************************************/    
    
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
    UiObject tw_beeeOn_t = new UiObject(new UiSelector().className(
    		android.widget.TextView.class.getName())
    		.text("BeeeOn (debug)"));
    UiObject currentPackage = new UiObject(
            new UiSelector());
    // google button
    UiObject googleImageButton = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .index(0));
    // TODO toto uz neplati
    // mojeID button
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
    // gamification
    UiObject iw_gamification_rid = new UiObject(new UiSelector().className(
    		android.widget.ImageView.class.getName())
            .resourceId("com.rehivetech.beeeon.debug:id/level"));
    // first adapter in menu
    UiObject firstAdapterInMenu = new UiObject(new UiSelector()
		.className(android.widget.LinearLayout.class.getName())
		.index(0));
    // second adapter in menu
    UiObject secondAdapterInMenu = new UiObject(new UiSelector()
		.className(android.widget.LinearLayout.class.getName())
		.index(1));
    // overview
    UiObject overview =  new UiObject(new UiSelector()
            .className(android.widget.TextView.class.getName())
            .text("Overview"));
    // graphs
    UiObject graphs =  new UiObject(new UiSelector()
	    .className(android.widget.TextView.class.getName())
	    .text("Graphs"));
    // watchdog
    UiObject tw_watchdog_t =  new UiObject(new UiSelector()
	    .className(android.widget.TextView.class.getName())
	    .text("Watchdog"));
    // settings
    UiObject tw_settings_t =  new UiObject(new UiSelector()
    .className(android.widget.TextView.class.getName())
    .text("Settings"));
    // about
    UiObject tw_about_t =  new UiObject(new UiSelector()
	    .className(android.widget.TextView.class.getName())
	    .text("About"));
    // wastebin - remove device
    UiObject tw_bin_rid =  new UiObject(new UiSelector()
	    .className(android.widget.TextView.class.getName())
	    .resourceId("com.rehivetech.beeeon.debug:id/sensor_menu_del"));
    // pencil - edit gateway
    UiObject tw_pencil_rid =  new UiObject(new UiSelector()
	    .className(android.widget.TextView.class.getName())
	    .resourceId("com.rehivetech.beeeon.debug:id/ada_menu_edit"));
    // users - userd of gateway
    UiObject tw_adausers_rid =  new UiObject(new UiSelector()
	    .className(android.widget.TextView.class.getName())
	    .resourceId("com.rehivetech.beeeon.debug:id/ada_menu_users"));
    // wastebin - delete gateway
    UiObject tw_adabin_rid =  new UiObject(new UiSelector()
	    .className(android.widget.TextView.class.getName())
	    .resourceId("com.rehivetech.beeeon.debug:id/ada_menu_del"));
    // device name text (TextView)
    UiObject senzorNameText =  new UiObject(new UiSelector()
            .className(android.widget.TextView.class.getName())
            .text("Karma"));
    // device name text (TextView)
    UiObject actorNameText =  new UiObject(new UiSelector()
            .className(android.widget.TextView.class.getName())
            .text("Televizor"));
 // device name text (TextView)
    UiObject actorNameText2 =  new UiObject(new UiSelector()
            .className(android.widget.TextView.class.getName())
            .text("Lampa u tv"));
    // actor switch
    UiObject sc_actor_i =  new UiObject(new UiSelector()
	    .index(1));
    UiObject sc_actor_rid =  new UiObject(new UiSelector()
    	.resourceId("com.rehivetech.beeeon.debug:id/sen_detail_value_switch")); 
    // On
    UiObject tw_on_i =  new UiObject(new UiSelector()
	    .className(android.widget.TextView.class.getName())
	    .text("On"));
    // Off
    UiObject tw_off_i =  new UiObject(new UiSelector()
	    .className(android.widget.TextView.class.getName())
	    .text("Off"));
    UiObject tw_onOff_rid =  new UiObject(new UiSelector()
    .className(android.widget.TextView.class.getName())
    .resourceId("com.rehivetech.beeeon.debug:id/sen_detail_value"));

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
    // ADD
    UiObject addButton =  new UiObject(new UiSelector()
            .className(android.widget.Button.class.getName())
            .text("ADD"));
    UiObject add =  new UiObject(new UiSelector()
		.className(android.widget.Button.class.getName())
		.resourceId("com.rehivetech.beeeon.debug:id/add_adapter_next"));
	UiObject add2 =  new UiObject(new UiSelector()
		.className(android.widget.Button.class.getName())
		.index(1));
    // ADD user
    UiObject b_addUser_rid =  new UiObject(new UiSelector()
            .className(android.widget.Button.class.getName())
            .resourceId("com.rehivetech.beeeon.debug:id/add_user_adapter_save"));
    // SAVE
    UiObject b_save_t =  new UiObject(new UiSelector()
            .className(android.widget.Button.class.getName())
            .text("SAVE"));
    UiObject b_save_rid =  new UiObject(new UiSelector()
		.className(android.widget.Button.class.getName())
		.resourceId("com.rehivetech.beeeon.debug:id/add_adapter_next"));
    // OK
    UiObject b_ok_t =  new UiObject(new UiSelector()
            .className(android.widget.Button.class.getName())
            .text("OK"));
    // Rate
    UiObject b_rate_t =  new UiObject(new UiSelector()
            .className(android.widget.Button.class.getName())
            .text("Rate"));
    // fab button - menu
    UiObject fabMenu = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .index(2));
    // fab add adapter
    UiObject fabAddAdapterButton = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .index(5));
    // fab add device
    UiObject fabAddDeviceButton = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .index(6));
    // fab add user
    UiObject b_fabAddUser_rid = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .resourceId("com.rehivetech.beeeon.debug:id/fab_add_user"));
    // adapter name
    UiObject nameAdapterEditText = new UiObject(new UiSelector().className(
            android.widget.EditText.class.getName())
            .index(1));
    // adapter id
    UiObject idAdapterEditText = new UiObject(new UiSelector().className(
            android.widget.EditText.class.getName())
            .index(2));
    // user email
    UiObject et_userEmail_rid = new UiObject(new UiSelector().className(
            android.widget.EditText.class.getName())
            .index(1));
            //.resourceId("rehivetech.beeeon.debug:id/add_user_email"));
    //
    UiObject toastGatewayActivated = new UiObject(new UiSelector()
    	.description("Gateway has been activated"));
    //
    UiObject toastGatewayRemoved = new UiObject(new UiSelector()
    	.className(android.widget.Toast.class.getName())
    	//.text("Gateway has been removed")
    	//.description("Gateway has been removed"));
        .resourceId("com.rehivetech.beeeon.debug:id/toast_adapter_removed"));
    // device title box - for swiping
    UiObject ll_deviceTitleBox_i =  new UiObject(new UiSelector()
	    .className(android.widget.LinearLayout.class.getName())
	    .index(0));
    // new devices layout
    UiObject ll_newDevices_i =  new UiObject(new UiSelector()
	    .className(android.widget.LinearLayout.class.getName())
	    .index(1));
    // new device name
    UiObject et_deviceName_rid = new UiObject(new UiSelector().className(
            android.widget.EditText.class.getName())
            .resourceId("com.rehivetech.beeeon.debug:id/setup_sensor_item_name"));
    UiObject et_deviceName_i = new UiObject(new UiSelector().className(
            android.widget.EditText.class.getName())
            .index(1));
    // new device location
    UiObject spi_newDevicelocation_rid = new UiObject(new UiSelector().className(
    		android.widget.Spinner.class.getName())
            .resourceId("com.rehivetech.beeeon.debug:id/addsensor_spinner_choose_location"));
    UiObject spi_newDevicelocation_i = new UiObject(new UiSelector().className(
            android.widget.Spinner.class.getName())
            .index(3));
    // user role
    UiObject spi_userRole_rid = new UiObject(new UiSelector().className(
    		android.widget.Spinner.class.getName())
            .resourceId("com.rehivetech.beeeon.debug:id/add_user_role"));
    // device added
    UiObject t_DeviceAdded_d = new UiObject(new UiSelector()
    	.description("New device was added"));
    // Menu button - guide
    UiObject ibBackGuide = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .index(0));
    // edit device
    // com.rehivetech.beeeon.debug:id/sen_detail_edit_fab
    UiObject ib_editDevice_i = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .index(1));
    UiObject ib_editDevice_rid = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .resourceId("com.rehivetech.beeeon.debug:id/sen_detail_edit_fab"));
    // renameDevice
    // com.rehivetech.beeeon.debug:id/sen_edit_name
    UiObject et_renameDevice_rid = new UiObject(new UiSelector().className(
            android.widget.EditText.class.getName())
            .resourceId("com.rehivetech.beeeon.debug:id/sen_edit_name"));
    UiObject et_renameDevice_i = new UiObject(new UiSelector().className(
            android.widget.EditText.class.getName())
            .index(1));
    // change location spinner
    // android.widget.Spinner
    // com.rehivetech.beeeon.debug:id/sen_edit_location
    UiObject spi_changeDevicelocation_rid = new UiObject(new UiSelector().className(
    		android.widget.Spinner.class.getName())
            .resourceId("com.rehivetech.beeeon.debug:id/sen_edit_location"));
    UiObject spi_changeDevicelocation_i = new UiObject(new UiSelector().className(
            android.widget.Spinner.class.getName())
            .index(1));
    // android.widget.CheckedTextView
    // com.rehivetech.beeeon.debug:id/custom_spinner_dropdown_label
    /*NOT SPEFICIF
     * UiObject chtw_changeDevicelocation_rid = new UiObject(new UiSelector().className(
    		android.widget.CheckedTextView.class.getName())
            .resourceId("com.rehivetech.beeeon.debug:id/custom_spinner_dropdown_label"));*/
    UiObject chtw_gardenDevicelocation_t = new UiObject(new UiSelector().className(
    		android.widget.CheckedTextView.class.getName())
            .text("Garden"));
    UiObject chtw_bedroomDevicelocation_t = new UiObject(new UiSelector().className(
    		android.widget.CheckedTextView.class.getName())
            .text("Bedroom"));
    // device refresh time
    // android.widget.SeekBar
    // com.rehivetech.beeeon.debug:id/sen_edit_refreshtime
    UiObject sb_changeDevicelocation_rid = new UiObject(new UiSelector().className(
    		android.widget.SeekBar.class.getName())
            .resourceId("com.rehivetech.beeeon.debug:id/sen_edit_refreshtime"));
    UiObject sb_changeDevicelocation_i = new UiObject(new UiSelector().className(
    		android.widget.SeekBar.class.getName())
            .index(1));
    // Save
    UiObject tw_save_rid =  new UiObject(new UiSelector()
	    .className(android.widget.TextView.class.getName())
	    .resourceId("com.rehivetech.beeeon.debug:id/action_save"));
    UiObject tw_save_t =  new UiObject(new UiSelector()
	    .className(android.widget.TextView.class.getName())
	    .text("Save"));
    UiObject tw_save_i =  new UiObject(new UiSelector()
	    .className(android.widget.TextView.class.getName())
	    .index(0));
    // logout button
    UiObject logout = new UiObject(new UiSelector().description("Logout"));
    // www rehivetech
    UiObject tw_www_t =  new UiObject(new UiSelector()
	    .className(android.widget.TextView.class.getName())
	    .text("http://rehivetech.com/"));
    UiObject tw_browser_t =  new UiObject(new UiSelector()
	    .className(android.widget.EditText.class.getName())
	    .resourceId("com.android.browser:id/url")
	    .text("rehivetech.com"));
    // @ rehivetech
    UiObject tw_email_t =  new UiObject(new UiSelector()
 	    .className(android.widget.TextView.class.getName())
 	    .text("info@BeeeOn.com"));
    // Owner
    UiObject chtw_owner_t =  new UiObject(new UiSelector()
	    .className(android.widget.CheckedTextView.class.getName())
	    .text("Owner")); 
    // Admin
    UiObject chtw_admin_t =  new UiObject(new UiSelector()
	    .className(android.widget.CheckedTextView.class.getName())
	    .text("Admin"));
    // User
    UiObject chtw_user_t =  new UiObject(new UiSelector()
	    .className(android.widget.CheckedTextView.class.getName())
	    .text("User"));
    // Guest
    UiObject chtw_guest_t =  new UiObject(new UiSelector()
	    .className(android.widget.CheckedTextView.class.getName())
	    .text("Guest"));

    
    /************************************************************************************
     * Actual Tests starts here
     ***********************************************************************************/ 

    /**
     * Launch BeeeOn Application and validate BeeeOn App by it's package name
     * @throws IOException 
     */
    //@Test	// for testNG
    public void test_00_LaunchApp() throws RemoteException, UiObjectNotFoundException, IOException {

        setCurrentTestName("testLaunchTestApp");
        trace_start(this.currentTestName);
        // good to start with this
        getUiDevice().pressHome();
        Runtime.getRuntime().exec("am start -n com.rehivetech.beeeon.debug/com.rehivetech.beeeon.activity.LoginActivity");
        
        if(!beeeonValidation.exists()){
        	sleep(5000);
        }
        assertTrue("Unable to detect BeeeOn", beeeonValidation.exists());
        trace_end(this.currentTestName);
    }
    
    //@Test	// for testNG
    public void test_01_Intro() throws UiObjectNotFoundException, Exception {
    	
    	setCurrentTestName("testIntro");
    	trace_start(this.currentTestName);
    	
        if(nextButton.exists()) {
        	
        	intro = true;

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
        else{
        	trace("No intro started.");
        }
        trace_end(this.currentTestName);
    }

    //@Test	// for testNG
    public void test_02_startDemoMode() throws UiObjectNotFoundException {
    	
    	setCurrentTestName("testStartDemoMode");
    	trace_start(this.currentTestName);
        // click on demo button
        assertTrue("Demo Button not found", demoImageButton.exists());
        demoImageButton.clickAndWaitForNewWindow(2000);
        //assertTrue("No new window", demoImageButton.clickAndWaitForNewWindow());
        trace_end(this.currentTestName);
    }

    //@Test	// for testNG
    public void test_03_AddAdapter() throws Throwable {

    	setCurrentTestName("testAddAdapter");
    	trace_start(this.currentTestName);
    	
        // sometimes a big fullscreen TextView appears
    	if(!fabMenu.exists()){ 
	    	// open menu - nav drawer
	        //assertTrue("Menu (NavDrawer) Button not found", menuNavDrawerButton.waitForExists(3000));
	        menuNavDrawerButton.waitForExists(3000);
	        menuNavDrawerButton.click();
	
	        // select "overview"
	        assertTrue("Overview field is not found", overview.exists());
	        overview.clickAndWaitForNewWindow();
    	}
    	
        // open fab menu
    	fabMenu.waitForExists(2000);
        fabMenu.click();

        // add adapter
        assertTrue("Fab menu item - Add adapter - not found", fabAddAdapterButton.exists());
        fabAddAdapterButton.clickAndWaitForNewWindow();

        // next
        UiObject nextButton1 =  new UiObject(new UiSelector()
	        .className(android.widget.Button.class.getName())
	        .text("NEXT"));
        UiObject nextButton2 =  new UiObject(new UiSelector()
	        .className(android.widget.Button.class.getName())
	        .text("next"));
        UiObject nextButton3 =  new UiObject(new UiSelector()
        	.className(android.widget.Button.class.getName())
        	.text("Next"));
        UiObject nextButton4 =  new UiObject(new UiSelector()
        	.className(android.widget.Button.class.getName())
        	.resourceId("com.rehivetech.beeeon.debug:id/add_adapter_next"));
        
//        nextButton1.click();
//        nextButton2.click();
//        nextButton3.click();
//        nextButton4.click();
        
        // TODO UiAutomator - swipe left
        //myDevice.swipe(0, 5, 5, 5, 1);
        
        // guide
        // TODO try SKIP, Cancel
        skipButton.click();
        //go_through_add_adapter_guide();

        // input
        String name = "Home";
        String id = "63214";

        nameAdapterEditText.setText(name);
        idAdapterEditText.setText(id);

        // confirm
        //UiObject xx =  new UiObject(new UiSelector() getObjectByText("ADD").clickAndWaitForNewWindow();

        assertTrue("Add button dos not exists.", add.exists());
        // TODO workaround - until the bug has been repaired
        if(!add.clickAndWaitForNewWindow()){
        	ibBackGuide.click();
            //if(ibBackGuide.exists()) assertTrue("not possible to go back", ibBackGuide.click());
            sleep(2000);
        	///assertTrue("Error Cancel button!", cancelButton.clickAndWaitForNewWindow());
        }
       
//        assertTrue("Error add button2!", add.clickAndWaitForNewWindow());
//        this.sleep(3000);
//        
//        assertTrue("Toast message dos not appear.", toastGatewayActivated.exists());
//        assertTrue("Toast message dos not appear.", toastGatewayActivated.waitForExists(2));
//
//        //addButton.clickAndWaitForNewWindow();
//
//        // verify that device was added
//        UiObject result = new UiObject(new UiSelector().className(
//                android.widget.LinearLayout.class.getName()).childSelector(
//                (new UiSelector().className(android.widget.ScrollView.class.getName())
//                        .childSelector(new UiSelector().className(android.widget.TextView.class
//                                .getName())))));
//        if (!testText.equals(result.getText())) {
//            throw new UiObjectNotFoundException("Test text: " + testText);
//        }

        trace_end(this.currentTestName);
    }
    
    //@Test	// for testNG
    public void test_03_AddDevice() throws UiObjectNotFoundException {
    	
    	setCurrentTestName("testAddDevice");
    	trace_start(this.currentTestName);

    	 // sometimes a big fullscreen TextView appears
    	if(!fabMenu.exists()){ 
	    	// open menu - nav drawer
	        assertTrue("Menu (NavDrawer) Button not found", menuNavDrawerButton.waitForExists(2000));
	        menuNavDrawerButton.click();
	
	        // select "overview"
	        assertTrue("Overview field is not found", overview.exists());
	        overview.clickAndWaitForNewWindow();
    	}
    	
        // open fab menu
    	fabMenu.waitForExists(2000);
        fabMenu.click();
       
        // add device
        assertTrue("Fab menu item - Add device - not found", fabAddDeviceButton.exists());
        fabAddDeviceButton.clickAndWaitForNewWindow();
        
        skipButton.clickAndWaitForNewWindow();
        
// go through the guide       
//        assertTrue("nextButton - not found", nextButton.clickAndWaitForNewWindow());
//        assertTrue("addButton - not found", add.clickAndWaitForNewWindow());
//        
//        UiObject forSwiping = new UiObject(new UiSelector()
//	    	.className(android.widget.ImageView.class.getName())
//			.index(1));
//        forSwiping.swipeLeft(1);

        // guide 
        // TODO check guide
        // assertTrue("NEXT button not found.", nextButton.exists());
        // nextButton.clickAndWaitForNewWindow();
        // assertTrue("NEXT button not found.", nextButton.exists());
        // nextButton.clickAndWaitForNewWindow();
        // assertTrue("NEXT button not found.", nextButton.exists());
        // nextButton.clickAndWaitForNewWindow();

        // input
        sleep(10000);
        // search sezor name atd...
        if(et_deviceName_rid.exists()){
        	
        	
//        	UiObject ll_newDevices_i =  new UiObject(new UiSelector()
//	    	    .className(android.widget.LinearLayout.class.getName())
//	    	    .index(1));
//        	
//        	// number of devices ?
//        	UiObject parent = new UiObject(new UiSelector()
//	    	    .className(android.widget.LinearLayout.class.getName())
//	    	    .index(1));
//        		    int c = parent.getChildCount();
//        		    for(int i = 0; i < c; i++) {
//        		        UiObject eachItem = parent.getChild(new UiSelector().index(i));
//        		        eachItem.setText("New deivce " + i);
//        		    }
        	
        		    
		    UiObject object = new UiObject(new UiSelector()
	    	    .className(android.widget.LinearLayout.class.getName())
	    	    .index(1));
	    		int cnt = object.getChildCount();
	    		for(int i = 0; i < cnt; i++) {
	    		    UiObject eachItem = object.getChild(new UiSelector().index(i)).getChild(new UiSelector()
			    	    .className(android.widget.EditText.class.getName())
			    	    .index(1));
	    		    eachItem.setText("New device " + cnt);
	    		}
	    		    
	    		    //com.rehivetech.beeeon.debug:id/setup_sensor_item_name
		    
        	
            // OR
//        		   
//		    int x = 0;
//		    while(new UiObject(new UiSelector()
//    	    .className(android.widget.LinearLayout.class.getName())
//    	    .index(x))
//		    .exists()){
//		    	UiObject device = new UiObject(new UiSelector()
//	    	    .className(android.widget.LinearLayout.class.getName())
//	    	    .index(x));
//		    	
//		    	device.setText("New device " + x);
//		    	x++;
//		    }
        		            	
        	// set name
        	// et_deviceName_rid.setText("New device");
        	// set location
        	spi_newDevicelocation_rid.click();
        	chtw_bedroomDevicelocation_t.click();
        	// save
        	b_save_t.click();
        	// verify toast
        	assertTrue("No toast message.", t_DeviceAdded_d.waitForExists(1000));
        	
        }
        else{
	        ibBackGuide.click();
	        sleep(2000);
        }
        

        // confirm
    	trace_end(this.currentTestName);
        
    }
    //@Test	// for testNG
    public void test_04_DeviceManagement() throws UiObjectNotFoundException {

    	setCurrentTestName("testDeviceManagement");
    	trace_start(this.currentTestName);
    	
    	// open nav Drawer
    	assertTrue("Menu (NavDrawer) Button not found", menuNavDrawerButton.exists());
        menuNavDrawerButton.click();
    	
    	//
    	firstAdapterInMenu.click();  
    	
    	// close Nav Drawer
    	assertTrue("Menu (NavDrawer) Button not found", menuNavDrawerButton.exists());
        menuNavDrawerButton.click();
    	
    	// scroll to device name
    	// senzor: "Karma"
    	// actor: "Televizor"
        assertTrue("Device text filed named " + senzorNameText.toString() + " is not found", 
                new UiScrollable(
                    new UiSelector().scrollable(true)
                ).scrollIntoView(senzorNameText));
    	
        // show device detail
        senzorNameText.clickAndWaitForNewWindow();
        
        if(!ib_editDevice_i.exists()){
        	sleep(5000);
        }
        ib_editDevice_i.click();
        
        // editing
        //ib_editDevice_i.waitForExists(2000);
        //assertTrue("No fab for edit device.", ib_editDevice_i.clickAndWaitForNewWindow());

        // rename device
        if(!et_renameDevice_rid.exists()){
        	sleep(5000);
        }
        assertTrue("Cannot rename device.", et_renameDevice_rid.exists());
        et_renameDevice_rid.clearTextField();
        assertTrue("Cannot rename device.", et_renameDevice_rid.setText("Renamed Karma"));
        
        // change device location
        spi_changeDevicelocation_rid.click();
        chtw_gardenDevicelocation_t.click();
        
        // change refresh time
//        sb_changeDevicelocation_rid
        UiScrollable refreshTime = new UiScrollable(new UiSelector().resourceId("com.rehivetech.beeeon.debug:id/sen_edit_refreshtime"));
        refreshTime.swipeRight(2);
        
        // save
        assertTrue("Cannot find save button.", tw_save_t.exists());
        tw_save_t.clickAndWaitForNewWindow();
        
        // back
        menuNavDrawerButton.click();
        
        trace_end(this.currentTestName);
    }

    //@Test	// for testNG
    public void test_05_SwitchActuator() throws UiObjectNotFoundException {

    	setCurrentTestName("testSwitchActuator");
    	trace_start(this.currentTestName);

    	// scroll to device name
    	// actor: "Televizor"
        assertTrue("Device text filed named " + actorNameText.toString() + " is not found", 
                new UiScrollable(
                    new UiSelector().scrollable(true)
                ).scrollIntoView(actorNameText));
    	
        // show device detail
        actorNameText.clickAndWaitForNewWindow();
        
        sleep(2000);
        
        String actorState = new UiObject(new UiSelector().className(
        		android.widget.TextView.class.getName()).index(0)).getText();
        
        // change aktor state
        sc_actor_rid.click();
        
        sleep(1000);
        
        // verify
        if(actorState.equals("On")){
        	assertTrue("Unable to switch actuator.", tw_onOff_rid.getText().equals("Off"));
        	//tw_off_i.exists());
        }
        else{
        	assertTrue("Unable to switch actuator.", tw_onOff_rid.getText().equals("On"));
        }    	
        
    	trace_end(this.currentTestName);
    }
    
    //@Test	// for testNG
    public void test_06_ScrollThroughDeviceList() throws UiObjectNotFoundException {

    	setCurrentTestName("testScrollThroughDeviceList");
    	trace_start(this.currentTestName);
        
    	UiScrollable v_deviceSwipeBox_rid =  new UiScrollable(new UiSelector()
			.className(android.view.View.class.getName())
			.resourceId("com.rehivetech.beeeon.debug:id/swipe_container"));
			//.scrollable(true)
    	
    	v_deviceSwipeBox_rid.swipeLeft(5);
    	v_deviceSwipeBox_rid.swipeRight(3);
    	
        // back
        menuNavDrawerButton.clickAndWaitForNewWindow();
        
        trace_end(this.currentTestName);
    }
    
  //@Test	// for testNG
    public void test_07_UnregisterFacility() throws UiObjectNotFoundException {

    	setCurrentTestName("testUnregisterFacility");
    	trace_start(this.currentTestName);
    	
    	assertTrue("Device text filed named " + actorNameText2.toString() + " is not found", 
                 new UiScrollable(
                     new UiSelector().scrollable(true)
                 ).scrollIntoView(actorNameText2));
    	
    	UiObject listView =  new UiObject(new UiSelector()
			.className(android.widget.ListView.class.getName())
			.index(0));
    	
    	UiObject item = listView.getChild(new UiSelector()
    			.className(android.view.View.class.getName())
    			.index(4).longClickable(true));
    	
    	// long press
    	//item.longClick();
    	myDevice.getInstance().swipe(540, 1000, 540, 1000, 400);
    	//actorNameText2.lo
    	
    	// click on wastebin
    	if(!tw_bin_rid.exists()){
    		tw_bin_rid.waitForExists(2000);
    	}
    	
    	// TODO automation -> report BeeeOn ANR !!! then comment NavDrawer, uncomment the rest
    	menuNavDrawerButton.click();
//    	tw_bin_rid.click();
//
//    	// verify that facily has been removed
//    	assertFalse("Device " + actorNameText2.toString() + " has not been removed!", 
//                new UiScrollable(
//                    new UiSelector().scrollable(true)
//                ).scrollIntoView(actorNameText2));
//    	
    	trace_end(this.currentTestName);
    }    

    //@Test	// for testNG
    public void test_08_OpenGraphs() throws UiObjectNotFoundException {
    
    	setCurrentTestName("testOpenGraphs");
    	trace_start(this.currentTestName);
    	
        // open NavDrawer menu
    	if(!menuNavDrawerButton.exists()){
        	sleep(5000);
        }
    	menuNavDrawerButton.click();
    	
    	// switch adapter
    	graphs.click();
    	
    	// TODO verify
    	
    	// scroll up and down
    	
    	trace_end(this.currentTestName);
    }
    
    //@Test	// for testNG
    public void test_09_SwitchGateway() throws UiObjectNotFoundException {
    
    	setCurrentTestName("testSwitchGateway");
    	trace_start(this.currentTestName);
    	
        // open NavDrawer menu
    	if(!menuNavDrawerButton.exists()){
        	sleep(5000);
        }
    	menuNavDrawerButton.click();
    	
    	// switch adapter
    	secondAdapterInMenu.click();
    	
    	// TODO verify
    	
    	// back to overview
    	overview.clickAndWaitForNewWindow();
    	
    	trace_end(this.currentTestName);
    }
    
    //@Test	// for testNG
    public void test_10_editGateway() throws UiObjectNotFoundException {
    
    	setCurrentTestName("testEditGateway");
    	trace_start(this.currentTestName);
    	
    	// open menu - nav drawer
//    	if(!menuNavDrawerButton.exists()){
//    		sleep(3000);
//    	}
//    	assertTrue("NavDrawer not clickable", menuNavDrawerButton.click());
      	  	
    	// edit adapter
    	// adapter long press
   		//firstAdapterInMenu.swipeRight(100);
   		// TODO tw_pencil_rid.click();
    	trace("Skip testEditAdapter - not implemeted yet.");
    	
    	// back
    	//menuNavDrawerButton.click();
    	
    	trace_end(this.currentTestName);
    }
    
    //@Test	// for testNG
    public void test_11_addUserToGateway() throws UiObjectNotFoundException {
    
    	setCurrentTestName("testAddUserToGateway");
    	trace_start(this.currentTestName);
    	
    	// TOOD comment after EditGateway implemeted
    	// open menu - nav drawer
    	if(!menuNavDrawerButton.exists()){
    		sleep(3000);
    	}
    	assertTrue("NavDrawer not clickable", menuNavDrawerButton.click());

		// adapter users
		// adapter long press
    	if(!firstAdapterInMenu.exists()){
    		sleep(3000);
    	}
    	firstAdapterInMenu.swipeRight(100);
		
    	trace("swiping");
    	sleep(2000);
    	trace("searching users");
    	
		tw_adausers_rid.waitForExists(3000);
		tw_adausers_rid.clickAndWaitForNewWindow();
		
		// add user icon
		b_fabAddUser_rid.clickAndWaitForNewWindow();
		
		// fill user email
		if(!et_userEmail_rid.exists()){
    		sleep(3000);
    		trace("sleeping - waiting for editText");
    	}
		et_userEmail_rid.clearTextField();
		et_userEmail_rid.setText("kurovamartina@gmail.com");
		trace("after settext");
		
		// choose user role
		spi_userRole_rid.click();
		chtw_admin_t.click();
		
		// confirm
		b_addUser_rid.clickAndWaitForNewWindow();
		
		sleep(8000);
		
		// back
		menuNavDrawerButton.click();
		// close
		//menuNavDrawerButton.click();
		
		trace_end(this.currentTestName);
	}

    //@Test	// for testNG
    public void test_12_unregisterGateway() throws UiObjectNotFoundException {
    
		setCurrentTestName("testUnregisterGateway");
		trace_start(this.currentTestName);
	  	  		
		// open menu - nav drawer
//    	if(!menuNavDrawerButton.exists()){
//    		sleep(3000);
//    	}
//    	assertTrue("NavDrawer not clickable", menuNavDrawerButton.click());
    	
		// unregister adapter
		// adapter long press
    	firstAdapterInMenu.swipeRight(100);
		if(!tw_adabin_rid.exists()){
    		sleep(3000);
    	}
		tw_adabin_rid.click();
		
		// TODO Toasts
		//assertTrue("Toast message dos not appear.", toastGatewayRemoved.waitForExists(3000));
		
		// back
		menuNavDrawerButton.click();
		
		trace_end(this.currentTestName);
	}

    //@Test	// for testNG
    public void test_90_Gamification() throws UiObjectNotFoundException {
    
    	setCurrentTestName("testGamification");
    	trace_start(this.currentTestName);
      	
    	// open menu - nav drawer
    	if(!menuNavDrawerButton.exists()){
    		sleep(3000);
    	}
        assertTrue("NavDrawer not clickable", menuNavDrawerButton.click());
        
    	iw_gamification_rid.clickAndWaitForNewWindow();
    	
    	// TODO
    	
    	// back
    	menuNavDrawerButton.click();
    	
    	trace_end(this.currentTestName);
    }
    
    //@Test	// for testNG
    public void test_91_Watchdog() throws UiObjectNotFoundException {
    
    	setCurrentTestName("testWatchdog");
    	trace_start(this.currentTestName);
      	
    	// open menu - nav drawer
    	if(!menuNavDrawerButton.exists()){
    		sleep(3000);
    	}
        assertTrue("NavDrawer not clickable", menuNavDrawerButton.click());
        
        // scroll to watchdog and click on it
//        if(!tw_watchdog_t.exists()){
//	        	new UiScrollable(
//	        		new UiSelector().scrollable(true)
//	            ).scrollIntoView(tw_watchdog_t);
//        }
    	tw_watchdog_t.clickAndWaitForNewWindow();
    	
        // TODO 
    	
    	// back
    	//assertTrue("NavDrawer not clickable", menuNavDrawerButton.click());
    	
    	trace_end(this.currentTestName);
    }
    
    //@Test	// for testNG
    public void test_92_Settings() throws UiObjectNotFoundException {
    	
    	setCurrentTestName("testSettingss");
    	trace_start(this.currentTestName);
    	
    	// open menu - nav drawer
    	if(!menuNavDrawerButton.exists()){
    		sleep(3000);
    	}
        assertTrue("NavDrawer not clickable", menuNavDrawerButton.click());
        
//        if(!tw_settings_t.exists()){
//	        // scroll to settings and click on it
//	    	assertTrue("Settings is not found", 
//	        	new UiScrollable(
//	        		new UiSelector().scrollable(true)
//	            ).scrollIntoView(tw_settings_t));
//        }
    	tw_settings_t.clickAndWaitForNewWindow();
    	
    	// TODO 
    	
    	// user profile
    	
    	// timezone
    	
    	// units
    	
    	// geofence areas
    	
    	// back
    	menuNavDrawerButton.click();
    	
    	// close
    	menuNavDrawerButton.click();
    	
    	trace_end(this.currentTestName);
    }
    //@Test	// for testNG
    public void test_93_About() throws UiObjectNotFoundException, RemoteException {
    	
    	setCurrentTestName("testAbout");
    	trace_start(this.currentTestName);
    	
    	// open menu - nav drawer
    	if(!menuNavDrawerButton.exists()){
    		sleep(3000);
    	}
        assertTrue("NavDrawer not clickable", menuNavDrawerButton.click());
        
        // scroll to about and click on it
    	assertTrue("About is not found", 
        	new UiScrollable(
        		new UiSelector().scrollable(true)
            ).scrollIntoView(tw_about_t));
       	tw_about_t.click();
       	// OK
       	b_ok_t.click();
       	
       	// scroll to about and click on it
//    	assertTrue("About is not found", 
//        	new UiScrollable(
//        		new UiSelector().scrollable(true)
//            ).scrollIntoView(tw_about_t));
//       	tw_about_t.click();
       	// TODO Rate
       	trace("Skip Rate - ANR!!!");
       	// b_rate_t.click();
       	
    	// scroll to about and click on it
    	assertTrue("About is not found", 
        	new UiScrollable(
        		new UiSelector().scrollable(true)
            ).scrollIntoView(tw_about_t));
       	tw_about_t.click();
       	// wwww
       	tw_www_t.clickAndWaitForNewWindow();
       	sleep(10000);
       	assertTrue("rehivetech.com has not loaded", tw_browser_t.exists());
       	myDevice.getInstance().pressRecentApps();
       	tw_beeeOn_t.clickAndWaitForNewWindow();
       	b_ok_t.click();
 
    	// scroll to about and click on it
//    	assertTrue("About is not found", 
//        	new UiScrollable(
//        		new UiSelector().scrollable(true)
//            ).scrollIntoView(tw_about_t));
//       	tw_about_t.click();
       	// TODO email
       	trace("Skip email - not supported yet.");
       	//tw_email_t.click();
       	
       	menuNavDrawerButton.click();
    	
    	trace_end(this.currentTestName);
    }
 
    public void test_94_testMinMax() throws UiObjectNotFoundException{
 
    	setCurrentTestName("testMinMax");
    	trace_start(this.currentTestName);
    	
      	// minimize
    	myDevice.getInstance().pressHome();
    	
//    	TODO minimize pres button Menu
//    	myDevice.pressMenu();
    	
    	// maximize
    	UiObject Applications = new UiObject(new UiSelector()
    		.description("Apps"));
        Applications.clickAndWaitForNewWindow();
        UiObject apps = new UiObject(new UiSelector()
        	.text("Apps"));
        apps.click();
        UiScrollable ListOfapplications = new UiScrollable(new UiSelector().scrollable(true));
        UiObject BeeeOnApp = ListOfapplications.getChildByText(new UiSelector().className(
        		android.widget.TextView.class.getName()), "BeeeOn (debug)");
        BeeeOnApp.clickAndWaitForNewWindow();
    	
    	trace_end(this.currentTestName);
    }
    
    //@Test	// for testNG
    public void test_95_Logout() throws UiObjectNotFoundException {
    	
    	setCurrentTestName("testLogout");
    	trace_start(this.currentTestName);
    	
    	// open menu - nav drawer
    	if(!menuNavDrawerButton.exists()){
    		sleep(4000);
    	}
        assertTrue("NavDrawer not clickable", menuNavDrawerButton.click());
        
        if(!logoutText.exists()){
	        // scroll to logout
	    	assertTrue("Logout is not found", 
	        	new UiScrollable(
	        		new UiSelector().scrollable(true)
	            ).scrollIntoView(logoutText));
        }
        logoutText.clickAndWaitForNewWindow();
        
        // verify
        if(!demoImageButton.exists()){
    		sleep(5000);
    	}
        assertTrue("Failed logout from BeeeOn application!", demoImageButton.exists());
        
        trace_end(this.currentTestName);
    }
 
    //@Test	// for testNG
    public void test_96_testMinimize() throws UiObjectNotFoundException {
    
    	setCurrentTestName("testMinimize");
    	trace_start(this.currentTestName);
       // minimize
    	myDevice.getInstance().pressHome();
    	// myDevice.pressMenu();

        // verify
    	UiObject Applications = new UiObject(new UiSelector()
		.description("Apps"));
    	Applications.exists();
    	
    	trace_end(this.currentTestName);
    }
    
    //@Test	// for testNG
    public void test_97_testMaximize() throws UiObjectNotFoundException, IOException, RemoteException {
    
    	setCurrentTestName("testMaximize");
    	trace_start(this.currentTestName);
    	
        // maximize
    	//myDevice.getInstance().pressMenu();
    	//myDevice.getInstance().pressKeyCode(0x139);
    	myDevice.getInstance().pressRecentApps();
    	
    	if(!tw_beeeOn_t.exists()){
    		sleep(5000);
    	}
    	
    	if(!tw_beeeOn_t.exists()){
//    		myDevice.getInstance().pressHome();
//        	myDevice.getInstance().pressMenu();
    		sleep(5000);
    	}
    	
    	tw_beeeOn_t.clickAndWaitForNewWindow();
    	
    	if(!beeeonValidation.exists()){
    		sleep(5000);
    	}
    	
        // verify
    	assertTrue("Unable to detect BeeeOn", beeeonValidation.exists());
    	
    	trace_end(this.currentTestName);
    }    
    
    /**
     * Test exit application.
     * @throws UiObjectNotFoundException
     */
    //@Test	// for testNG
    public void test_98_ExitApp() throws UiObjectNotFoundException {
    
    	setCurrentTestName("testExitApp");
    	trace_start(this.currentTestName);
    	
        // exit
    	myDevice.getInstance().pressBack();
    	
    	if(!AppTrayButton.exists()){
    		myDevice.getInstance().pressBack();
    		sleep(5000);
    	}
    	
    	if(!AppTrayButton.exists()){
    		myDevice.getInstance().pressBack();
    		sleep(5000);
    	}

        // verify that application exits
    	assertTrue("BeeOn app doesnt exit properly.", AppTrayButton.exists());
    	
    	trace_end(this.currentTestName);
    }
    
    public void test_99_introForTheSecondTime() throws IOException, UiObjectNotFoundException{
    	
    	setCurrentTestName("testIntroForTheSecondTime");
    	
    	// start BeeeOn app
    	UiObject Applications = new UiObject(new UiSelector().description("Apps"));
        Applications.clickAndWaitForNewWindow();
        UiObject apps = new UiObject(new UiSelector().text("Apps"));
        apps.click();
        UiScrollable ListOfapplications = new UiScrollable(new UiSelector().scrollable(true));
        UiObject BeeeOnApp = ListOfapplications.getChildByText(new UiSelector().className(android.widget.TextView.class.getName()), "BeeeOn (debug)");
        BeeeOnApp.clickAndWaitForNewWindow();

        // assert that no intro appear this time
        assertFalse("Intro started for the second time.", nextButton.exists());
        
        // exit
    	getUiDevice().pressBack();
    	
    	trace_end(this.currentTestName);
    }

    
    
    /************************************************************************************
     * Methods definition
     ***********************************************************************************/    
    
    private void setCurrentTestName(String testName) {
        this.currentTestName = testName;
        //takeScreenshot("start");
    }

    protected void trace(String message){
        System.out.println("### " + message + " ###");
    }
    protected void trace_start(String message){
        System.out.println("\n### " + message + " start ###");
    }
    protected void trace_end(String message){
        System.out.println("### " + message + " end ###\n");
    }

    public void check_intro(){

    }

    public void check_no_account(){

    }
    
    public void choose_from_accounts(){

    }
    
    public void confirm_google(){

    }
    
    public void go_through_add_adapter_guide() throws UiObjectNotFoundException{
	    assertTrue("NEXT button not found.", nextButton.exists());
	    nextButton.clickAndWaitForNewWindow();
	    assertTrue("NEXT button not found.", nextButton.exists());
	    nextButton.clickAndWaitForNewWindow();
	    assertTrue("NEXT button not found.", nextButton.exists());
	    nextButton.clickAndWaitForNewWindow();
    }
}
