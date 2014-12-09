package cz.vutbr.fit.iha.activity.fragment;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.AddAdapterActivity;
import cz.vutbr.fit.iha.util.Log;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public final class IntroImageFragment extends Fragment {
    private static final String KEY_CONTENT = "TestFragment:Content";

    public static IntroImageFragment newInstance(int resourceImg) {
        IntroImageFragment fragment = new IntroImageFragment();

        fragment.mContent = resourceImg;

        return fragment;
    }

    private int mContent = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getInt(KEY_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ImageView image = new ImageView(getActivity());
        image.setImageResource(mContent);
        image.setPadding(20, 20, 20, 20);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        layout.setGravity(Gravity.CENTER);
        layout.addView(image);

        return layout;
    }
    
    @Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
	    super.setUserVisibleHint(isVisibleToUser);
	    if (isVisibleToUser) {
	    	Context mActivity = getActivity();
	    	if(mActivity instanceof AddAdapterActivity) {
	    		((AddAdapterActivity)mActivity).resetBtn();
	    	}
	    }

	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CONTENT, mContent);
    }
}
