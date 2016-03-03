## Ticwear 设计辅助库使用说明

使用 Ticwear 设计辅助库（后文简称设计库），你可以轻松让你的应用符合 Ticwear 的[设计规范][ticwear-design]，并支持挠挠等新型交互方式。

除挠挠之类 Ticwear 特有的交互外，设计库的其他部分也可以工作在 Android Wear 上。

设计库包含下面几个部分：

1. [样式和主题](#style-and-theme)：定义了一些列文字、页面和控件的样式，已经页面切换等动效支持。
2. [可拉伸、跟随内容的标题](#title-bar)：基于 [Android Design Support][google-design-support]，构建了一套适合手表展示的页面结构，除了 Google Design 中的跟随滚动等效果，我们还为标题栏增加了可拉伸等效果。
3. [支持挠挠的 Listview](#ticklable-listview)：基于 `RecyclerView`，我们创造了一个列表展示控件，使其在触摸操作时与普通线性布局的列表操作无异，而使用挠挠交互时，具有聚焦效果（聚焦效果类似 `WearableListView`），方便挠挠的操作。
4. [设置](#preference)：提供一套类似 [Android Settings][android-settings] 的、符合 [Ticwear Design][ticwear-design] 的设置系统，更适合手表展示，并支持挠挠交互。
5. [数值选择器](#picker)：使用数值选择器，你能方便的获取用户的[数值输入](#number-picker)，或者[日期时间](#date-picker)的输入。

### <a id="style-and-theme"></a> 样式和主题

### <a id="title-bar"></a> 响应内容滚动操作的标题栏

### <a id="ticklable-listview"></a> 支持挠挠交互的 Listview

### <a id="preference"></a> 设置系统

Ticwear 的设置系统类似 [Android Settings][android-settings]，你可以使用与 Android Preference 相同的方式来使用 Ticwear Preference。但请注意，Ticwear Preference 已经将内置的 `ListView` 改成了 `TicklableListView`，你需要使用 `RecyclerView.ViewHolder` 的方式来绑定数据到 Preference view 上面。

当你需要实现自定义的 `Preference` 时，需要继承 `Preference.ViewHolder`，并按需要覆盖其方法，以绑定你的自定义数据。

### <a id="picker"></a> 数值和日期时间选择器

#### <a id="number-picker"></a> 数值选择器

#### <a id="date-picker"></a> 日期时间选择器


[ticwear-design]: #
[google-design-support]: http://android-developers.blogspot.hk/2015/05/android-design-support-library.html
[android-settings]: http://developer.android.com/guide/topics/ui/settings.html


