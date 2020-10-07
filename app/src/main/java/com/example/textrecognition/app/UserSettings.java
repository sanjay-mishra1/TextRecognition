package com.example.textrecognition.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.textrecognition.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class UserSettings extends AppCompatActivity {

    public static boolean userImgChanged;
    public static boolean userNameChanged;
    private String uid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        setData();

    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;

    }
    private void setData() {
        ImageView imageView=findViewById(R.id.profileImg);
        Uri img=FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        uid=FirebaseAuth.getInstance().getCurrentUser().getEmail();
        uid=uid.substring(0,uid.indexOf("@"));
        if (img!=null)
            Glide.with(this)
                    .load(img)
                    .into(imageView);
        setUserid();
        setUserName();
    }
    public void  setUserid(){
        uid=FirebaseAuth.getInstance().getCurrentUser().getEmail();
        uid=uid.substring(0,uid.indexOf("@"));
        ((TextView)findViewById(R.id.email)).setText(uid);
    }
    public  void  setUserName(){
        String name=FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if (name!=null)
            ((TextView)findViewById(R.id.name)).setText(name);
    }
    public void loginOutClick(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent=new Intent(this,LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void changeUserIdClicked(View view) {
        BottomSheetDialogFragment dialogFragment=new UserProfileChangeFragment();
        dialogFragment.show(getSupportFragmentManager(),"userid");
    }

    public void changePasswordClicked(View view) {
        BottomSheetDialogFragment dialogFragment=new UserProfileChangeFragment();
        dialogFragment.show(getSupportFragmentManager(),"password");
    }
    public void changeUserNameClicked(View view) {
        BottomSheetDialogFragment dialogFragment=new UserProfileChangeFragment();
        dialogFragment.show(getSupportFragmentManager(),"name");
    }
    public void changeProfileImageClicked(View view) {
        showImageChooser();
    }

    private void showImageChooser() {
        if (ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 102);
            }catch (ActivityNotFoundException e){
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), 102);
            }catch (Exception e){
//                setMsg(false,"Error occurred");
//                showMsg(R.color.red,false);
            }
        }else{
            ActivityCompat.requestPermissions(this,new String[]{READ_EXTERNAL_STORAGE},102);
        }
    }
    private void uploadImageToFirebaseStorage(Uri link,ImageView    imageView) {
        TextView textView=findViewById(R.id.change_img);
        textView.setText("Changing image");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference("files/"+uid+"_pr");
        storageRef.putFile(link).addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Toast.makeText(UserSettings.this,"Failed to upload profile image",Toast.LENGTH_SHORT).show();
        }).addOnSuccessListener(taskSnapshot -> {
            userImgChanged=true;
            taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(task -> {

                if (task.isSuccessful())
                {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        imageView.setClickable(true);
                        changeUserProfile("img",uri.toString());
                        imageChangeAfterSteps(String.valueOf(uri),imageView,textView);
                    });
                }
            });
        });
    }
    void changeUserProfile(String key,String data){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null) {
            UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder();
            if (key.equals("img"))
                profileUpdates.setPhotoUri(Uri.parse(data));
            else  {
                ((TextView)findViewById(R.id.name)).setText("Saving name...");
                profileUpdates.setDisplayName(data);
            }
                   // profileUpdates.build();

            user.updateProfile(profileUpdates.build())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(UserSettings.this,key+" changed",Toast.LENGTH_SHORT).show();
                            if (key.equals("name"))
                            {   setUserName();
                                userNameChanged=true;
                            }
                            else userImgChanged=true;
                            Log.d("Settings", "User profile updated.");
                        }
                    });
        }
    }

    public void moveDatabaseToNewUid(String newUid,String oldUid) {
        Log.e("MoveDb","old->"+oldUid+" new->"+newUid);
       DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Customers");
       databaseReference.child(oldUid).addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               databaseReference.child(newUid).setValue(snapshot.getValue());
               snapshot.getRef().setValue(null);
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });
    }

    void imageChangeAfterSteps(String url, ImageView imageView, TextView textView){
        Glide.with(this)
                .load(url)
                .into(imageView);
        textView.setText("Profile image changed");
        new Handler().postDelayed(() -> {
            if (!isFinishing())
                textView.animate().alpha(0).setDuration(500).withEndAction(() -> {
                    textView.setAlpha(1);
                    textView.setText("Select new profile image");
                });
        },3000);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uriProfileImage = data.getData();
            ImageView imageView=findViewById(R.id.profileImg);
            Glide.with(this).load(uriProfileImage)
                    .into(imageView);
            imageView.setClickable(false);
            uploadImageToFirebaseStorage(uriProfileImage,imageView);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            showImageChooser();
        }else{
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,permissions[0])){
                Snackbar    snackbar=Snackbar.make(findViewById(R.id.parent),"Require storage permission", Snackbar.LENGTH_LONG);
                snackbar.setAction("Settings", v -> {
                    Intent  intent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("package:"+getPackageName()));
                    startActivity(intent);
                });
                snackbar.setBackgroundTint(getResources().getColor(R.color.colorPrimary));
                snackbar.setTextColor(getResources().getColor(R.color.white));
                snackbar.setActionTextColor(getResources().getColor(R.color.white));
                snackbar.show();
            }

        }
    }


}
