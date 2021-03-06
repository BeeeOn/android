package com.rehivetech.beeeon.gui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;
import com.rehivetech.beeeon.gui.adapter.dashboard.PlaceAdapter;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.VentilationItem;
import com.rehivetech.beeeon.gui.dialog.BetterProgressDialog;
import com.rehivetech.beeeon.household.device.ModuleType;
import com.rehivetech.beeeon.model.place.Place;
import com.rehivetech.beeeon.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

import static com.rehivetech.beeeon.gui.adapter.dashboard.DashboardModuleSelectAdapter.ModuleItem;

/**
 * @author martin
 * @since 20.3.16
 */
public class AddDashboardVentilationItemFragment extends BaseAddDashBoardItemFragment implements OnMapReadyCallback, AdapterView.OnItemClickListener {

	private static final String ARG_GATE_ID = "gate_id";
	private static final String ARG_INSIDE_MODULE_ITEM = "inside_module_item";
	private static final String ARG_OUTSIDE_MODULE_ITEM = "outside_module_item";
	private static final String ARG_LOCATION = "location";
	private static final String ARG_OUTSIDE_PROVIDER_TYPE = "outside_provider_type";

	@IntDef({OutSideProviderType.OUTSIDE_TYPE_MODULE, OutSideProviderType.OUTSIDE_TYPE_WEATHER, OutSideProviderType.NONE})
	private @interface OutSideProviderType {
		int OUTSIDE_TYPE_MODULE = 0;
		int OUTSIDE_TYPE_WEATHER = 1;
		int NONE = -1;
	}

	private ModuleItem mInsideModuleItem;
	private ModuleItem mOutSideModuleItem;
	private Place mLocation;
	@OutSideProviderType
	private int mOutSideProviderType;

	@Nullable
	@BindView(R.id.fragment_add_dashboard_item_title) TextView mTitle;

	@Nullable
	@BindView(R.id.fragment_add_dashboard_item_ventilation_location_textview)
	AutoCompleteTextView mAutoCompleteTextView;

	private PlaceAdapter mPlaceAdapter;
	private GoogleMap mMap;

	public static AddDashboardVentilationItemFragment newInstance(int index, String gateId, @Nullable Place location, @Nullable ModuleItem outSideModuleItem, @Nullable ModuleItem insideModuleItem, @Nullable @OutSideProviderType Integer outSideType) {

		Bundle args = new Bundle();
		fillBaseArgs(args, index, gateId);
		args.putParcelable(ARG_LOCATION, location);
		args.putParcelable(ARG_OUTSIDE_MODULE_ITEM, outSideModuleItem);
		args.putParcelable(ARG_INSIDE_MODULE_ITEM, insideModuleItem);
		if (outSideType != null) {
			args.putInt(ARG_OUTSIDE_PROVIDER_TYPE, outSideType);
		}
		AddDashboardVentilationItemFragment fragment = new AddDashboardVentilationItemFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			mLocation = args.getParcelable(ARG_LOCATION);
			mOutSideModuleItem = args.getParcelable(ARG_OUTSIDE_MODULE_ITEM);
			mInsideModuleItem = args.getParcelable(ARG_INSIDE_MODULE_ITEM);
			//noinspection ResourceType
			mOutSideProviderType = args.getInt(ARG_OUTSIDE_PROVIDER_TYPE, OutSideProviderType.NONE);
		}
	}

	@SuppressLint("InflateParams")
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		FrameLayout rootView = (FrameLayout) inflater.inflate(R.layout.fragment_add_dasboard_ventilation, container, false);

		View view = null;
		if (mInsideModuleItem == null && mLocation == null && mOutSideModuleItem == null && mOutSideProviderType == OutSideProviderType.NONE) {
			view = LayoutInflater.from(mActivity).inflate(R.layout.fragment_add_dashboard_ventilation_layout_1, null);
		} else if (mOutSideProviderType != OutSideProviderType.OUTSIDE_TYPE_WEATHER && (mOutSideProviderType == OutSideProviderType.OUTSIDE_TYPE_MODULE || (mLocation != null || mOutSideModuleItem != null))) {
			view = LayoutInflater.from(mActivity).inflate(R.layout.add_dashboard_recyclerview_item_layout1, null);
		} else if (mOutSideProviderType == OutSideProviderType.OUTSIDE_TYPE_WEATHER) {
			view = LayoutInflater.from(mActivity).inflate(R.layout.fragment_add_dashboard_ventilation_location_layout, null);
		}
		rootView.addView(view, 0);

		mUnbinder = ButterKnife.bind(this, rootView);
		return rootView;
	}

	@Override
	public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
		mButtonDone = ButterKnife.findById(view, R.id.fragment_add_dashboard_item_button_done);

		if (mInsideModuleItem == null && mOutSideModuleItem == null && mOutSideProviderType == OutSideProviderType.NONE && mLocation == null) {
			mButtonDone.setImageResource(R.drawable.arrow_right_bold);

			mButtonDone.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AddDashboardVentilationItemFragment fragment;
					RadioGroup radioGroup = ButterKnife.findById(view, R.id.fragment_add_dashboard_ventilation_radiogroup);
					int selectedId = radioGroup.getCheckedRadioButtonId();
					if (selectedId == R.id.fragment_add_dashboard_radio_btn_select_module) {
						fragment = AddDashboardVentilationItemFragment.newInstance(mIndex, mGateId, null, null, null, OutSideProviderType.OUTSIDE_TYPE_MODULE);
					} else {
						fragment = AddDashboardVentilationItemFragment.newInstance(mIndex, mGateId, null, null, null, OutSideProviderType.OUTSIDE_TYPE_WEATHER);
					}

					mActivity.replaceFragment(getTag(), fragment);
				}
			});
		}

		if (mOutSideProviderType == OutSideProviderType.OUTSIDE_TYPE_MODULE && mLocation == null) {
			super.onViewCreated(view, savedInstanceState);
			fillAdapter(false, ModuleType.TYPE_TEMPERATURE);
			mAdapter.selectFirstModuleItem();

			if (mTitle != null) {
				mTitle.setText(R.string.dashboard_add_ventilation_outside_module_select_title);
			}

			mButtonDone.setImageResource(R.drawable.arrow_right_bold);
			mButtonDone.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int selectedItem = mAdapter.getFirstSelectedItem();
					mOutSideModuleItem = (ModuleItem) mAdapter.getItem(selectedItem);
					Fragment fragment = AddDashboardVentilationItemFragment.newInstance(mIndex, mGateId, null, mOutSideModuleItem, null, OutSideProviderType.NONE);
					mActivity.replaceFragment(getTag(), fragment);
				}
			});
		} else if (mOutSideProviderType == OutSideProviderType.OUTSIDE_TYPE_WEATHER) {
			mButtonDone.setImageResource(R.drawable.arrow_right_bold);
			mButtonDone.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mLocation == null) {
						Toast.makeText(mActivity, R.string.select_location, Toast.LENGTH_SHORT).show();
						return;
					}
					Fragment fragment = AddDashboardVentilationItemFragment.newInstance(mIndex, mGateId, mLocation, null, null, OutSideProviderType.NONE);
					mActivity.replaceFragment(getTag(), fragment);
				}
			});

			SupportMapFragment mapFragment = SupportMapFragment.newInstance();
			getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
			mapFragment.getMapAsync(this);

			mPlaceAdapter = new PlaceAdapter(mActivity);

			if (mAutoCompleteTextView != null) {
				mAutoCompleteTextView.setAdapter(mPlaceAdapter);
				mAutoCompleteTextView.setOnItemClickListener(this);
			}


		} else if (mOutSideModuleItem != null && mOutSideProviderType == OutSideProviderType.NONE || mLocation != null) {
			super.onViewCreated(view, savedInstanceState);
			fillAdapter(false, ModuleType.TYPE_TEMPERATURE);
			mAdapter.selectFirstModuleItem();

			if (mTitle != null) {
				mTitle.setText(R.string.dashboard_add_ventilation_inside_module_select_title);
			}

			mButtonDone.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					VentilationItem item;
					int selectedItem = mAdapter.getFirstSelectedItem();
					mInsideModuleItem = (ModuleItem) mAdapter.getItem(selectedItem);
					if (mLocation != null) {
						item = new VentilationItem(mLocation.getName(), mGateId, mLocation.getCoordinates(), null, mInsideModuleItem.getAbsoluteId());
					} else {
						item = new VentilationItem("asdf", mGateId, null, mOutSideModuleItem.getAbsoluteId(), mInsideModuleItem.getAbsoluteId());

					}

					finishActivity(item);
				}
			});
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		GoogleAnalyticsManager.getInstance().logScreen(GoogleAnalyticsManager.ADD_DASHBOARD_VENTILATION_HELPER_SCREEN);
	}

	@Optional
	@OnClick(R.id.fragment_add_dashboard_item_ventilation_location_gps_icon)
	public void onGpsButtonClicked() {
		if (!checkLocationPermissions()) {
			return;
		}
		requestLocation();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case Constants.PERMISSION_CODE_LOCATION:
				if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
					Toast.makeText(mActivity, R.string.unable_to_get_location, Toast.LENGTH_SHORT).show();
				} else {
					requestLocation();
				}
				break;
		}
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		if (!Utils.isGooglePlayServicesAvailable(mActivity)) return;

		mMap = googleMap;
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
		MapsInitializer.initialize(mActivity);
		if (mLocation != null) {
			updateMapCamera(mLocation);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Place place = mPlaceAdapter.getItem(position);

		if (Utils.isGooglePlayServicesAvailable(mActivity)) {
			updateMapCamera(place);
		}

		mLocation = place;
	}

	private void updateMapCamera(Place place) {
		double[] coordinates = place.getCoordinates();
		LatLng latLng = new LatLng(coordinates[0], coordinates[1]);
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
		mMap.animateCamera(cameraUpdate);
		MarkerOptions marker = new MarkerOptions();
		marker.position(latLng);
		mMap.clear();
		mMap.addMarker(marker);
	}


	private boolean checkLocationPermissions() {
		if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
					Constants.PERMISSION_CODE_LOCATION);
			return false;
		}
		return true;
	}

	/**
	 * Obtain current location
	 */
	private void requestLocation() {
		if (!checkIfLocationAvailable()) {
			return;
		}

		final LocationManager locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);

		String locationProvider = getProviderName(locationManager);
		final BetterProgressDialog dialog = new BetterProgressDialog(mActivity);
		dialog.setMessage(mActivity.getString(R.string.obtain_location));
		final LocationListener locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {

				if (location != null) {

					if (mAutoCompleteTextView != null) {
						mAutoCompleteTextView.requestFocus();
						mAutoCompleteTextView.setHint(R.string.my_location);
						mAutoCompleteTextView.setText("");
					}

					try {
						locationManager.removeUpdates(this);
						dialog.dismiss();

					} catch (SecurityException ignored) {
					}

					mLocation = new Place();
					mLocation.setName(mActivity.getString(R.string.my_location));
					mLocation.setCoordinates(location.getLatitude(), location.getLongitude());

					if (Utils.isGooglePlayServicesAvailable(mActivity)) {
						updateMapCamera(mLocation);
					}
				}
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {

			}

			@Override
			public void onProviderEnabled(String provider) {

			}

			@Override
			public void onProviderDisabled(String provider) {

			}
		};

		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				try {
					locationManager.removeUpdates(locationListener);

				} catch (SecurityException ignored) {
				}
			}
		});

		try {
			locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
			dialog.show();
		} catch (SecurityException ignored) {
		}


	}

	/**
	 * Get locationManager provider name.
	 *
	 * @return Name of best suiting provider.
	 */
	private String getProviderName(LocationManager locationManager) {
		Criteria criteria = new Criteria();
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(false);

		return locationManager.getBestProvider(criteria, true);
	}

	/**
	 * Check if location is enabled
	 *
	 * @return true/false
	 */
	private boolean checkIfLocationAvailable() {
		LocationManager lm = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
		boolean gpsEnabled = false;
		boolean networkEnabled = false;

		try {
			gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
			networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (SecurityException ignored) {
		}

		if (!gpsEnabled && !networkEnabled) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
			dialog.setMessage(R.string.location_not_available);
			dialog.setPositiveButton(R.string.open_location_settings, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface paramDialogInterface, int paramInt) {
					Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					mActivity.startActivity(myIntent);
				}
			});
			dialog.setNegativeButton(R.string.activity_fragment_btn_cancel, null);
			dialog.show();

			return false;
		}
		return true;
	}
}
