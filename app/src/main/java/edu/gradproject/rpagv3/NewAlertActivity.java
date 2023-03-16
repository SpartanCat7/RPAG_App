package edu.gradproject.rpagv3;

import static edu.gradproject.rpagv3.MainActivity.LOG_TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.gradproject.rpagv3.Dialogs.AlertTypesDialog;
import edu.gradproject.rpagv3.Models.AlertData;
import edu.gradproject.rpagv3.Models.AlertType;
import edu.gradproject.rpagv3.Models.ImageData;
import edu.gradproject.rpagv3.Providers.AlertProvider;
import edu.gradproject.rpagv3.Providers.AlertTypeProvider;
import edu.gradproject.rpagv3.Utils.CONSTS;

public class NewAlertActivity extends AppCompatActivity {

    private ImageView imgAlertTypeIcon, imgAlertPhoto;
    private TextView lblAlertTypeName;
    private EditText txtDescriptionInput;
    private Button btnSendAlert, btnChangeAlertType, btnAddImage, btnRemoveImage;
    private ConstraintLayout layoutPhotoPreview;

    private static final int CAMERA_REQUEST = 1888;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private static final String CHANGE_TYPE_DIALOG_TAG = "CHANGE_TYPE_DIALOG_TAG";
    public static final String ALERT_TYPE_ID_EXTRA = "ALERT_TYPE_ID_EXTRA";
    public static final String DEVICE_LAT_EXTRA = "DEVICE_LAT_EXTRA", DEVICE_LNG_EXTRA = "DEVICE_LNG_EXTRA", DEVICE_ADDRESS_EXTRA = "DEVICE_ADDRESS_EXTRA";
    public static final String NEW_ALERT_JSON_DATA_KEY = "NEW_ALERT_JSON_DATA_KEY";
    private static final String NEW_IMAGE_FILE_PATH_KEY = "NEW_IMAGE_FILE_PATH_KEY";

    private FirebaseAuth mAuth;

    //private ImageProvider mImageProvider;
    private AlertProvider mAlertProvider;
    private AlertTypeProvider mAlertTypeProvider;

    private ArrayList<AlertType> alertTypeList = new ArrayList<>();
    private String alertTypeId;
    private static final String ALERT_TYPE_ID_SAVED_KEY = "ALERT_TYPE_ID_SAVED_KEY";
    private double userLat, userLng;
    private String deviceAddress;
    private static final String LAT_SAVED_KEY = "LAT_SAVED_KEY";
    private static final String LNG_SAVED_KEY = "LNG_SAVED_KEY";
    private ArrayList<ImageData> imageDataList = new ArrayList<>();
    private String description;
    private static final String DESCRIPTION_SAVED_KEY = "DESCRIPTION_SAVED_KEY";

    private boolean alertTypeListFullyLoaded = false;
    private boolean pendingAlertTypesDialog = false;
    //private String pendingAlertTypeToRestoreId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_alert);

        alertTypeId = getIntent().getExtras().getString(ALERT_TYPE_ID_EXTRA);
        userLat = getIntent().getDoubleExtra(DEVICE_LAT_EXTRA, 0);
        userLng = getIntent().getDoubleExtra(DEVICE_LNG_EXTRA, 0);
        deviceAddress = getIntent().getStringExtra(DEVICE_ADDRESS_EXTRA);

        mAuth = FirebaseAuth.getInstance();

        //mImageProvider = new ImageProvider();
        mAlertProvider = new AlertProvider();
        mAlertTypeProvider = new AlertTypeProvider();

        imgAlertTypeIcon = findViewById(R.id.img_newAlert_TypeIcon);
        imgAlertPhoto = findViewById(R.id.img_NewAlert_Photo);
        lblAlertTypeName = findViewById(R.id.label_newAlert_TypeName);
        txtDescriptionInput = findViewById(R.id.txt_newAlert_DescriptionInput);
        btnSendAlert = findViewById(R.id.btn_newAlert_SendButton);
        btnChangeAlertType = findViewById(R.id.btn_NewAlert_ChangeType);
        btnAddImage = findViewById(R.id.btn_NewAlert_AddImage);
        btnRemoveImage = findViewById(R.id.btn_NewAlert_RemoveImage);

        layoutPhotoPreview = findViewById(R.id.layout_NewAlert_PhotoViewLayout);
        layoutPhotoPreview.setVisibility(View.GONE);
        btnAddImage.setVisibility(View.VISIBLE);

        txtDescriptionInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                description = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnSendAlert.setOnClickListener(view -> {
            SendNewAlert();
        });

        btnRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageDataList.size() > 0) {
                    imageDataList.remove(imageDataList.get(0));
                    layoutPhotoPreview.setVisibility(View.GONE);
                    btnAddImage.setVisibility(View.VISIBLE);
                }
            }
        });

        btnChangeAlertType.setOnClickListener(view -> {
            if (alertTypeListFullyLoaded) {
                showChangeAlertTypeDialog();
            } else {
                pendingAlertTypesDialog = true;
            }
        });

        btnAddImage.setOnClickListener(view -> {
            takeImage();
        });

        mAlertTypeProvider.getActiveAlertTypes().addOnSuccessListener(queryDocumentSnapshots -> {
            ArrayList<AlertType> newAlertTypeList = new ArrayList<>();
            alertTypeListFullyLoaded = false;
            for (DocumentSnapshot snap : queryDocumentSnapshots.getDocuments()) {
                AlertType type = new AlertType(snap);
                mAlertTypeProvider.getTypeIconFile(this, type.getIcon(), new AlertTypeProvider.getTypeIconFileCallback() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        type.setIconBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                    }

                    @Override
                    public void onFailure() {
                        Log.e(LOG_TAG, "COULD NOT GET AN ICON FILE FOR TYPE: " + type.getName() + " (" + type.getId() + ")");
                        type.setIconBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.custom_alert_icon_orange));
                    }

                    @Override
                    public void Finally() {
                        newAlertTypeList.add(type);
                        AlertTypeProvider.updateAlertTypeList(type, alertTypeList); // alertTypeList is updated until it is time to replace it entirely

                        if (newAlertTypeList.size() == queryDocumentSnapshots.getDocuments().size()) { // check if newAlertTypeList is ready to become the new alertTypeList
                            alertTypeList = newAlertTypeList;
                            alertTypeListFullyLoaded = true;
                            if (pendingAlertTypesDialog) {
                                showChangeAlertTypeDialog();
                                pendingAlertTypesDialog = false;
                            }
                            AlertType type = AlertTypeProvider.getAlertType(alertTypeId, alertTypeList);
                            assert type != null;
                            lblAlertTypeName.setText(type.getName());
                            imgAlertTypeIcon.setImageBitmap(type.getIconBitmap());
                        }
                    }
                });
            }
        });
    }

    AlertData newAlertDataFromForm() {
        if (mAuth.getCurrentUser() != null) {
            return new AlertData(null, mAuth.getCurrentUser().getUid(), alertTypeId, userLat, userLng, deviceAddress, new Date(), description, null, false);
        } else {
            return new AlertData(null, null, alertTypeId, userLat, userLng, deviceAddress, new Date(), description, null, false);
        }
    }

    void showChangeAlertTypeDialog() {
        AlertTypesDialog alertTypesDialog = new AlertTypesDialog(new AlertTypesDialog.AlertTypesDialogInterface() {
            @Override
            public void onAlertTypeClicked(String typeId) {
                alertTypeId = typeId;
                AlertType type = AlertTypeProvider.getAlertType(alertTypeId, alertTypeList);
                lblAlertTypeName.setText(type.getName());
                imgAlertTypeIcon.setImageBitmap(type.getIconBitmap());
            }

            @Override
            public ArrayList<AlertType> getAlertTypes() {
                return alertTypeList;
            }
        });
        alertTypesDialog.show(getSupportFragmentManager(), CHANGE_TYPE_DIALOG_TAG);
    }

    void SendNewAlert() {
        if (mAuth.getCurrentUser() != null) {
            //String userId = mAuth.getCurrentUser().getUid();
            DocumentReference newDoc = mAlertProvider.getNewDocument();
            AlertData alertData = newAlertDataFromForm();

            mAlertProvider.create(alertData, newDoc).addOnCompleteListener(createAlertTask -> {
                if (createAlertTask.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), getText(R.string.alert_sent), Toast.LENGTH_SHORT).show();
                    List<String> imagesList = new ArrayList<>();
                    for (ImageData imageData : imageDataList) {
                        mAlertProvider.uploadFileToStorage(Uri.fromFile(imageData.getFile())).addOnCompleteListener(uploadTask -> {
                            if (uploadTask.isSuccessful()) {
                                imagesList.add(uploadTask.getResult().getMetadata().getName());
                                if (imagesList.size() == imageDataList.size()) {
                                    AlertProvider.setImagesOnAlertDocument(newDoc, imagesList).addOnCompleteListener(arraySetTask -> {
                                        if (arraySetTask.isSuccessful()) {
                                            Log.i(LOG_TAG, "Images array correctly set on alert document: " + newDoc.getId());
                                        } else {
                                            Log.e(LOG_TAG, "Images array could not be set in alert document: " + newDoc.getId());
                                        }
                                    });
                                }
                            } else {
                                Log.e(LOG_TAG, "Image could not be uploaded: " + imageData.getFileName());
                            }
                        });
                    }
                    NewAlertActivity.this.finish();
                } else {
                    Toast.makeText(getApplicationContext(), getText(R.string.alert_not_sent), Toast.LENGTH_SHORT).show();
                }
            });

            /*if (alertClass.help_service != null) {
                adminNumEmergencias.dialogEmergencyCall(this, alertClass.help_service);
            }*/
        }
    }

    void saveNewAlertData(AlertData data) {
        try {
            String jsonAlertData = AlertData.toJsonString(data);
            getPreferences(Context.MODE_PRIVATE).edit().putString(NEW_ALERT_JSON_DATA_KEY, jsonAlertData).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    AlertData readNewAlertDataFromPreferences() {
        String jsonData = getPreferences(Context.MODE_PRIVATE).getString(NEW_ALERT_JSON_DATA_KEY, null);
        if (jsonData != null) {
            return AlertData.fromJsonString(jsonData);
        }
        return null;
    }

    void takeImage() {
        saveNewAlertData(newAlertDataFromForm());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //String TEMP_IMAGE_PATH = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/OriginalImages";
        File tempImageDirectory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), CONSTS.USER_TEMP_PHOTOS_DIRECTORY_NAME);
        String tempImageName = "Original_" + new Date().getTime();
        String tempImageSuffix = ".jpg";

        if (tempImageDirectory.mkdirs()) {
            Log.d(LOG_TAG, "Temporary images directory created");
        }

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null && tempImageDirectory.exists()) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = File.createTempFile(
                        tempImageName,
                        tempImageSuffix,
                        tempImageDirectory
                );
            } catch (IOException ex) {
                ex.fillInStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "edu.gradproject.rpagv3.fileprovider",
                        photoFile);

                // Save a file: path for use with ACTION_VIEW intents
                getPreferences(Context.MODE_PRIVATE).edit().putString(NEW_IMAGE_FILE_PATH_KEY, photoFile.getAbsolutePath()).apply();

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    static File SaveBitmapAsJPEG(Bitmap bitmap, int quality, File directory, String filename){

        File file = new File(directory, filename);

        if(directory.mkdirs()){
            Log.d(LOG_TAG, "Nuevo directorio creado");
        }

        if (directory.exists()) {
            Log.d(LOG_TAG, "Directorio existente, creando archivo");
            try {
                if(file.createNewFile()){
                    Log.d(LOG_TAG, "Nuevo archivo creado");
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, new FileOutputStream(file));
                } else {
                    Log.d(LOG_TAG, "Nuevo archivo no pudo ser creado");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return file;
        } else {
            Log.d(LOG_TAG, "Directorio aun no existente, retornando null");
            return null;
        }
    }

    private Bitmap RescaleBitmap(Bitmap bitmap, int biggestDimension) {
        float scale;
        if (bitmap.getHeight() > bitmap.getWidth()) {
            scale = (float) biggestDimension / (float) bitmap.getHeight();
        } else {
            scale = (float) biggestDimension / (float) bitmap.getWidth();
        }
        return Bitmap.createScaledBitmap(bitmap, Math.round(bitmap.getWidth() * scale), Math.round(bitmap.getHeight() * scale), true);
    }

    private Bitmap RotateBitmap90Degrees(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        //matrix.postScale(scaleWidth, scaleHeight);
        matrix.postRotate(90);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

            String tempFilePath = sharedPref.getString(NEW_IMAGE_FILE_PATH_KEY, null);
            //final String alertId = sharedPref.getString("NewPhotoAlertId", null);
            AlertData alertData = readNewAlertDataFromPreferences();
            txtDescriptionInput.setText(alertData.getDescription());
            imgAlertTypeIcon.setImageBitmap(AlertTypeProvider.getAlertType(alertData.getTypeId(), alertTypeList).getIconBitmap());

//            SharedPreferences.Editor editor = sharedPref.edit();
//            editor.remove("NewPhotoFilePath");
//            editor.remove("NewPhotoAlertId");
//            editor.apply();

            Bitmap savedBitmap = BitmapFactory.decodeFile(tempFilePath);
            Bitmap scaledBitmap = RescaleBitmap(savedBitmap, 1280);
            Bitmap rotatedBitmap = RotateBitmap90Degrees(scaledBitmap);

            imgAlertPhoto.setImageBitmap(rotatedBitmap);

            final String imageName = alertData.getId() + "_" + new Date().getTime() + ".jpg";
            final File savedImageFile = SaveBitmapAsJPEG(rotatedBitmap, 80, getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageName);

            if (savedImageFile != null) {
                imageDataList.add(new ImageData(savedImageFile.getName(), new Date(), rotatedBitmap, savedImageFile));
                layoutPhotoPreview.setVisibility(View.VISIBLE);
                btnAddImage.setVisibility(View.GONE);
            } else {
                Toast.makeText(this, "Image could not be saved", Toast.LENGTH_SHORT).show();
            }

            /*File tempImage = new File(tempFilePath);
            if (tempImage.delete()) {
                Log.d(LOG_TAG, "Temporary image file deleted");
            }*/

            /*if (savedImageFile != null) {
                mImageProvider.uploadStorageFile(Uri.fromFile(savedImageFile)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ImageData imageData = new ImageData(null, savedImageFile.getName(), alertId, mAuth.getCurrentUser().getUid(), new Date(), null);
                        mImageProvider.create(imageData).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Successful Image Upload", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Image Registration Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Image Upload Failed", Toast.LENGTH_SHORT).show();
                        Log.e(LOG_TAG, "Image Upload Failed");
                        e.fillInStackTrace();
                    }
                });
            } else {
                Toast.makeText(this, "Image could not be saved", Toast.LENGTH_SHORT).show();
            }*/
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(ALERT_TYPE_ID_SAVED_KEY, alertTypeId);
        outState.putString(DESCRIPTION_SAVED_KEY, description);
        outState.putDouble(LAT_SAVED_KEY, userLat);
        outState.putDouble(LNG_SAVED_KEY, userLng);

        super.onSaveInstanceState(outState);
    }
}