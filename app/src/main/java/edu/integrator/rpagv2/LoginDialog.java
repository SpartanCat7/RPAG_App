package edu.integrator.rpagv2;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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


public class LoginDialog extends DialogFragment {

    private LoginDialogInterface loginDialogInterface;

    private EditText emailField, passwordField;
    private TextView emailError, passwordError;
    private Button btnLogin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emailField =        view.findViewById(R.id.login_email_field);
        passwordField =     view.findViewById(R.id.login_password_field);
        emailError =        view.findViewById(R.id.login_email_error);
        passwordError =     view.findViewById(R.id.login_password_error);
        btnLogin =          view.findViewById(R.id.btnLogin);

        emailError.setVisibility(View.GONE);
        passwordError.setVisibility(View.GONE);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginDialogInterface.loginButtonClicked(
                        emailField.getText().toString(),
                        passwordField.getText().toString(),
                        LoginDialog.this);

            }
        });
    }

    void loginSuccessful() {
        Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
        this.dismiss();
    }

    void loginFailed() {
        Toast.makeText(getContext(), "Login attempt failed :(", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        loginDialogInterface = (MainActivity) context;
    }

    public interface LoginDialogInterface {
        void loginButtonClicked(String email, String password, LoginDialog dialog);
    }
}
