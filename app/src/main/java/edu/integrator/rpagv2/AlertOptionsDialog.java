package edu.integrator.rpagv2;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.integrator.rpagv2.R;

public class AlertOptionsDialog extends DialogFragment {

    private AlertOptionsDialogInterface alertOptionsDialogInterface;

    TextView txtTitle;
    ImageView imgAlertIcon;
    TextView txtConfirmations, txtReports, txtCoordinates;
    Button btnConfirm, btnReport;
    EditText editComment;
    Button btnComment;
    Button btnOpenComments;

    Alerta alerta;
    int confirmationsCount;
    int reportsCount;
    Bitmap imagen;

    public AlertOptionsDialog(Alerta alerta, int confirmationsCount, int reportsCount, Bitmap imagen) {
        this.alerta = alerta;
        this.confirmationsCount = confirmationsCount;
        this.reportsCount = reportsCount;
        this.imagen = imagen;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.alerts_options_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        txtTitle = view.findViewById(R.id.txtAlertOptionsTitle);
        imgAlertIcon = view.findViewById(R.id.imgAlertOptionsIcon);
        txtConfirmations = view.findViewById(R.id.txtAlertOptionsConfirmationsData);
        txtReports = view.findViewById(R.id.txtAlertOptionsReportsData);
        txtCoordinates = view.findViewById(R.id.txtAlertOptionsCoordinatesData);
        btnConfirm = view.findViewById(R.id.btnAlertOptionsVoteConfirm);
        btnReport = view.findViewById(R.id.btnAlertOptionsVoteReport);
        editComment = view.findViewById(R.id.editAlertOptionsCommentField);
        btnComment = view.findViewById(R.id.btnAlertOptionsCommentButton);
        btnOpenComments = view.findViewById(R.id.btnAlertOptionsOpenCommentsButton);

        txtTitle.setText(alerta.claseAlerta.name_string_ID);
        imgAlertIcon.setImageBitmap(imagen);
        txtConfirmations.setText(String.valueOf(confirmationsCount));
        txtReports.setText(String.valueOf(reportsCount));
        txtCoordinates.setText(alerta.lat + " / " + alerta.len);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertOptionsDialogInterface.sendAlertConfirmation(alerta);
                txtConfirmations.setText(String.valueOf(confirmationsCount + 1));
            }
        });
        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertOptionsDialogInterface.sendAlertReport(alerta);
                txtReports.setText(String.valueOf(reportsCount + 1));
            }
        });
        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editComment.getText().toString().length() > 0) {
                    alertOptionsDialogInterface.sendAlertComment(alerta, editComment.getText().toString());
                }
            }
        });
        btnOpenComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertOptionsDialogInterface.openComments(alerta);
            }
        });
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        alertOptionsDialogInterface = (MainActivity) context;
    }

    public interface AlertOptionsDialogInterface {
        void sendAlertConfirmation(Alerta alerta);
        void sendAlertReport(Alerta alerta);
        void sendAlertComment(Alerta alerta, String comentario);
        void openComments(Alerta alerta);
    }
}
