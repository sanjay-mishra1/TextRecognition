/*
 * Copyright 2020 Google LLC. All rights reserved.
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

package com.example.textrecognition.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.example.textrecognition.CameraSource;
import com.example.textrecognition.CameraSource.SizePair;
import com.example.textrecognition.R;
import com.google.android.gms.common.images.Size;
import com.google.common.base.Preconditions;

/** Utility class to retrieve shared preferences. */
public class PreferenceUtils {

  static void saveString(Context context, @StringRes int prefKeyId, @Nullable String value) {
    PreferenceManager.getDefaultSharedPreferences(context)
        .edit()
        .putString(context.getString(prefKeyId), value)
        .apply();
  }

  @Nullable
  public static SizePair getCameraPreviewSizePair(Context context, int cameraId) {
    Preconditions.checkArgument(
        cameraId == CameraSource.CAMERA_FACING_BACK
            || cameraId == CameraSource.CAMERA_FACING_FRONT);
    String previewSizePrefKey;
    String pictureSizePrefKey;
    if (cameraId == CameraSource.CAMERA_FACING_BACK) {
      previewSizePrefKey = context.getString(R.string.pref_key_rear_camera_preview_size);
      pictureSizePrefKey = context.getString(R.string.pref_key_rear_camera_picture_size);
    } else {
      previewSizePrefKey = context.getString(R.string.pref_key_front_camera_preview_size);
      pictureSizePrefKey = context.getString(R.string.pref_key_front_camera_picture_size);
    }

    try {
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
      return new SizePair(
          Size.parseSize(sharedPreferences.getString(previewSizePrefKey, null)),
          Size.parseSize(sharedPreferences.getString(pictureSizePrefKey, null)));
    } catch (Exception e) {
      return null;
    }
  }




  /**
   * Mode type preference is backed by {@link android.preference.ListPreference} which only support
   * storing its entry value as string type, so we need to retrieve as string and then convert to
   * integer.
   */

  public static boolean isCameraLiveViewportEnabled(Context context) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String prefKey = context.getString(R.string.pref_key_camera_live_viewport);
    return sharedPreferences.getBoolean(prefKey, false);
  }

  private PreferenceUtils() {}
}
