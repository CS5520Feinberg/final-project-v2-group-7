package edu.northeastern.pawsomepals.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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
import com.google.android.gms.tasks.OnFailureListener;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Dogs;
import edu.northeastern.pawsomepals.network.BaseUiThreadCallback;
import edu.northeastern.pawsomepals.network.PawsomePalWebService;
import edu.northeastern.pawsomepals.utils.DialogHelper;
import edu.northeastern.pawsomepals.utils.ImageUtil;

public class EditDogUserActivity extends AppCompatActivity {


    private Spinner spinnerDogBreed;
    private Spinner spinnerMixedBreed;
    private CheckBox checkBoxMixedBreed;
    private EditText editTextDogName;
    private TextView textViewDOB;
    private RadioGroup radioGroupDogGender;
    private RadioGroup radioGroupDogSize;
    private RadioButton radioButtonSmall;
    private RadioButton radioButtonMedium;
    private RadioButton radioButtonLarge;
    private boolean isMixedBreedChecked = false;
    private ProgressBar progressBar;
    private Calendar calendar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference profileImageRef;
    private FirebaseUser currentUser;
    private String userId;
    private boolean isGenderSelected = false;
    private boolean isDogSizeSelected = false;
    private ImageView imageProfile;
    private Button btnUpdate;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static final int PERMISSIONS_REQUEST_CAMERA = 3;
    private static final int PERMISSIONS_REQUEST_STORAGE = 4;
    private Uri photoUri;
    private String dogDocId;
    private PawsomePalWebService pawsomePalWebService;
    private ArrayAdapter<String> breedAdapter;
    private ArrayAdapter<String> mixedBreedAdapter;
    private String dogId;
    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_dog_profile);

        Intent intent = getIntent();
        dogId = intent.getStringExtra("dogId");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

            actionBar.setTitle("Edit Pal's Profile");
        }

        spinnerDogBreed = findViewById(R.id.spinnerDogBreed);
        spinnerMixedBreed = findViewById(R.id.spinnerMixedBreed);
        checkBoxMixedBreed = findViewById(R.id.checkBoxMixedBreed);
        editTextDogName = findViewById(R.id.editTextName);
        textViewDOB = findViewById(R.id.textViewDOB);
        radioGroupDogGender = findViewById(R.id.radioGroupGender);
        radioGroupDogSize = findViewById(R.id.radioGroupSize);
        radioButtonSmall = findViewById(R.id.radioButtonSmall);
        radioButtonMedium = findViewById(R.id.radioButtonMedium);
        radioButtonLarge = findViewById(R.id.radioButtonLarge);
        progressBar = findViewById(R.id.progressBar);
        calendar = Calendar.getInstance();
        imageProfile = findViewById(R.id.imageProfile);
        btnUpdate = findViewById(R.id.btnUpdate);

        profileImageRef = firebaseStorage.getReference().child("dog_profile_images");

        progressBar.setVisibility(View.GONE);

        checkBoxMixedBreed.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isMixedBreedChecked = isChecked;
            if (isChecked) {
                spinnerMixedBreed.setVisibility(View.VISIBLE);
            } else {
                spinnerMixedBreed.setVisibility(View.GONE);
            }
        });

        radioGroupDogGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                isGenderSelected = true;
            }
        });

        radioGroupDogSize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                isDogSizeSelected = true;
            }
        });


        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageUtil.showPhotoSelectionDialog(EditDogUserActivity.this);
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

                UpdateDataToFirebaseStorage();
            }
        });

        breedAdapter = new ArrayAdapter<>(this, R.layout.spinner_list, new ArrayList<>());
        mixedBreedAdapter = new ArrayAdapter<>(this, R.layout.spinner_list, new ArrayList<>());

        breedAdapter.insert("Dog's Breed", 0);
        mixedBreedAdapter.insert("Dog's Mixed Breed", 0);

        spinnerDogBreed.setAdapter(breedAdapter);
        spinnerMixedBreed.setAdapter(mixedBreedAdapter);

        PawsomePalWebService.UiThreadCallback uiThreadCallback = new BaseUiThreadCallback() {

            public void onGetBreedsName(List<String> breeds) {
                progressBar.setVisibility(View.GONE);

                breedAdapter.clear();
                mixedBreedAdapter.clear();

                breedAdapter.insert("Dog's Breed", 0);
                mixedBreedAdapter.insert("Dog's Mixed Breed", 0);

                breedAdapter.addAll(breeds);
                mixedBreedAdapter.addAll(breeds);

                breedAdapter.notifyDataSetChanged();
                mixedBreedAdapter.notifyDataSetChanged();

                getDogInfo();
            }

            @Override
            public void onError() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditDogUserActivity.this, "Error while fetching breeds.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEmptyResult() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditDogUserActivity.this, "Error while fetching breeds.", Toast.LENGTH_SHORT).show();
            }

        };

        pawsomePalWebService = new PawsomePalWebService(uiThreadCallback);
        pawsomePalWebService.getBreedsNames();

        progressBar.setVisibility(View.VISIBLE);


    }

    private void getDogInfo() {

        firebaseFirestore.collection("dogs")
                .document(dogId)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Convert the data to your Dog model class
                                Dogs dog = document.toObject(Dogs.class);

                                String profileImagePath = dog.getProfileImage();

                                if (!Objects.isNull(profileImagePath)) {
                                    if (!profileImagePath.equals("") && !profileImagePath.equals("null")) {
                                        // Load the profile image using Glide
                                        Glide.with(EditDogUserActivity.this)
                                                .load(profileImagePath)
                                                .into(imageProfile);
                                    } else {
                                        // If the profile image path is empty or null, you can use a placeholder image
                                        Glide.with(EditDogUserActivity.this)
                                                .load(R.drawable.ud)
                                                .into(imageProfile);
                                    }
                                } else {
                                    // If the profile image path is empty or null, you can use a placeholder image
                                    Glide.with(EditDogUserActivity.this)
                                            .load(R.drawable.ud)
                                            .into(imageProfile);
                                }

                                editTextDogName.setText(dog.getName());

                                textViewDOB.setText(dog.getDob());


                                int radioButtonId = (dog.getGender() != null && dog.getGender().equals("Male")) ? R.id.radioButtonMale : R.id.radioButtonFemale;
                                radioGroupDogGender.check(radioButtonId);


                                int radioButtonSizeId = 0;
                                if (dog.getSize() != null) {
                                    switch (dog.getSize()) {
                                        case "Small" -> radioButtonSizeId = R.id.radioButtonSmall;
                                        case "Medium" -> radioButtonSizeId = R.id.radioButtonMedium;
                                        case "Large" -> radioButtonSizeId = R.id.radioButtonLarge;
                                    }
                                }
                                radioGroupDogSize.check(radioButtonSizeId);


                                int position = breedAdapter.getPosition(dog.getBreed());
                                if (position != -1) {
                                    spinnerDogBreed.setSelection(position);
                                }

                                if (dog.getIsMixedBreed()) {
                                    checkBoxMixedBreed.setChecked(true);
                                    int mixedBreedPosition = mixedBreedAdapter.getPosition(dog.getMixedBreed());
                                    if (mixedBreedPosition != -1) {
                                        spinnerMixedBreed.setSelection(mixedBreedPosition);
                                    }
                                }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ImageUtil.checkCameraPermission(EditDogUserActivity.this)) {
                    ImageUtil.openCamera(EditDogUserActivity.this);
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

    private String getSelectedRadioGroupValue(RadioGroup radioGroupObject) {
        int selectedId = radioGroupObject.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectedId);
        if (radioButton != null) {
            return radioButton.getText().toString();
        }
        return "";
    }


    private void UpdateDataToFirebaseStorage() {
        String dogName = editTextDogName.getText().toString();
        String dogBreed = spinnerDogBreed.getSelectedItem().toString();
        String dogMixedBreed = isMixedBreedChecked ? spinnerMixedBreed.getSelectedItem().toString() : "Not mixed";
        String dogGender = getSelectedRadioGroupValue(radioGroupDogGender);
        String dogSize = getSelectedRadioGroupValue(radioGroupDogSize);
        String dob = textViewDOB.getText().toString();

        // Check if all the required fields are filled
        if (TextUtils.isEmpty(dogName)) {
            editTextDogName.setError("Please add dog's name.");
            Toast.makeText(this, "Please add dog's name.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(dogGender)) {
            Toast.makeText(this, "Please select gender.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(dob)) {

            Toast.makeText(this, "Please select Date of Birth/Adoption.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(dogSize)) {
            Toast.makeText(this, "Please select dog's size.", Toast.LENGTH_SHORT).show();
            return;
        }


        // Check if the selected breed is not the default text
        if (dogBreed.equals("Dog's Breed")) {
            Toast.makeText(this, "Please select a valid dog breed.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the mixed breed is checked and if the selected mixed breed is not the default text
        if (isMixedBreedChecked && dogMixedBreed.equals("Dog's Mixed Breed")) {
            Toast.makeText(this, "Please select a valid mixed breed.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dogBreed.equals(dogMixedBreed)) {
            Toast.makeText(this, "Please select a different mixed breed.", Toast.LENGTH_SHORT).show();
            return;
        }

        DialogHelper.showProgressDialog("Profile is being saved...", progressDialog, EditDogUserActivity.this);

        if (photoUri != null) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageName = "dog_image_" + timestamp + ".jpg";

            StorageReference photoRef = profileImageRef.child("dog_images/" + imageName);
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

                    Map<String, Object> dogData = new HashMap<>();
                    dogData.put("userId", userId);
                    dogData.put("name", dogName);
                    dogData.put("breed", dogBreed);
                    dogData.put("isMixedBreed", isMixedBreedChecked);
                    dogData.put("mixedBreed", dogMixedBreed);
                    dogData.put("profileImage", downloadUri.toString());
                    dogData.put("gender", dogGender);
                    dogData.put("dob", dob);
                    dogData.put("size", dogSize);

                    // Use .document() instead of .add() to set a custom document ID
                    firebaseFirestore.collection("dogs")
                            .document(dogId)
                            .update(dogData) // Use .set() to create or update the document
                            .addOnSuccessListener(aVoid -> {

                                DialogHelper.hideProgressDialog(progressDialog);
                                Toast.makeText(EditDogUserActivity.this, "Pal's profile updated successfully!", Toast.LENGTH_SHORT).show();

                                finish();

                            })
                            .addOnFailureListener(e -> {
                                DialogHelper.hideProgressDialog(progressDialog);
                                Toast.makeText(EditDogUserActivity.this, "Failed to update Pal's profile. Please try again.", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    DialogHelper.hideProgressDialog(progressDialog);
                    Toast.makeText(this, "Failed to upload profile picture. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    DialogHelper.hideProgressDialog(progressDialog);
                    Toast.makeText(EditDogUserActivity.this, "Failed to upload profile picture. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Map<String, Object> dogData = new HashMap<>();
            dogData.put("userId", userId);
            dogData.put("name", dogName);
            dogData.put("breed", dogBreed);
            dogData.put("isMixedBreed", isMixedBreedChecked);
            dogData.put("mixedBreed", dogMixedBreed);
            dogData.put("gender", dogGender);
            dogData.put("dob", dob);
            dogData.put("size", dogSize);

            // Use .document() instead of .add() to set a custom document ID
            firebaseFirestore.collection("dogs")
                    .document(dogId)
                    .update(dogData) // Use .set() to create or update the document
                    .addOnSuccessListener(aVoid -> {
                        DialogHelper.hideProgressDialog(progressDialog);
                        Toast.makeText(EditDogUserActivity.this, "Pal's profile updated successfully!", Toast.LENGTH_SHORT).show();

                        finish();

                    })
                    .addOnFailureListener(e -> {
                        DialogHelper.hideProgressDialog(progressDialog);
                        Toast.makeText(EditDogUserActivity.this, "Failed to update Pal's profile. Please try again.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set the maximum date to the current date to prevent selecting future dates
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            // Update the calendar instance with the selected date
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Format the selected date and set it to the editTextDOB field
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            textViewDOB.setText(sdf.format(calendar.getTime()));
        }
    };

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("photoUri", photoUri);

        String dogName = editTextDogName.getText().toString();
        String dogBreed = spinnerDogBreed.getSelectedItem().toString();
        String dogMixedBreed = isMixedBreedChecked ? spinnerMixedBreed.getSelectedItem().toString() : "Not mixed";
        String dogGender = getSelectedRadioGroupValue(radioGroupDogGender);
        String dogSize = getSelectedRadioGroupValue(radioGroupDogSize);
        String dob = textViewDOB.getText().toString();


        outState.putString("name", dogName);
        outState.putString("dob", dob);
        outState.putString("gender", dogGender);
        outState.putString("size", dogSize);
        outState.putBoolean("isDogSizeSelected", isDogSizeSelected);
        outState.putBoolean("isGenderSelected", isGenderSelected);
        outState.putBoolean("isMixedBreedChecked", isMixedBreedChecked);
        outState.putString("dogBreed", dogBreed);
        outState.putString("dogMixedBreed", dogMixedBreed);
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
            String size = savedInstanceState.getString("size");
            String gender = savedInstanceState.getString("gender");
            String dogBreed = savedInstanceState.getString("dogBreed");
            String dogMixedBreed = savedInstanceState.getString("dogMixedBreed");
            isDogSizeSelected = savedInstanceState.getBoolean("isDogSizeSelected");
            isGenderSelected = savedInstanceState.getBoolean("isGenderSelected");
            isMixedBreedChecked = savedInstanceState.getBoolean("isMixedBreedChecked");

            editTextDogName.setText(name);
            textViewDOB.setText(dob);

            // Restore gender selection
            if (isGenderSelected) {
                int radioButtonId = (gender != null && gender.equals("Male")) ? R.id.radioButtonMale : R.id.radioButtonFemale;
                radioGroupDogGender.check(radioButtonId);
            }

            if (isDogSizeSelected) {
                int radioButtonId = 0;
                if (size != null) {
                    switch (size) {
                        case "Small" -> radioButtonId = R.id.radioButtonSmall;
                        case "Medium" -> radioButtonId = R.id.radioButtonMedium;
                        case "Large" -> radioButtonId = R.id.radioButtonLarge;
                    }
                }
                radioGroupDogSize.check(radioButtonId);
            }

            if (isMixedBreedChecked) {

            }

        }
    }
}