package edu.integrator.rpagv2;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.integrator.rpagv2.R;

public class AlertMenuDialog extends DialogFragment {

    private AlertMenuDialogInterface alertMenuDialogInterface;

    private CheckBox chkIncluidePic;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.alerts_menu_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {


        chkIncluidePic = view.findViewById(R.id.chkIncluidePic);

        Button
                btnAlertAccidente,
                btnAlertIncendio,
                btnAlertHerido,
                btnAlertBloqueo,
                btnAlertCongestionamiento,
                btnAlertMarchas,
                btnAlertCalleDanada,
                btnAlertCorte;
        btnAlertAccidente = view.findViewById(R.id.btnAccidente);
        btnAlertIncendio = view.findViewById(R.id.btnIncendio);
        btnAlertHerido = view.findViewById(R.id.btnHerido);
        btnAlertBloqueo = view.findViewById(R.id.btnBloqueo);
        btnAlertCongestionamiento = view.findViewById(R.id.btnCongestionamiento);
        btnAlertMarchas = view.findViewById(R.id.btnMarchas);
        btnAlertCalleDanada = view.findViewById(R.id.btnCalleDanada);
        btnAlertCorte = view.findViewById(R.id.btnCorte);

        View.OnClickListener alertBtnListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                switch (id){
                    case R.id.btnAccidente:
                        alertMenuDialogInterface.onAlertClicked(MainActivity.ACCIDENTE_CLASS_ID, chkIncluidePic.isChecked());
                        break;
                    case R.id.btnIncendio:
                        alertMenuDialogInterface.onAlertClicked(MainActivity.INCENDIO_CLASS_ID, chkIncluidePic.isChecked());
                        break;
                    case R.id.btnHerido:
                        alertMenuDialogInterface.onAlertClicked(MainActivity.HERIDO_CLASS_ID, chkIncluidePic.isChecked());
                        break;
                    case R.id.btnBloqueo:
                        alertMenuDialogInterface.onAlertClicked(MainActivity.BLOQUEO_CLASS_ID, chkIncluidePic.isChecked());
                        break;
                    case R.id.btnCongestionamiento:
                        alertMenuDialogInterface.onAlertClicked(MainActivity.CONGESTIONAMIENTO_CLASS_ID, chkIncluidePic.isChecked());
                        break;
                    case R.id.btnMarchas:
                        alertMenuDialogInterface.onAlertClicked(MainActivity.MARCHAS_CLASS_ID, chkIncluidePic.isChecked());
                        break;
                    case R.id.btnCalleDanada:
                        alertMenuDialogInterface.onAlertClicked(MainActivity.CALLE_DANADA_CLASS_ID, chkIncluidePic.isChecked());
                        break;
                    case R.id.btnCorte:
                        alertMenuDialogInterface.onAlertClicked(MainActivity.CORTE_ELECTRICO_CLASS_ID, chkIncluidePic.isChecked());
                        break;
                }
            }

        };
        btnAlertAccidente.setOnClickListener(alertBtnListener);
        btnAlertIncendio.setOnClickListener(alertBtnListener);
        btnAlertHerido.setOnClickListener(alertBtnListener);
        btnAlertBloqueo.setOnClickListener(alertBtnListener);
        btnAlertCongestionamiento.setOnClickListener(alertBtnListener);
        btnAlertMarchas.setOnClickListener(alertBtnListener);
        btnAlertCalleDanada.setOnClickListener(alertBtnListener);
        btnAlertCorte.setOnClickListener(alertBtnListener);
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        alertMenuDialogInterface = (MainActivity) context;
    }

    public interface AlertMenuDialogInterface {
        void onAlertClicked(int class_id, boolean includePic);
    }
}
