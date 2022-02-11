package edu.integrator.rpagv2;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.integrator.rpagv2.R;

public class RegisterDialog extends DialogFragment {

    RegisterDialogInterface registerDialogInterface;

    EditText usernameField, emailField, passwordField;
    TextView usernameError, emailError, passwordError;
    Button btnRegister;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.register_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        usernameField =     view.findViewById(R.id.register_username_field);
        emailField =        view.findViewById(R.id.register_email_field);
        passwordField =     view.findViewById(R.id.register_password_field);
        usernameError =     view.findViewById(R.id.register_username_error);
        emailError =        view.findViewById(R.id.register_email_error);
        passwordError =     view.findViewById(R.id.register_password_error);
        btnRegister =       view.findViewById(R.id.btnRegister);

        usernameError.setVisibility(View.GONE);
        emailError.setVisibility(View.GONE);
        passwordError.setVisibility(View.GONE);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameField.getText().toString().trim();
                boolean usernamePass = false;
                if (username.length() >= 0) {
                    usernamePass = true;
                    emailError.setVisibility(View.GONE);
                } else {
                    usernameError.setVisibility(View.VISIBLE);
                }

                String email = emailField.getText().toString().trim();
                boolean emailPass = false;
                if (email.length() >= 0) {
                    emailPass = true;
                    emailError.setVisibility(View.GONE);
                } else {
                    usernameError.setVisibility(View.VISIBLE);
                }

                String password = passwordField.getText().toString().trim();
                boolean passwordPass = false;
                if (password.length() >= 0) {
                    passwordPass = true;
                    passwordError.setVisibility(View.GONE);
                } else {
                    usernameError.setVisibility(View.VISIBLE);
                }

                if (usernamePass && emailPass && passwordPass)
                    registerDialogInterface.registerButtonClicked(username, email, password, RegisterDialog.this);
            }
        });
    }

    void registerSuccessful() {
        Toast.makeText(getContext(), "New user registered, you can now sign in", Toast.LENGTH_LONG).show();
        this.dismiss();
    }

    void registerFailed() {
        Toast.makeText(getContext(), "Register unsuccessful, please try again", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        registerDialogInterface = (MainActivity) context;
    }

    public interface RegisterDialogInterface {
        void registerButtonClicked(String username, String email, String password, RegisterDialog dialog);
    }
}
