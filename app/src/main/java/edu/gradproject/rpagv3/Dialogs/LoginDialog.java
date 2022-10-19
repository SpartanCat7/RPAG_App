package edu.gradproject.rpagv3.Dialogs;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import edu.gradproject.rpagv3.R;


public class LoginDialog extends DialogFragment {

    private final LoginDialogInterface loginDialogInterface;

    private EditText phoneField, codeField;
    private TextInputLayout phoneEditTextLayout, codeEditTextLayout;
    private Button btnNextStep;

    private String phoneNumber;
    private String code;

    private String verificationId;

    public LoginDialog(LoginDialogInterface loginDialogInterface) {
        this.loginDialogInterface = loginDialogInterface;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        phoneField =    view.findViewById(R.id.login_phone_field);
        codeField =     view.findViewById(R.id.login_code_field);
        phoneEditTextLayout =   view.findViewById(R.id.login_phone_field_layout);
        codeEditTextLayout =    view.findViewById(R.id.login_code_field_layout);
        btnNextStep =          view.findViewById(R.id.btn_login_next_step);

        phoneField.setEnabled(true);
        codeField.setEnabled(false);
        codeField.setVisibility(View.GONE);
        btnNextStep.setText(R.string.send_code);

        phoneField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                phoneNumber = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        codeField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                code = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnNextStep.setOnClickListener(v -> {
            if (verificationId == null) {
                FragmentActivity activity = getActivity();
                PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }

                    @Override
                    public void onCodeSent(@NonNull String vId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        verificationId = vId;
                        phoneField.setEnabled(false);
                        codeField.setEnabled(true);
                        codeField.setVisibility(View.VISIBLE);
                        btnNextStep.setText(R.string.confirm);
                    }
                };
                if (activity != null) {
                    PhoneAuthProvider.verifyPhoneNumber(PhoneAuthOptions.newBuilder(loginDialogInterface.getAuth())
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(2L, TimeUnit.MINUTES)
                            .setActivity(activity)
                            .setCallbacks(mCallbacks)
                            .build());
                }
            } else {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                loginDialogInterface.loginWithPhoneAuthCredentials(credential, this);
            }
        });
    }



    public void loginSuccessful() {
        Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
        this.dismiss();
    }

    public void loginFailed() {
        Toast.makeText(getContext(), "Login attempt failed :(", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //loginDialogInterface = (LoginDialogInterface) context;
    }

    public interface LoginDialogInterface {
        void loginButtonClicked(String email, String password, LoginDialog dialog);
        FirebaseAuth getAuth();
        void loginWithPhoneAuthCredentials(PhoneAuthCredential credential, LoginDialog dialog);
    }
}
