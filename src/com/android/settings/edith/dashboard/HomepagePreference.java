package com.android.settings.edith.dashboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;

public class HomepagePreference extends Preference {

    private View mIcon;
    private View mText;
    private boolean mIconVisible = true;
    private int mIconPaddingStart = -1;
    private int mTextPaddingStart = -1;

    public HomepagePreference(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initLayout();
    }

    public HomepagePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout();
    }

    public HomepagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout();
    }

    public HomepagePreference(Context context) {
        super(context);
        initLayout();
    }

    private void initLayout() {
        setLayoutResource(R.layout.edith_homepage_preference);
    }

    public void setIconVisible(boolean visible) {
        mIconVisible = visible;
        if (mIcon != null) {
            mIcon.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setIconPaddingStart(int paddingStart) {
        mIconPaddingStart = paddingStart;
        if (mIcon != null && paddingStart >= 0) {
            mIcon.setPaddingRelative(paddingStart, mIcon.getPaddingTop(), mIcon.getPaddingEnd(),
                    mIcon.getPaddingBottom());
        }
    }

    public void setTextPaddingStart(int paddingStart) {
        mTextPaddingStart = paddingStart;
        if (mText != null && paddingStart >= 0) {
            mText.setPaddingRelative(paddingStart, mText.getPaddingTop(), mText.getPaddingEnd(),
                    mText.getPaddingBottom());
        }
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mIcon = holder.findViewById(R.id.icon_frame);
        mText = holder.findViewById(R.id.text_frame);
        setIconVisible(mIconVisible);
        setIconPaddingStart(mIconPaddingStart);
        setTextPaddingStart(mTextPaddingStart);
    }
}
