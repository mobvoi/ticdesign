/*
 * Copyright (c) 2017 Mobvoi Inc.
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

package ticwear.design.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Create some common ticwear dialogs.
 *
 * Created by tankery on 08/03/2017.
 */

public class TicwearDialogs {

    @NonNull
    public static AlertDialog permissionRequestDialog(@NonNull Context context,
                                                      @Nullable String message,
                                                      @Nullable final Runnable positiveAction) {
        return permissionRequestDialog(context, message, positiveAction, null);
    }

    @NonNull
    public static AlertDialog permissionRequestDialog(@NonNull Context context,
                                                      @Nullable String message,
                                                      @Nullable final Runnable positiveAction,
                                                      @Nullable final Runnable negativeAction) {
        return permissionRequestDialog(context, message, positiveAction, negativeAction, null);
    }

    @NonNull
    public static AlertDialog permissionRequestDialog(@NonNull Context context,
                                                      @Nullable String message,
                                                      @Nullable final Runnable positiveAction,
                                                      @Nullable final Runnable negativeAction,
                                                      @Nullable final Runnable cancelAction) {
        class ActionStatus {
            boolean hasAction = false;
        }

        final ActionStatus actionStatus = new ActionStatus();

        return new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButtonIcon(ticwear.design.R.drawable.tic_ic_btn_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                actionStatus.hasAction = true;
                                dialog.dismiss();
                                if (positiveAction != null) {
                                    positiveAction.run();
                                }
                            }
                        })
                .setNegativeButtonIcon(ticwear.design.R.drawable.tic_ic_btn_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                actionStatus.hasAction = true;
                                dialog.dismiss();
                                if (negativeAction != null) {
                                    negativeAction.run();
                                }
                            }
                        })
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        // If dismiss with no user action, we know the user has cancel the dialog.
                        if (!actionStatus.hasAction && cancelAction != null) {
                            cancelAction.run();
                        }
                    }
                })
                .setDelayConfirmAction(DialogInterface.BUTTON_POSITIVE, 5000)
                .create();
    }

}
