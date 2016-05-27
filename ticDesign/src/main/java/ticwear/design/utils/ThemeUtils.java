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

package ticwear.design.utils;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.TypedValue;

import ticwear.design.R;

public class ThemeUtils {

	private static final int[] DESIGN_CHECK_ATTRS = { R.attr.tic_windowIconStyle };
	private static TypedValue value;

	public static void checkDesignTheme(Context context) {
		TypedArray a = context.obtainStyledAttributes(DESIGN_CHECK_ATTRS);
		final boolean failed = !a.hasValue(0);
		a.recycle();
		if (failed) {
			throw new IllegalArgumentException("You need to use a Theme.Ticwear theme "
					+ "(or descendant) with the design library.");
		}
	}

	public static int dpToPx(Context context, int dp){
		return (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()) + 0.5f);
	}

	public static int spToPx(Context context, int sp){
		return (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics()) + 0.5f);
	}

	private static int getColor(Context context, int id, int defaultValue){
		if(value == null)
			value = new TypedValue();

		try{
			Theme theme = context.getTheme();
			if(theme != null && theme.resolveAttribute(id, value, true)){
                if (value.type >= TypedValue.TYPE_FIRST_INT && value.type <= TypedValue.TYPE_LAST_INT)
                    return value.data;
                else if (value.type == TypedValue.TYPE_STRING)
                    return context.getResources().getColor(value.resourceId);
            }
		}
		catch(Exception ex){}

		return defaultValue;
	}

	public static int windowBackground(Context context, int defaultValue){
		return getColor(context, android.R.attr.windowBackground, defaultValue);
	}

    public static int textColorPrimary(Context context, int defaultValue){
        return getColor(context, android.R.attr.textColorPrimary, defaultValue);
    }

    public static int textColorSecondary(Context context, int defaultValue){
        return getColor(context, android.R.attr.textColorSecondary, defaultValue);
    }

	public static int colorPrimary(Context context, int defaultValue){
		return getColor(context, android.R.attr.colorPrimary, defaultValue);
	}

	public static int colorPrimaryDark(Context context, int defaultValue){
			return getColor(context, android.R.attr.colorPrimaryDark, defaultValue);
	}

	public static int colorAccent(Context context, int defaultValue){
			return getColor(context, android.R.attr.colorAccent, defaultValue);
	}

	public static int colorControlNormal(Context context, int defaultValue){
			return getColor(context, android.R.attr.colorControlNormal, defaultValue);
	}

	public static int colorControlActivated(Context context, int defaultValue){
			return getColor(context, android.R.attr.colorControlActivated, defaultValue);
	}

	public static int colorControlHighlight(Context context, int defaultValue){
			return getColor(context, android.R.attr.colorControlHighlight, defaultValue);
	}

	public static int colorButtonNormal(Context context, int defaultValue){
			return getColor(context, android.R.attr.colorButtonNormal, defaultValue);
	}

    public static int getType(TypedArray array, int index){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return array.getType(index);
        else{
            TypedValue value = array.peekValue(index);
            return value == null ? TypedValue.TYPE_NULL : value.type;
        }
    }

    public static CharSequence getString(TypedArray array, int index, CharSequence defaultValue){
        String result = array.getString(index);
        return result == null ? defaultValue : result;
    }
}