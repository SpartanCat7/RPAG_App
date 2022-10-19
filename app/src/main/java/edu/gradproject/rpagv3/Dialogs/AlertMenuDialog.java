package edu.gradproject.rpagv3.Dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import edu.gradproject.rpagv3.R;

public class AlertMenuDialog extends DialogFragment {

//    private AlertMenuDialogInterface alertMenuDialogInterface;
//
//    private CheckBox chkIncluidePic;
//
//    EditText editCustomAlertName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_alert_menu_old, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        chkIncluidePic = view.findViewById(R.id.chkIncluidePic);

//        Button
//                btnAlertAccidente,
//                btnAlertIncendio,
//                btnAlertHerido,
//                btnAlertBloqueo,
//                btnAlertCongestionamiento,
//                btnAlertMarchas,
//                btnAlertCalleDanada,
//                btnAlertCorte;
//
//        LinearLayout btnCustomAlert = view.findViewById(R.id.btnCustomAlert);
//        editCustomAlertName = view.findViewById(R.id.editTxtCustomAlert);

//        btnAlertAccidente = view.findViewById(R.id.btnAccidente);
//        btnAlertIncendio = view.findViewById(R.id.btnIncendio);
//        btnAlertHerido = view.findViewById(R.id.btnHerido);
//        btnAlertBloqueo = view.findViewById(R.id.btnBloqueo);
//        btnAlertCongestionamiento = view.findViewById(R.id.btnCongestionamiento);
//        btnAlertMarchas = view.findViewById(R.id.btnMarchas);
//        btnAlertCalleDanada = view.findViewById(R.id.btnCalleDanada);
//        btnAlertCorte = view.findViewById(R.id.btnCorte);

//        btnAlertAccidente.setOnClickListener(v -> {
//            alertMenuDialogInterface.onAlertClicked(MainActivity.ACCIDENTE_CLASS_ID, chkIncluidePic.isChecked());
//        });
//        btnAlertIncendio.setOnClickListener(v -> {
//            alertMenuDialogInterface.onAlertClicked(MainActivity.INCENDIO_CLASS_ID, chkIncluidePic.isChecked());
//        });
//        btnAlertHerido.setOnClickListener(v -> {
//            alertMenuDialogInterface.onAlertClicked(MainActivity.HERIDO_CLASS_ID, chkIncluidePic.isChecked());
//        });
//        btnAlertBloqueo.setOnClickListener(v -> {
//            alertMenuDialogInterface.onAlertClicked(MainActivity.BLOQUEO_CLASS_ID, chkIncluidePic.isChecked());
//        });
//        btnAlertCongestionamiento.setOnClickListener(v -> {
//            alertMenuDialogInterface.onAlertClicked(MainActivity.CONGESTIONAMIENTO_CLASS_ID, chkIncluidePic.isChecked());
//        });
//        btnAlertMarchas.setOnClickListener(v -> {
//            alertMenuDialogInterface.onAlertClicked(MainActivity.MARCHAS_CLASS_ID, chkIncluidePic.isChecked());
//        });
//        btnAlertCalleDanada.setOnClickListener(v -> {
//            alertMenuDialogInterface.onAlertClicked(MainActivity.CALLE_DANADA_CLASS_ID, chkIncluidePic.isChecked());
//        });
//        btnAlertCorte.setOnClickListener(v -> {
//            alertMenuDialogInterface.onAlertClicked(MainActivity.CORTE_ELECTRICO_CLASS_ID, chkIncluidePic.isChecked());
//        });
//        btnCustomAlert.setOnClickListener(v -> {
//            if (editCustomAlertName.getText().length() > 0)
//                alertMenuDialogInterface.onCustomAlertClicked(editCustomAlertName.getText().toString(), chkIncluidePic.isChecked());
//            else {
//                Toast.makeText(getContext(), getText(R.string.custom_alert_empty_error), Toast.LENGTH_SHORT).show();
//            }
//        });
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

//    public interface AlertMenuDialogInterface {
//        void onAlertClicked(int class_id, boolean includePic);
//        void onCustomAlertClicked(String customName, boolean includePic);
//    }
}
