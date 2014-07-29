package cz.vutbr.fit.iha.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.LoginActivity;
import cz.vutbr.fit.iha.thread.ToastMessageThread;

/**
 * Class communicate with Google server
 * Get basic user info and picture if is possible 
 * @author Leopold Podmolik
 */
public class GetGoogleAuth extends AsyncTask<Void, Void, GoogleAuthState> {
	private static final String TAG = GetGoogleAuth.class.getSimpleName();
	private static GetGoogleAuth mThis;
	
	private LoginActivity mActivity;
	private String mEmail;
	private String mToken;
	private String mUserName;
	private String mPictureUrl;
	private Bitmap mPicture;
	
	//////////////////////////////////////////////////////////////////////////////////////
	///////////////////  Constructors ////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Constructor
	 * @param mActivity
	 * @param mEmail
	 */
	public GetGoogleAuth(LoginActivity mActivity, String mEmail) {
		this.mActivity = mActivity;
		this.mEmail = mEmail;
		GetGoogleAuth.mThis = this;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	///////////////////  Get-Set METHODS /////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * @return the mEmail
	 */
	public String getEmail() {
		return mEmail;
	}

	/**
	 * @param mEmail the mEmail to set
	 */
	public void setEmail(String mEmail) {
		this.mEmail = mEmail;
	}

	/**
	 * @return the mUserName
	 */
	public String getUserName() {
		return mUserName;
	}

	/**
	 * @param mUserName the mUserName to set
	 */
	public void setUserName(String mUserName) {
		this.mUserName = mUserName;
	}

	/**
	 * @return the mPicture
	 */
	public String getPicture() {
		return mPictureUrl;
	}

	/**
	 * @param mPicture the mPicture to set
	 */
	public void setPicture(String mPicture) {
		this.mPictureUrl = mPicture;
	}

	/**
	 * @return the mPicture
	 */
	public Bitmap getPictureIMG() {
		return mPicture;
	}

	/**
	 * @param mPicture the mPicture to set
	 */
	public void setPictureIMG(Bitmap mPictureIMG) {
		this.mPicture = mPictureIMG;
	}

	/**
	 * Getter of token
	 * @return
	 */
	protected String getToken(){
		if(this.mToken != null) 
			return this.mToken;
		return "";
	}
	
	public void setDebugToken(String token){
		mToken = token;
	}
	
	/**
	 * Singleton-like method, but not initializing
	 * @return static object
	 * @throws Exception
	 */
	public static GetGoogleAuth getGetGoogleAuth() throws Exception{
		if(mThis != null){
			return mThis;
		}else
			throw new Exception("Not initialized");
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	///////////////////  Override METHODS  ///////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	protected GoogleAuthState doInBackground(Void... params) {
		try {
			mToken = GoogleAuthUtil.getToken(mActivity, mEmail,"oauth2:https://www.googleapis.com/auth/userinfo.profile");
			Log.d(TAG, "Token");
			
			fetchInfoFromProfileServer(mToken);
			
			return GoogleAuthState.eOK;
		} catch (UserRecoverableAuthException userRecoverableException) {
			mActivity.startActivityForResult(userRecoverableException.getIntent(),LoginActivity.USER_RECOVERABLE_AUTH);
			return GoogleAuthState.eRecorver;
		} catch(IOException e){
			return GoogleAuthState.eNoConnection;
		} catch (Exception e) {
			//TODO: check more exceptions and show toast 
			e.printStackTrace();
		}
		return GoogleAuthState.eUnknown;
	}
	
	@Override
	protected void onPostExecute(final GoogleAuthState result) {
		super.onPostExecute(result);

		switch(result){
			case eOK:
				mActivity.ProgressDismiss();
				break;
			case eRecorver:
				//TODO: check if better with progressBar
				mActivity.ProgressDismiss();
				new ToastMessageThread(mActivity, "Google authentication").start();
				break;
			case eUnknown:
				mActivity.ProgressDismiss();
				new ToastMessageThread(mActivity, "Something goes wrong, try it later please.").start();
				break;
			case eNoConnection:
				mActivity.ProgressDismiss();
				new ToastMessageThread(mActivity, mActivity.getString(R.string.toast_internet_connection)).start();
				break;
			default:
				break;
		}
		
			//FIXME: this code below need refactoring !!!!! Leo?
//			Thread fetchUserInfo = new Thread(new Runnable(){
//			    @Override
//			    public void run() {
//			        try {
//			        	fetchInfoFromProfileServer(result);
//			        } catch (Exception e) {
//			            e.printStackTrace();
//			        }
//			    }
//			});
			// Fetch user name and surname
			//fetchUserInfo.start();

			
			// Wait for user name
//			try {
//				synchronized(this) {
//					this.wait();
//				}
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			
			//LayoutInflater inflater = mActivity.getLayoutInflater();
			 
            // Inflate the Layout
           // View welcomeToastlayout = inflater.inflate(R.layout.toast_welcome,(ViewGroup) mActivity.findViewById(R.id.toast_welcome_layout));
 
			
			// Log - Token and Name
//			Log.d(TAG, "Token: "+result);
//			Log.d(TAG, "User name: "+this.mUserName);
//			Log.d(TAG, "Picture url:"+mPicture);
//			mToken = result;
			
//			if(mPicture != null) {
//				// Set User profile picture if is set
//				//final ImageView picture = (ImageView) welcomeToastlayout.findViewById(R.id.toast_welcome_picture);
//				Thread fetchUserPicture = new Thread(new Runnable(){
//				    @Override
//				    public void run() {
//				        try {
//				        	fetchPictureFromProfileServer(mPicture,picture);
//				        } catch (Exception e) {
//				            e.printStackTrace();
//				        }
//				    }
//				});
//				// Fetch user picture
//				fetchUserPicture.start(); 
//				// Wait for user picture
//				try {
//					synchronized(this) {
//						this.wait();
//					}
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			
//            TextView text = (TextView) welcomeToastlayout.findViewById(R.id.toast_welcome_text);
//            // Set the Text to show in TextView
//            text.setText("Welcome \n"+this.mUserName);
//            Toast toast = new Toast(mActivity.getApplicationContext());
//            toast.setGravity(Gravity.BOTTOM, 0, 0);
//            toast.setDuration(Toast.LENGTH_LONG);
//            toast.setView(welcomeToastlayout);
//            toast.show();
//			
//            DemoData demo = new DemoData(mActivity);
//            if (demo.checkDemoData()) {
//				Intent intent = new Intent(mActivity, LocationScreenActivity.class);
//				intent.putExtra(Constants.LOGIN, Constants.LOGIN_DEMO);
//				//intent.putExtra(name, value);
//				mActivity.startActivity(intent);
//            }
//	    	mActivity.finish();
//	    	Log.d(TAG, "FINISH");
////	    	synchronized(this) {
////	        	  this.notify();
////	          }
//	    	return;
	}
	
	
	
	/****************************************************************************************/
	/* Prevzato z SDK - EXTRAS - SAMPLE - AUTH  											*/
	/****************************************************************************************/
	
	/**
	 * Method download name and picture URL
	 * @param token
	 * @throws IOException
	 * @throws JSONException
	 */
	private void fetchInfoFromProfileServer(String token) throws IOException, JSONException {	
		URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + token);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        int sc = con.getResponseCode();
        if (sc == 200) {
	          InputStream is = con.getInputStream();
	          String respond = readResponse(is);
	          
	          mUserName = getName(respond);
	          mPictureUrl = getPicture(respond);
	          
	          Log.d(TAG, String.format("Hello %s!", mUserName));
	          Log.i(TAG, mPictureUrl);
	          is.close();
//	          synchronized(this) {
//	        	  this.notify();
//	          }
	          if (mPictureUrl != null && mPictureUrl.length() > 0) {
	        	  fetchPictureFromProfileServer(mPictureUrl);
	          }
	          
        } else if (sc == 401) {
            GoogleAuthUtil.invalidateToken(mActivity, token);
        }
    }
	
	/**
     * Reads the response from the input stream and returns it as a string.
     */
    private  String readResponse(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = new byte[2048];
        int len = 0;
        while ((len = is.read(data, 0, data.length)) >= 0) {
            bos.write(data, 0, len);
        }
        return new String(bos.toByteArray(), "UTF-8");
    }

    /**
     * Parses the response and returns the name of the user.
     * @throws JSONException if the response is not JSON or if first name does not exist in response
     */
    private String getName(String jsonResponse) throws JSONException {
      JSONObject profile = new JSONObject(jsonResponse);
      return profile.getString("name");
    }
    /**
     * Parses the response and returns the url of picture of the user.
     * @throws JSONException if the response is not JSON or if first name does not exist in response
     */
    private String getPicture(String jsonResponse) throws JSONException {
      JSONObject profile = new JSONObject(jsonResponse);
      boolean result = profile.isNull("picture");
      return (!result) ? profile.getString("picture") : null;
    }
    
    /**
     * Method download picture
     * @param urlPicture
     * @throws IOException
     * @throws JSONException
     */
	private void fetchPictureFromProfileServer(String urlPicture/*, ImageView imageView*/) throws IOException, JSONException {
    	try {
    		URL imageURL = new URL(urlPicture);         
            HttpURLConnection connection = (HttpURLConnection) imageURL
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            
            setPictureIMG(BitmapFactory.decodeStream(inputStream));// Convert to bitmap
//            imageView.setImageBitmap(mPicture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    	
//    	synchronized(this) {
//      	  this.notify();
//        }
        return;
    }
    
}
