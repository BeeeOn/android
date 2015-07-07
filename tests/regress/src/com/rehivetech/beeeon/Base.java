package com.rehivetech.beeeon;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Rect;
import android.os.RemoteException;

import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.core.UiWatcher;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

public class Base extends UiAutomatorTestCase {

    /************************************************************************************
     * Objects definition
     ***********************************************************************************/
    // login google button
    public UiObject ib_loginGoogle_ridi = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .resourceId("com.rehivetech.beeeon.debug:id/login_btn_google")
            .index(0));

    // login mojeID button
    UiObject ib_loginMojeid_ridi = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .resourceId("com.rehivetech.beeeon.debug:id/login_btn_mojeid")
            .index(1));

    // demo button
    UiObject ib_demo_ridi = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .resourceId("com.rehivetech.beeeon.debug:id/login_btn_demo")
            .index(2));

    // Close
    UiObject b_close_rid =  new UiObject(new UiSelector()
		.className(android.widget.Button.class.getName())
		.resourceId("com.rehivetech.beeeon.debug:id/showcase_button"));
  
    // www
    UiObject tw_www_t =  new UiObject(new UiSelector()
            .className(android.widget.TextView.class.getName())
            .text("http://rehivetech.com/"));
    
    // browser title
    UiObject tw_browser_t =  new UiObject(new UiSelector()
	    .className(android.widget.EditText.class.getName())
	    .text("rehivetech.com")
	    //.text("www.fit.vutbr.cz")
	    .resourceId("com.android.chrome:id/url_bar"));
	    //.text("http://www.fit.vutbr.cz")
	    //.text("http://www.fit.vutbr.cz/ is not available"));
    
    // email 
    UiObject tw_email_t =  new UiObject(new UiSelector()
    .className(android.widget.TextView.class.getName())
    .text("info@BeeeOn.com"));
    
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
    UiObject tw_beeeOn_t = new UiObject(new UiSelector().className(
    		android.widget.TextView.class.getName())
    		.text("BeeeOn (debug)"));
    UiObject currentPackage = new UiObject(
            new UiSelector());
    // menu - nav drawer
    UiObject ib_navDrawer_i = new UiObject(new UiSelector().className(
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
	UiObject add3 =  new UiObject(new UiSelector()
		.className(android.widget.Button.class.getName())
		.resourceId("com.rehivetech.beeeon.debug:id/add_adapter_next")
		.index(1)
		.text("ADD"));
    // ADD user
    UiObject b_addUser_rid =  new UiObject(new UiSelector()
            .className(android.widget.Button.class.getName())
            .resourceId("com.rehivetech.beeeon.debug:id/add_user_adapter_save"));
    // SAVE
    UiObject b_save_t =  new UiObject(new UiSelector()
            .className(android.widget.Button.class.getName())
            .text("Save"));
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
    UiObject ib_fabMenu_rid = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .resourceId("com.rehivetech.beeeon.debug:id/fab"));
    // fab add device
    
    
    // fab add adapter
    UiObject fabAddAdapterButton = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .index(5));
    UiObject im_fabAddAdapter_i = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .index(3));
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
            .resourceId("com.rehivetech.beeeon.debug:id/add_user_gate_save"));    
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
    
 
    /**
     * UiWatcher 'anrWatcher' for applications ANR.
     * Checks whether a dialog box with "Application not responding" message appears.
     */     
    UiWatcher anrWatcher = new UiWatcher() {
    	
    	/**
    	 * Override method checkForCondition() that terminates the executions of tests
    	 * when 'application not responding' text appears in application being tested
    	 * and informs user about it.
    	 * @Override
    	 */
        public boolean checkForCondition() {
        	
            UiObject tw_anr_t = new UiObject(new UiSelector().className(
            		android.widget.TextView.class.getName())
            		.text("Unfortunately, BeeeOn (debug) has stopped."));
            
            if(tw_anr_t.exists()) {
                trace("Application not responding. Test will be terminated");
                Runtime.getRuntime().exit(0);
            }
            // TODO co to tu vracim ? true / false
            return false;
        }
    };

    /************************************************************************************
     * Methods definition
     ***********************************************************************************/    
  

    public void trace(String message){
        System.out.println("### " + message + " ###");
    }
    public void trace_start(String message){
        System.out.println("\n### " + message + " start ###");
    }
    public void trace_end(String message){
        System.out.println("### " + message + " end ###");
    }

    public void check_intro(){

    }
    
    public void check_google_allow() throws UiObjectNotFoundException{
    	// allow - ok	
    	b_ok_t.waitForExists(5000);
    	if(b_ok_t.exists()) b_ok_t.clickAndWaitForNewWindow();
    }

    public void check_no_account() throws UiObjectNotFoundException{
    	// create beeeon accout - OK
    	b_ok_t.waitForExists(5000);
    	if(b_ok_t.exists()) b_ok_t.clickAndWaitForNewWindow();
    }
    
    public void choose_an_account(){

    }
    
    public void confirm_google(){

    }
    
    public void click_through_add_gateway_guide() throws UiObjectNotFoundException{
    	
        // b: next
        UiObject b_next_u =  new UiObject(new UiSelector()
	    	.className(android.widget.Button.class.getName())
	    	.index(2)
	    	.resourceId("com.rehivetech.beeeon.debug:id/add_adapter_next")
	    	.text("NEXT")
	    	.text("Next")
	    	.text("next"));
        
        // clicking on NEXT button
	    assertTrue("'NEXT' button not found.", b_next_u.exists());
	    b_next_u.clickAndWaitForNewWindow();
	    assertTrue("'NEXT' button not found.", b_next_u.exists());
	    b_next_u.clickAndWaitForNewWindow();
	    assertTrue("'NEXT' button not found.", b_next_u.exists());
	    b_next_u.clickAndWaitForNewWindow();
    }
    
    /**
     * 
     * @throws UiObjectNotFoundException
     */
    public void swipe_through_add_gateway_guide() throws UiObjectNotFoundException{
    	
        // b: next
        UiObject iw_guide_i =  new UiObject(new UiSelector()
	    	.className(android.widget.ImageView.class.getName())
	    	.index(1));
        
        iw_guide_i.swipeLeft(70);
        iw_guide_i.swipeLeft(70);
        iw_guide_i.swipeLeft(70);
    }
    
    public void swipe_through_add_device_guide() throws UiObjectNotFoundException{
    	
        // b: next
        UiObject iw_guide_i =  new UiObject(new UiSelector()
	    	.className(android.widget.ImageView.class.getName())
	    	.index(1));
        
        iw_guide_i.swipeLeft(70);
        iw_guide_i.swipeLeft(70);
    }
    
    /**
     * Method for taking screenshot with timestamp
     * @param fileName
     * @throws IOException
     * @throws InterruptedException
     */
    public void takeScreenShot(String fileName) throws IOException, InterruptedException{
    	
        long xx = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
        Date resultdate = new Date(xx);
        String tstamp = sdf.format(resultdate);
        
    	Process ptakeScreenShot = Runtime.getRuntime().exec("screencap -p " + "/data/local/tmp/screenshots/" + fileName + "." + tstamp + ".png");
    	ptakeScreenShot.waitFor();
    }    
    
    public void add_accounts() throws UiObjectNotFoundException{
        
	    // email .address
	    UiObject et_email_i = new UiObject(new UiSelector().className(
	            android.widget.EditText.class.getName())
	            .index(0));
	    et_email_i.waitForExists(3000);
	    if(et_email_i.exists()) {
	    	et_email_i.clearTextField();
	    	et_email_i.setText("android.test.regress.1@gmail.com");
	    }
	    
	    // next
	    UiObject b_next_t = new UiObject(new UiSelector().className(
	            android.widget.Button.class.getName())
	            .text("Next"));
	    UiObject b_i = new UiObject(new UiSelector().className(
	            android.widget.Button.class.getName())
	            .index(0));
	    if(b_i.exists()) b_i.clickAndWaitForNewWindow();
	    
	    // passwd
	    UiObject et_passwd_i = new UiObject(new UiSelector().className(
	            android.widget.EditText.class.getName())
	            .index(0));
	    et_passwd_i.waitForExists(3000);
	    /*if(et_passwd_i.exists()) */
	    getUiDevice().getInstance().click(540, 924);
	    et_passwd_i.clearTextField();
	    et_passwd_i.setText("atr1atr1");
	    
	    // next
	    if(b_i.exists()) b_i.clickAndWaitForNewWindow();
	    
	    // accept Terms of service and Privacy Policy
	    UiObject b_accept_d = new UiObject(new UiSelector()
	    	.description("ACCEPT"));
	    b_i.waitForExists(3000);
	    if(b_i.exists()) {
	    	b_i.clickAndWaitForNewWindow();
	    	sleep(10000);
	    }
	    	    
	    // more
	    UiObject b_more_t = new UiObject(new UiSelector().className(
	    	android.widget.EditText.class.getName())
	    	.text("More"));
	    b_i.waitForExists(3000);
	    if(b_i.exists()) {
	    	b_i.clickAndWaitForNewWindow();
	    }
    	// next
    	if(b_i.exists()) b_next_t.clickAndWaitForNewWindow();
    	sleep(1000);
    
    }
    
    public void maximize() throws UiObjectNotFoundException, IOException, RemoteException {
    	
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
    	
    }    

}
