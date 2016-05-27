/*
 * Copyright (c) 2016 Mobvoi Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ticwear.design.widget;

import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.util.LongSparseArray;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.Checkable;
import android.widget.ListAdapter;

public class TrackSelectionAdapterWrapper<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    final RecyclerView.Adapter<VH> mAdapter;

    /**
     * The listener that receives notifications when an item is selected.
     */
    OnItemSelectedListener mOnItemSelectedListener;

    /**
     * The listener that receives notifications when an item is clicked.
     */
    OnItemClickListener mOnItemClickListener;

    /**
     * The listener that receives notifications when an item is long clicked.
     */
    OnItemLongClickListener mOnItemLongClickListener;

    RecyclerView mAttachedRecyclerView;

    /**
     * Controls if/how the user may choose/check items in the list
     */
    int mChoiceMode = AbsListView.CHOICE_MODE_NONE;

    /**
     * Controls CHOICE_MODE_MULTIPLE_MODAL. null when inactive.
     */
    ActionMode mChoiceActionMode;

    /**
     * Wrapper for the multiple choice mode callback; AbsListView needs to perform
     * a few extra actions around what application code does.
     */
    MultiChoiceModeWrapper mMultiChoiceModeCallback;

    /**
     * Running count of how many items are currently checked
     */
    int mCheckedItemCount;

    /**
     * Running state of which positions are currently checked
     */
    SparseBooleanArray mCheckStates;

    /**
     * Running state of which IDs are currently checked.
     * If there is a value for a given key, the checked state for that ID is true
     * and the value holds the last known position in the adapter for that id.
     */
    LongSparseArray<Integer> mCheckedIdStates;

    public TrackSelectionAdapterWrapper(RecyclerView.Adapter<VH> adapter) {
        this.mAdapter = adapter;

        setHasStableIds(mAdapter.hasStableIds());
    }

    public RecyclerView.Adapter<VH> getAdapter() {
        return mAdapter;
    }

    @Override
    public int getItemViewType(int position) {
        return mAdapter.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return mAdapter.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return mAdapter.getItemCount();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        final VH vh = mAdapter.onCreateViewHolder(parent, viewType);
        View itemView = vh.itemView;

        // Handle item click and set the selection
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performItemClick(v, vh.getLayoutPosition(), vh.getItemId());
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return performLongPress(v, vh.getLayoutPosition(), vh.getItemId());
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(VH viewHolder, int position) {
        mAdapter.onBindViewHolder(viewHolder, position);
        if (mChoiceMode != AbsListView.CHOICE_MODE_NONE) {
            // Set selected state; use a state list drawable to style the view
            View itemView = viewHolder.itemView;
            updateCheckState(itemView, position);
        }
    }

    private void updateCheckState(View itemView, int position) {
        if (itemView instanceof Checkable) {
            ((Checkable) itemView).setChecked(mCheckStates.get(position));
        } else {
            itemView.setActivated(mCheckStates.get(position));
        }
    }

    private void updateCheckStateIfPossible(View itemView, int position) {
        if (itemView == null) {
            itemView = findItemView(position);
        }

        if (itemView != null) {
            updateCheckState(itemView, position);
        } else if (canNotifyChange()) {
            notifyItemChanged(position);
        }
    }

    private boolean canNotifyChange() {
        return mAttachedRecyclerView != null && !mAttachedRecyclerView.isComputingLayout();
    }

    private View findItemView(int position) {
        if (mAttachedRecyclerView == null || Looper.getMainLooper() != Looper.myLooper()) {
            return null;
        }
        for (int i = 0; i < mAttachedRecyclerView.getChildCount(); i++) {
            View child = mAttachedRecyclerView.getChildAt(i);
            if (mAttachedRecyclerView.getChildAdapterPosition(child) == position) {
                return child;
            }
        }

        return null;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        if (mAttachedRecyclerView == recyclerView) {
            return;
        }
        if (mAttachedRecyclerView != null) {
            throw new RuntimeException("Can't attach TrackSelectionAdapterWrapper to multiple RecyclerView.");
        }
        mAttachedRecyclerView = recyclerView;
        if (mChoiceMode == AbsListView.CHOICE_MODE_MULTIPLE_MODAL) {
            recyclerView.setLongClickable(true);
        }
        startSelectionModeIfNeeded(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mAttachedRecyclerView = null;
        if (mChoiceActionMode != null) {
            mChoiceActionMode.finish();
            mChoiceActionMode = null;
        }
    }

    /**
     * @see #setChoiceMode(int)
     *
     * @return The current choice mode
     */
    public int getChoiceMode() {
        return mChoiceMode;
    }

    /**
     * Defines the choice behavior for the List. By default, Lists do not have any choice behavior
     * ({@link AbsListView#CHOICE_MODE_NONE}). By setting the choiceMode to {@link AbsListView#CHOICE_MODE_SINGLE}, the
     * List allows up to one item to  be in a chosen state. By setting the choiceMode to
     * {@link AbsListView#CHOICE_MODE_MULTIPLE}, the list allows any number of items to be chosen.
     *
     * @param choiceMode One of {@link AbsListView#CHOICE_MODE_NONE}, {@link AbsListView#CHOICE_MODE_SINGLE}, or
     * {@link AbsListView#CHOICE_MODE_MULTIPLE}
     */
    public void setChoiceMode(int choiceMode) {
        mChoiceMode = choiceMode;
        if (mChoiceActionMode != null) {
            mChoiceActionMode.finish();
            mChoiceActionMode = null;
        }
        if (mChoiceMode != AbsListView.CHOICE_MODE_NONE) {
            if (mCheckStates == null) {
                mCheckStates = new SparseBooleanArray(0);
            }
            if (mCheckedIdStates == null && hasStableIds()) {
                mCheckedIdStates = new LongSparseArray<Integer>(0);
            }
            // Modal multi-choice mode only has choices when the mode is active. Clear them.
            if (mChoiceMode == AbsListView.CHOICE_MODE_MULTIPLE_MODAL) {
                clearChoices();
                if (mAttachedRecyclerView != null) {
                    mAttachedRecyclerView.setLongClickable(true);
                }
            }
        }
    }

    /**
     * Returns the number of items currently selected. This will only be valid
     * if the choice mode is not {@link AbsListView#CHOICE_MODE_NONE} (default).
     *
     * <p>To determine the specific items that are currently selected, use one of
     * the <code>getChecked*</code> methods.
     *
     * @return The number of items currently selected
     *
     * @see #getCheckedItemPosition()
     * @see #getCheckedItemPositions()
     * @see #getCheckedItemIds()
     */
    public int getCheckedItemCount() {
        return mCheckedItemCount;
    }

    /**
     * Returns the checked state of the specified position. The result is only
     * valid if the choice mode has been set to {@link AbsListView#CHOICE_MODE_SINGLE}
     * or {@link AbsListView#CHOICE_MODE_MULTIPLE}.
     *
     * @param position The item whose checked state to return
     * @return The item's checked state or <code>false</code> if choice mode
     *         is invalid
     *
     * @see #setChoiceMode(int)
     */
    public boolean isItemChecked(int position) {
        if (mChoiceMode != AbsListView.CHOICE_MODE_NONE && mCheckStates != null) {
            return mCheckStates.get(position);
        }

        return false;
    }

    /**
     * Returns the currently checked item. The result is only valid if the choice
     * mode has been set to {@link AbsListView#CHOICE_MODE_SINGLE}.
     *
     * @return The position of the currently checked item or
     *         {@link AbsListView#INVALID_POSITION} if nothing is selected
     *
     * @see #setChoiceMode(int)
     */
    public int getCheckedItemPosition() {
        if (mChoiceMode == AbsListView.CHOICE_MODE_SINGLE && mCheckStates != null && mCheckStates.size() == 1) {
            return mCheckStates.keyAt(0);
        }

        return AbsListView.INVALID_POSITION;
    }

    /**
     * Returns the set of checked items in the list. The result is only valid if
     * the choice mode has not been set to {@link AbsListView#CHOICE_MODE_NONE}.
     *
     * @return  A SparseBooleanArray which will return true for each call to
     *          get(int position) where position is a checked position in the
     *          list and false otherwise, or <code>null</code> if the choice
     *          mode is set to {@link AbsListView#CHOICE_MODE_NONE}.
     */
    public SparseBooleanArray getCheckedItemPositions() {
        if (mChoiceMode != AbsListView.CHOICE_MODE_NONE) {
            return mCheckStates;
        }
        return null;
    }

    /**
     * Returns the set of checked items ids. The result is only valid if the
     * choice mode has not been set to {@link AbsListView#CHOICE_MODE_NONE} and the adapter
     * has stable IDs. ({@link ListAdapter#hasStableIds()} == {@code true})
     *
     * @return A new array which contains the id of each checked item in the
     *         list.
     */
    public long[] getCheckedItemIds() {
        if (mChoiceMode == AbsListView.CHOICE_MODE_NONE || mCheckedIdStates == null) {
            return new long[0];
        }

        final LongSparseArray<Integer> idStates = mCheckedIdStates;
        final int count = idStates.size();
        final long[] ids = new long[count];

        for (int i = 0; i < count; i++) {
            ids[i] = idStates.keyAt(i);
        }

        return ids;
    }

    /**
     * Clear any choices previously set
     */
    public void clearChoices() {
        if (mCheckStates != null) {
            mCheckStates.clear();
        }
        if (mCheckedIdStates != null) {
            mCheckedIdStates.clear();
        }
        mCheckedItemCount = 0;
    }

    /**
     * Sets the checked state of the specified position. The is only valid if
     * the choice mode has been set to {@link AbsListView#CHOICE_MODE_SINGLE} or
     * {@link AbsListView#CHOICE_MODE_MULTIPLE}.
     *
     * @param position The item whose checked state is to be checked
     * @param value The new checked state for the item
     */
    public void setItemChecked(int position, boolean value) {
        if (mChoiceMode == AbsListView.CHOICE_MODE_NONE) {
            return;
        }

        // Start selection mode if needed. We don't need to if we're unchecking something.
        if (value && mAttachedRecyclerView != null) {
            startSelectionModeIfNeeded(mAttachedRecyclerView);
        }

        if (mChoiceMode == AbsListView.CHOICE_MODE_MULTIPLE || mChoiceMode == AbsListView.CHOICE_MODE_MULTIPLE_MODAL) {
            boolean oldValue = mCheckStates.get(position);
            mCheckStates.put(position, value);
            if (mCheckedIdStates != null && hasStableIds()) {
                if (value) {
                    mCheckedIdStates.put(getItemId(position), position);
                } else {
                    mCheckedIdStates.delete(getItemId(position));
                }
            }
            if (oldValue != value) {
                if (value) {
                    mCheckedItemCount++;
                } else {
                    mCheckedItemCount--;
                }
                updateCheckStateIfPossible(null, position);
                fireOnSelected(null);
            }
            if (mChoiceActionMode != null) {
                final long id = getItemId(position);
                mMultiChoiceModeCallback.onItemCheckedStateChanged(mChoiceActionMode,
                        position, id, value);
            }
        } else {
            int checkedPosition = getCheckedItemPosition();
            boolean updateIds = mCheckedIdStates != null && hasStableIds();
            // Clear all values if we're checking something, or unchecking the currently
            // selected item
            if (value || isItemChecked(position)) {
                mCheckStates.clear();
                if (updateIds) {
                    mCheckedIdStates.clear();
                }
            }
            // this may end up selecting the value we just cleared but this way
            // we ensure length of mCheckStates is 1, a fact getCheckedItemPosition relies on
            if (value) {
                mCheckStates.put(position, true);
                if (updateIds) {
                    mCheckedIdStates.put(getItemId(position), position);
                }
                mCheckedItemCount = 1;
            } else if (mCheckStates.size() == 0 || !mCheckStates.valueAt(0)) {
                mCheckedItemCount = 0;
            }
            if (checkedPosition != position) {
                if (checkedPosition != AbsListView.INVALID_POSITION)
                    updateCheckStateIfPossible(null, checkedPosition);
                updateCheckStateIfPossible(null, position);
                fireOnSelected(null);
            }
        }
    }

    private boolean startSelectionModeIfNeeded(View view) {
        if (mChoiceMode == AbsListView.CHOICE_MODE_MULTIPLE_MODAL &&
                mChoiceActionMode != null) {
            if (mMultiChoiceModeCallback == null ||
                    !mMultiChoiceModeCallback.hasWrappedCallback()) {
                throw new IllegalStateException("TrackSelectionAdapter: attempted to start selection mode " +
                        "for AbsListView.CHOICE_MODE_MULTIPLE_MODAL but no choice mode callback was " +
                        "supplied. Call setMultiChoiceModeListener to set a callback.");
            }
            mChoiceActionMode = view.startActionMode(mMultiChoiceModeCallback);
            return true;
        }
        return false;
    }

    public boolean performItemClick(View view, int position, long id) {
        boolean handled = false;
        boolean dispatchItemClick = true;

        if (mChoiceMode != AbsListView.CHOICE_MODE_NONE) {
            handled = true;

            if (mChoiceMode == AbsListView.CHOICE_MODE_MULTIPLE ||
                    (mChoiceMode == AbsListView.CHOICE_MODE_MULTIPLE_MODAL && mChoiceActionMode != null)) {
                boolean checked = !mCheckStates.get(position, false);
                mCheckStates.put(position, checked);
                if (mCheckedIdStates != null && hasStableIds()) {
                    if (checked) {
                        mCheckedIdStates.put(getItemId(position), position);
                    } else {
                        mCheckedIdStates.delete(getItemId(position));
                    }
                }
                if (checked) {
                    mCheckedItemCount++;
                } else {
                    mCheckedItemCount--;
                }
                if (mChoiceActionMode != null) {
                    mMultiChoiceModeCallback.onItemCheckedStateChanged(mChoiceActionMode,
                            position, id, checked);
                    dispatchItemClick = false;
                }
                updateCheckStateIfPossible(view, position);
                fireOnSelected(view);
            } else {
                if (mChoiceMode == AbsListView.CHOICE_MODE_SINGLE) {
                    int checkedPosition = getCheckedItemPosition();
                    boolean checked = !mCheckStates.get(position, false);
                    if (checked) {
                        mCheckStates.clear();
                        mCheckStates.put(position, true);
                        if (mCheckedIdStates != null && hasStableIds()) {
                            mCheckedIdStates.clear();
                            mCheckedIdStates.put(getItemId(position), position);
                        }
                        mCheckedItemCount = 1;
                    } else if (mCheckStates.size() == 0 || !mCheckStates.valueAt(0)) {
                        mCheckedItemCount = 0;
                    }
                    if (checkedPosition != position) {
                        if (checkedPosition != AbsListView.INVALID_POSITION)
                            updateCheckStateIfPossible(null, checkedPosition);
                        updateCheckStateIfPossible(view, position);
                        fireOnSelected(view);
                    }
                }
            }
        }

        if (dispatchItemClick) {
            handled |= superPerformItemClick(view, position, id);
        }

        return handled;
    }

    /**
     * Call the OnItemClickListener, if it is defined. Performs all normal
     * actions associated with clicking: reporting accessibility event, playing
     * a sound, etc.
     *
     * @param view The view within the AdapterView that was clicked.
     * @param position The position of the view in the adapter.
     * @param id The row id of the item that was clicked.
     * @return True if there was an assigned OnItemClickListener that was
     *         called, false otherwise is returned.
     */
    public boolean superPerformItemClick(View view, int position, long id) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(this, view, position, id);
            if (view != null) {
                view.playSoundEffect(SoundEffectConstants.CLICK);
                view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
            }
            return true;
        }

        return false;
    }

    boolean performLongPress(final View child,
                             final int longPressPosition, final long longPressId) {
        // CHOICE_MODE_MULTIPLE_MODAL takes over long press.
        if (mChoiceMode == AbsListView.CHOICE_MODE_MULTIPLE_MODAL) {
            if (mChoiceActionMode == null) {
                setItemChecked(longPressPosition, true);
                child.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
            return true;
        }

        boolean handled = false;
        if (mOnItemLongClickListener != null) {
            handled = mOnItemLongClickListener.onItemLongClick(this, child,
                    longPressPosition, longPressId);
        }
        if (handled) {
            child.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }

        return handled;
    }

    private void fireOnSelected(View v) {
        if (mOnItemSelectedListener == null) {
            return;
        }
        final int selection = getCheckedItemPosition();
        if (selection >= 0) {
            mOnItemSelectedListener.onItemSelected(this, v, selection,
                    getAdapter().getItemId(selection));
        } else {
            mOnItemSelectedListener.onNothingSelected(this);
        }
    }

    /**
     * Register a callback to be invoked when an item in this AdapterView has
     * been clicked.
     *
     * @param listener The callback that will be invoked.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * @return The callback to be invoked with an item in this AdapterView has
     *         been clicked, or null id no callback has been set.
     */
    public final OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    /**
     * Register a callback to be invoked when an item in this AdapterView has
     * been clicked and held
     *
     * @param listener The callback that will run
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    /**
     * @return The callback to be invoked with an item in this AdapterView has
     *         been clicked and held, or null id no callback as been set.
     */
    public final OnItemLongClickListener getOnItemLongClickListener() {
        return mOnItemLongClickListener;
    }

    /**
     * Register a callback to be invoked when an item in this AdapterView has
     * been selected.
     *
     * @param listener The callback that will run
     */
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener;
    }

    public final OnItemSelectedListener getOnItemSelectedListener() {
        return mOnItemSelectedListener;
    }

    class MultiChoiceModeWrapper implements MultiChoiceModeListener {
        private MultiChoiceModeListener mWrapped;

        public void setWrapped(MultiChoiceModeListener wrapped) {
            mWrapped = wrapped;
        }

        public boolean hasWrappedCallback() {
            return mWrapped != null;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (mWrapped.onCreateActionMode(mode, menu)) {
                // Initialize checked graphic state?
                if (mAttachedRecyclerView != null) {
                    mAttachedRecyclerView.setLongClickable(false);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return mWrapped.onPrepareActionMode(mode, menu);
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return mWrapped.onActionItemClicked(mode, item);
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mWrapped.onDestroyActionMode(mode);
            mChoiceActionMode = null;

            // Ending selection mode means deselecting everything.
            clearChoices();

            notifyDataSetChanged();
            fireOnSelected(null);

            if (mAttachedRecyclerView != null) {
                mAttachedRecyclerView.setLongClickable(true);
            }
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode,
                                              int position, long id, boolean checked) {
            mWrapped.onItemCheckedStateChanged(mode, position, id, checked);

            // If there are no items selected we no longer need the selection mode.
            if (getCheckedItemCount() == 0) {
                mode.finish();
            }
        }
    }

    /**
     * Interface definition for a callback to be invoked when an item in this
     * AdapterView has been clicked.
     */
    public interface OnItemClickListener {

        /**
         * Callback method to be invoked when an item in this AdapterView has
         * been clicked.
         * <p>
         * Implementers can call getItemAtPosition(position) if they need
         * to access the data associated with the selected item.
         *
         * @param parent The AdapterView where the click happened.
         * @param view The view within the AdapterView that was clicked (this
         *            will be a view provided by the adapter)
         * @param position The position of the view in the adapter.
         * @param id The row id of the item that was clicked.
         */
        void onItemClick(TrackSelectionAdapterWrapper<?> parent, View view, int position, long id);
    }

    /**
     * Interface definition for a callback to be invoked when an item in this
     * view has been clicked and held.
     */
    public interface OnItemLongClickListener {
        /**
         * Callback method to be invoked when an item in this view has been
         * clicked and held.
         *
         * Implementers can call getItemAtPosition(position) if they need to access
         * the data associated with the selected item.
         *
         * @param parent The AbsListView where the click happened
         * @param view The view within the AbsListView that was clicked
         * @param position The position of the view in the list
         * @param id The row id of the item that was clicked
         *
         * @return true if the callback consumed the long click, false otherwise
         */
        boolean onItemLongClick(TrackSelectionAdapterWrapper<?> parent, View view, int position, long id);
    }

    /**
     * Interface definition for a callback to be invoked when
     * an item in this view has been selected.
     */
    public interface OnItemSelectedListener {
        /**
         * <p>Callback method to be invoked when an item in this view has been
         * selected. This callback is invoked only when the newly selected
         * position is different from the previously selected position or if
         * there was no selected item.</p>
         *
         * Impelmenters can call getItemAtPosition(position) if they need to access the
         * data associated with the selected item.
         *
         * @param parent The AdapterView where the selection happened
         * @param view The view within the AdapterView that was clicked
         * @param position The position of the view in the adapter
         * @param id The row id of the item that is selected
         */
        void onItemSelected(TrackSelectionAdapterWrapper<?> parent, View view, int position, long id);

        /**
         * Callback method to be invoked when the selection disappears from this
         * view. The selection can disappear for instance when touch is activated
         * or when the adapter becomes empty.
         *
         * @param parent The AdapterView that now contains no selected item.
         */
        void onNothingSelected(TrackSelectionAdapterWrapper<?> parent);
    }

}