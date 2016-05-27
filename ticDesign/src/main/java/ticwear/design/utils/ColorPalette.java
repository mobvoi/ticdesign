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
import android.support.annotation.NonNull;

import ticwear.design.R;

/**
 * Color palette for ticwear design basic colors.
 *
 * Created by tankery on 3/25/16.
 */
public class ColorPalette {

    public enum ColorName {
        LIGHT_GREEN("LightGreen"),
        GREEN("Green"),
        CYAN("Cyan"),
        DARK_CYAN("DarkCyan"),
        INDIGO("Indigo"),
        DEEP_PURPLE("DeepPurple"),
        DARK_BLUE("DarkBlue"),
        COLD_GREY("ColdGrey"),
        PINK("Pink"),
        RED("Red"),
        DEEP_ORANGE("DeepOrange"),
        ORANGE("Orange"),
        AMBER("Amber"),
        YELLOW("Yellow"),
        BROWN("Brown"),
        WARM_GREY("WarmGrey"),
        ;

        private String colorName;

        ColorName(String name) {
            this.colorName = name;
        }

        public String colorName() {
            return colorName;
        }
    }

    public enum ColorDecorate {
        DARKEN("Darken"),
        NORMAL("Normal"),
        LIGHTEN("Lighten"),
        ;

        private String decorateName;

        ColorDecorate(String name) {
            this.decorateName = name;
        }

        public String decorateName() {
            return decorateName;
        }
    }

    private static final int[][] colorList = {
            {
                    R.color.tic_basic_light_green_darken,
                    R.color.tic_basic_light_green,
                    R.color.tic_basic_light_green_lighten,
            },
            {
                    R.color.tic_basic_green_darken,
                    R.color.tic_basic_green,
                    R.color.tic_basic_green_lighten,
            },
            {
                    R.color.tic_basic_cyan_darken,
                    R.color.tic_basic_cyan,
                    R.color.tic_basic_cyan_lighten,
            },
            {
                    R.color.tic_basic_dark_cyan_darken,
                    R.color.tic_basic_dark_cyan,
                    R.color.tic_basic_dark_cyan_lighten,
            },
            {
                    R.color.tic_basic_indigo_darken,
                    R.color.tic_basic_indigo,
                    R.color.tic_basic_indigo_lighten,
            },
            {
                    R.color.tic_basic_deep_purple_darken,
                    R.color.tic_basic_deep_purple,
                    R.color.tic_basic_deep_purple_lighten,
            },
            {
                    R.color.tic_basic_deep_blue_darken,
                    R.color.tic_basic_deep_blue,
                    R.color.tic_basic_deep_blue_lighten,
            },
            {
                    R.color.tic_basic_gold_grey_darken,
                    R.color.tic_basic_gold_grey,
                    R.color.tic_basic_gold_grey_lighten,
            },
            {
                    R.color.tic_basic_pink_darken,
                    R.color.tic_basic_pink,
                    R.color.tic_basic_pink_lighten,
            },
            {
                    R.color.tic_basic_red_darken,
                    R.color.tic_basic_red,
                    R.color.tic_basic_red_lighten,
            },
            {
                    R.color.tic_basic_deep_orange_darken,
                    R.color.tic_basic_deep_orange,
                    R.color.tic_basic_deep_orange_lighten,
            },
            {
                    R.color.tic_basic_orange_darken,
                    R.color.tic_basic_orange,
                    R.color.tic_basic_orange_lighten,
            },
            {
                    R.color.tic_basic_amber_darken,
                    R.color.tic_basic_amber,
                    R.color.tic_basic_amber_lighten,
            },
            {
                    R.color.tic_basic_yellow_darken,
                    R.color.tic_basic_yellow,
                    R.color.tic_basic_yellow_lighten,
            },
            {
                    R.color.tic_basic_brown_darken,
                    R.color.tic_basic_brown,
                    R.color.tic_basic_brown_lighten,
            },
            {
                    R.color.tic_basic_warm_grey_darken,
                    R.color.tic_basic_warm_grey,
                    R.color.tic_basic_warm_grey_lighten,
            },
    };

    private Context context;

    protected ColorPalette(Context context) {
        this.context = context;
    }

    public static ColorPalette from(Context context) {
        return new ColorPalette(context);
    }

    // Return the normal color of this name.
    public Color color(@NonNull ColorName name) {
        return new Color(context, name.ordinal());
    }


    public static class Color {

        private static final int COLORS_COUNT = ColorName.values().length;
        private static final int DECORATES_COUNT = ColorDecorate.values().length;

        private Context context;
        private int colorResIndex;
        private int colorDecIndex;

        static {
            if (COLORS_COUNT != colorList.length) {
                throw new RuntimeException("ColorPalette is broken!" +
                        " Make sure the color names matches the color list.");
            }
            if (colorList.length > 0 && DECORATES_COUNT != colorList[0].length) {
                throw new RuntimeException("ColorPalette is broken!" +
                        " Make sure the color decorates matches the color list.");
            }
        }

        Color(Context context, int resIndex) {
            this(context, resIndex, ColorDecorate.values().length / 2);
        }

        Color(Context context, int resIndex, int decIndex) {
            this.context = context;
            this.colorResIndex = resIndex;
            this.colorDecIndex = decIndex;
        }

        public int value() {
            if (colorResIndex >= 0 && colorResIndex < COLORS_COUNT &&
                    colorDecIndex >= 0 && colorDecIndex < DECORATES_COUNT) {
                return context.getResources().getColor(colorList[colorResIndex][colorDecIndex]);
            } else {
                return android.graphics.Color.TRANSPARENT;
            }
        }

        public String rgbString() {
            int value = value();

            if (android.graphics.Color.alpha(value) < 0xff) {
                return String.format("#%08x", value());
            } else {
                return String.format("#%06x", value() & 0xffffff);
            }
        }

        public ColorName name() {
            return ColorName.values()[colorResIndex];
        }

        public ColorDecorate decorate() {
            return ColorDecorate.values()[colorDecIndex];
        }

        @Override
        public String toString() {
            String decorate = decorate() == ColorDecorate.NORMAL ?
                    "" : " " + decorate().decorateName();

            return rgbString() + " " + name().colorName() + decorate;
        }

        public Color lighten() {
            // Return if the lighten color is in same color name range.
            if (colorDecIndex + 1 < DECORATES_COUNT) {
                return new Color(context, colorResIndex, colorDecIndex + 1);
            } else {
                return this;
            }
        }

        public Color darken() {
            // Return if the lighten color is in same color name range.
            if (colorDecIndex - 1 >= 0) {
                return new Color(context, colorResIndex, colorDecIndex - 1);
            } else {
                return this;
            }
        }
    }

}
