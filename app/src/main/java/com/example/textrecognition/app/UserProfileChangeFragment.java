package com.example.textrecognition.app;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.textrecognition.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class UserProfileChangeFragment extends BottomSheetDialogFragment {

    private ProgressBar progressBar;
    private EditText e1;
    private Button button;
    private EditText e2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_change,container,false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        e1=view.findViewById(R.id.edit1);
        progressBar =view. findViewById(R.id.progressbar);
        button=view.findViewById(R.id.button);
        if (getTag()!=null&& getTag().equals("name")){
            button.setText("Change name");
            e1.setHint("New user name");
            button.setOnClickListener(v ->saveName(e1.getText().toString().trim()));
        }else if (getTag()!=null&& getTag().equals("userid")){
            e1.setHint("Password");
            e1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            e2=view.findViewById(R.id.edit2);
            e2.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            e2.setHint("New user id");
            e2.setVisibility(View.VISIBLE);
            button.setText("Change user id");
            button.setOnClickListener(v ->changeUserid(e2.getText().toString().trim()));
        }else{
            e1.setHint("Old password");
            e2=view.findViewById(R.id.edit2);
            e2.setHint("New password");
            e2.setVisibility(View.VISIBLE);
            button.setText("Change password");
            button.setOnClickListener(v ->changePassword());
        }
    }

    private void changePassword() {
        String old = e1.getText().toString().trim();
        String newp = e2.getText().toString().trim();
        if (old.isEmpty()) {
            showAlertDialog(false, "Change Password", "Enter old password");
            return;
        }
        if (newp.isEmpty()) {
            showAlertDialog(false,"Change Password","Enter new password");

            return;
        }
        if(old.equals(newp)){
            showAlertDialog(false,"Change Password","Both Passwords cannot be same");
            return;
        }
        String password=getActivity().getSharedPreferences("CRED", MODE_PRIVATE).getString("Password","");
        if(!old.equals(password)){
            showAlertDialog(false,"Change Password","Old password is incorrect");

            return;
        }
        if (password.length() < 6) {
            showAlertDialog(false,"Change Password","Minimum length of password should be 6");

            e2.requestFocus();
            return;
        }
        button.setText("");
        button.setClickable(false);
        authForNewPass(old, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(), newp);
    }
    private void authForNewPass(String old_pass, String email, String new_pass) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

// Get auth credentials from the user for re-authentication. The example below shows
// email and password credentials but there are multiple possible providers,
// such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(email, old_pass);

// Prompt the user to re-provide their sign-in credentials
        Objects.requireNonNull(user).reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updatePassword(new_pass).addOnCompleteListener(task1 -> {
                            progressBar.setVisibility(View.GONE);
                            if (task1.isSuccessful()) {
                                savePassword(new_pass);
                                showAlertDialog(true, "Change Password", "Password updated");
                                button.setText("Sending back");
                                getDialog().dismiss();
                            } else {
                                button.setText("Change Password");
                                button.setClickable(true);
                                showAlertDialog(false, "Change Password", "Error password not updated");
                            }
                        });
                    } else {
                        button.setText("Change password");
                        progressBar.setVisibility(View.GONE);
                        showAlertDialog(false, "Change Password", "An error occurred during authorization");

                    }
                });


    }

    private void savePassword(String new_pass) {

        SharedPreferences sharedPreferences =getActivity(). getSharedPreferences("CRED", MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("Password",
                new_pass);
        edit.apply();
    }

    private void changeUserid(String text) {
        if (text.isEmpty()){
            showAlertDialog(false, "Change user id", "Enter new user id");
            e2.requestFocus();
            return;
        }
        if (e1.getText().toString().trim().isEmpty()){
            showAlertDialog(false, "Change user id", "Enter the password");
            e1.requestFocus();
            return;
        }

        String password=getActivity().getSharedPreferences("CRED", MODE_PRIVATE).getString("Password","");

        if (!password.equals(e1.getText().toString().trim())) {
            showAlertDialog(false, "Change user id", "The password entered is wrong. Please enter the correct password.");
            e2.requestFocus();
            return;
        }
        text=text+"@gmail.com";

        String oldEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (text.equals(oldEmail)) {
            showAlertDialog(false, "Change user id", "Both user id is same. Please enter a new user id.");
            e2.requestFocus();
            return;
        }


        if (!Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
            showAlertDialog(false, "Change user id", "Invalid user id");
            e1.requestFocus();
            return;
        }
        button.setText("");
        button.setClickable(false);
        authForNewEmail(oldEmail,
                text, password);

    }

    private void authForNewEmail(String oldemail, String newemail, String pass) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Get auth credentials from the user for re-authentication
        AuthCredential credential = EmailAuthProvider
                .getCredential(oldemail, pass); // Current Login Credentials \\
        // Prompt the user to re-provide their sign-in credentials

        Objects.requireNonNull(user).reauthenticate(credential)
                .addOnSuccessListener(task -> {
                    //Now change your email address \\
                    //----------------Code for Changing Email Address----------\\
                    FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();
                    Objects.requireNonNull(user1).updateEmail(newemail)
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    getDialog().dismiss();
//                                    bt.setText("Sending back");
                                    progressBar.setVisibility(View.GONE);
                                    ((UserSettings)getActivity()).setUserid();
                                    ((UserSettings)getActivity()).moveDatabaseToNewUid(newemail.substring(0,newemail.indexOf("@")),oldemail.substring(0,oldemail.indexOf("@")));
                                    showAlertDialog(true, "Change Email", "user id is updated");
                                }
                            }).addOnFailureListener(e -> {
                        button.setText("Change user id");
                        button.setClickable(true);
                        progressBar.setVisibility(View.GONE);

                        if (e instanceof FirebaseAuthUserCollisionException)
                            showAlertDialog(false,"Change Email","User id already exist");
                        else
                        showAlertDialog(false,"Change Email","Failed to change the user id.Check your password and new user id again");

                    });
                });
    }

    private void showAlertDialog(boolean success,String header, String msg) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle(header);
        alert.setMessage(msg);
        if (!success){
            alert.setPositiveButton("OK", (dialogInterface, i) -> {
                dialogInterface.dismiss();

            });
        }else {
            alert.setPositiveButton("OK", (dialogInterface, i) -> {
               try {
                   dialogInterface.dismiss();
                   getDialog().dismiss();
               }catch (Exception e){e.printStackTrace();}
            });
            if (!msg.contains("updated"))
                alert.setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });
            else alert.setOnDismissListener(dialog -> {
              try {
                  dialog.dismiss();
                getDialog().dismiss();
              }catch (Exception e){e.printStackTrace();}
            });
        }



        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }

    private void saveName(String text) {
        if (text.trim().isEmpty())
        {
            Toast.makeText(getActivity(),"Enter name",Toast.LENGTH_SHORT).show();
            return;
        }
        if (getActivity()!=null)
{        ((UserSettings)getActivity()).changeUserProfile("name",text);
//        ((UserSettings)getActivity()).setUserName();

}        dismiss();

    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        if (getTag()!=null&& !getTag().equals("name")) {
            bottomSheetDialog.setOnShowListener(dia -> {
                BottomSheetDialog dialog = (BottomSheetDialog) dia;
                FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                    BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                    BottomSheetBehavior.from(bottomSheet).setHideable(false);
                    BottomSheetBehavior.from(bottomSheet).setDraggable(false);
                }
            });
        }
        return bottomSheetDialog;
    }
}
