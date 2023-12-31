package edu.northeastern.pawsomepals.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.utils.DialogHelper;
import edu.northeastern.pawsomepals.utils.ImageUtil;

public class EditUserProfileActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;

    private static final int PERMISSIONS_REQUEST_CAMERA = 3;
    private static final int PERMISSIONS_REQUEST_STORAGE = 4;
    private boolean shouldShowDatePicker = false;
    private final boolean datePickerOpened = false;
    private boolean isGenderSelected = false;

    private ImageView imageProfile;
    private EditText editTextName;
    private TextView textViewDOB;
    private RadioGroup radioGroupGender;
    private Button btnUpdate;
    private Uri photoUri;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference profileImageRef;
    private FirebaseUser currentUser;
    private String userId;
    private ProgressBar progressBar;
    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        // Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        profileImageRef = firebaseStorage.getReference().child("user_profile_images");

        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        imageProfile = findViewById(R.id.imageProfile);
        editTextName = findViewById(R.id.editTextName);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        textViewDOB = findViewById(R.id.textViewDOB);
        btnUpdate = findViewById(R.id.btnUpdate);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Edit Profile");
        }

        radioGroupGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                isGenderSelected = true; // Set the flag when the user selects a gender
            }
        });

        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageUtil.showPhotoSelectionDialog(EditUserProfileActivity.this);
            }
        });

        textViewDOB.setInputType(InputType.TYPE_NULL);
        textViewDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDataToFirebaseStorage();
            }
        });

        userInfo();
    }

    private void userInfo() {
        firebaseFirestore.collection("user")
                .document(userId)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Convert the data to your Dog model class
                                Users user = document.toObject(Users.class);
                                String profileImagePath = user.getProfileImage();

                                if (!Objects.isNull(profileImagePath)) {
                                    if (!profileImagePath.equals("") && !profileImagePath.equals("null")) {
                                        // Load the profile image using Glide
                                        Glide.with(EditUserProfileActivity.this)
                                                .load(profileImagePath)
                                                .into(imageProfile);
                                    } else {
                                        // If the profile image path is empty or null, you can use a placeholder image
                                        Glide.with(EditUserProfileActivity.this)
                                                .load(R.drawable.ud)
                                                .into(imageProfile);
                                    }
                                } else {
                                    // If the profile image path is empty or null, you can use a placeholder image
                                    Glide.with(EditUserProfileActivity.this)
                                            .load(R.drawable.ud)
                                            .into(imageProfile);
                                }

                                editTextName.setText(user.getName());
                                textViewDOB.setText(user.getDob());

                                int radioButtonId = (user.getGender() != null && user.getGender().equals("Male")) ? R.id.radioButtonMale : R.id.radioButtonFemale;
                                radioGroupGender.check(radioButtonId);

                                progressBar.setVisibility(View.GONE);
                            } else {
                                // Document does not exist
                            }
                        } else {
                            // Handle errors
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        showConfirmationDialog();
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to discard changes?");
        builder.setPositiveButton("Discard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ImageUtil.checkCameraPermission(EditUserProfileActivity.this)) {
                    ImageUtil.openCamera(EditUserProfileActivity.this);
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            } } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Storage permissions granted, proceed with image pick
            handleImagePickFromGallery();
        } else {
            // Storage permissions denied, show a message or handle accordingly
            Toast.makeText(this, "Storage permissions denied.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImagePickFromGallery() {
        // Check if storage permissions are granted
        if (checkStoragePermission()) {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_GALLERY);
        } else {
            // Request storage permissions if not granted
            requestStoragePermission();
        }
    }

    private void handleImageCaptureFromCamera() {
        // Check if camera permissions are granted
        if (checkCameraPermission()) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            // Request camera permissions if not granted
            requestCameraPermission();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_IMAGE_GALLERY);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_STORAGE);
        }
    }


    private void updateDataToFirebaseStorage() {
        String name = editTextName.getText().toString().trim();
        String gender = getSelectedGender();
        String dob = textViewDOB.getText().toString().trim();

        // Check if all the required fields are filled

        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Please add name.");
            Toast.makeText(this, "Please add name.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(gender)) {
            Toast.makeText(this, "Please select gender.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(dob)) {
            Toast.makeText(this, "Please select Date of Birth.", Toast.LENGTH_SHORT).show();
            return;
        }

        DialogHelper.showProgressDialog("Profile is being updated...", progressDialog, EditUserProfileActivity.this);

        if (photoUri != null) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageName = "user_image_" + timestamp + ".jpg";

            StorageReference photoRef = profileImageRef.child("user_images/" + imageName);
            UploadTask uploadTask = photoRef.putFile(photoUri);

            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return photoRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Get the download URL of the uploaded profile picture
                    Uri downloadUri = task.getResult();

                    // Create a map to store the user data
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("name", name);
                    userData.put("dob", dob);
                    userData.put("gender", gender);
                    userData.put("profileImage", downloadUri.toString());
                    userData.put("searchName", name.toLowerCase());

                    // Save the user data to Firebase Firestore
                    firebaseFirestore.collection("user")
                            .document(firebaseAuth.getUid())
                            .update(userData)
                            .addOnSuccessListener(aVoid -> {
                                DialogHelper.hideProgressDialog(progressDialog);
                                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                                navigateToProfileFragment();
                            })
                            .addOnFailureListener(e -> {
                                DialogHelper.hideProgressDialog(progressDialog);
                                Toast.makeText(this, "Failed to save profile. Please try again.", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    DialogHelper.hideProgressDialog(progressDialog);
                    Toast.makeText(this, "Failed to upload profile picture. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {// Create a map to store the user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("dob", dob);
            userData.put("gender", gender);
            userData.put("searchName", name.toLowerCase());

            // Save the user data to Firebase Firestore
            firebaseFirestore.collection("user")
                    .document(firebaseAuth.getUid())
                    .update(userData)
                    .addOnSuccessListener(aVoid -> {
                        DialogHelper.hideProgressDialog(progressDialog);
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                        finish();
                    })
                    .addOnFailureListener(e -> {
                        DialogHelper.hideProgressDialog(progressDialog);
                        Toast.makeText(this, "Failed to save profile. Please try again.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void navigateToProfileFragment() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("profileId", userId);
        setResult(RESULT_OK, resultIntent);

        SharedPreferences sharedPreferences = this.getSharedPreferences("ProfileId", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profileId", userId);
        editor.apply();

        finish();
    }

    private void openImageChooserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Profile Picture");
        builder.setItems(new String[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
            if (which == 0) {
                handleImageCaptureFromCamera();
            } else if (which == 1) {
                handleImagePickFromGallery();
            }
        });
        builder.show();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Handle the selected date
                        textViewDOB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, dayOfMonth);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private String getSelectedGender() {
        int selectedId = radioGroupGender.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectedId);
        if (radioButton != null) {
            return radioButton.getText().toString();
        }
        return "";
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            photoUri = savedInstanceState.getParcelable("photoUri");
            if (photoUri != null) {
                imageProfile.setImageURI(photoUri);
            }
            // Restore other values if needed
            String name = savedInstanceState.getString("name");
            String dob = savedInstanceState.getString("dob");
            String gender = savedInstanceState.getString("gender");
            shouldShowDatePicker = savedInstanceState.getBoolean("shouldShowDatePicker");
            isGenderSelected = savedInstanceState.getBoolean("isGenderSelected");

            editTextName.setText(name);
            textViewDOB.setText(dob);

            // Restore gender selection
            if (isGenderSelected) {
                int radioButtonId = (gender != null && gender.equals("Male")) ? R.id.radioButtonMale : R.id.radioButtonFemale;
                radioGroupGender.check(radioButtonId);
            }

            // Check if the DatePicker should be shown
            if (shouldShowDatePicker) {
                // Get the year, month, and day from the existing date value in editTextDOB
                String[] dateParts = dob.split("/");
                if (dateParts.length == 3) {
                    int day = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]) - 1; // Month is 0-indexed
                    int year = Integer.parseInt(dateParts[2]);
                    showDatePickerWithValues(year, month, day);
                }
            }

            // Restore the isGenderSelected flag
            isGenderSelected = savedInstanceState.getBoolean("isGenderSelected");
        }
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("photoUri", photoUri);

        String name = editTextName.getText().toString();
        String dob = textViewDOB.getText().toString();
        String gender = getSelectedGender();
        String datePickerValue = textViewDOB.getText().toString();

        outState.putString("name", name);
        outState.putString("dob", dob);
        outState.putString("gender", gender);
        outState.putString("datePickerValue", datePickerValue);
        outState.putBoolean("shouldShowDatePicker", textViewDOB.hasFocus() && !datePickerOpened);
        outState.putBoolean("isGenderSelected", isGenderSelected); // Save the isGenderSelected flag
    }

    private void showDatePickerWithValues(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Handle the selected date
                        textViewDOB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, dayOfMonth);

        //datePickerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                photoUri = ImageUtil.saveCameraImageToFile(data, this);

                Glide.with(this).load(photoUri).centerCrop().into(imageProfile);

            } else if (requestCode == REQUEST_IMAGE_GALLERY) {
                photoUri = data.getData();

                Glide.with(this).load(photoUri).centerCrop().into(imageProfile);

            }
        }
    }

    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "TempImage", null);
        return Uri.parse(path);
    }


}
