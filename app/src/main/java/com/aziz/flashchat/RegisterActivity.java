package com.aziz.flashchat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class RegisterActivity extends AppCompatActivity {

    // Constants
    static final String CHAT_PREFS = "ChatPrefs";
    static final String DISPLAY_NAME_KEY = "username";
    static int PReqCode = 1;
    static int REQUESCODE = 1;
    String randomName;
    Uri pickedImgUri;
    ProgressBar mprogress;
    // TODO: Add member variables here:
    // UI references.
    private EditText mEmailView;
    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private CircleImageView regUserPhoto;
    private Button regBtn;
    // Firebase instance variables
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmailView = findViewById(R.id.register_email);
        mPasswordView = findViewById(R.id.register_password);
        mConfirmPasswordView = findViewById(R.id.register_confirm_password);
        mUsernameView = findViewById(R.id.register_username);
        regUserPhoto = findViewById(R.id.regUserPhoto);
        mprogress = findViewById(R.id.regProgressBar);
        regBtn = findViewById(R.id.register_sign_up_button);

        // Keyboard sign in action
//        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                if (id == R.id.register_form_finished || id == EditorInfo.IME_NULL) {
//                    attemptRegistration();
//                    return true;
//                }
//                return false;
//            }
//        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final String email = mEmailView.getText().toString();
                final String password = mPasswordView.getText().toString();
                final String password2 = mConfirmPasswordView.getText().toString();
                final String name = mUsernameView.getText().toString();


                if (name.isEmpty()) {
                    mUsernameView.setError(getString(R.string.check_mail));
                }
                else if (email.isEmpty()) {
                    mEmailView.setError(getString(R.string.check_mail));
                } else if (password.isEmpty()) {
                    mPasswordView.setError(getString(R.string.check_mail));
                } else if (!password.equals(password2)) {
                    mPasswordView.setError(getString(R.string.confirm_pass));
                } else {
                    regBtn.setVisibility(View.INVISIBLE);
                    mprogress.setVisibility(View.VISIBLE);

                    CreateUserAccount(email, name, password);
                }

            }
        });


        regUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= 22) {

                    checkAndRequestForPermission();


                } else {
                    openGallery();
                }


            }
        });
        // TODO: Get hold of an instance of FirebaseAuth
        mAuth = FirebaseAuth.getInstance();


    }

    private void CreateUserAccount(String email, final String name, String password) {


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // user account created successfully
//                            showMessage("Account created");
                            // after we created user account we need to update his profile picture and name
                            updateUserInfo(name, pickedImgUri, mAuth.getCurrentUser());


                        } else {

//                            showMessage("account creation failed" + task.getException().getMessage());
                            Toast.makeText(RegisterActivity.this, "account creation failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            regBtn.setVisibility(View.VISIBLE);
                            mprogress.setVisibility(View.INVISIBLE);

                        }
                    }
                });
    }

    private void updateUserInfo(final String name, Uri pickedImgUri, final FirebaseUser currentUser) {


        randomName = getRandomName();
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");
        final StorageReference imageFilePath = mStorage.child(randomName);

        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        // uri contain user image url


                        UserProfileChangeRequest profleUpdate = new UserProfileChangeRequest
                                .Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();


                        currentUser.updateProfile(profleUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            // user info updated successfully
//                                            showMessage("Register Complete");
                                            updateUI();
                                        }

                                    }
                                });

                    }
                });


            }
        });


    }

    private void updateUI() {
        Intent homeActivity = new Intent(getApplicationContext(),MainChatActivity.class);
        startActivity(homeActivity);
        finish();

    }

    private String getRandomName() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
        return simpleDateFormat.format(new Date());
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (result != null) {
                    pickedImgUri = result.getUri();

                    Glide.with(this)
                            .load(pickedImgUri)
                            .into(regUserPhoto);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = Objects.requireNonNull(result).getError();
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }


        }

//        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null ) {
//
//            // the user has successfully picked an image
//            // we need to save its reference to a Uri variable
//            pickedImgUri = data.getData() ;
//
//            Toast.makeText(this, ""+pickedImgUri, Toast.LENGTH_SHORT).show();
//
//            Glide.with(this)
//                    .load(pickedImgUri)
//                    .into(regUserPhoto);
//
//
//        }
    }

    private void checkAndRequestForPermission() {


        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(RegisterActivity.this, "Please accept for required permission", Toast.LENGTH_SHORT).show();

            } else {
                ActivityCompat.requestPermissions(RegisterActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode);
            }

        } else
            openGallery();

    }

    private void openGallery() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(RegisterActivity.this);

    }

    // Executed when Sign Up button is pressed.
    public void signUp(View v) {
        attemptRegistration();
    }

    private void attemptRegistration() {

        // Reset errors displayed in the form.

        mUsernameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String name = mUsernameView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.

        if (TextUtils.isEmpty(name)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            mUsernameView.requestFocus();
            ;

        }
        // Check for a valid email address.
        else if (TextUtils.isEmpty(email) || !isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            mEmailView.requestFocus();

        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            mPasswordView.requestFocus();

        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            mPasswordView.requestFocus();

        } else {
            // TODO: Call create FirebaseUser() here
            createFirebaseUser();
//            Toast.makeText(this, "Good !!!", Toast.LENGTH_SHORT).show();

        }
    }

    private boolean isEmailValid(String email) {
        // You can add more checking logic here.
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Add own logic to check for a valid password
        String confirmPassword = mConfirmPasswordView.getText().toString();
        return confirmPassword.equals(password) && password.length() > 4;
    }

    // TODO: Create a Firebase user
    private void createFirebaseUser() {

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();


        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("FlashChat", "createUser onComplete: " + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Log.d("FlashChat", "user creation failed");
                            Toast.makeText(RegisterActivity.this, "\"Registration attempt failed\"", Toast.LENGTH_SHORT).show();
                        } else {
                            saveDisplayName();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            finish();
                            startActivity(intent);
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showErrorDialog(e.getMessage());
            }
        });
    }


    // TODO: Save the display name to Shared Preferences
    private void saveDisplayName() {
        String displayName = mUsernameView.getText().toString();
        SharedPreferences prefs = getSharedPreferences(CHAT_PREFS, 0);
        prefs.edit().putString(DISPLAY_NAME_KEY, displayName).apply();
    }


    // TODO: Create an alert dialog to show in case registration failed
    private void showErrorDialog(String message) {

        new AlertDialog.Builder(this)
                .setTitle("Oops")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }


}
