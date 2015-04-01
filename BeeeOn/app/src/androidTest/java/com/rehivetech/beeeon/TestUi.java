package com.rehivetech.beeeon;
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
 * Tests for GUI
 * @uthor Martina Kůrová
 */
public class TestUi extends UiAutomatorTestCase {

    private String currentTestName;
    protected Boolean testFailed = true;
    private static final String TEST_APP_PKG = "com.rehivetech.beeeon.debug";
    private static final Intent START_LOGIN_ACTIVITY = new Intent(Intent.ACTION_MAIN)
            .setComponent(new ComponentName(TEST_APP_PKG, TEST_APP_PKG + ".LoginActivity"))
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

    // Get the device properties
    UiDevice myDevice = getUiDevice();

    private void setCurrentTestName(String testName) {
        this.currentTestName = testName;
        //takeScreenshot("start");
    }

    protected void trace(String message){
        System.out.println("### " + message + " ###");
    }
    protected void launchApp(String application) throws UiObjectNotFoundException{
        //Simulate a short press on the HOME button.
        getUiDevice().pressHome();
        //Now we get the allApps button to launch the allapps screen
        UiObject allAppsButton = new UiObject(new UiSelector().description("Apps"));
        allAppsButton.clickAndWaitForNewWindow();
        //this is to make sure we are in the apps page, So even if the page was in widgets tab, it will come to the apps tab
        UiObject appsTab = new UiObject(new UiSelector().text("Apps"));
        appsTab.click();
        // Next, in the apps tabs, we can simulate a user swiping until
        // they come to the Settings app icon. Since the container view
        // is scrollable, we can use a UiScrollable object.
        UiScrollable appViews = new UiScrollable(new UiSelector()
                .scrollable(true));
        // Set the swiping mode to horizontal (the default is vertical)
        appViews.setAsHorizontalList();
        //Getting hold of our app to be opened
        UiObject appToBeLaunched = appViews.getChildByText(new UiSelector()
                        .className(android.widget.TextView.class.getName()),
                application);
        appToBeLaunched.clickAndWaitForNewWindow();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Launch the BeeeOn app
        //getInstrumentation().getContext().startActivity(START_LOGIN_ACTIVITY);
        /*getUiDevice().pressHome();
        String packageName = "com.rehivetech.beeeon.debug";
        //String component = packageName + "/activity.LoginActivity";
        String action = "am start -a android.intent.action.MAIN -n ";

        // start settings application
        Runtime.getRuntime().exec(action + packageName);

        UiObject settingsUi = new UiObject(new UiSelector().packageName(packageName));
        assertTrue("Application settings not started", settingsUi.waitForExists(5000));*/
        // TODO Auto-generated method stub
        trace("In setup, Launching the application");
        /*
        * Over here, I am trying to launch the application
        * Now there are two ways to do that
        * 1. Getting runtime environment and starting the application using "am" and call the invoking path for the
        * activity from the AndroidManifest.xml*/
        Runtime.getRuntime().exec("am start -n com.rehivetech.beeeon.debug/com.rehivetech.beeeon.activity.LoginActivity");
        /** Now, since I do not have the activity name to launch the application, I cannot use the "am" command to
         * launch the application (Unless i reverse engineer the apk to get the AndroidManifest.xml to get the activity name
         *
         * 2. Conventional way, Launch the application via the apps page from the device, which I am going to use
         */
        //launchApp("BeeeOn (debug)");
        sleep(3000);
        testFailed = true;
    }

    @Override
    protected void tearDown() throws Exception {
        //takeScreenshot("end");
        // Simulate a short press on the HOME button.
        getUiDevice().pressHome();
        super.tearDown();
    }


    // All App Tray Button
    UiObject AppTrayButton = new UiObject(new UiSelector().description("Apps"));

    // Get AppTray container
    UiScrollable appView = new UiScrollable(new UiSelector().className(
            "android.view.View").scrollable(true));
    // Apps Tab
    UiObject AppsTab = new UiObject(new UiSelector().className(
            "android.widget.TextView").description("Apps"));
    // Verify the launched application by it's Package name
    UiObject beeeonValidation = new UiObject(
            new UiSelector().packageName("com.rehivetech.beeeon"));
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

    // Actual Tests starts here


    // Launch BeeeOn Application
    public void test_0_LaunchTestApp() throws RemoteException, UiObjectNotFoundException {

        setCurrentTestName("testLaunchTestApp");

        // Validate BeeeOn App by it's package name
        //assertTrue("BeeeON App not launched! Please, run BeeeOn app.", beeeonValidation.exists());
        // for beta testers
        //assertEquals("com.rehivetech.beeeon", currentPackage.getPackageName());
        // for alpha tester

        UiObject beeeonValidation = new UiObject(
                new UiSelector().packageName("com.rehivetech.beeeon.debug"));
        assertTrue("Unable to detect BeeeOn", beeeonValidation.exists());
        //assertEquals("BeeeON App not launched! Please, run BeeeOn app.","com.rehivetech.beeeon.debug", currentPackage.getPackageName());
        //if(!currentPackage.getPackageName().equals("com.rehivetech.beeeon.debug")){
        //}
        //assertEquals(super.getClass().toString(), "LoginActivity.class");
        //beeeOnApp.clickAndWaitForNewWindow();

    }

    public void test_1_accessViaGoogle() throws UiObjectNotFoundException {
        // click on demo button
        assertTrue("Google Button not found", googleImageButton.exists());
        assertTrue("No new window", googleImageButton.clickAndWaitForNewWindow());
        logout();
    }

    public void test_2_acceessViaMojeID() throws UiObjectNotFoundException {
        // click on demo button
        assertTrue("MojeID Button not found", mojeIDImageButton.exists());
        assertTrue("No new window", mojeIDImageButton.clickAndWaitForNewWindow());
        logout();
    }
/*
    public void test_3_accesToDemoMode() throws UiObjectNotFoundException {
        // click on demo button
        assertTrue("Demo Button not found", demoImageButton.exists());
        assertTrue("No new window", demoImageButton.clickAndWaitForNewWindow());
        logout();
    }*/

    // Change device location
    public void test_4_ChangeDeviceLocation() throws UiObjectNotFoundException {

        // Click on Menu button
        //myDevice.pressMenu();


        // click on demo button
        assertTrue("Demo Button not found", demoImageButton.exists());
        demoImageButton.clickAndWaitForNewWindow();

        // open menu - nav drawer
        assertTrue("Menu (NavDrawer) Button not found", menuNavDrawerButton.exists());
        menuNavDrawerButton.click();

        // select "overview"
        assertTrue("Overview field is not found", overview.exists());
        overview.clickAndWaitForNewWindow();

        // click on some senzor
        //assertTrue("Device text filed named \"" + deviceNameText.getText() + "\" is not found", deviceNameText.exists());
        assertTrue("Device text filed named \\\"\" + deviceNameText.getText() + \"\\\" is not found",
                new UiScrollable(
                        new UiSelector().scrollable(true)
                ).scrollIntoView(deviceNameText));
        deviceNameText.clickAndWaitForNewWindow();


            /*
            // Read the text enetered in Text box
            assertEquals("Note 1", addNoteText.getText());

            // Click on Menu button
            myDevice.pressMenu();

            // Save button in the menu
            assertTrue("Save Button not found", saveNoteButton.exists());
            saveNoteButton.clickAndWaitForNewWindow();
         */
    }
/*
        // Add Note2
        public void testAddNote2() throws UiObjectNotFoundException {
            // ------Add Note 2--------

            // Click on Menu button
            myDevice.pressMenu();

            assertTrue("Add note Button not found", addNoteButton.exists());
            addNoteButton.click();

            // Add note button in menu
            assertTrue("Add note Text field not found", addNoteText.exists());
            addNoteText.setText("Note 2");

            // Read the Text entered
            assertEquals("Note 2", addNoteText.getText());

            // Save button in the menu
            myDevice.pressMenu();

            assertTrue("Save Button not found", saveNoteButton.exists());
            saveNoteButton.clickAndWaitForNewWindow();
        }

        // Delete Note 1
        public void testDeleteNote1() throws UiObjectNotFoundException {
            // Select Note 2 and long press on it
            UiObject noteListItem = notesList.getChild(new UiSelector()
                    .className(android.widget.TextView.class.getName())
                    .text("Note 1").longClickable(true));

            // Check if Note 1 exists in the list
            assertEquals("Note 1", noteListItem.getText());

            // Long press the menu item
            noteListItem.longClick();

            UiObject note1Text = longPressNoteMenu.getChild(new UiSelector()
                    .className(android.widget.TextView.class.getName()).index(0));

            // Check if Note 1 menu is opened
            assertEquals("Note 1", note1Text.getText());
            assertTrue("Delete Button not found", deleteButton.exists());

            deleteButton.clickAndWaitForNewWindow();
        }
*/
        /*@After
        public void closeBeeeOnApp() {
            getUiDevice().pressBack();
        }*/

    /*public void testHome() throws RemoteException,
            UiObjectNotFoundException {

        // press Home
        UiDevice mydevice = getUiDevice();
        if (!mydevice.isScreenOn()) {
            mydevice.wakeUp();
        }
        mydevice.pressHome();

        // Script path
        String filePath = "/mnt/sdcard_ext/command.sh";
        File command = new File(filePath);
        command.setExecutable(true);

        // Run command
        Process p = null;
        try {
            Runtime.getRuntime().exec(
                    "/system/bin/sh /mnt/sdcard_ext/command.sh");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (p != null)
                p.destroy();
        }

    }*/


    public static void logMessage(String logMessage) {
        String strLogMessage = "*** GNM: " + logMessage;
        System.out.println(strLogMessage);
    }

    public void logout() throws UiObjectNotFoundException {
        // open menu - nav drawer
        assertTrue("Menu (NavDrawer) Button not found", menuNavDrawerButton.exists());
        menuNavDrawerButton.click();
        // scroll to logout
        assertTrue("Logout is not found",
                new UiScrollable(
                        new UiSelector().scrollable(true)
                ).scrollIntoView(logoutText));
        logoutText.clickAndWaitForNewWindow();
    }
}
