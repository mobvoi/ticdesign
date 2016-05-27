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

import android.graphics.Color;

public class ColorUtils {

	private static int getMiddleValue(int prev, int next, float factor){
		return Math.round(prev + (next - prev) * factor);
	}

	public static int getMiddleColor(int prevColor, int curColor, float factor){
		if(prevColor == curColor)
			return curColor;

		if(factor == 0f)
			return prevColor;
		else if(factor == 1f)
			return curColor;

		int a = getMiddleValue(Color.alpha(prevColor), Color.alpha(curColor), factor);
		int r = getMiddleValue(Color.red(prevColor), Color.red(curColor), factor);
		int g = getMiddleValue(Color.green(prevColor), Color.green(curColor), factor);
		int b = getMiddleValue(Color.blue(prevColor), Color.blue(curColor), factor);

		return Color.argb(a, r, g, b);
	}

	public static int getColor(int baseColor, float alphaPercent){
		int alpha = Math.round(Color.alpha(baseColor) * alphaPercent);

		return (baseColor & 0x00FFFFFF) | (alpha << 24);
	}
}