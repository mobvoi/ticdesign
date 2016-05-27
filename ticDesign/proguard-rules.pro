# Copyright (C) 2016 Mobvoi Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# For DefaultBehavior
-keepattributes *Annotation*

# CoordinatorLayout resolves the behaviors of its child components with reflection.
-keep public class * extends ticwear.design.widget.CoordinatorLayout$Behavior {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>();
}

# GenericInflater create preference instance with name from xml file.
-keep public class * extends ticwear.design.preference.Preference {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>();
}

-keepclassmembers class * {
    void onDrawVerticalScrollBar(...);
}

# Side panel event target should keep for system call.
-keep interface com.mobvoi.ticwear.view.SidePanelEventDispatcher {
    <methods>;
}
-keep interface com.mobvoi.ticwear.view.SidePanelEventDispatcher$SuperCallback {
    <methods>;
}
-keep interface com.mobvoi.ticwear.view.SidePanelEventTarget {
    <methods>;
}
-keep interface com.mobvoi.ticwear.view.SidePanelGestureTarget {
    <methods>;
}

-dontwarn com.mobvoi.ticwear.view.SidePanelEventDispatcher
-dontnote com.mobvoi.ticwear.view.SidePanelEventDispatcher
-dontwarn com.mobvoi.ticwear.view.SidePanelEventDispatcher$SuperCallback
-dontnote com.mobvoi.ticwear.view.SidePanelEventDispatcher$SuperCallback
-dontwarn com.mobvoi.ticwear.view.SidePanelEventTarget
-dontnote com.mobvoi.ticwear.view.SidePanelEventTarget
-dontwarn com.mobvoi.ticwear.view.SidePanelGestureTarget
-dontnote com.mobvoi.ticwear.view.SidePanelGestureTarget

