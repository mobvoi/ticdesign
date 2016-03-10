## Ticwear 设计辅助库使用说明

使用 Ticwear 设计辅助库（后文简称设计库），你可以轻松让你的应用符合 Ticwear 的[设计规范][ticwear-design]，并支持挠挠等新型交互方式。

除挠挠之类 Ticwear 特有的交互外，设计库的其他部分也可以工作在 Android Wear 上。

设计库包含下面几个部分：

1. [样式和主题](#style-and-theme)：定义了一些列文字、页面和控件的样式，已经页面切换等动效支持。
2. [可拉伸、跟随内容的标题](#title-bar)：基于 [Android Design Support][google-design-support]，构建了一套适合手表展示的页面结构，除了 Google Design 中的跟随滚动等效果，我们还为标题栏增加了可拉伸等效果。
3. [支持挠挠的 Listview](#ticklable-listview)：基于 `RecyclerView`，我们创造了一个列表展示控件，使其在触摸操作时与普通线性布局的列表操作无异，而使用挠挠交互时，具有聚焦效果（聚焦效果类似 `WearableListView`），方便挠挠的操作。
4. [设置](#preference)：提供一套类似 [Android Settings][android-settings] 的、符合 [Ticwear Design][ticwear-design] 的设置系统，更适合手表展示，并支持挠挠交互。
5. [其他小控件](#widgets)：Ticwear提供了一系列适合手表使用的小控件，包括[可缩放文本框](#scale-textview)、[悬浮按钮](#fab)、[弹出式对话框](#alert-dialog)、[数值选择器](#number-picker)、[日期时间选择器](#date-picker)等。

### <a id="style-and-theme"></a> 样式和主题

### <a id="title-bar"></a> 响应内容滚动操作的标题栏

### <a id="ticklable-listview"></a> 支持挠挠交互的 Listview

### <a id="preference"></a> 设置系统

Ticwear 的设置系统类似 [Android Settings][android-settings]，你可以使用与 Android Preference 相同的方式来使用 Ticwear Preference。但请注意，Ticwear Preference 已经将内置的 `ListView` 改成了 `TicklableListView`，你需要使用 `RecyclerView.ViewHolder` 的方式来绑定数据到 Preference view 上面。

当你需要实现自定义的 `Preference` 时，需要继承 `Preference.ViewHolder`，并按需要覆盖其方法，以绑定你的自定义数据。

### <a id="widgets"></a> 小控件

#### <a id="scale-textview"></a> 可缩放文本框

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

#### <a id="fab"></a> 悬浮按钮

`FloatingActionButton`，悬浮按钮，是一个扩展的`ImageButton`。我们从 [Android Design Support][google-design-support] 库中将其移植过来，去除了一些不利于手表展示的部分（比如与`SnackBar`的交互），增加了Ticwear特有的设计元素。

常规的使用方式，可以查看 [Android 官方文档][android-fab]。

而Ticwear特有的修改，主要是增加了`minimum`，最小化状态（原生控件只支持`shown`和`hidden`状态），这个状态，会使得按钮缩小到一个小点，不会遮挡文字，又可以提示用户这里有可操作的元素。

使用方式类似`show()`和`hide()`，调用`minimize()`可以最小化按钮，按钮在完成最小化以后，会触发 `OnVisibilityChangedListener.onMinimum` 回调。

#### <a id="alert-dialog"></a> 弹出式对话框

移植并扩展了 Android 的 [`AlertDialog`][android-alert-dialog]。为其定制了适用于手表的主题。并提供了利于手表显示的圆形图标按钮，以替代原生的文字按钮。

当设置的文本消息非常长时，消息将可以滚动，并且，滚动时底部的图标按钮会消失，以便更方便的阅读文本内容。

使用方式与原生的 `AlertDialog` 无异，只是需要制定图标资源文件，或图标的`Drawable`，类似下面的使用方式：

``` Java
new AlertDialog.Builder(context)
        .setTitle(R.string.dialog_title)
        .setMessage(R.string.dialog_content)
        .setPositiveButtonIcon(R.drawable.ic_btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something for positive action.
                dialog.dismiss();
            }
        })
        .show();
```

#### <a id="number-picker"></a> 数值选择器

#### <a id="date-picker"></a> 日期时间选择器


[ticwear-design]: http://developer.ticwear.com/doc/guideline
[google-design-support]: http://android-developers.blogspot.hk/2015/05/android-design-support-library.html
[android-settings]: http://developer.android.com/guide/topics/ui/settings.html
[android-alert-dialog]: http://developer.android.com/reference/android/app/AlertDialog.html


