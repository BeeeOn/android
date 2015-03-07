package com.rehivetech.beeeon;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;

import com.rehivetech.beeeon.activity.LoginActivity;
import com.rehivetech.beeeon.util.Log;
import com.robotium.solo.Solo;

import static android.support.v4.app.ActivityCompat.startActivity;

// ActivityInstrumentationTestCase2 ... class provides methods and activities to interact with the app
public class RobotiumTest extends ActivityInstrumentationTestCase2<LoginActivity>{

    private static final String TARGET_PACKAGE_ID = "com.rehivetech.beeeon";
    private static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME = "com.rehivetech.beeeon.activity.LoginActivity";
    //private static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME = TARGET_PACKAGE_ID + "LoginActivity";
    private static Class launcherActivityClass;
	private Solo solo; // object Solo ... provides access to the entire Robotium framework along with all of the provided methods

    // This will launch the application specified above on the device
    static    {
        try{
            launcherActivityClass = Class.forName(LAUNCHER_ACTIVITY_FULL_CLASSNAME);
        } catch(ClassNotFoundException e)        {
            throw new RuntimeException(e);
        }
    }

	public RobotiumTest() throws ClassNotFoundException {

        super(launcherActivityClass);
	}

    /**
     * Set up method
     * @throws Exception
     */
	@Override
	public void setUp() throws Exception {
        //super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
	}
	
	public void test_UI(){
		//. use solo.drag
		//2. use solo.clickOnScreen at any position of your slider, after you got slider View you can get its XY coords on the screen
		//3. get slider View, and setProgress for it or something like that (working fine for brightness controls)
		//4. Add your case

            System.out.println("[robotium] Begin Robotium test");
            int indexGoogle = 0; // Google button
            int indexDemo = 2; // Demo button

		    solo.waitForActivity("LoginActivity.class");
            solo.assertCurrentActivity("Current Activity", LoginActivity.class);
            // click on demo mode
            solo.getImageButton(indexGoogle);
            solo.clickOnImageButton(indexGoogle);
            System.out.println("[robotium] Click on google btn");

            solo.waitForActivity("MainActivity.class");

            System.out.println("[robotium] Click on Home button");
            //solo.clickOnActionBarHomeButton();
            solo.sendKey(KeyEvent.KEYCODE_HOME);
            Log.d(getActivity().toString(), "[robotium] click on device HOME BUTTON");
           // solo.goBackToActivity("MainActivity.class");
            //Log.d(getActivity().toString(), "[robotium] go back to Main activity");
            //solo.clickOnImageButton(2);

        //View btnGoogle = solo.getView(R.id.login_btn_google);
			 //solo.clickOnView(btnGoogle);
			 //solo.sleep(5000);
			 
			 // solo.getView(R.id.login_btn_demo);
			// solo.clickOnView(solo.getView(R.id.login_btn_demo)); 
			
			// View view = solo.getView(R.string.login_demo);
			// solo.clickOnView(view);
            // click on demo mode
            /*solo.getImageButton(index);
            solo.clickOnImageButton(index);
            //solo.sleep(5000);
            solo.waitForActivity("MainActivity.class");
            //solo.sleep(5000);
				if(solo.searchText("Karma")){
					solo.clickOnText("Karma");
				}
				solo.sleep(5000);
*/

		
		// White-box
		// solo.getView(R.id.login_btn_demo); //and then 
		// solo.clickOnView(R.id.login_btn_demo); 
		
		//solo.clickOnButton("Save");
	}

    /**
     * Tear down method
     * @throws Exception
     */
	@Override
	public void tearDown() throws Exception {
        solo.finishOpenedActivities();
	}
}
