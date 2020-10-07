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

package com.example.textrecognition.java;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.textrecognition.BuildConfig;
import com.example.textrecognition.R;
import com.example.textrecognition.app.UserSettings;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Demo app chooser which takes care of runtime permission requesting and allow you pick from all
 * available testing Activities.
 */
public final class ChooserActivity extends AppCompatActivity
    implements OnRequestPermissionsResultCallback, AdapterView.OnItemClickListener {
  private static final String TAG = "ChooserActivity";
  private static final int PERMISSION_REQUESTS = 1;
  static public Set<String> allTextExtracted=new HashSet<>();
  static public StringBuilder formattedText=new StringBuilder();
  private static final Class<?>[] CLASSES =
      new Class<?>[] {
        LivePreviewActivity.class, StillImageActivity.class
      };

  private static final int[] DESCRIPTION_IDS =
      new int[] {
        R.string.desc_camera_source_activity,
        R.string.desc_still_image_activity

      };
  private static final int[] TITLE_IDS =
          new int[] {
                  R.string.title_camera_source_activity,
                  R.string.title_still_image_activity

          };
  private ImageView profileImage;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    if (BuildConfig.DEBUG) {
      StrictMode.setThreadPolicy(
          new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
      StrictMode.setVmPolicy(
          new StrictMode.VmPolicy.Builder()
              .detectLeakedSqlLiteObjects()
              .detectLeakedClosableObjects()
              .penaltyLog()
              .build());
    }
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    setContentView(R.layout.activity_chooser);
    profileImage= findViewById(R.id.userimage);
    // Set up ListView and Adapter
    ListView listView = findViewById(R.id.test_activity_list_view);

    MyArrayAdapter adapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_2, CLASSES);
    adapter.setDescriptionIds(DESCRIPTION_IDS);
    adapter.setTitleIds(TITLE_IDS);
    listView.setAdapter(adapter);
    listView.setOnItemClickListener(this);

    if (!allPermissionsGranted()) {
      getRuntimePermissions();
    }
    loadUserImage();
    loadUserName();
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Class<?> clicked = CLASSES[position];
    allTextExtracted.clear();
    if (allPermissionsGranted())
    startActivity(new Intent(this, clicked));
    else getRuntimePermissions();
  }

  private String[] getRequiredPermissions() {
    try {
      PackageInfo info =
          this.getPackageManager()
              .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
      String[] ps = info.requestedPermissions;
      if (ps != null && ps.length > 0) {
        return ps;
      } else {
        return new String[0];
      }
    } catch (Exception e) {
      return new String[0];
    }
  }

  private boolean allPermissionsGranted() {
    for (String permission : getRequiredPermissions()) {
      if (!isPermissionGranted(this, permission)) {
        return false;
      }
    }
    return true;
  }

  private void getRuntimePermissions() {
    List<String> allNeededPermissions = new ArrayList<>();
    for (String permission : getRequiredPermissions()) {
      if (!isPermissionGranted(this, permission)) {
        allNeededPermissions.add(permission);
      }
    }

    if (!allNeededPermissions.isEmpty()) {
      ActivityCompat.requestPermissions(
          this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
    }
  }

  private static boolean isPermissionGranted(Context context, String permission) {
    if (ContextCompat.checkSelfPermission(context, permission)
        == PackageManager.PERMISSION_GRANTED) {
      Log.i(TAG, "Permission granted: " + permission);
      return true;
    }
    Log.i(TAG, "Permission NOT granted: " + permission);
    return false;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if(grantResults[0]==PackageManager.PERMISSION_GRANTED){

    }else{
      if (!ActivityCompat.shouldShowRequestPermissionRationale(this,permissions[0])){

          showPermissionErrorMessage();
      }
    }
  }

  private void showPermissionErrorMessage() {
    Snackbar    snackbar=Snackbar.make(findViewById(R.id.parent),"Required all permissions to work properly", Snackbar.LENGTH_LONG);
    snackbar.setAction("Settings", v -> {
      Intent  intent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
      intent.addCategory(Intent.CATEGORY_DEFAULT);
      intent.setData(Uri.parse("package:"+getPackageName()));
      startActivity(intent);
    });
    snackbar.show();

  }

  @Override
  protected void onStart() {
    super.onStart();
    if (UserSettings.userImgChanged)
      loadUserImage();
    if (UserSettings.userNameChanged)
      loadUserName();
  }

  private void loadUserName() {
    FirebaseUser auth=FirebaseAuth.getInstance().getCurrentUser();
    if (auth!=null)
    {
      TextView textView = findViewById(R.id.username);
      if ((auth.getPhotoUrl()==null||auth.getPhotoUrl().toString().isEmpty())&&!auth.getDisplayName().isEmpty())
      {Log.e("Name","Setting user name ");
        textView.setVisibility(View.VISIBLE);
       profileImage.setVisibility(View.GONE);
        textView.setText(auth.getDisplayName().substring(0,1).toUpperCase());
      }
      else Log.e("Name","No name found");

    }
  }

  private void loadUserImage() {
    try {
      TextView textView = findViewById(R.id.username);
      textView.setVisibility(View.VISIBLE);
      String name=FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
      if (name!=null&&!name.isEmpty()) textView.setText(name.substring(0, 1));
      Uri img = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
      if (img!=null&&!img.toString().isEmpty())
      {
        Glide.with(this)
                .load(img)
                .apply(RequestOptions.circleCropTransform()).into(profileImage);
        profileImage.setVisibility(View.VISIBLE);

      }


    }catch (Exception e){e.printStackTrace();
      findViewById(R.id.nameLayout).setVisibility(View.GONE);
    }
  }
  public void settingsOnClick(View view) {
    startActivity(new Intent(this, UserSettings.class));
  }

  private static class MyArrayAdapter extends ArrayAdapter<Class<?>> {

    private final Context context;
    private final Class<?>[] classes;
    private int[] descriptionIds;
    private int[] titleIds;

    MyArrayAdapter(Context context, int resource, Class<?>[] objects) {
      super(context, resource, objects);

      this.context = context;
      classes = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = convertView;

      if (convertView == null) {
        LayoutInflater inflater =
            (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(android.R.layout.simple_list_item_2, null);
      }

      ((TextView) view.findViewById(android.R.id.text1)).setText(titleIds[position]);
      ((TextView) view.findViewById(android.R.id.text2)).setText(descriptionIds[position]);

      return view;
    }

    void setDescriptionIds(int[] descriptionIds) {
      this.descriptionIds = descriptionIds;
    }
    void setTitleIds(int[] titleIds) {
      this.titleIds = titleIds;
    }
  }
}
