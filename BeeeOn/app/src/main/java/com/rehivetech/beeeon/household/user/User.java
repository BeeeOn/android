package com.rehivetech.beeeon.household.user;

import android.content.Context;
import android.widget.ImageView;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.INameIdentifier;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;


/**
 * Represents single person.
 */
public class User implements INameIdentifier {

	private String mId = "";

	private String mName = "";

	private String mSurname = "";

	private String mEmail = "";

	private Role mRole = Role.Guest;

	private Gender mGender = Gender.UNKNOWN;

	private String mPictureUrl = "";

	private HashMap<String, String> mJoinedProviders = new HashMap<>();


	public enum Role implements IIdentifier {
		Guest("guest", R.string.user_role_guest), // can only read gate and devices' data
		User("user", R.string.user_role_user), // = guest + can switch state of switch devices
		Admin("admin", R.string.user_role_admin), // = user + can change devices' settings (rename, logging, refresh,...)
		Owner("owner", R.string.user_role_owner); // = admin + can change whole gate's settings (devices, users,...)

		private final String mRole;
		private final int mStringRes;


		Role(String role, int stringRes) {
			mRole = role;
			mStringRes = stringRes;
		}


		public String getId() {
			return mRole;
		}


		public int getStringResource() {
			return mStringRes;
		}
	}


	public enum Gender implements IIdentifier {
		UNKNOWN("unknown"),
		MALE("male"),
		FEMALE("female");

		String mValue;


		Gender(String value) {
			mValue = value;
		}


		public String getId() {
			return mValue;
		}
	}


	public User() {
	}


	public User(final String id, final String name, final String surname, final String email, final Gender gender, final Role role) {
		mId = id;
		mName = name;
		mSurname = surname;
		mEmail = email;
		mGender = gender;
		mRole = role;
	}


	public User(final String id, final String email, final Role role) {
		mId = id;
		mEmail = email;
		mRole = role;
	}


	public String getId() {
		if(!mId.isEmpty())
			return mId;
		return getEmail();
	}


	public void setId(String id) {
		this.mId = id;
	}


	public String getName() {
		return mName;
	}


	public void setName(String name) {
		mName = name;
	}


	public HashMap<String, String> getJoinedProviders() {
		return mJoinedProviders;
	}


	public void setJoinedProviders(HashMap<String, String> joinedProviders) {
		mJoinedProviders = joinedProviders;
	}


	public String getFullName() {
		return String.format("%s %s", mName, mSurname).trim();
	}


	public String getSurname() {
		return mSurname;
	}


	public void setSurname(String surname) {
		this.mSurname = surname;
	}


	public String getEmail() {
		return mEmail;
	}


	public void setEmail(String email) {
		mEmail = email;
	}


	public Role getRole() {
		return mRole;
	}


	public void setRole(Role role) {
		mRole = role;
	}


	public Gender getGender() {
		return mGender;
	}


	public void setGender(Gender gender) {
		mGender = gender;
	}


	/**
	 * @return picture url or empty string
	 */
	public String getPictureUrl() {
		return mPictureUrl;
	}


	/**
	 * Sets url for user's picture. (Raw without URL decoding)
	 *
	 * @param encodedUrl string in raw format which might be urlencoded
	 */
	public void setPictureUrl(String encodedUrl) {
		mPictureUrl = encodedUrl;
	}


	/**
	 * Loads user's picture from its url address and sets into specified imageview
	 *
	 * @param context     for gettin resources and picasso
	 * @param pictureView into this view will be picture loaded
	 */
	public void loadPicture(Context context, final ImageView pictureView) {
		String url = getPictureUrl();
		final int padding = (int) context.getResources().getDimension(R.dimen.space_normal);

		// loads default icon with padding
		if(url == null || url.isEmpty()) {
			pictureView.setImageResource(R.drawable.ic_person_white_24dp);
			pictureView.setPadding(padding, padding, padding, padding);
			return;
		}

		// starts loading picture (sets padding on error + error icon .. without padding on success)
		Picasso.with(context)
				.load(url)
				.error(R.drawable.ic_person_white_24dp)
				.transform(Utils.getCircleTransformation())
				.into(pictureView, new Callback() {
					@Override
					public void onSuccess() {
						pictureView.setPadding(0, 0, 0, 0);
					}


					/**
					 * Error shows error drawable + sets padding for it
					 */
					@Override
					public void onError() {
						pictureView.setPadding(padding, padding, padding, padding);
					}
				});
	}


	/**
	 * Represents pair of user and gate for saving it to server
	 */
	public static class DataPair {
		public final User user;
		public final String gateId;


		public DataPair(final User user, final String gateId) {
			this.user = user;
			this.gateId = gateId;
		}
	}

}
