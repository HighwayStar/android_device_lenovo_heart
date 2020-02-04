/*
 * Copyright (C) 2018 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.device;

import android.content.Context;

import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.Vibrator;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.KeyEvent;
import com.android.internal.os.DeviceKeyHandler;

public class KeyHandler implements DeviceKeyHandler {
    private static final String TAG = KeyHandler.class.getSimpleName();

    // Slider key codes
    private static final int SLIDER_CLOSED = 221;
    private static final int SLIDER_OPENED = 220;

    private static final int SWITCH_WAKELOCK_DURATION = 3000;

    private final Context mContext;

    private final Vibrator mVibrator;
    private final TelecomManager mTelecomManager;
    private final PowerManager mPowerManager;
    private final WakeLock mSwitchWakeLock;

    public KeyHandler(Context context) {
        mContext = context;

        mVibrator = mContext.getSystemService(Vibrator.class);

        mTelecomManager = (TelecomManager) mContext.getSystemService(Context.TELECOM_SERVICE);
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mSwitchWakeLock = mPowerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "SwitchWakeLock");
    }


    public KeyEvent handleKeyEvent(KeyEvent event) {
        int scanCode = event.getScanCode();

        switch (scanCode) {
            case SLIDER_CLOSED:
                Log.e(TAG, "SLIDER_CLOSED");
                if (mTelecomManager != null) {
                        if (mTelecomManager.isInCall()) {
                                mTelecomManager.endCall();
                        };
                };
                break;
            case SLIDER_OPENED:
                Log.e(TAG, "SLIDER_OPENED");
                if (mTelecomManager != null) {
                        if (mTelecomManager.isRinging()) {
                                mTelecomManager.acceptRingingCall();
                         };
                };
                //wakeup display on slider opened
                mSwitchWakeLock.acquire(SWITCH_WAKELOCK_DURATION);
                mPowerManager.wakeUp(SystemClock.uptimeMillis(), "switch-wakeup");
                break;
            default:
                return event;
        }
        doHapticFeedback();

        return null;
    }

    private void doHapticFeedback() {
        if (mVibrator == null || !mVibrator.hasVibrator()) {
            return;
        }

        mVibrator.vibrate(50);
    }
}
