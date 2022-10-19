package edu.gradproject.rpagv3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;

import edu.gradproject.rpagv3.Models.User;
import edu.gradproject.rpagv3.Providers.UserProvider;

public class ProfileSettingsActivity extends AppCompatActivity {

    public static final String NEW_USER_PROFILE_ACTION = "NEW_USER_PROFILE_ACTION";

    public static final String EXTRA_KEY_USER_ID = "EXTRA_KEY_USER_ID";

    private UserProvider userProvider;

    private User currentUser;

    private TextView txtViewUsernameCurrent, txtViewUsernameChange, txtViewPhoneCurrent, txtViewPhoneChange, txtViewFullnameCurrent, txtViewFullnameChange, txtViewEmailCurrent, txtViewEmailChange;
    private TextInputLayout txtEditLayoutUsername, txtEditLayoutPhone, txtEditLayoutFullname, txtEditLayoutEmail;
    private EditText txtEditUsername, txtEditPhone, txtEditFullname, txtEditEmail;
    private Button btnUsernameConfirm, btnUsernameCancel, btnPhoneConfirm, btnPhoneCancel, btnFullnameConfirm, btnFullnameCancel, btnEmailConfirm, btnEmailCancel;
    private ConstraintLayout layoutUsernameChange, layoutPhoneChange, layoutFullnameChange, layoutEmailChange;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        txtViewUsernameCurrent = findViewById(R.id.activity_settings_username_current);
        txtViewUsernameChange = findViewById(R.id.activity_settings_username_change);
        txtViewPhoneCurrent = findViewById(R.id.activity_settings_phone_current);
        txtViewPhoneChange = findViewById(R.id.activity_settings_phone_change);
        txtViewFullnameCurrent = findViewById(R.id.activity_settings_fullname_current);
        txtViewFullnameChange = findViewById(R.id.activity_settings_fullname_change);
        txtViewEmailCurrent = findViewById(R.id.activity_settings_email_current);
        txtViewEmailChange = findViewById(R.id.activity_settings_email_change);

        txtEditLayoutUsername = findViewById(R.id.activity_settings_username_field_layout);
        txtEditLayoutPhone = findViewById(R.id.activity_settings_phone_field_layout);
        txtEditLayoutFullname = findViewById(R.id.activity_settings_fullname_field_layout);
        txtEditLayoutEmail = findViewById(R.id.activity_settings_email_field_layout);

        txtEditUsername = findViewById(R.id.activity_settings_username_field);
        txtEditPhone = findViewById(R.id.activity_settings_phone_field);
        txtEditFullname = findViewById(R.id.activity_settings_fullname_field);
        txtEditEmail = findViewById(R.id.activity_settings_email_field);

        btnUsernameConfirm = findViewById(R.id.activity_settings_username_btn_change_confirm);
        btnUsernameCancel = findViewById(R.id.activity_settings_username_btn_change_cancel);
        btnPhoneConfirm = findViewById(R.id.activity_settings_phone_btn_change_confirm);
        btnPhoneCancel = findViewById(R.id.activity_settings_phone_btn_change_cancel);
        btnFullnameConfirm = findViewById(R.id.activity_settings_fullname_btn_change_confirm);
        btnFullnameCancel = findViewById(R.id.activity_settings_fullname_btn_change_cancel);
        btnEmailConfirm = findViewById(R.id.activity_settings_email_btn_change_confirm);
        btnEmailCancel = findViewById(R.id.activity_settings_email_btn_change_cancel);

        layoutUsernameChange = findViewById(R.id.activity_settings_username_change_layout);
        layoutPhoneChange = findViewById(R.id.activity_settings_phone_change_layout);
        layoutFullnameChange = findViewById(R.id.activity_settings_fullname_change_layout);
        layoutEmailChange = findViewById(R.id.activity_settings_email_change_layout);

        userProvider = new UserProvider();

        String userId = getIntent().getStringExtra(EXTRA_KEY_USER_ID);

        layoutUsernameChange.setVisibility(View.GONE);
        layoutPhoneChange.setVisibility(View.GONE);
        layoutFullnameChange.setVisibility(View.GONE);
        layoutEmailChange.setVisibility(View.GONE);

        txtViewUsernameChange.setOnClickListener(v -> layoutUsernameChange.setVisibility(View.VISIBLE));
        txtViewPhoneChange.setOnClickListener(v -> layoutPhoneChange.setVisibility(View.VISIBLE));
        txtViewFullnameChange.setOnClickListener(v -> layoutFullnameChange.setVisibility(View.VISIBLE));
        txtViewEmailChange.setOnClickListener(v -> layoutEmailChange.setVisibility(View.VISIBLE));

        btnUsernameCancel.setOnClickListener(v -> layoutUsernameChange.setVisibility(View.GONE));
        btnPhoneCancel.setOnClickListener(v -> layoutPhoneChange.setVisibility(View.GONE));
        btnFullnameCancel.setOnClickListener(v -> layoutFullnameChange.setVisibility(View.GONE));
        btnEmailCancel.setOnClickListener(v -> layoutEmailChange.setVisibility(View.GONE));

        btnUsernameConfirm.setOnClickListener(v -> {
            String newUsername = txtEditUsername.getText().toString();
            userProvider.updateUserUsername(currentUser.getUserId(), newUsername).addOnCompleteListener(updateUsernameTask -> {
                if (updateUsernameTask.isSuccessful()) {
                    Toast.makeText(this, "Username updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "ERROR: Could not update username", Toast.LENGTH_SHORT).show();
                }
            });
        });
        btnPhoneConfirm.setOnClickListener(v -> {
            String newPhone = txtEditPhone.getText().toString();
            userProvider.updateUserPhone(currentUser.getUserId(), newPhone).addOnCompleteListener(updateUsernameTask -> {
                if (updateUsernameTask.isSuccessful()) {
                    Toast.makeText(this, "Phone updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "ERROR: Could not update phone", Toast.LENGTH_SHORT).show();
                }
            });
        });
        btnFullnameConfirm.setOnClickListener(v -> {
            // ...
            // Add updating auth credentials
            // ...
            String newFullname = txtEditFullname.getText().toString();
            userProvider.updateUserFullname(currentUser.getUserId(), newFullname).addOnCompleteListener(updateUsernameTask -> {
                if (updateUsernameTask.isSuccessful()) {
                    Toast.makeText(this, "Full name updated: " + newFullname, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "ERROR: Could not update full name", Toast.LENGTH_SHORT).show();
                }
            });
        });
        btnEmailConfirm.setOnClickListener(v -> {
            String newEmail = txtEditEmail.getText().toString();
            userProvider.updateUserEmail(currentUser.getUserId(), newEmail).addOnCompleteListener(updateUsernameTask -> {
                if (updateUsernameTask.isSuccessful()) {
                    Toast.makeText(this, "Email updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "ERROR: Could not update email", Toast.LENGTH_SHORT).show();
                }
            });
        });

        userProvider.getUserById(userId).addOnCompleteListener(getUserTask -> {
            if (getUserTask.isSuccessful()) {
                currentUser = new User(getUserTask.getResult());
                if (currentUser.getUsername() != null) {
                    txtViewUsernameCurrent.setText(currentUser.getUsername());
                } else {
                    txtViewUsernameCurrent.setText("Not set yet");
                }

                if (currentUser.getTelfNumber() != null) {
                    txtViewPhoneCurrent.setText(currentUser.getTelfNumber());
                } else {
                    txtViewPhoneCurrent.setText("Not set yet");
                }

                if (currentUser.getFullName() != null) {
                    txtViewFullnameCurrent.setText(currentUser.getFullName());
                } else {
                    txtViewFullnameCurrent.setText("Not set yet");
                }

                if (currentUser.getEmail() != null) {
                    txtViewEmailCurrent.setText(currentUser.getEmail());
                } else {
                    txtViewEmailCurrent.setText("Not set yet");
                }

            } else {
                Toast.makeText(this, "ERROR: Could not recover profile", Toast.LENGTH_SHORT).show();
            }
        });

        /*switch (getIntent().getAction()) {
            case NEW_USER_PROFILE_ACTION: {
                // ...
                // prepare to take data of new user
                // ...
            }
        }*/
    }
}