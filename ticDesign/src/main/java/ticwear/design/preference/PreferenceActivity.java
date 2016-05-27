/*
 * Copyright (C) 2016 Mobvoi Inc.
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ticwear.design.preference;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.XmlRes;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ticwear.design.R;
import ticwear.design.app.RecyclerActivity;
import ticwear.design.internal.XmlUtils;
import ticwear.design.widget.SelectableAdapter;
import ticwear.design.widget.TicklableRecyclerView;

public abstract class PreferenceActivity extends RecyclerActivity implements
        PreferenceFragment.OnPreferenceStartFragmentCallback {

    private static final String TAG = "PreferenceActivity";

    // Constants for state save/restore
    private static final String HEADERS_TAG = ":android:headers";
    private static final String CUR_HEADER_TAG = ":android:cur_header";

    /**
     * When starting this activity, the invoking Intent can contain this extra
     * string to specify which fragment should be initially displayed.
     * <p/>Starting from Key Lime Pie, when this argument is passed in, the PreferenceActivity
     * will call isValidFragment() to confirm that the fragment class name is valid for this
     * activity.
     */
    public static final String EXTRA_SHOW_FRAGMENT = ":android:show_fragment";

    /**
     * When starting this activity and using {@link #EXTRA_SHOW_FRAGMENT},
     * this extra can also be specified to supply a Bundle of arguments to pass
     * to that fragment when it is instantiated during the initial creation
     * of PreferenceActivity.
     */
    public static final String EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":android:show_fragment_args";

    /**
     * When starting this activity and using {@link #EXTRA_SHOW_FRAGMENT},
     * this extra can also be specify to supply the title to be shown for
     * that fragment.
     */
    public static final String EXTRA_SHOW_FRAGMENT_TITLE = ":android:show_fragment_title";

    /**
     * When starting this activity and using {@link #EXTRA_SHOW_FRAGMENT},
     * this extra can also be specify to supply the short title to be shown for
     * that fragment.
     */
    public static final String EXTRA_SHOW_FRAGMENT_SHORT_TITLE
            = ":android:show_fragment_short_title";

    /**
     * When starting this activity, the invoking Intent can contain this extra
     * boolean that the header list should not be displayed.  This is most often
     * used in conjunction with {@link #EXTRA_SHOW_FRAGMENT} to launch
     * the activity to display a specific fragment that the user has navigated
     * to.
     */
    public static final String EXTRA_NO_HEADERS = ":android:no_headers";

    private static final String BACK_STACK_PREFS = ":android:prefs";

    // extras that allow any preference activity to be launched as part of a wizard

    // show Back and Next buttons? takes boolean parameter
    // Back will then return RESULT_CANCELED and Next RESULT_OK
    private static final String EXTRA_PREFS_SHOW_BUTTON_BAR = "extra_prefs_show_button_bar";

    // add a Skip button?
    private static final String EXTRA_PREFS_SHOW_SKIP = "extra_prefs_show_skip";

    // specify custom text for the Back or Next buttons, or cause a button to not appear
    // at all by setting it to null
    private static final String EXTRA_PREFS_SET_NEXT_TEXT = "extra_prefs_set_next_text";
    private static final String EXTRA_PREFS_SET_BACK_TEXT = "extra_prefs_set_back_text";

    // --- State for new mode when showing a list of headers + prefs fragment

    private final ArrayList<Header> mHeaders = new ArrayList<Header>();

    private TextView mTitleView;

    private FrameLayout mListFooter;

    private ViewGroup mPrefsContainer;

    private boolean mSinglePane;

    private Header mCurHeader;

    // --- Common state

    private Button mNextButton;

    private int mPreferenceHeaderItemResId = 0;
    private boolean mPreferenceHeaderRemoveEmptyIcon = false;

    /**
     * The starting request code given out to preference framework.
     */
    private static final int FIRST_REQUEST_CODE = 100;

    private static final int MSG_BUILD_HEADERS = 2;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_BUILD_HEADERS: {
                    ArrayList<Header> oldHeaders = new ArrayList<Header>(mHeaders);
                    mHeaders.clear();
                    onBuildHeaders(mHeaders);
                    if (getListAdapter() != null) {
                        getListAdapter().notifyDataSetChanged();
                    }
                    Header header = onGetNewHeader();
                    if (header != null && header.fragment != null) {
                        Header mappedHeader = findBestMatchingHeader(header, oldHeaders);
                        if (mappedHeader == null || mCurHeader != mappedHeader) {
                            switchToHeader(header);
                        }
                    } else if (mCurHeader != null) {
                        Header mappedHeader = findBestMatchingHeader(mCurHeader, mHeaders);
                        if (mappedHeader != null) {
                            setSelectedHeader(mappedHeader);
                        }
                    }
                } break;
            }
        }
    };

    private static class HeaderAdapter extends SelectableAdapter<HeaderAdapter.ViewHolder> {

        protected class ViewHolder extends PreferenceViewHolder
                implements View.OnClickListener {

            public ViewHolder(@NonNull ViewGroup parent, @LayoutRes int layoutResId) {
                super(parent, layoutResId);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (mOnHeaderClickListener != null && !mClickEntering) {
                    final int position = getAdapterPosition();
                    final Header header = getItem(position);
                    if (mOnHeaderClickListener.onHeaderClick(header, position)) {
                        mClickEntering = true;
                    }
                }
            }
        }

        protected interface OnHeaderClickListener {
            boolean onHeaderClick(Header header, int position);
        }

        private final Context mContext;
        private final int mLayoutResId;
        private final boolean mRemoveIconIfEmpty;
        private final List<Header> mHeaders;

        private OnHeaderClickListener mOnHeaderClickListener;
        private boolean mClickEntering = false;

        public HeaderAdapter(Context context, List<Header> objects, int layoutResId,
                             boolean removeIconBehavior) {
            super();
            mContext = context;
            mLayoutResId = layoutResId;
            mRemoveIconIfEmpty = removeIconBehavior;
            mHeaders = objects;

            setHasStableIds(true);

            registerAdapterDataObserver(new AdapterDataObserver() {
                @Override
                public void onChanged() {
                    onDataChanged();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    onDataChanged();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    onDataChanged();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    onDataChanged();
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    onDataChanged();
                }

                private void onDataChanged() {
                    resetClickEntering();
                }
            });
        }

        public void setOnHeaderClickListener(OnHeaderClickListener onHeaderClickListener) {
            this.mOnHeaderClickListener = onHeaderClickListener;
        }

        @Override
        public HeaderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(parent, mLayoutResId);
        }

        @Override
        public void onBindViewHolder(HeaderAdapter.ViewHolder holder, int position) {
            Header header = getItem(position);

            PreferenceViewHolder.PreferenceData data = new PreferenceViewHolder.PreferenceData();
            data.title = header.getTitle(mContext.getResources());
            data.summary = header.getSummary(mContext.getResources());
            data.icon = header.iconRes == 0 ? null : mContext.getDrawable(header.iconRes);
            data.removeIconIfEmpty = mRemoveIconIfEmpty;

            holder.bind(data);
        }

        @Override
        public int getItemCount() {
            return mHeaders.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public Header getItem(int position) {
            return mHeaders.get(position);
        }

        public void resetClickEntering() {
            mClickEntering = false;
        }
    }

    /**
     * Default value for {@link Header#id Header.id} indicating that no
     * identifier value is set.  All other values (including those below -1)
     * are valid.
     */
    public static final long HEADER_ID_UNDEFINED = -1;

    /**
     * Description of a single Header item that the user can select.
     */
    public static final class Header implements Parcelable {
        /**
         * Identifier for this header, to correlate with a new list when
         * it is updated.  The default value is
         * {@link PreferenceActivity#HEADER_ID_UNDEFINED}, meaning no id.
         * @attr ref android.R.styleable#PreferenceHeader_id
         */
        public long id = HEADER_ID_UNDEFINED;

        /**
         * Resource ID of title of the header that is shown to the user.
         * @attr ref android.R.styleable#PreferenceHeader_title
         */
        @StringRes
        public int titleRes;

        /**
         * Title of the header that is shown to the user.
         * @attr ref android.R.styleable#PreferenceHeader_title
         */
        public CharSequence title;

        /**
         * Resource ID of optional summary describing what this header controls.
         * @attr ref android.R.styleable#PreferenceHeader_summary
         */
        @StringRes
        public int summaryRes;

        /**
         * Optional summary describing what this header controls.
         * @attr ref android.R.styleable#PreferenceHeader_summary
         */
        public CharSequence summary;

        /**
         * Resource ID of optional text to show as the title in the bread crumb.
         * @attr ref android.R.styleable#PreferenceHeader_breadCrumbTitle
         */
        @StringRes
        public int breadCrumbTitleRes;

        /**
         * Optional text to show as the title in the bread crumb.
         * @attr ref android.R.styleable#PreferenceHeader_breadCrumbTitle
         */
        public CharSequence breadCrumbTitle;

        /**
         * Resource ID of optional text to show as the short title in the bread crumb.
         * @attr ref android.R.styleable#PreferenceHeader_breadCrumbShortTitle
         */
        @StringRes
        public int breadCrumbShortTitleRes;

        /**
         * Optional text to show as the short title in the bread crumb.
         * @attr ref android.R.styleable#PreferenceHeader_breadCrumbShortTitle
         */
        public CharSequence breadCrumbShortTitle;

        /**
         * Optional icon resource to show for this header.
         * @attr ref android.R.styleable#PreferenceHeader_icon
         */
        public int iconRes;

        /**
         * Full class name of the fragment to display when this header is
         * selected.
         * @attr ref android.R.styleable#PreferenceHeader_fragment
         */
        public String fragment;

        /**
         * Optional arguments to supply to the fragment when it is
         * instantiated.
         */
        public Bundle fragmentArguments;

        /**
         * Intent to launch when the preference is selected.
         */
        public Intent intent;

        /**
         * Optional additional data for use by subclasses of PreferenceActivity.
         */
        public Bundle extras;

        public Header() {
            // Empty
        }

        /**
         * Return the currently set title.  If {@link #titleRes} is set,
         * this resource is loaded from <var>res</var> and returned.  Otherwise
         * {@link #title} is returned.
         */
        public CharSequence getTitle(Resources res) {
            if (titleRes != 0) {
                return res.getText(titleRes);
            }
            return title;
        }

        /**
         * Return the currently set summary.  If {@link #summaryRes} is set,
         * this resource is loaded from <var>res</var> and returned.  Otherwise
         * {@link #summary} is returned.
         */
        public CharSequence getSummary(Resources res) {
            if (summaryRes != 0) {
                return res.getText(summaryRes);
            }
            return summary;
        }

        /**
         * Return the currently set bread crumb title.  If {@link #breadCrumbTitleRes} is set,
         * this resource is loaded from <var>res</var> and returned.  Otherwise
         * {@link #breadCrumbTitle} is returned.
         */
        public CharSequence getBreadCrumbTitle(Resources res) {
            if (breadCrumbTitleRes != 0) {
                return res.getText(breadCrumbTitleRes);
            }
            return breadCrumbTitle;
        }

        /**
         * Return the currently set bread crumb short title.  If
         * {@link #breadCrumbShortTitleRes} is set,
         * this resource is loaded from <var>res</var> and returned.  Otherwise
         * {@link #breadCrumbShortTitle} is returned.
         */
        public CharSequence getBreadCrumbShortTitle(Resources res) {
            if (breadCrumbShortTitleRes != 0) {
                return res.getText(breadCrumbShortTitleRes);
            }
            return breadCrumbShortTitle;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);
            dest.writeInt(titleRes);
            TextUtils.writeToParcel(title, dest, flags);
            dest.writeInt(summaryRes);
            TextUtils.writeToParcel(summary, dest, flags);
            dest.writeInt(breadCrumbTitleRes);
            TextUtils.writeToParcel(breadCrumbTitle, dest, flags);
            dest.writeInt(breadCrumbShortTitleRes);
            TextUtils.writeToParcel(breadCrumbShortTitle, dest, flags);
            dest.writeInt(iconRes);
            dest.writeString(fragment);
            dest.writeBundle(fragmentArguments);
            if (intent != null) {
                dest.writeInt(1);
                intent.writeToParcel(dest, flags);
            } else {
                dest.writeInt(0);
            }
            dest.writeBundle(extras);
        }

        public void readFromParcel(Parcel in) {
            id = in.readLong();
            titleRes = in.readInt();
            title = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            summaryRes = in.readInt();
            summary = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            breadCrumbTitleRes = in.readInt();
            breadCrumbTitle = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            breadCrumbShortTitleRes = in.readInt();
            breadCrumbShortTitle = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            iconRes = in.readInt();
            fragment = in.readString();
            fragmentArguments = in.readBundle();
            if (in.readInt() != 0) {
                intent = Intent.CREATOR.createFromParcel(in);
            }
            extras = in.readBundle();
        }

        Header(Parcel in) {
            readFromParcel(in);
        }

        public static final Creator<Header> CREATOR = new Creator<Header>() {
            public Header createFromParcel(Parcel source) {
                return new Header(source);
            }
            public Header[] newArray(int size) {
                return new Header[size];
            }
        };
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Theming for the PreferenceActivity layout and for the Preference Header(s) layout
        TypedArray sa = obtainStyledAttributes(null,
                R.styleable.PreferenceActivity,
                R.attr.tic_preferenceActivityStyle,
                0);

        final int layoutResId = sa.getResourceId(
                R.styleable.PreferenceActivity_android_layout,
                R.layout.preference_list_content);

        mPreferenceHeaderItemResId = sa.getResourceId(
                R.styleable.PreferenceActivity_tic_headerLayout,
                R.layout.preference_ticwear);
        mPreferenceHeaderRemoveEmptyIcon = sa.getBoolean(
                R.styleable.PreferenceActivity_tic_headerRemoveIconIfEmpty,
                false);

        sa.recycle();

        setContentView(layoutResId);

        mTitleView = (TextView) findViewById(android.R.id.title);
        mListFooter = (FrameLayout)findViewById(R.id.list_footer);
        mPrefsContainer = (ViewGroup) findViewById(R.id.prefs_frame);
        boolean hidingHeaders = onIsHidingHeaders();
        mSinglePane = hidingHeaders || !onIsMultiPane();
        String initialFragment = getIntent().getStringExtra(EXTRA_SHOW_FRAGMENT);
        Bundle initialArguments = getIntent().getBundleExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS);
        int initialTitle = getIntent().getIntExtra(EXTRA_SHOW_FRAGMENT_TITLE, 0);
        int initialShortTitle = getIntent().getIntExtra(EXTRA_SHOW_FRAGMENT_SHORT_TITLE, 0);

        if (savedInstanceState != null) {
            // We are restarting from a previous saved state; used that to
            // initialize, instead of starting fresh.
            ArrayList<Header> headers = savedInstanceState.getParcelableArrayList(HEADERS_TAG);
            if (headers != null) {
                mHeaders.addAll(headers);
                int curHeader = savedInstanceState.getInt(CUR_HEADER_TAG,
                        (int) HEADER_ID_UNDEFINED);
                if (curHeader >= 0 && curHeader < mHeaders.size()) {
                    setSelectedHeader(mHeaders.get(curHeader));
                }
            }

        } else {
            if (initialFragment != null && mSinglePane) {
                // If we are just showing a fragment, we want to run in
                // new fragment mode, but don't need to compute and show
                // the headers.
                switchToHeader(initialFragment, initialArguments);
                if (initialTitle != 0) {
                    CharSequence initialTitleStr = getText(initialTitle);
                    CharSequence initialShortTitleStr = initialShortTitle != 0
                            ? getText(initialShortTitle) : null;
                    showBreadCrumbs(initialTitleStr, initialShortTitleStr);
                }

            } else {
                // We need to try to build the headers.
                onBuildHeaders(mHeaders);

                // If there are headers, then at this point we need to show
                // them and, depending on the screen, we may also show in-line
                // the currently selected preference fragment.
                if (mHeaders.size() > 0) {
                    if (!mSinglePane) {
                        if (initialFragment == null) {
                            Header h = onGetInitialHeader();
                            switchToHeader(h);
                        } else {
                            switchToHeader(initialFragment, initialArguments);
                        }
                    }
                }
            }
        }

        // The default configuration is to only show the list view.  Adjust
        // visibility for other configurations.
        if (initialFragment != null && mSinglePane) {
            // Single pane, showing just a prefs fragment.
            findViewById(R.id.headers).setVisibility(View.GONE);
            mPrefsContainer.setVisibility(View.VISIBLE);
            if (initialTitle != 0) {
                CharSequence initialTitleStr = getText(initialTitle);
                CharSequence initialShortTitleStr = initialShortTitle != 0
                        ? getText(initialShortTitle) : null;
                showBreadCrumbs(initialTitleStr, initialShortTitleStr);
            }
        } else if (mHeaders.size() > 0) {
            HeaderAdapter adapter = new HeaderAdapter(this, mHeaders, mPreferenceHeaderItemResId,
                    mPreferenceHeaderRemoveEmptyIcon);
            adapter.setOnHeaderClickListener(mOnHeaderClickListener);
            setListAdapter(adapter);
            if (!mSinglePane) {
                // Multi-pane.
                getListAdapter().setMode(SelectableAdapter.MODE_SINGLE);
                if (mCurHeader != null) {
                    setSelectedHeader(mCurHeader);
                }
                mPrefsContainer.setVisibility(View.VISIBLE);
            }
        }

        if (mTitleView != null) {
            mTitleView.setText(getTitle());
        }

        // see if we should show Back/Next buttons
        Intent intent = getIntent();
        if (intent.getBooleanExtra(EXTRA_PREFS_SHOW_BUTTON_BAR, false)) {

            findViewById(R.id.button_bar).setVisibility(View.VISIBLE);

            Button backButton = (Button)findViewById(R.id.back_button);
            backButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
            Button skipButton = (Button)findViewById(R.id.skip_button);
            skipButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setResult(RESULT_OK);
                    finish();
                }
            });
            mNextButton = (Button)findViewById(R.id.next_button);
            mNextButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setResult(RESULT_OK);
                    finish();
                }
            });

            // set our various button parameters
            if (intent.hasExtra(EXTRA_PREFS_SET_NEXT_TEXT)) {
                String buttonText = intent.getStringExtra(EXTRA_PREFS_SET_NEXT_TEXT);
                if (TextUtils.isEmpty(buttonText)) {
                    mNextButton.setVisibility(View.GONE);
                }
                else {
                    mNextButton.setText(buttonText);
                }
            }
            if (intent.hasExtra(EXTRA_PREFS_SET_BACK_TEXT)) {
                String buttonText = intent.getStringExtra(EXTRA_PREFS_SET_BACK_TEXT);
                if (TextUtils.isEmpty(buttonText)) {
                    backButton.setVisibility(View.GONE);
                }
                else {
                    backButton.setText(buttonText);
                }
            }
            if (intent.getBooleanExtra(EXTRA_PREFS_SHOW_SKIP, false)) {
                skipButton.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Returns true if this activity is currently showing the header list.
     */
    public boolean hasHeaders() {
        return getListView().getVisibility() == View.VISIBLE;
    }

    /**
     * Returns the Header list
     * @hide
     */
    public List<Header> getHeaders() {
        return mHeaders;
    }

    /**
     * Returns true if this activity is showing multiple panes -- the headers
     * and a preference fragment.
     */
    public boolean isMultiPane() {
        return hasHeaders() && mPrefsContainer.getVisibility() == View.VISIBLE;
    }

    /**
     * Called to determine if the activity should run in multi-pane mode.
     * The default implementation returns true if the screen is large
     * enough.
     */
    public boolean onIsMultiPane() {
        boolean preferMultiPane = getResources().getBoolean(
                R.bool.tic_preferences_prefer_dual_pane);
        return preferMultiPane;
    }

    /**
     * Called to determine whether the header list should be hidden.
     * The default implementation returns the
     * value given in {@link #EXTRA_NO_HEADERS} or false if it is not supplied.
     * This is set to false, for example, when the activity is being re-launched
     * to show a particular preference activity.
     */
    public boolean onIsHidingHeaders() {
        return getIntent().getBooleanExtra(EXTRA_NO_HEADERS, false);
    }

    /**
     * Called to determine the initial header to be shown.  The default
     * implementation simply returns the fragment of the first header.  Note
     * that the returned Header object does not actually need to exist in
     * your header list -- whatever its fragment is will simply be used to
     * show for the initial UI.
     */
    public Header onGetInitialHeader() {
        for (int i=0; i<mHeaders.size(); i++) {
            Header h = mHeaders.get(i);
            if (h.fragment != null) {
                return h;
            }
        }
        throw new IllegalStateException("Must have at least one header with a fragment");
    }

    /**
     * Called after the header list has been updated ({@link #onBuildHeaders}
     * has been called and returned due to {@link #invalidateHeaders()}) to
     * specify the header that should now be selected.  The default implementation
     * returns null to keep whatever header is currently selected.
     */
    public Header onGetNewHeader() {
        return null;
    }

    /**
     * Called when the activity needs its list of headers build.  By
     * implementing this and adding at least one item to the list, you
     * will cause the activity to run in its modern fragment mode.  Note
     * that this function may not always be called; for example, if the
     * activity has been asked to display a particular fragment without
     * the header list, there is no need to build the headers.
     *
     * <p>Typical implementations will use {@link #loadHeadersFromResource}
     * to fill in the list from a resource.
     *
     * @param target The list in which to place the headers.
     */
    public void onBuildHeaders(List<Header> target) {
        // Should be overloaded by subclasses
    }

    @Override
    public SelectableAdapter getListAdapter() {
        return (SelectableAdapter) super.getListAdapter();
    }

    @Override
    public TicklableRecyclerView getListView() {
        return (TicklableRecyclerView) super.getListView();
    }

    /**
     * Call when you need to change the headers being displayed.  Will result
     * in onBuildHeaders() later being called to retrieve the new list.
     */
    public void invalidateHeaders() {
        if (!mHandler.hasMessages(MSG_BUILD_HEADERS)) {
            mHandler.sendEmptyMessage(MSG_BUILD_HEADERS);
        }
    }

    /**
     * Parse the given XML file as a header description, adding each
     * parsed Header into the target list.
     *
     * @param resid The XML resource to load and parse.
     * @param target The list in which the parsed headers should be placed.
     */
    public void loadHeadersFromResource(@XmlRes int resid, List<Header> target) {
        XmlResourceParser parser = null;
        try {
            parser = getResources().getXml(resid);
            AttributeSet attrs = Xml.asAttributeSet(parser);

            int type;
            while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
                    && type != XmlPullParser.START_TAG) {
                // Parse next until start tag is found
            }

            String nodeName = parser.getName();
            if (!"preference-headers".equals(nodeName)) {
                throw new RuntimeException(
                        "XML document must start with <preference-headers> tag; found"
                                + nodeName + " at " + parser.getPositionDescription());
            }

            Bundle curBundle = null;

            final int outerDepth = parser.getDepth();
            while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }

                nodeName = parser.getName();
                if ("header".equals(nodeName)) {
                    Header header = new Header();

                    TypedArray sa = obtainStyledAttributes(
                            attrs, R.styleable.PreferenceHeader);
                    header.id = sa.getResourceId(
                            R.styleable.PreferenceHeader_android_id,
                            (int)HEADER_ID_UNDEFINED);
                    TypedValue tv = sa.peekValue(
                            R.styleable.PreferenceHeader_android_title);
                    if (tv != null && tv.type == TypedValue.TYPE_STRING) {
                        if (tv.resourceId != 0) {
                            header.titleRes = tv.resourceId;
                        } else {
                            header.title = tv.string;
                        }
                    }
                    tv = sa.peekValue(
                            R.styleable.PreferenceHeader_android_summary);
                    if (tv != null && tv.type == TypedValue.TYPE_STRING) {
                        if (tv.resourceId != 0) {
                            header.summaryRes = tv.resourceId;
                        } else {
                            header.summary = tv.string;
                        }
                    }
                    tv = sa.peekValue(
                            R.styleable.PreferenceHeader_android_breadCrumbTitle);
                    if (tv != null && tv.type == TypedValue.TYPE_STRING) {
                        if (tv.resourceId != 0) {
                            header.breadCrumbTitleRes = tv.resourceId;
                        } else {
                            header.breadCrumbTitle = tv.string;
                        }
                    }
                    tv = sa.peekValue(
                            R.styleable.PreferenceHeader_android_breadCrumbShortTitle);
                    if (tv != null && tv.type == TypedValue.TYPE_STRING) {
                        if (tv.resourceId != 0) {
                            header.breadCrumbShortTitleRes = tv.resourceId;
                        } else {
                            header.breadCrumbShortTitle = tv.string;
                        }
                    }
                    header.iconRes = sa.getResourceId(
                            R.styleable.PreferenceHeader_android_icon, 0);
                    header.fragment = sa.getString(
                            R.styleable.PreferenceHeader_android_fragment);
                    sa.recycle();

                    if (curBundle == null) {
                        curBundle = new Bundle();
                    }

                    final int innerDepth = parser.getDepth();
                    while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
                            && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                        if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                            continue;
                        }

                        String innerNodeName = parser.getName();
                        if (innerNodeName.equals("extra")) {
                            getResources().parseBundleExtra("extra", attrs, curBundle);
                            XmlUtils.skipCurrentTag(parser);

                        } else if (innerNodeName.equals("intent")) {
                            header.intent = Intent.parseIntent(getResources(), parser, attrs);

                        } else {
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }

                    if (curBundle.size() > 0) {
                        header.fragmentArguments = curBundle;
                        curBundle = null;
                    }

                    target.add(header);
                } else {
                    XmlUtils.skipCurrentTag(parser);
                }
            }

        } catch (XmlPullParserException e) {
            throw new RuntimeException("Error parsing headers", e);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing headers", e);
        } finally {
            if (parser != null) parser.close();
        }
    }

    /**
     * Subclasses should override this method and verify that the given fragment is a valid type
     * to be attached to this activity. The default implementation returns <code>true</code> for
     * apps built for <code>android:targetSdkVersion</code> older than
     * {@link android.os.Build.VERSION_CODES#KITKAT}. For later versions, it will throw an exception.
     * @param fragmentName the class name of the Fragment about to be attached to this activity.
     * @return true if the fragment class name is valid for this Activity and false otherwise.
     */
    protected boolean isValidFragment(String fragmentName) {
        if (getApplicationInfo().targetSdkVersion  >= android.os.Build.VERSION_CODES.KITKAT) {
            throw new RuntimeException(
                    "Subclasses of PreferenceActivity must override isValidFragment(String)"
                            + " to verify that the Fragment class is valid! " + this.getClass().getName()
                            + " has not checked if fragment " + fragmentName + " is valid.");
        } else {
            return true;
        }
    }

    /**
     * Set a footer that should be shown at the bottom of the header list.
     */
    public void setListFooter(View view) {
        mListFooter.removeAllViews();
        mListFooter.addView(view, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
    }

    @Override
    protected void onDestroy() {
        mHandler.removeMessages(MSG_BUILD_HEADERS);

        if (getListAdapter() instanceof HeaderAdapter) {
            ((HeaderAdapter) getListAdapter()).setOnHeaderClickListener(null);
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getListAdapter() instanceof HeaderAdapter) {
            HeaderAdapter headerAdapter = (HeaderAdapter) getListAdapter();
            headerAdapter.resetClickEntering();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mHeaders.size() > 0) {
            outState.putParcelableArrayList(HEADERS_TAG, mHeaders);
            if (mCurHeader != null) {
                int index = mHeaders.indexOf(mCurHeader);
                if (index >= 0) {
                    outState.putInt(CUR_HEADER_TAG, index);
                }
            }
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        // Only call this if we didn't save the instance state for later.
        // If we did save it, it will be restored when we bind the adapter.
        super.onRestoreInstanceState(state);
    }

    HeaderAdapter.OnHeaderClickListener mOnHeaderClickListener = new HeaderAdapter.OnHeaderClickListener() {
        @Override
        public boolean onHeaderClick(Header header, int position) {
            return PreferenceActivity.this.onHeaderClick(header, position);
        }
    };

    /**
     * Called when the user selects an item in the header list.  The default
     * implementation will call either
     * {@link #startWithFragment(String, Bundle, Fragment, int, int, int)}
     * or {@link #switchToHeader(Header)} as appropriate.
     *
     * @param header The header that was selected.
     * @param position The header's position in the list.
     */
    public boolean onHeaderClick(Header header, int position) {
        if (header.fragment != null) {
            if (mSinglePane) {
                int titleRes = header.breadCrumbTitleRes;
                int shortTitleRes = header.breadCrumbShortTitleRes;
                if (titleRes == 0) {
                    titleRes = header.titleRes;
                    shortTitleRes = 0;
                }
                startWithFragment(header.fragment, header.fragmentArguments, null, 0,
                        titleRes, shortTitleRes);
                return true;
            } else {
                switchToHeader(header);
            }
        } else if (header.intent != null) {
            startActivity(header.intent);
            return true;
        }
        return false;
    }

    /**
     * Called by {@link #startWithFragment(String, Bundle, Fragment, int, int, int)} when
     * in single-pane mode, to build an Intent to launch a new activity showing
     * the selected fragment.  The default implementation constructs an Intent
     * that re-launches the current activity with the appropriate arguments to
     * display the fragment.
     *
     * @param fragmentName The name of the fragment to display.
     * @param args Optional arguments to supply to the fragment.
     * @param titleRes Optional resource ID of title to show for this item.
     * @param shortTitleRes Optional resource ID of short title to show for this item.
     * @return Returns an Intent that can be launched to display the given
     * fragment.
     */
    public Intent onBuildStartFragmentIntent(String fragmentName, Bundle args,
                                             @StringRes int titleRes, int shortTitleRes) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClass(this, getClass());
        intent.putExtra(EXTRA_SHOW_FRAGMENT, fragmentName);
        intent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, args);
        intent.putExtra(EXTRA_SHOW_FRAGMENT_TITLE, titleRes);
        intent.putExtra(EXTRA_SHOW_FRAGMENT_SHORT_TITLE, shortTitleRes);
        intent.putExtra(EXTRA_NO_HEADERS, true);
        return intent;
    }

    /**
     * Like {@link #startWithFragment(String, Bundle, Fragment, int, int, int)}
     * but uses a 0 titleRes.
     */
    public void startWithFragment(String fragmentName, Bundle args,
                                  Fragment resultTo, int resultRequestCode) {
        startWithFragment(fragmentName, args, resultTo, resultRequestCode, 0, 0);
    }

    /**
     * Start a new instance of this activity, showing only the given
     * preference fragment.  When launched in this mode, the header list
     * will be hidden and the given preference fragment will be instantiated
     * and fill the entire activity.
     *
     * @param fragmentName The name of the fragment to display.
     * @param args Optional arguments to supply to the fragment.
     * @param resultTo Option fragment that should receive the result of
     * the activity launch.
     * @param resultRequestCode If resultTo is non-null, this is the request
     * code in which to report the result.
     * @param titleRes Resource ID of string to display for the title of
     * this set of preferences.
     * @param shortTitleRes Resource ID of string to display for the short title of
     * this set of preferences.
     */
    public void startWithFragment(String fragmentName, Bundle args,
                                  Fragment resultTo, int resultRequestCode, @StringRes int titleRes,
                                  @StringRes int shortTitleRes) {
        Intent intent = onBuildStartFragmentIntent(fragmentName, args, titleRes, shortTitleRes);
        if (resultTo == null) {
            startActivity(intent);
        } else {
            resultTo.startActivityForResult(intent, resultRequestCode);
        }
    }

    /**
     * Change the base title of the bread crumbs for the current preferences.
     * This will normally be called for you.  See
     * {@link android.app.FragmentBreadCrumbs} for more information.
     */
    public void showBreadCrumbs(CharSequence title, CharSequence shortTitle) {
    }

    /**
     * Should be called after onCreate to ensure that the breadcrumbs, if any, were created.
     * This prepends a title to the fragment breadcrumbs and attaches a listener to any clicks
     * on the parent entry.
     * @param title the title for the breadcrumb
     * @param shortTitle the short title for the breadcrumb
     */
    public void setParentTitle(CharSequence title, CharSequence shortTitle,
                               View.OnClickListener listener) {
    }

    void setSelectedHeader(Header header) {
        mCurHeader = header;
        if (getListAdapter() != null) {
            int index = mHeaders.indexOf(header);
            if (index >= 0) {
                if (getListAdapter().getSelectedItemCount() != 1 ||
                        ((Integer) getListAdapter().getSelectedItems().get(0)) != index) {
                    getListAdapter().clearSelection();
                    getListAdapter().toggleSelection(index);
                }
            } else {
                getListAdapter().clearSelection();
            }
        }
        showBreadCrumbs(header);
    }

    void showBreadCrumbs(Header header) {
        if (header != null) {
            CharSequence title = header.getBreadCrumbTitle(getResources());
            if (title == null) title = header.getTitle(getResources());
            if (title == null) title = getTitle();
            showBreadCrumbs(title, header.getBreadCrumbShortTitle(getResources()));
        } else {
            showBreadCrumbs(getTitle(), null);
        }
    }

    private void switchToHeaderInner(String fragmentName, Bundle args) {
        getFragmentManager().popBackStack(BACK_STACK_PREFS,
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
        if (!isValidFragment(fragmentName)) {
            throw new IllegalArgumentException("Invalid fragment for this activity: "
                    + fragmentName);
        }
        Fragment f = Fragment.instantiate(this, fragmentName, args);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.prefs, f);
        transaction.commitAllowingStateLoss();
    }

    /**
     * When in two-pane mode, switch the fragment pane to show the given
     * preference fragment.
     *
     * @param fragmentName The name of the fragment to display.
     * @param args Optional arguments to supply to the fragment.
     */
    public void switchToHeader(String fragmentName, Bundle args) {
        Header selectedHeader = null;
        for (int i = 0; i < mHeaders.size(); i++) {
            if (fragmentName.equals(mHeaders.get(i).fragment)) {
                selectedHeader = mHeaders.get(i);
                break;
            }
        }
        setSelectedHeader(selectedHeader);
        switchToHeaderInner(fragmentName, args);
    }

    /**
     * When in two-pane mode, switch to the fragment pane to show the given
     * preference fragment.
     *
     * @param header The new header to display.
     */
    public void switchToHeader(Header header) {
        if (mCurHeader == header) {
            // This is the header we are currently displaying.  Just make sure
            // to pop the stack up to its root state.
            getFragmentManager().popBackStack(BACK_STACK_PREFS,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            if (header.fragment == null) {
                throw new IllegalStateException("can't switch to header that has no fragment");
            }
            switchToHeaderInner(header.fragment, header.fragmentArguments);
            setSelectedHeader(header);
        }
    }

    Header findBestMatchingHeader(Header cur, ArrayList<Header> from) {
        ArrayList<Header> matches = new ArrayList<Header>();
        for (int j=0; j<from.size(); j++) {
            Header oh = from.get(j);
            if (cur == oh || (cur.id != HEADER_ID_UNDEFINED && cur.id == oh.id)) {
                // Must be this one.
                matches.clear();
                matches.add(oh);
                break;
            }
            if (cur.fragment != null) {
                if (cur.fragment.equals(oh.fragment)) {
                    matches.add(oh);
                }
            } else if (cur.intent != null) {
                if (cur.intent.equals(oh.intent)) {
                    matches.add(oh);
                }
            } else if (cur.title != null) {
                if (cur.title.equals(oh.title)) {
                    matches.add(oh);
                }
            }
        }
        final int NM = matches.size();
        if (NM == 1) {
            return matches.get(0);
        } else if (NM > 1) {
            for (int j=0; j<NM; j++) {
                Header oh = matches.get(j);
                if (cur.fragmentArguments != null &&
                        cur.fragmentArguments.equals(oh.fragmentArguments)) {
                    return oh;
                }
                if (cur.extras != null && cur.extras.equals(oh.extras)) {
                    return oh;
                }
                if (cur.title != null && cur.title.equals(oh.title)) {
                    return oh;
                }
            }
        }
        return null;
    }

    /**
     * Start a new fragment.
     *
     * @param fragment The fragment to start
     * @param push If true, the current fragment will be pushed onto the back stack.  If false,
     * the current fragment will be replaced.
     */
    public void startPreferenceFragment(Fragment fragment, boolean push) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.prefs, fragment);
        if (push) {
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(BACK_STACK_PREFS);
        } else {
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        }
        transaction.commitAllowingStateLoss();
    }

    /**
     * Start a new fragment containing a preference panel.  If the preferences
     * are being displayed in multi-pane mode, the given fragment class will
     * be instantiated and placed in the appropriate pane.  If running in
     * single-pane mode, a new activity will be launched in which to show the
     * fragment.
     *
     * @param fragmentClass Full name of the class implementing the fragment.
     * @param args Any desired arguments to supply to the fragment.
     * @param titleRes Optional resource identifier of the title of this
     * fragment.
     * @param titleText Optional text of the title of this fragment.
     * @param resultTo Optional fragment that result data should be sent to.
     * If non-null, resultTo.onActivityResult() will be called when this
     * preference panel is done.  The launched panel must use
     * {@link #finishPreferencePanel(Fragment, int, Intent)} when done.
     * @param resultRequestCode If resultTo is non-null, this is the caller's
     * request code to be received with the result.
     */
    public void startPreferencePanel(String fragmentClass, Bundle args, @StringRes int titleRes,
                                     CharSequence titleText, Fragment resultTo, int resultRequestCode) {
        if (mSinglePane) {
            startWithFragment(fragmentClass, args, resultTo, resultRequestCode, titleRes, 0);
        } else {
            Fragment f = Fragment.instantiate(this, fragmentClass, args);
            if (resultTo != null) {
                f.setTargetFragment(resultTo, resultRequestCode);
            }
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.prefs, f);
            if (titleRes != 0) {
                transaction.setBreadCrumbTitle(titleRes);
            } else if (titleText != null) {
                transaction.setBreadCrumbTitle(titleText);
            }
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(BACK_STACK_PREFS);
            transaction.commitAllowingStateLoss();
        }
    }

    /**
     * Called by a preference panel fragment to finish itself.
     *
     * @param caller The fragment that is asking to be finished.
     * @param resultCode Optional result code to send back to the original
     * launching fragment.
     * @param resultData Optional result data to send back to the original
     * launching fragment.
     */
    public void finishPreferencePanel(Fragment caller, int resultCode, Intent resultData) {
        if (mSinglePane) {
            setResult(resultCode, resultData);
            finish();
        } else {
            // XXX be smarter about popping the stack.
            onBackPressed();
            if (caller != null) {
                if (caller.getTargetFragment() != null) {
                    caller.getTargetFragment().onActivityResult(caller.getTargetRequestCode(),
                            resultCode, resultData);
                }
            }
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        startPreferencePanel(pref.getFragment(), pref.getExtras(), pref.getTitleRes(),
                pref.getTitle(), null, 0);
        return true;
    }

    // give subclasses access to the Next button
    /** @hide */
    protected boolean hasNextButton() {
        return mNextButton != null;
    }
    /** @hide */
    protected Button getNextButton() {
        return mNextButton;
    }
}
