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

import android.support.annotation.NonNull;

/**
 * An interface for container has multiple picker.
 *
 * Created by tankery on 4/13/16.
 */
public interface MultiPickerContainer {

    interface MultiPickerClient {

        /**
         * Invoke before a number picker is focused.
         *
         * This method give the client a chance to handle the 'next focus'
         * event. If the client handled the event, multi-picker will not
         * response this focus request.
         *
         * @param numberPicker the number picker who about to be focus.
         * @param fromLast If this focus is changed from last picker.
         * @return true if the client wan't to handle the focus event.
         */
        boolean onPickerPreFocus(NumberPicker numberPicker, boolean fromLast);

        /**
         * Notify the view has gain focus
         *
         * @param numberPicker the picker who gain focus
         */
        void onPickerPostFocus(@NonNull  NumberPicker numberPicker);

    }

    void setMultiPickerClient(MultiPickerClient client);

}
