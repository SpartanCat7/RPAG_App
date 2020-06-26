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

    EditText emailField, passwordField;
    TextView emailError, passwordError;
    Button btnRegister;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.register_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emailField =        view.findViewById(R.id.register_email_field);
        passwordField =     view.findViewById(R.id.register_password_field);
        emailError =        view.findViewById(R.id.register_email_error);
        passwordError =     view.findViewById(R.id.register_password_error);
        btnRegister =       view.findViewById(R.id.btnRegister);

        emailError.setVisibility(View.GONE);
        passwordError.setVisibility(View.GONE);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerDialogInterface.registerButtonClicked(
                        emailField.getText().toString(),
                        passwordField.getText().toString(),
                        RegisterDialog.this);
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
        void registerButtonClicked(String email, String password, RegisterDialog dialog);
    }
}
