//package com.rehivetech.beeeon;
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

/**
 * Start the video
 * @uthor Martina Kůrová
 */
public class Record extends UiAutomatorTestCase {

    // start button - for andoid < 5.0
   /* UiObject start =  new UiObject(new UiSelector()
            .className(android.widget.Button.class.getName())
            .text("Spustit"));*/

    // nav drawer menu / x button for close adcvertisements
    UiObject ib_navDrawer_i = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .index(0));

    // NAVIGATE UP
    UiObject navigate_up = new UiObject(new UiSelector().description("Navigate up"));
    UiObject tw_gifmaker_t = new UiObject(new UiSelector().className(
            android.widget.TextView.class.getName())
            .text("GIF maker"));

    // record button
    UiObject ib_record_i = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .index(1));
    /*UiObject ib_record_rid = new UiObject(new UiSelector().className(
            android.widget.ImageButton.class.getName())
            .resourseId("com.nll.screenrecorder:id/record"));*/
    // button start now 
    UiObject b_startNow_i = new UiObject(new UiSelector().className(
            android.widget.Button.class.getName())
            .index(1));
    /*UiObject b_startNow_rid = new UiObject(new UiSelector().className(
            android.widget.Button.class.getName())
            .resourceId("android:id/button1"));*/
    UiObject b_startNow_t = new UiObject(new UiSelector().className(
            android.widget.Button.class.getName())
            .text("Start now"));

    // ok button
    UiObject b_ok_t = new UiObject(new UiSelector().className(
            android.widget.Button.class.getName())
            .text("OK"));
    // rid: android:id/button1
    // index 0

	protected void setUp() throws Exception {
        super.setUp();
        Runtime.getRuntime().exec("am start -n com.nll.screenrecorder/com.nll.screenrecorder.activity.RouterActivity");
        sleep(3000);
    }

    // start recording
    public void testStart() throws RemoteException, UiObjectNotFoundException, Exception {

        if(b_ok_t.exists()){
            b_ok_t.click();
        }
        if(!ib_record_i.exists()){
            sleep(2000);
        }
        if(!ib_record_i.exists()){
            ib_navDrawer_i.click();
        }
        if(!ib_record_i.exists()){
            ib_navDrawer_i.click();
        }
	if(tw_gifmaker_t.exists()){
            getUiDevice().getInstance().pressBack();
        }   
        ib_record_i.clickAndWaitForNewWindow();
        if(b_startNow_i.exists()){
            b_startNow_i.click();
        }
        sleep(4000);

    }
    
    // stop recording
    public void testStop() throws RemoteException, UiObjectNotFoundException, Exception {

	if(b_ok_t.exists()){
            b_ok_t.click();
        }
        if(!ib_record_i.exists()){
            sleep(2000);
        }
        if(!ib_record_i.exists()){
            ib_navDrawer_i.click();
        }
        ib_record_i.clickAndWaitForNewWindow();
        //sleep(7000);
    }
}

