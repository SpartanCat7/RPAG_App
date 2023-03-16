package edu.gradproject.rpagv3.Utils;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import edu.gradproject.rpagv3.Models.HelpNumber;
import edu.gradproject.rpagv3.Providers.CountryProvider;
import edu.gradproject.rpagv3.R;


public class AdminNumEmergencias {

    CountryProvider mCountryProvider;
    public ArrayList<HelpNumber> localNumbersList = new ArrayList<>();

    /*
    private int numAccident = 911;
    private int numIncendio = 911;
    private int numHerido = 911;
    private int numBloqueo = 911;
    private int numCongestionamiento = 911;
    private int numMarchas = 911;
    private int numCalleDanada = 911;
    private int numCorte = 911;
    */

    //private String country;

    public AdminNumEmergencias(String country) {
        //this.country = country;

        mCountryProvider = new CountryProvider();
        mCountryProvider.getCountryNumbers(country).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot number : queryDocumentSnapshots.getDocuments()) {
                    HelpNumber newHelpNumber = new HelpNumber();
                    newHelpNumber.setClassCode(number.getId());
                    newHelpNumber.setCountryCode(country);
                    newHelpNumber.setNumber((String) number.get("telfNumber"));

                    localNumbersList.add(newHelpNumber);
                }
            }
        });

        //configLocalNumbers();
    }

    /*
    public int getEmergencyNumber(int emergency_id) {
        int number = 911;
        switch (emergency_id) {
            case 1:
                number = numAccident;
                break;
            case 2:
                number = numIncendio;
                break;
            case 3:
                number = numHerido;
                break;
            case 4:
                number = numBloqueo;
                break;
            case 5:
                number = numCongestionamiento;
                break;
            case 6:
                number = numMarchas;
                break;
            case 7:
                number = numCalleDanada;
                break;
            case 8:
                number = numCorte;
                break;
        }

        return number;
    }
    */

    public HelpNumber getEmergencyNumber(String helpService) {
        for (HelpNumber helpNumber : localNumbersList) {
            if (helpService.equals(helpNumber.getClassCode())) {
                return helpNumber;
            }
        }
        return null;
    }

    /*
    void configLocalNumbers() {
        switch (country) {
            case "us":
                numCorte = R.integer.us_corte_electrico;
                break;
            case "bo":
                numAccident = R.integer.bo_accident;
                numCorte = R.integer.bo_corte_electrico;
                break;
        }
    }
    */

    public static String getSystemCountry(Context context) {
//        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//
//        String country = "us";
//
//        if (tm != null) {
//            country = tm.getSimCountryIso();
//        }
        //return country;
        return "bo";
    }

    void callEmergencyNumber(Context context, HelpNumber helpNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + helpNumber.getNumber()));
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Toast.makeText(context, context.getString(R.string.permisos_faltantes), Toast.LENGTH_SHORT).show();
            }
            else {
                context.startActivity(intent);
            }

        } catch (Exception e) {
            Log.e("RPAG-Log", "Error: " + e.getMessage());
        }
    }

    /**
     * Para eliminar mas tarde...
     */
    public void dialogEmergencyCall(final Context context, final String helpService) {
        HelpNumber helpNumber = getEmergencyNumber(helpService);

        if (helpNumber != null) {
            String helpServiceName = context.getResources().getString(helpNumber.getClassNameId());

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.llamada_emergencia);
            String message = context.getResources().getString(R.string.dialogo_llamar, helpServiceName, helpNumber.getNumber());
            builder.setMessage(message);

            DialogInterface.OnClickListener llamarEmergencias = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    callEmergencyNumber(context, helpNumber);
                }
            };
            DialogInterface.OnClickListener cancelarLlamadaEmergencias = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i("RPAG-Log", "Llamada Cancelada");
                }
            };

            builder.setPositiveButton(R.string.llamar, llamarEmergencias);
            builder.setNegativeButton(R.string.cancelar, cancelarLlamadaEmergencias);

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }



}
