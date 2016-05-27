/*
 * Copyright (C) 2016 Mobvoi Inc.
 * Copyright (C) 2006 The Android Open Source Project
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

package ticwear.design.widget;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import ticwear.design.DesignConfig;

/**
 * An easy adapter to map static data to views defined in an XML file like {@link SimpleAdapter} for
 * {@link RecyclerView}.
 */
public class SimpleRecyclerAdapter extends RecyclerView.Adapter<SimpleRecyclerAdapter.ViewHolder> {

    static final String TAG = "SimpleRA";

    private final LayoutInflater mInflater;

    private int[] mTo;
    private String[] mFrom;
    private ViewBinder mViewBinder;
    private ViewHolderCreator mViewHolderCreator;

    private List<? extends Map<String, ?>> mData;

    private int mResource;

    /**
     * Constructor
     *
     * @param context The context where the View associated with this SimpleAdapter is running
     * @param data A List of Maps. Each entry in the List corresponds to one row in the list. The
     *        Maps contain the data for each row, and should include all the entries specified in
     *        "from"
     * @param resource Resource identifier of a view layout that defines the views for this list
     *        item. The layout file should include at least those named views defined in "to"
     * @param from A list of column names that will be added to the Map associated with each
     *        item.
     * @param to The views that should display column in the "from" parameter. These should all be
     *        TextViews. The first N views in this list are given the values of the first N columns
     *        in the from parameter.
     */
    public SimpleRecyclerAdapter(Context context, List<? extends Map<String, ?>> data,
                                 @LayoutRes int resource, String[] from, @IdRes int[] to) {
        mData = data;
        mResource = resource;
        mFrom = from;
        mTo = to;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setHasStableIds(true);
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @see android.widget.Adapter#getItem(int)
     */
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = createViewFromResource(mInflater, parent, mResource);
        if (mViewHolderCreator != null) {
            return mViewHolderCreator.create(v, mTo);
        } else {
            return new ViewHolder(v, mTo);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        bindView(holder, position);
    }

    /**
     * @see RecyclerView.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * @see RecyclerView.Adapter#getItemCount()
     */
    @Override
    public int getItemCount() {
        return mData.size();
    }

    protected View createViewFromResource(LayoutInflater inflater, ViewGroup parent, int resource) {
        return inflater.inflate(resource, parent, false);
    }

    private void bindView(ViewHolder holder, int position) {
        final Map dataSet = mData.get(position);
        if (dataSet == null) {
            return;
        }

        if (DesignConfig.DEBUG_RECYCLER_VIEW) {
            Log.v(TAG, holder.getLogPrefix() + "bind to " + dataSet + holder.getLogSuffix());
        }

        holder.dataSet = dataSet;

        final ViewBinder binder = mViewBinder;
        final String[] from = mFrom;
        final int[] to = mTo;
        final int count = to.length;

        for (int i = 0; i < count; i++) {
            final View v = holder.views[i];
            if (v != null) {
                final Object data = dataSet.get(from[i]);
                String text = data == null ? "" : data.toString();
                if (text == null) {
                    text = "";
                }

                boolean bound = false;
                if (binder != null) {
                    bound = binder.setViewValue(v, data, text);
                }

                if (!bound) {
                    if (v instanceof Checkable) {
                        if (data instanceof Boolean) {
                            ((Checkable) v).setChecked((Boolean) data);
                        } else if (v instanceof TextView) {
                            // Note: keep the instanceof TextView check at the bottom of these
                            // ifs since a lot of views are TextViews (e.g. CheckBoxes).
                            setViewText((TextView) v, text);
                        } else {
                            throw new IllegalStateException(v.getClass().getName() +
                                    " should be bound to a Boolean, not a " +
                                    (data == null ? "<unknown type>" : data.getClass()));
                        }
                    } else if (v instanceof TextView) {
                        // Note: keep the instanceof TextView check at the bottom of these
                        // ifs since a lot of views are TextViews (e.g. CheckBoxes).
                        if (data instanceof Integer) {
                            setViewText((TextView) v, (Integer) data);
                        } else {
                            setViewText((TextView) v, text);
                        }
                    } else if (v instanceof ImageView) {
                        if (data instanceof Integer) {
                            setViewImage((ImageView) v, (Integer) data);
                        } else {
                            setViewImage((ImageView) v, text);
                        }
                    } else {
                        throw new IllegalStateException(v.getClass().getName() + " is not a " +
                                " view that can be bounds by this SimpleAdapter");
                    }
                }
            }
        }
    }

    /**
     * Returns the {@link ViewBinder} used to bind data to views.
     *
     * @return a ViewBinder or null if the binder does not exist
     *
     * @see #setViewBinder(ViewBinder)
     */
    public ViewBinder getViewBinder() {
        return mViewBinder;
    }

    /**
     * Sets the binder used to bind data to views.
     *
     * @param viewBinder the binder used to bind data to views, can be null to
     *        remove the existing binder
     *
     * @see #getViewBinder()
     */
    public void setViewBinder(ViewBinder viewBinder) {
        mViewBinder = viewBinder;
    }

    public ViewHolderCreator getViewHolderCreator() {
        return mViewHolderCreator;
    }

    public void setViewHolderCreator(ViewHolderCreator viewHolderCreator) {
        this.mViewHolderCreator = viewHolderCreator;
    }

    /**
     * Called by bindView() to set the image for an ImageView but only if
     * there is no existing ViewBinder or if the existing ViewBinder cannot
     * handle binding to an ImageView.
     *
     * This method is called instead of {@link #setViewImage(ImageView, String)}
     * if the supplied data is an int or Integer.
     *
     * @param v ImageView to receive an image
     * @param value the value retrieved from the data set
     *
     * @see #setViewImage(ImageView, String)
     */
    public void setViewImage(ImageView v, int value) {
        v.setImageResource(value);
    }

    /**
     * Called by bindView() to set the image for an ImageView but only if
     * there is no existing ViewBinder or if the existing ViewBinder cannot
     * handle binding to an ImageView.
     *
     * By default, the value will be treated as an image resource. If the
     * value cannot be used as an image resource, the value is used as an
     * image Uri.
     *
     * This method is called instead of {@link #setViewImage(ImageView, int)}
     * if the supplied data is not an int or Integer.
     *
     * @param v ImageView to receive an image
     * @param value the value retrieved from the data set
     *
     * @see #setViewImage(ImageView, int)
     */
    public void setViewImage(ImageView v, String value) {
        try {
            v.setImageResource(Integer.parseInt(value));
        } catch (NumberFormatException nfe) {
            v.setImageURI(Uri.parse(value));
        }
    }

    /**
     * Called by bindView() to set the text for a TextView but only if
     * there is no existing ViewBinder or if the existing ViewBinder cannot
     * handle binding to a TextView.
     *
     * @param v TextView to receive text
     * @param text the text to be set for the TextView
     */
    public void setViewText(TextView v, String text) {
        v.setText(text);
    }

    /**
     * Called by bindView() to set the text for a TextView but only if
     * there is no existing ViewBinder or if the existing ViewBinder cannot
     * handle binding to a TextView.
     *
     * @param v TextView to receive text
     * @param resId the text resource to be set for the TextView
     */
    public void setViewText(TextView v, int resId) {
        v.setText(resId);
    }

    /**
     * This class can be used by external clients of SimpleAdapter to bind
     * values to views.
     *
     * You should use this class to bind values to views that are not
     * directly supported by SimpleAdapter or to change the way binding
     * occurs for views supported by SimpleAdapter.
     *
     * @see SimpleRecyclerAdapter#setViewImage(ImageView, int)
     * @see SimpleRecyclerAdapter#setViewImage(ImageView, String)
     * @see SimpleRecyclerAdapter#setViewText(TextView, String)
     */
    public interface ViewBinder {
        /**
         * Binds the specified data to the specified view.
         *
         * When binding is handled by this ViewBinder, this method must return true.
         * If this method returns false, SimpleAdapter will attempts to handle
         * the binding on its own.
         *
         * @param view the view to bind the data to
         * @param data the data to bind to the view
         * @param textRepresentation a safe String representation of the supplied data:
         *        it is either the result of data.toString() or an empty String but it
         *        is never null
         *
         * @return true if the data was bound to the view, false otherwise
         */
        boolean setViewValue(android.view.View view, Object data, String textRepresentation);
    }

    public static class ViewHolder extends FocusableLinearLayoutManager.ViewHolder {

        private final View[] views;
        private Map dataSet;

        public ViewHolder(View itemView, final int[] to) {
            super(itemView);
            final int count = to.length;
            views = new View[count];
            for (int i = 0; i < count; i++) {
                views[i] = itemView.findViewById(to[i]);
            }
        }

        protected Object getBindingData(String key) {
            return dataSet.get(key);
        }
    }

    public interface ViewHolderCreator {

        ViewHolder create(View view, int[] to);
    }

}
