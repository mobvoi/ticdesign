---
layout: doc
title: Developer Doc
permalink: /en/doc/
---

By using Ticwear Design Support Library (later referred to as the design lib), you can easily develop an app that meets Ticwear’s [design guideline][ticwear-design] and supports novel interactions like Tickle.

Except Ticwear’s own unique interactions, other parts of work can be applied to Android Wear also.

Before you start using the design lib,  you ought to set your Application Theme as either `Theme.Ticwear` or other derivative themes. Then you are able to use the various sorts of components in the design lib.

The design lib includes the following parts:

1. [Style and Theme](#style-and-theme): define a few styles of text, page or widgets, and transitions of pages.
2. [Coordinator Layout](#coordinator-layout): establish a set of page structure for wearables in addition to the scroll effect in Google Design and add the overscroll-bounce effect for the App bar based on [Android Design Support][google-design-support]. 
3. [Support Tickle](#support-tickle): provide developers a set of convenient methods to support Tickle, plus widgets that work better with Tickle, such as [Ticklable RecycleView](#ticklable-RV), [Focusable LayoutManger](#focusable-LM), etc.
4. [Preference](#preference): provide a preference system like [Android Settings][android-settings] to fit [Ticwear Design][ticwear-design]. It works better on smartwatch, and supports Tickle interactions. 
5. [Dialogs](#dialogs): we also provide a set of dialogs suitable on smartwatch, including [Normal AlertDialog](#alert-dialog), [Number Picker Dialog](#number-picker-dialog), [Date & Time Picker Dialog](#date-picker-dialog), [List Choice Dialog](#list-choice-dialog), etc.
6. [Floating Context Menu](#menu): Similar to [Android's FloatingContextMenu][android-FloatingContextMenu], create menu items via [Menu resource][android-menu-resource], then use `FloatingContextMenu` to load and display them, and get the callback of their selection.
7. [Other Widgets](#widgets): Ticwear provides a set of widgets suitable for wear, including [ScalableTextView](#scale-textview), [FloatingActionButton](#fab), [PrimaryButton](#primary-button), [NumberPicker](#number-picker), [DatetimePicker](#date-picker), and [Checkbox, RadioButton, SimpleSwitch](#two-state-button), etc.

> Any of the content in the design lib can be demonstrated with "demo" App from the [source code][ticdesign-source].

## <a name="style-and-theme"></a>Style and Theme {#style-and-theme}

Ticwear provides a set of themes that meet Ticwear design guideline. Developers can use or extends those themes, including:

1. `Theme.Ticwear`: Default theme of Ticwear Design that defines a series of styles for text, page, transitions, settings, etc.
2. `Theme.Ticwear.Dialog`: applies tor dialogs on Ticwatch with Fullscreen display, swipe to dismiss with slide in/out transition.

Beside Themes, developers can also use the set of styles we have customized. Please refer to `styles_ticwear.xml` for more details.

### <a name="list-styles"></a>List Styles {#list-styles}

To acquire better user experience, please:

1. Set a style pointing to `Widget.Ticwear.ListView` for your `ListView` (or `TickableRecyclerView`, etc.)
2. Set a style pointing to `Widget.Ticwear.ListItem` for your list item container.

These two styles, has covered display or list view on smartwatch including the margin of top or bottom of the list, horizontal padding and click effect of list items, etc.

### <a name="text-styles"></a>Text Styles {#text-styles}

Ticwear defines a series of text styles that are all compatible with the [Material Design Typography](https://www.google.com/design/spec/style/typography.html#typography-styles), including text size, line spacing, font, etc. (Given that "Display" font is too large for smartwatches, it is not defined here)

Ticwear Design has defined the styles as shown below：

``` xml
TextAppearance.Ticwear
TextAppearance.Ticwear.Headline
TextAppearance.Ticwear.Title
TextAppearance.Ticwear.Title.Inverse
TextAppearance.Ticwear.Body2
TextAppearance.Ticwear.Body1
TextAppearance.Ticwear.Hint1
TextAppearance.Ticwear.Hint2
TextAppearance.Ticwear.Button
TextAppearance.Ticwear.Inverse

TextAppearance.Ticwear.Large
TextAppearance.Ticwear.Large.Inverse
TextAppearance.Ticwear.Medium
TextAppearance.Ticwear.Medium.Inverse
TextAppearance.Ticwear.Small
TextAppearance.Ticwear.Small.Inverse
TextAppearance.Ticwear.Widget
TextAppearance.Ticwear.Widget.Button
```

And, some basic font size:

``` xml
<dimen name="tic_text_size_extra_extra_large">27sp</dimen>
<dimen name="tic_text_size_extra_large">20sp</dimen>
<dimen name="tic_text_size_large_1">18sp</dimen>
<dimen name="tic_text_size_large_2">17sp</dimen>
<dimen name="tic_text_size_medium_1">16sp</dimen>
<dimen name="tic_text_size_medium_2">15sp</dimen>
<dimen name="tic_text_size_small_1">14sp</dimen>
<dimen name="tic_text_size_small_2">13sp</dimen>
<dimen name="tic_text_size_extra_small">12sp</dimen>
```

Developers are free to use and combine the styles and sizes listed above.

### <a name="color-styles"></a>TiColor palette {#color-styles}

We provide a set of Ticwear-style palette resource for developers, allowing them to directly use a specific color through resource. The naming of basic colors conforms to the following format:

``` java
R.color.tic_basic_<name>{_<decorate>}
```

In this format, `name` means color’s name. All available color names are defined in `ColorPalette.ColorName`, corresponding with each specific color. And `decorate` implies a modification of color with three options to choose from: `darken`, `lighten` and `normal`. When `decorate` is not assigned, the default setting is `normal`.

In addition to directly using the resource files, developers are also able to acquire Ticwear colors via class `ColorPalette` programmatically:

1. Call `ColorPalette.from(Context)` to obtain a palette instance.
2. Use `ColorPalette.color(ColorName)` to acquire a color object that corresponds with `ColorName`.
3. If decorate is needed, call `ColorPalette.Color.lighten()` or `ColorPalette.Color.darken()` to acquire the modified color object.

  > If the current color can not assign modifications, it returns to the original state. For example, ‘Indigo’.darken().darken() equals to ‘Indigo Darken’.

4. In the end, get the final color value through `ColorPalette.Color.value()`.

For example, if we need to acquire `Indigo Darken` as a modified color value, we need to call the following codes:

``` java
int color = ColorPalette.from(context)
                .color(ColorPalette.ColorName.INDIGO)
                .darken()
                .value();
```

## <a name="coordinator-layout"></a>Coordinator Layout {#coordinator-layout}

Similar to [Android Design Support][google-design-support], Use `CoordinatorLayout` to structure `AppBarLayout` and other page content can let the App bar response to the scrolling of content, therefore achieving multiple effects.

### Enable Circular Scroll Bar and Edge Effect

<div class="row">
<div class="col s12 m7" markdown="1">

By using `CoordinatorLayout` to wrap the scrollable content, developers are able to acquire a circular scroll bar, a edge effect with illuminant, and a overscroll-bounce effect.

To enable this, assign `app:tic_layout_behavior` as `"@string/tic_appbar_scrolling_view_behavior"` for the scrollable content to let `CoordinatorLayout` to operate your View.

</div>
<div class="col s12 m4 push-m1 center">
<img src="res/scroll-edge-effect.png">
</div>
</div>

For example:

``` xml
<ticwear.design.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    >

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:nestedScrollingEnabled="true"
        android:paddingStart="?android:listPreferredItemPaddingStart"
        android:paddingEnd="?android:listPreferredItemPaddingEnd"
        android:paddingTop="@dimen/tic_list_padding_bottom_ticwear"
        android:paddingBottom="@dimen/tic_list_padding_bottom_ticwear"
        app:tic_layout_behavior="@string/tic_appbar_scrolling_view_behavior"
        style="@style/Widget.Ticwear.ListView"
        >

        <TextView
            android:id="@+id/text_content"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textAppearance="?android:textAppearanceSmall"
            android:text="@string/text.long_content"
            />

    </ScrollView>

</ticwear.design.widget.CoordinatorLayout>
```

### <a name="title-bar"></a>AppBar Response to Scroll {#title-bar}

In addition to effects such as "fixed, scrolling, quick enter, collapsed" supported by Android's `AppBarLayout`, TicDesign also supports "overscroll-bount" effect, along with a `ScalableTextView`, to ensure that the Appbar can be stretched up with a resistance.  

Here is an example for page layout:

``` xml
<?xml version="1.0" encoding="utf-8"?>
<ticwear.design.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:scrollbars="vertical"
    app:tic_overScrollEffect="bounce">

    <include layout="@layout/content_main" />

    <ticwear.design.widget.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <ticwear.design.widget.ScalableTextView
            style="?android:textAppearanceSmall"
            android:layout_width="match_parent"
            android:layout_height="32dp"
            android:gravity="center"
            android:text="@string/main.title"
            app:tic_layout_scrollFlags="scroll|overScrollBounce|enterAlways"
            app:tic_layout_scrollResistanceFactor="0.5"
            app:tic_scaleFactor="0.5"
            />

    </ticwear.design.widget.AppBarLayout>

</ticwear.design.widget.CoordinatorLayout>
```

By incorporating a variety of effects, this layout achieves a fast enter into the Appbar (when the page is scrolled to the bottom and gets pulled down, the Appbar appears immediately) and the stretch rebound effect (when the page scrolls to the top and gets pulled down, the Appbar gets stretched and then returns to its original state when stop scrolling).

`Tic_layout_XXX`, similar to `android:layout_XXX`, shows the layout behavior of View in parent, which is irrelevant to the content. The attributes that do not start with `tic_layout_` are the actual attributes of View. Here is an elaborate explanation of those attributes:  

* `app:tic_overScrollEffect` assigns the effect when the page content is scrolled to the end. When `CoordinatorLayout’s` subelement fails to consume the nested scroll event, and it triggers the effect defined here. Currently, it merely supports `none` and `bounce` effect(overscroll bounce).
* `app:tic_layout_scrollFlags` assigns the corresponding action as the Appbar scrolls, with a combination of various actions based on needs.
* `app:tic_layout_scrollResistanceFactor` assigns the fold change of overall height when the Appbar stretches. When the fold change is 1, corresponding the Appbar height change and rolling distance with no damping effect. As the fold change gets closer to 0, the damping effect becomes greater along with smaller changes in height.
* `app:tic_scaleFactor` assigns the text scaleFactor. When scaleFactor is 1, the text will be aligned with the size of textView through geometric scaling. The scaling effect gets less and less obvious as the scaleFactor gradually reaches 0. Please refer to the details of [scale-textview](#scale-textview).

## <a name="support-tickle"></a>Support for Tickle {#support-tickle}

By achieving `SidePanelEventTarget` or `SidePanelGestureTarget`, developers can easily add support for Tickle for their customized view.

`SidePanelEventTarget` interface contains a function for basic events. Similar to touch event. Tickle also includes the following processing steps:

1. `DispatchTouchSidePanelEvent`: In the dispatch step, it decides whether to use the current view to process Tickle event, or dispatch it.
2. `onTouchSidePanel`: It decides whether to use the current view to process Tickle.

`SidePanelGestureTarget` packs common Tickle gestures, such as single click, double click, long click, scroll, bounce, etc. If these is no `SidePanelEventTarget` to process the Tickle event，the tickle event will be packed as the Tickle gestures and be dispatched to various `SidePanelGestureTarget` to process.

Please refer to [Tickle API](http://developer.ticwear.com/doc/tickle-api) for more information.

### <a name="ticklable-RV"></a>RecyclerView for supporting Tickle interaction {#ticklable-RV}

`TickableRecyclerView` expands `RecyclerView` to support Tickle. You can assign a regular [`LayoutManager`][android-LM] for Tickle to make its touch gesture no different from `RecyclerView`, which means using Tickle is like touching the far right side of View. In that case, you just need to combine `TickableRecyclerView` with regular `LayoutManager` to make Tickle work. 

By implementing `TicklableLayoutManager` interface, you can customize [`LayoutManager`][android-LM] for Tickle. Feel free to check `FocusableLinearLayoutManager` for your reference. 

We have a special design for `TickableRecyclerView` as to make it align with`AppBarLayout`, therefore enabling all sorts of TitleBar effects when it focuses. Please refer to `TickableRecyclerViewBehavior` listed in source code for more details.

To make it easier for developers, we have provided a series of Adapter to quickly meet specific needs:

1. `SimpleRecyclerAdapter` applies to simple items with merely icon or text. It automatically binds data and views by mapping relationship, ausage method similar to [`ListView.SimpleAdapter`][simple-adapter].
2. `CursorRecyclerViewAdapter` provides the visits to database, such as `android.widget.CursorAdapter`.
3. `TrackSelectionAdapterWrapper` packs other Adapters to acquire similar selection abilities in `ListView`. You can refer to `AlertController` for more details on its usage.

### <a name="focusable-LM"></a>LayoutManager with Focusing Effects {#focusable-LM}

In order to display diverse and elegant visual effects, `FocusableLinearLayoutManager` combines the strengths of `LinearLayoutManager` and `WearableListView`, allowing the list controller to perform as regular LinearLayout RecyclerView at its normal state. In addition, the users can click any listed item in the interface. When users touch the tickle, the layout manager focuses and its content becomes larger with a focus on the elements in the middle of the page, making the operation more accurate and clear.

<div class="row">
<div class="col-half">
<img src="res/settings.png" width="320">
</div>
<div class="col-half">
<img src="res/settings-tickle.png" width="320">
</div>
</div>

When using `FocusableLinearLayoutManager`, you need to have your ViewHolder inherit `FocusableLinearLayoutManager.ViewHolder`, to define the animation switch effect of focusing state, non-focusing state and normal state.

`FocusableLinearLayoutManager.ViewHolder` sets the default focusing animation, which enlarges and lights up at its focusing state and narrows and turns dark at its normal state.

If you want to define a more refined animation effect, you can make ItemView achieve `FocusableLinearLayoutManager.OnFocusStateChangedListener` interface. Or you can override `ViewHolder.onFocusStateChanged****.

If you want your animation to follow every gesture beyond a simple switch between the focusing and non-focusing state, you ought to use your `ItemView` to achieve `FocusableLinearLayoutManager.OnCentralProgressUpdatedListener` or to override `ViewHolder.onCentralProgressUpdated`.

When the Tickle slides, `FocusableLinearLayoutManager` first uses `onFocusStateChanged` to update status and then uses `onCentralProgressUpdated` to achieve more refined effects. As the focusing state turns into the normal state, use `onFocusStateChanged`.

A better solution is to enable transition via `View.animate()` after switching to the normal state. While at the focusing state, use progress to update the size and style of View. 

Below is a simple override demonstration (same as the default animation)

``` java
@Override
protected void onCentralProgressUpdated(float progress, long animateDuration) {
    float scaleMin = 1.0f;
    float scaleMax = 1.1f;
    float alphaMin = 0.6f;
    float alphaMax = 1.0f;

    float scale = scaleMin + (scaleMax - scaleMin) * progress;
    float alphaProgress = getFocusInterpolator().getInterpolation(progress);
    float alpha = alphaMin + (alphaMax - alphaMin) * alphaProgress;
    transform(scale, alpha, animateDuration);
}

@Override
protected void onFocusStateChanged(@FocusState int focusState, boolean animate) {
    if (focusState == FocusableLinearLayoutManager.FOCUS_STATE_NORMAL) {
        transform(1, 1, animate ? getDefaultAnimDuration() : 0);
    }
}

private void transform(float scale, float alpha, long duration) {
    itemView.animate().cancel();
    if (duration > 0) {
        itemView.animate()
                .setDuration(duration)
                .alpha(alpha)
                .scaleX(scale)
                .scaleY(scale)
                .start();
    } else {
        itemView.setScaleX(scale);
        itemView.setScaleY(scale);
        itemView.setAlpha(alpha);
    }
}
```

## <a name="preference"></a>Setting System {#preference}

Ticwear’s settings system is similar to [Android Settings][android-settings]. You can use Ticwear Preference in a similar way you would use Android Preference. But note that Ticwear Preference’s built-in `Listview` has been changed to `TicklableRecyclerView`. You need to use `RecyclerView.Viewholder` in order to bind statistics with Preference View.  

You have to inherit `Preference.VewHolder` in order to  customize `Preference`. We also need to override its methods as to bind your customized data.


## <a name="dialogs"></a>Dialog {#dialogs}

<div class="row">
<div class="col s12 m7" markdown="1">

Being fully aware of the convenience of Dialog, we have renovated Dialog to make it fit smartwatch. We also extended the push button and list display of dialog ,and provide numerical selection dialog. All the changes listed above have helped provide handy user experience while remaining the convenience of Android interface.

</div>
<div class="col s12 m4 push-m1 center">
<img src="res/delay-confirm-dialog.png">
</div>
</div>

### <a name="alert-dialog"></a>Alert Dialog {#alert-dialog}

We transplant and extend Android’s [`AlertDialog`][android-alert-dialog] with a customized theme for smartwatch. A circular button that fits smartwatch better is provided as well to replace the original text button.

When the set-up text message gets really long, it can scroll up and down and the bottom of the icon button will disappear as the message scrolls to ensure better reading experience.

The way to use it is no different from using the original `AlertDialog`. The only thing needed is to formulate either an icon document or the icon’s `Drawable`, similar to the method as shown below:

``` java
new AlertDialog.Builder(context)
        .setTitle(R.string.dialog_title)
        .setMessage(R.string.dialog_content)
        .setPositiveButtonIcon(R.drawable.ic_btn_ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something for positive action.
                dialog.dismiss();
            }
        })
        .show();
```

While we use the dialog, we can achieve the effects we want by assigning `android:alertDialogTheme` from a customized dialog style.

`AlertDialog` has the following styles that can be included in themes:

1. `tic_windowIconStyle`：assigning the style of alert dialog’s headline icon
2. `android:windowTitleStyle`：assigning the style of dialog’s title
3. `tic_iconButtonBarStyle`：assigning the style of icon button bar
4. `tic_iconButtonBarPositiveButtonStyle`：assigning the button style of positve
5. `tic_iconButtonBarNegativeButtonStyle`：assigning the button style of negative
6. `tic_iconButtonBarNeutralButtonStyle`：assigning the button style of neutral. Since smartwatches have tiny screens,  we don’t recommend this option.


### <a name="list-choice-dialog"></a>List Selection Dialog {#list-choice-dialog}


Similar to Android’s [`AlertDialog`][android-alert-dialog], you can also create a list selection dialog by setting the item, singleChoiceItems and multipleChoiceItems of Dialog, in order to acquire results of user’s selection of list.

The way to use it is no different from that of [AlertDialog](#alert-dialog). The following is the codes:

``` java
final List<Integer> selection = new ArrayList<>();
dialog = new AlertDialog.Builder(getActivity())
        .setTitle(R.string.category_dialog_multiple_choice)
        .setMultiChoiceItems(listItems, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    selection.add(which);
                } else {
                    selection.remove((Integer) which);
                }
            }
        })
        .setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                String message = "Picked item:\n";
                for (int which : selection) {
                    message += listItems[which] + ";\n";
                }
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        })
        .show();
```

### <a name="number-picker-dialog"></a>Number Picker Dialog {#number-picker-dialog}

Nesting [`NumberPicker`](#number-picker), developers are able to use [`AlertDialog`][android-alert-dialog] to show users a dialog to pick a number. The way to use it is shown below:

``` java
new NumberPickerDialog.Builder(context)
        .minValue(0)
        .maxValue(20)
        .defaultValue(5)
        .valuePickedlistener(new NumberPickerDialog.OnValuePickedListener() {
            @Override
            public void onValuePicked(NumberPickerDialog dialog, int value) {
                Toast.makeText(dialog.getContext(), "Picked value " + value,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        })
        .show();
```

### <a name="date-picker-dialog"></a>Date and Time Picker {#date-picker-dialog}

To make the developing process easier, we have packed [`DatePicker` and `TimePicker`](#date-picker) together and provided a `DatetimePickerDialog`. Just as [`AlertDialog`][android-alert-dialog], we show a dialog to let the users pick the date, time or both. The way to use it is shown below:

``` java
new DatetimePickerDialog.Builder(getActivity())
        .defaultValue(Calendar.getInstance())
        .listener(new DatetimePickerDialog.OnCalendarSetListener() {
            @Override
            public void onCalendarSet(DatetimePickerDialog dialog,
                                      Calendar calendar) {
                Toast.makeText(dialog.getContext(), "Picked datetime: " +
                                SimpleDateFormat.getDateTimeInstance()
                                        .format(calendar.getTime()),
                        Toast.LENGTH_LONG)
                        .show();
            }
        })
        .show();
```

If you only want users to pick the date or time, the only thing you need to do is to assign `disableTimePicker()` or `disableDatePicker()` while building it. Feel free to refer to `DialogsFragment` in our Demo for more information.


## <a name="menu"></a>Floating Context Menu {#menu}

You can easily create a floating context menu that floats above the content through `FloatingContextMenu`.

Similar to Android’s [long pop-up menu][android-FloatingContextMenu], developers can create a menu resource within [menu resource][android-menu-resource], and appoint content for `FloatingContextMenu` through `ContextMenuCreator` during the process.

User’s selection of choice will be returned to the server through `OnMenuSelectedListener`.

After specifying the callback interface to create and choose the menu, developers can display the floating menu through `show(View)` and bind the menu to the appointed View. This action will affect the menu’s life cycle. When the View gets detached from the window, the bound menu will be destroyed. Besides, the assigned view will be conveyed to `ContextMenuCreator` as a contextual reference for creating a menu resource.

Here is a simple example:

``` java
new FloatingContextMenu(context)
        .setContextMenuCreator(new ContextMenuCreator() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v) {
                getMenuInflater().inflate(R.menu.default_hint, menu);
            }
        })
        .setOnMenuSelectedListener(new OnMenuSelectedListener() {
            @Override
            public boolean onContextItemSelected(@NonNull MenuItem item) {
                Toast.makeText(context, item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        })
        .show(view);
```
By specifying the menu creation and select the callback, developers can appoint the menu content, and also obtain the results of menu selection.  

One thing to note here is that our current `FloatingContextMenu` merely supports basic/rudimentary menu content (icon, title, and intent) without no support on nested menus (menu group), action, and other complex attributes.

## <a name="widgets"></a>Small Widgets {#widgets}

### <a name="scale-textview"></a>Scalable Text Frames {#scale-textview}

`ScalableTextView` can follow the control size in changing script size. This is commonly used in showing title in `TitleBar`.  

During usage, you can appoint scaling factors `scaleFactor` through either XML document or code.  The change of script size, text frame size, and scaling factors all follow the equation as shown below:

$$
\Delta_{textScale} = \Delta_{frameScale} \times scaleFactor
$$

Among them：

$$
\Delta_{frameScale} = max\left(
    \frac{w_{dst}}{w_{src}},
    \frac{h_{dst}}{h_{src}}
\right)
$$

You need to be aware that when scripts undergo scaling, it might exceed the boundary. While using it, it is best to appoint a sufficient padding, or set up a non-changeable over there to match_parent. For example, the `ScalableTextView` in the `TitleBar` usually follows the layout below:

``` xml
<ticwear.design.widget.AppBarLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    <ticwear.design.widget.ScalableTextView
        style="?android:textAppearanceMedium"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:clipToPadding="false"
        android:padding="8dp"
        android:text="Title"
        app:tic_layout_scrollFlags="scroll|overScrollBounce"
        app:tic_layout_scrollResistanceFactor="0.5"
        app:tic_scaleFactor="0.5"
        />
</ticwear.design.widget.AppBarLayout>
```

### <a name="fab"></a>Floating Action Button {#fab}

`FloatingActionButton`, the floating action button, is an expanded `ImageButton`. We transplanted it from the [Android Design Support][google-design-support] library, and removed some undesirable features for smartwatch (such as interactions with `SnackBar`) as to add more Ticwear’s unique design element.

Please refer to the [official Android file] [android-fab] for more details on regular usage.

One of the main specific modification for Ticwear is adding `minimum`, a minimizing mode (an original widget that merely supports shown and `hidden` mode). This mode will minimize the push button  to a small dot without blocking the text and also give user a heads-up on operable elements here.

The usage is similar to `show()` and `hide()`.You call minimize() to minimize the button. When the minimization is done,  it will trigger the call-back of `OnVisibilityChangedListener.onMinimum`.

`BackgroundDrawable` cites a [circular progress drawable](#cpd) and  is able to set the progress, transparency, and mode for the drawable.

### <a name="cpd"></a>CircularProgressDrawable {#cpd}

The circular progress drawable, similar to Android’s [`progressBar`][android-progressbar], is divided into two modes, `determinate` and `indeterminate` according to its effect. The determinate mode displays the current progress of `progressBar` and the indeterminate mode dynamically rotates the `progressBar`.

Users can dynamically set the progress, alpha value, and color for `progressBar` within the codes. While no settings are done, we automatically `tint` the `progressBar` and the background color with an alpha value as 50%. In any other cases, the `progress` mode automatically turns into `determinate` when it is set within the codes.

At initialization, users can start a variety of default settings for `progressBar`, such as every circle’s duration, initial angle, the maximum and minimum angle, etc. These attributes cannot be modified after being generated in `CircularProgressDrawable`.  

In FAB, users can choose whether to include the progress bar or not. When there is no progress bar, the progress and `progressbar’s ` color can be still set in FAB, but no effect.


### <a name="primary-button"></a>Primary Button {#primary-button}

One principle of developing the smartwatch interface is simplifying content and selection, allowing users to quickly understand what they need to do. Therefore we constantly need to display one major button at the bottom of the page that occupies a relatively large area. In this case, the `PrimaryButton` will do.

`PrimaryButton` is a special `ImageButton`; its background is a semicircular color block that looks nice when placed at the bottom of the round watch.

### <a name="number-picker"></a>Numerical Picker {#number-picker}

Similar to Android’s [`NumberPicker`][android-numberpicker], we have developed a numerical picker aligning with Ticwear’s design standards. The picker can be directly applied to its layout if you merely need one page to obtain the numerical value entered by users. We have also offer a handy, usable [`NumberPickerDialog`](#number-picker-dialog) to enable quick development.

### <a name="date-picker"></a>Date & Time Picker {#date-picker}

Similar to Android’s [`TimePicker`][android-timepicker] and [`DatePicker`][android-datepicker], we have made a time and date picker that meets Ticwear’s design standards. Developers can use them as they would with Android ones. We have also provided [`DateTimePickerDialog`](#date-picker-dialog) to quickly help obtain users’ date and time inputs.  

### <a name="two-state-button"></a>Checkbox, RadioButton and SimpleSwitch {#two-state-button}

We have set up a `Checkbox` and `RadioButton` that are in line with Ticwear’s design style for [Ticwear Theme](#style-and-theme). We have also provided `SimpleSwitch` to simplify the `Switch` button operations in an unified style with the other two switch buttons as a whole nicely designed set.

[ticdesign-source]: https://github.com/mobvoi/ticdesign
[ticwear-design]: ../design/
[google-design-support]: http://android-developers.blogspot.hk/2015/05/android-design-support-library.html
[android-settings]: http://developer.android.com/guide/topics/ui/settings.html
[android-alert-dialog]: http://developer.android.com/reference/android/app/AlertDialog.html
[android-fab]: http://developer.android.com/reference/android/support/design/widget/FloatingActionButton.html
[recycler-view]: http://developer.android.com/reference/android/support/v7/widget/RecyclerView.html
[simple-adapter]: http://developer.android.com/reference/android/widget/SimpleAdapter.html
[android-numberpicker]: http://developer.android.com/reference/android/widget/NumberPicker.html
[android-timepicker]: http://developer.android.com/reference/android/widget/TimePicker.html
[android-datepicker]: http://developer.android.com/reference/android/widget/DatePicker.html
[android-progressbar]: http://developer.android.com/intl/zh-cn/reference/android/widget/ProgressBar.html
[android-FloatingContextMenu]: https://developer.android.com/guide/topics/ui/menus.html#FloatingContextMenu
[android-menu-resource]: https://developer.android.com/guide/topics/resources/menu-resource.html
[android-LM]: https://developer.android.com/reference/android/support/v7/widget/RecyclerView.LayoutManager.html
