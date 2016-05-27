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

package ticwear.design;

import android.os.Build;

/**
 * Library configurations, delete when we integrated the gradle into make.
 *
 * Created by tankery on 4/15/16.
 */
@SuppressWarnings("PointlessBooleanExpression")
public class DesignConfig {

    public static final boolean DEBUG = !Build.TYPE.equals("user");

    public static final boolean DEBUG_PICKERS = DEBUG && false;
    public static final boolean DEBUG_RECYCLER_VIEW = DEBUG && false;
    public static final boolean DEBUG_SCROLLBAR = DEBUG && false;
    public static final boolean DEBUG_COORDINATOR = DEBUG && false;

}
