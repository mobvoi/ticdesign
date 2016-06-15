---
layout: doc
title: TicDesign 使用说明
permalink: /doc/
---

使用 Ticwear 设计辅助库（后文简称设计库），你可以轻松让你的应用符合 Ticwear 的[设计规范][ticwear-design]，并支持挠挠等新型交互方式。

除挠挠之类 Ticwear 特有的交互外，设计库的其他部分也可以工作在 Android Wear 上。

使用 Ticwear 设计辅助库。你需要首先将你的 Application theme，设置为 `Theme.Ticwear` 或其衍生主题。然后就可以开始使用 Ticwear 设计库的各类组件了。

设计库包含下面几个部分：

1. [样式和主题](#style-and-theme)：定义了一些列文字、页面和控件的样式，以及页面切换等动效支持。
2. [协作布局](#coordinator-layout)：基于 [Android Design Support][google-design-support]，构建了一套适合手表展示的页面结构，除了 Google Design 中的跟随滚动等效果，我们还为标题栏增加了可拉伸等效果。
3. [对侧面挠挠的支持](#support-tickle)：我们提供了一套较为便捷的方式为开发者提供了对挠挠的支持，并增加了一些对挠挠有较好交互的控件。比如[支持挠挠的 RecyclerView](#ticklable-RV) 以及[挠挠触碰时有聚焦效果的 LayoutManger](#focusable-LM) 等。
4. [设置](#preference)：提供一套类似 [Android Settings][android-settings] 的、符合 [Ticwear Design][ticwear-design] 的设置系统，更适合手表展示，并支持挠挠交互。
5. [对话框](#dialogs)：对应 Android 的 [AlertDialog][android-alert-dialog]，我们也提供了一系列适合手表展示的对话框。包括[普通弹出式对话框](#alert-dialog)、[数值选择对话框](#number-picker-dialog)、[日期时间选择对话框](#date-picker-dialog)、[列表选择对话框](#list-choice-dialog)等。
6. [悬浮菜单](#menu)：类似 Android 的[长按弹出菜单][android-FloatingContextMenu]，你可以通过 [Menu resource][android-menu-resource] 来创建菜单项，并利用 `FloatingContextMenu` 来加载和显示菜单，并获取菜单选中的回调事件。
7. [其他小控件](#widgets)：Ticwear提供了一系列适合手表使用的小控件，包括[可缩放文本框](#scale-textview)、[悬浮按钮](#fab)、[重要按钮](#primary-button)、[数值选择器](#number-picker)、[日期时间选择器](#date-picker)、[Checkbox、RadioButton、SimpleSwitch](#two-state-button)等。

> 文档中的所有内容，均可通过阅读[源码][ticdesign-source]中的 demo 代码来获得使用示例。

## <a name="style-and-theme"></a>样式和主题 {#style-and-theme}

Ticwear为开发者提供了一套符合Ticwear设计规范的主题。开发者可以直接使用或基于此主题进行自己的个性化扩展。包括以下主题：

1. `Theme.Ticwear` Ticwear Design 默认主题，定义了文字、窗口样式、页面切换效果、设置样式等一系列适用于手表的样式。
2. `Theme.Ticwear.Dialog`，适用于手表的对话框。全屏显示、滑动方式的进入、退出动画。

除了主题，开发者可以使用我们定义好的一系列样式。详情参看`styles_ticwear.xml`代码。

### <a name="list-styles"></a>列表样式 {#list-styles}

要获得更好的用户体验，请：

1. 为你的 `ListView` （或 `TickableRecyclerView` 等）设置一个style，指向 `Widget.Ticwear.ListView`。
2. 为你的列表项容器设置style，指向 `Widget.Ticwear.ListItem`。

这两个样式，已经较好的处理了手表上列表的显示，包括列表顶部、底部的边距、列表项左右边距，列表项点击效果等等。


### <a name="text-styles"></a>文本样式 {#text-styles}

Ticwear 定义了一系列适用于手表的文本样式，包括文本大小、行间距、字体等。并使之兼容了 [Material Design Typography](https://www.google.com/design/spec/style/typography.html#typography-styles) 的文本样式（Display字体对于手表来说太大，故没有做定义）。

Ticwear Design 定义了以下文本样式：

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

除此之外，还定义了一些列基础字体大小：

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

开发者可以随意使用和组合这些样式以及字体大小。

### <a name="color-styles"></a>Ticwear 调色板 {#color-styles}

我们为开发者定义了一套 Ticwear 风格的调色板资源。开发者可以通过资源直接使用某种颜色。基础颜色命名符合下面的规范：

``` java
R.color.tic_basic_<name>{_<decorate>}
```

其中，`name` 是颜色名称，所有可用的颜色名称，定义在 `ColorPalette.ColorName` 中，标识了一种特定的颜色。而 `decorate` 则是对改颜色的修饰，是可选的，其值可用是 `darken`，`lighten` 和 `normal`，未指定 `decorate` 时，默认为 `normal`。

除了直接只用资源文件外，开发者也可用通过 `ColorPalette` 这一辅助类来获取 Ticwear 颜色。

1. 调用 `ColorPalette.from(Context)` 并传入 `Context` 来获取调色板对象。
2. 使用 `ColorPalette.color(ColorName)` 来获取 `ColorName` 对应的颜色对象。
3. 如果需要修饰，调用 `ColorPalette.Color.lighten()` 或 `ColorPalette.Color.darken()` 来获取修饰后的颜色对象。

  > 如果当前颜色不能做指定的修饰，将返回其本身，比如 'Indigo'.darken().darken() 将等于 'Indigo Darken'。

4. 最后，通过 `ColorPalette.Color.value()` 来获取最终的颜色值。

例如，我们需要获取 `Indigo Darken` 这个修饰后的颜色值，我们需要调用以下代码：

``` java
int color = ColorPalette.from(context)
                .color(ColorPalette.ColorName.INDIGO)
                .darken()
                .value();
```

## <a name="coordinator-layout"></a>协作布局 {#coordinator-layout}

类似 [Android Design Support][google-design-support]，使用 `CoordinatorLayout` 来组织 `AppBarLayout` 以及页面内容，可以使得标题响应页面内容的滚动，实现多种效果。

### 启用圆形滚动条和滑动边缘效果

<div class="row">
<div class="col s12 m7" markdown="1">

使用 `CoordinatorLayout` 嵌套可以滚动的内容，开发者就可以获得圆形滚动条、滑动到边缘的光源特效以及拉伸回弹效果。

使用时，为内部可滑动的 View 指定 `app:tic_layout_behavior` 为 `"@string/tic_appbar_scrolling_view_behavior"`，以使得 `CoordinatorLayout` 可以操作你的 View。

</div>
<div class="col s12 m4 push-m1 center">
<img src="res/scroll-edge-effect.png">
</div>
</div>

请看示例：

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

### <a name="title-bar"></a>响应内容滚动操作的标题栏 {#title-bar}

除了 Android Design 的 `AppBarLayout` 支持的“固定、滚动、快速进入、折叠”等效果外，TicDesign 额外支持了“拉伸回弹”效果，并配套提供了可缩放的文本框，`ScalableTextView`，使得标题可以在拉伸时变大，并伴随着阻尼效果。

下面的代码给出了一个页面的布局示例：

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

通过多种效果的配合，此布局实现了标题快速进入（页面滚动到底部后下拉，标题马上出现，而不用滚动到顶部才出现），和拉伸回弹效果（页面滚动到顶部后，继续下拉，标题会随滚动操作被拉大，松手后弹回原状）。

其中，`tic_layout_XXX`，类似于`android:layout_XXX`，表示此View在parent中的布局行为，与内容无关。而其他非`tic_layout_`开头的属性，则是View本身的属性。下面对这些属性做详细解释：

* `app:tic_overScrollEffect` 指定了页面内容滚动到头时的效果，当 `CoordinatorLayout` 的子元素没能消耗掉 nested scroll 事件时，将触发此处定义的效果。目前仅支持 `none`，无效果；以及 `bounce`，拉伸回弹效果。
* `app:tic_layout_scrollFlags` 指定了标题的滚动响应行为，可以根据需要组合不同的行为。
* `app:tic_layout_scrollResistanceFactor` 指定了标题在拉伸时，整体高度的变化倍数。为1时，标题高度变化与滚动距离对应，没有阻尼效果。越接近0，阻尼效果越大，高度变化越小。
* `app:tic_scaleFactor` 指定了可缩放文字的缩放倍数。为1时，文字会跟随文本框大小做等比缩放，约接近0则缩放效果越不明显。详情可以参考[可缩放文本框](#scale-textview)。

## <a name="support-tickle"></a>对挠挠的支持 {#support-tickle}

通过实现 `SidePanelEventTarget` 或 `SidePanelGestureTarget`，开发者可以方便的为其自定义view增加对挠挠的支持。

`SidePanelEventTarget` 接口，包含了挠挠的基础事件的处理函数，类似 touch event，挠挠也包含下面步处理流程：

1. `dispatchTouchSidePanelEvent`，在分发步骤，决定是使用当前 View 处理挠挠事件，还是分发下去。
2. `onTouchSidePanel`，决定是否由当前 View 处理挠挠

`SidePanelGestureTarget` 接口，封装了常见的挠挠手势，如单击、双击、长按，滚动、抛掷等。如果没有 `SidePanelEventTarget` 来处理挠挠事件，则挠挠事件会被封装成挠挠手势，分发给各个 `SidePanelGestureTarget` 来处理。

对挠挠事件派发的详细说明，可以参看[挠挠API](http://developer.ticwear.com/doc/tickle-api)。

### <a name="ticklable-RV"></a>支持挠挠交互的 RecyclerView {#ticklable-RV}

`TickableRecyclerView` 扩展了 `RecyclerView`，使其支持挠挠的操作方式。你可以为它指定一个普通的 [`LayoutManager`][android-LM]。使其触摸行为与普通的 `RecyclerView` 无异，只是转发了挠挠的事件，使操作挠挠时，就相当于触摸 View 的最右侧。这样，`TickableRecyclerView` 结合普通 `LayoutManager` 就可以支持挠挠操作了。

通过实现 `TicklableLayoutManager` 接口，你可以自定义支持挠挠操作的 [`LayoutManager`][android-LM]。详情可以参考 `FocusableLinearLayoutManager` 的实现。

我们为`TickableRecyclerView`做了特别的设计，使得它能与`AppBarLayout`相互配合，在聚焦态时，也能较好的实现 TitleBar 的各种效果。详情可以阅读源码中的 `TickableRecyclerViewBehavior` 代码。

为了方便开发者，我们提供了一系列便捷的 Adapter 来快速实现特定需求：

1. `SimpleRecyclerAdapter` 适用于简单的只有 icon 或文字的 item，通过映射关系来自动绑定数据和视图。其使用方式类似 [`ListView.SimpleAdapter`][simple-adapter]。
2. `CursorRecyclerViewAdapter` 提供对数据库的访问，类似 `android.widget.CursorAdapter`。
3. `TrackSelectionAdapterWrapper` 使用其包装其他 `Adapter` 可获得类似 `ListView` 中 selection 的能力。详情可以参考 `AlertController` 中对它的使用。

### <a name="focusable-LM"></a>有聚焦效果的 LayoutManger {#focusable-LM}

`FocusableLinearLayoutManager` 结合了 `LinearLayoutManager` 和 `WearableListView` 的优势，使得列表控件在正常状态时表现的像普通的 LinearLayout RecyclerView，以呈现较丰富、美观的视觉效果，且可以任意点击视图中的列表项；而在挠挠触碰上去之后，转变成聚焦态，内容变大，聚焦在页面中部的元素，使得操作变得准确有目标。

<div class="row">
<div class="col-half">
<img src="res/settings.png" width="320">
</div>
<div class="col-half">
<img src="res/settings-tickle.png" width="320">
</div>
</div>

使用 `FocusableLinearLayoutManager`，你需要使你的 `ViewHolder` 继承`FocusableLinearLayoutManager.ViewHolder`，以定义聚焦、非聚焦和普通状态的动画切换效果。

`FocusableLinearLayoutManager.ViewHolder` 设置了默认的聚焦态动效，即聚焦时放大、变亮，非聚焦时缩小、变暗，普通态时原始样式。

如果你想定义更细致的动画效果。可以使你的 `ItemView` 实现 `FocusableLinearLayoutManager.OnFocusStateChangedListener` 接口，或者，直接重载`ViewHolder.onFocusStateChanged`方法。

如果你希望你的动效是跟手的，而不仅仅是聚焦、非聚焦的状态切换，你需要使你的 `ItemView` 进一步实现 `FocusableLinearLayoutManager.OnCentralProgressUpdatedListener` 或者，直接重载 `ViewHolder.onCentralProgressUpdated`。

`FocusableLinearLayoutManager` 会在挠挠滑动时，先调用 `onFocusStateChanged` 来更新状态，再调用 `onCentralProgressUpdated` 来获得更加细腻的效果。而在聚焦态切换到普通状态时，调用 `onFocusStateChanged`。

因此，一个比较好的方案是，在状态切换为普通态时，通过 `View.animate()` 来实现切换动效，而在聚焦态时，通过 progress 来更新 View 的大小和样式。

下面是一个比较简单粗暴的重载示例（与默认动效相同）：

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

## <a name="preference"></a>设置系统 {#preference}

Ticwear 的设置系统类似 [Android Settings][android-settings]，你可以使用与 Android Preference 相同的方式来使用 Ticwear Preference。但请注意，Ticwear Preference 已经将内置的 `ListView` 改成了 `TicklableRecyclerView`，你需要使用 `RecyclerView.ViewHolder` 的方式来绑定数据到 Preference view 上面。

当你需要实现自定义的 `Preference` 时，需要继承 `Preference.ViewHolder`，并按需要覆盖其方法，以绑定你的自定义数据。

## <a name="dialogs"></a>对话框 {#dialogs}

<div class="row">
<div class="col s12 m7" markdown="1">

我们深知 Dialog 使用的便捷性，因此我们改造了 Dialog，使 Dialog 也能适合展示在手表上，并扩展了对话框的按钮、列表展示，也提供了一些数值选择对话框，所有这些，在保持 Android 接口的便捷性同时，也提供了手表上较为便捷的用户体验。

</div>
<div class="col s12 m4 push-m1 center">
<img src="res/delay-confirm-dialog.png">
</div>
</div>

### <a name="alert-dialog"></a>弹出式对话框 {#alert-dialog}

移植并扩展了 Android 的 [`AlertDialog`][android-alert-dialog]。为其定制了适用于手表的主题。并提供了利于手表显示的圆形图标按钮，以替代原生的文字按钮。

当设置的文本消息非常长时，消息将可以滚动，并且，滚动时底部的图标按钮会消失，以便更方便的阅读文本内容。

使用方式与原生的 `AlertDialog` 无异，只是需要制定图标资源文件，或图标的`Drawable`，类似下面的使用方式：

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

使用对话框时，可通过指定 `android:alertDialogTheme` 来自定义对话框的样式，以实现想要的效果。

`AlertDialog` 中，有如下样式可以在主题中覆盖：

1. `tic_windowIconStyle`：指定弹出对话框标题图标的样式。
2. `android:windowTitleStyle`：指定对话框标题样式。
3. `tic_iconButtonBarStyle`：指定按钮条样式。
4. `tic_iconButtonBarPositiveButtonStyle`：指定正面选项的按钮样式。
5. `tic_iconButtonBarNegativeButtonStyle`：指定负面选项的按钮样式。
6. `tic_iconButtonBarNeutralButtonStyle`：指定中立选项的按钮样式，由于手表的屏幕较小，我们不推荐为对话框指定中立选项的按钮。


### <a name="list-choice-dialog"></a>列表选择对话框 {#list-choice-dialog}

类似 Android 的 [`AlertDialog`][android-alert-dialog]，你也可以通过设置Dialog的items、singleChoiceItems 和 multipleChoiceItems 来创建一个列表选择对话框，来获取用户对于一个列表的选择结果。

使用方式与[弹出式对话框](#alert-dialog)一致，类似下面的代码：

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

### <a name="number-picker-dialog"></a>数值选择对话框 {#number-picker-dialog}

通过内嵌 [`NumberPicker`](#number-picker)，开发者可以使用[`AlertDialog`][android-alert-dialog]来显示一个对话框让用户选择一个值。使用方式如下：

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

### <a name="date-picker-dialog"></a>日期时间选择器 {#date-picker-dialog}

为了开发的方便，我们封装了[`DatePicker`和`TimePicker`](#date-picker)，提供了一个日期时间选择对话框，`DatetimePickerDialog`，可以像使用[`AlertDialog`][android-alert-dialog]一样，显示一个对话框让用户选择日期、时间，或同时选择两者。使用方式如下：

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

如果你只想让用户选择日期，或者只选择时间，那么你需要做的，是在 Building 时，指定 `disableTimePicker()` 或者 `disableDatePicker()`。详情可以参考我们 Demo 中的 `DialogsFragment`。

## <a name="menu"></a>悬浮上下文菜单 {#menu}

通过 `FloatingContextMenu`，你可以非常方便的创建一个悬浮在内容之上的上下文内容相关的选择菜单。

类似 Android 的 [长按弹出菜单][android-FloatingContextMenu]，开发者可以在 [menu resource][android-menu-resource] 中创建菜单资源。并在 Menu 创建时，通过 `ContextMenuCreator` 回调为 `FloatingContextMenu` 指定内容。

用户的选择结果，将通过 `OnMenuSelectedListener` 回调给使用者。

指定好菜单创建和选择的回调接口以后，开发者可以通过 `show(View)` 来显示悬浮菜单，并将菜单绑定到指定的 View 之上。这个 View 将会影响 menu 的生命周期，在 View 从窗口中 detach 时，绑定的 menu 将会销毁。另外，指定的 view，将会传递给 `ContextMenuCreator` 作为菜单资源创建的上下文参考。

下面是一个简单的使用示例：

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

通过指定菜单的创建和选择回调，开发者可以指定菜单的内容，并获取菜单选择的结果。

需要注意的是，目前我们的 `FloatingContextMenu` 仅支持基础的菜单项内容（icon、 title 和 intent），不支持嵌套菜单（menu group）和 action 等复杂属性。


## <a name="widgets"></a>小控件 {#widgets}

### <a name="scale-textview"></a>可缩放文本框 {#scale-textview}

`ScalableTextView`，可缩放文本框，可以跟随控件大小而改变文字大小。常用于`TitleBar`中的标题显示。

使用时，可以从XML文件，或者代码中，指定缩放因子 `scaleFactor`。文字大小变化、文本框大小变化与缩放因子之间符合下面的等式：

$$
\Delta_{textScale} = \Delta_{frameScale} \times scaleFactor
$$

其中：

$$
\Delta_{frameScale} = max\left(
    \frac{w_{dst}}{w_{src}},
    \frac{h_{dst}}{h_{src}}
\right)
$$

需要注意的是，由于文字会进行缩放，所以可能会越出边界。使用时最好指定足量的padding，或者设置无变化的那边为 match_parent。例如，`TitleBar`中的`ScalableTextView`，通常会以如下方式布局：

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

### <a name="fab"></a>悬浮按钮 {#fab}

`FloatingActionButton`，悬浮按钮，是一个扩展的`ImageButton`。我们从 [Android Design Support][google-design-support] 库中将其移植过来，去除了一些不利于手表展示的部分（比如与`SnackBar`的交互），增加了Ticwear特有的设计元素。

常规的使用方式，可以查看 [Android 官方文档][android-fab]。

而Ticwear特有的修改，主要是增加了`minimum`，最小化状态（原生控件只支持`shown`和`hidden`状态），这个状态，会使得按钮缩小到一个小点，不会遮挡文字，又可以提示用户这里有可操作的元素。

使用方式类似`show()`和`hide()`，调用`minimize()`可以最小化按钮，按钮在完成最小化以后，会触发 `OnVisibilityChangedListener.onMinimum` 回调。

`backgroundDrawable`中引用了[圆形进度条](#cpd)，并可对进度条的进度，透明度，模式等进行设置

### <a name="cpd"></a>圆形进度条（CircularProgressDrawable） {#cpd}

圆形的进度条，仿照安卓的[`progressBar`][android-progressbar]，效果模式分为`determinate`及`indeterminate`，`determinate`模式静态显示当前`progressBar`的进度，`indeterminate`模式动态旋转`progressBar`。

使用者可在代码中动态设置`progressBar`的progress，alpha值，
及颜色，若使用者未做任何设置，则默认对`progressBar`及背景色进行`tint`，`alpha`值为50%，在任意情况下，当使用者在代码中设置`progress`的进度后，`progress`的模式自动变为`determinate`。

在初始化时，使用者可对`progressBar`进行各项设定，如设定每圈持续时间，初始角度，动态最大/最小角度等，这些属性在`CircularProgressDrawable`对象生成后不可修改。

在FAB中使用者可选择是否包含进度条，当无进度条时，FAB仍可设
置进度，`progressbar`颜色等，但无任何效果。

### <a name="primary-button"></a>主要按钮（PrimaryButton） {#primary-button}

手表界面开发的一项原则，是精简内容和选择，让用户能迅速明白自己需要做什么。为此，我们经常需要一个放置在页面底部，占用较大区域的主要按钮。而 `PrimaryButton` 则提供这样一个按钮。

`PrimaryButton` 是一个特殊的 `ImageButton`，它的背景是一个半圆的色块，放置在圆表底部时，会显得非常和谐。

### <a name="number-picker"></a>数值选择器 {#number-picker}

类似 Android 的 [`NumberPicker`][android-numberpicker]，我们实现了符合Ticwear设计标准的数值选择器。开发者可以直接使用到其 layout 中。如果你只是需要一个页面来获取用户输入的数值。我们还提供了一个便于使用的[`NumberPickerDialog`](#number-picker-dialog)，方便你快速开发。

### <a name="date-picker"></a>日期时间选择器 {#date-picker}

类似于 Android 的 [`TimePicker`][android-timepicker] 和 [`DatePicker`][android-datepicker]，我们实现了符合Ticwear设计标准的日期和时间选择器。开发者可以像 Android 控件一样使用它们。同样的，我们也提供了[`DateTimePickerDialog`](#date-picker-dialog)来快速获取用户的时间、日期输入。

### <a name="two-state-button"></a>Checkbox、RadioButton 和 SimpleSwitch {#two-state-button}

我们为 [Ticwear Theme](#style-and-theme) 设置了符合Ticwear设计风格的 `Checkbox` 和 `RadioButton`，并提供了一个 `SimpleSwitch`，简化 `Switch` 按钮的操作，并与其他两个状态切换Button统一了风格。以提供一套美观的状态切换按钮。


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


