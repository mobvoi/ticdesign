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

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageButton;

import ticwear.design.R;
import ticwear.design.drawable.ArcDrawable;

/**
 * Primary button is a semi-circle large image button align to button.
 *
 * Created by tankery on 4/8/16.
 */
public class PrimaryButton extends ImageButton {

    public PrimaryButton(Context context) {
        this(context, null);
    }

    public PrimaryButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.tic_primaryButtonStyle);
    }

    public PrimaryButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_Ticwear_PrimaryButton);
    }

    public PrimaryButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(android.R.attr.colorControlHighlight, typedValue, true);
        ColorStateList rippleColor = ColorStateList.valueOf(typedValue.data);

        Drawable drawable = new ArcDrawable(background);
        RippleDrawable ripple = new RippleDrawable(rippleColor, drawable, drawable);

        super.setBackgroundDrawable(ripple);
    }

}
