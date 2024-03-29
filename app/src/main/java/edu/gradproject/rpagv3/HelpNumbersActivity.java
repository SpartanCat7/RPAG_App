package edu.gradproject.rpagv3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import edu.gradproject.rpagv3.Models.HelpNumber;
import edu.gradproject.rpagv3.Providers.CountryProvider;
import edu.gradproject.rpagv3.Utils.LocaleManager;

public class HelpNumbersActivity extends AppCompatActivity {

    CountryProvider mCountryProvider;

    TextView txtCountry;
    RecyclerView recyclerHelpNumbers;

    ArrayList<HelpNumber> localNumbersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_numbers);

        setTitle(R.string.local_help_numbers);

        mCountryProvider = new CountryProvider();
        txtCountry = findViewById(R.id.txtCountryName);
        recyclerHelpNumbers = findViewById(R.id.recyclerHelpNumbers);

        recyclerHelpNumbers.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        final String currentCountry = "bo";
        // Temporal. Buscar forma de obtener pais actual de forma dinamica.

        mCountryProvider.getCountryNumbers(currentCountry).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot number : queryDocumentSnapshots.getDocuments()) {
                    HelpNumber newHelpNumber = new HelpNumber();
                    newHelpNumber.setClassCode(number.getId());
                    newHelpNumber.setCountryCode(currentCountry);
                    newHelpNumber.setNumber((String) number.get("telephone"));
                    newHelpNumber.setImageUrl((String) number.get("imageUrl"));
                    newHelpNumber.setName((String) number.get("name"));

                    localNumbersList.add(newHelpNumber);
                }

                recyclerHelpNumbers.setAdapter(new HelpNumberAdapter(localNumbersList));
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtHelpNumberLabel;
        private final TextView txtHelpNumber;
        private final ImageView imgHelpServiceIcon;

        public ViewHolder(View view) {
            super(view);

            txtHelpNumberLabel = view.findViewById(R.id.txtHelpNumberLabel);
            txtHelpNumber = view.findViewById(R.id.txtHelpNumber);
            imgHelpServiceIcon = view.findViewById(R.id.imgHelpNumberIcon);
        }

        public TextView getTxtHelpNumberLabel() {
            return txtHelpNumberLabel;
        }

        public TextView getTxtHelpNumber() {
            return txtHelpNumber;
        }

        public ImageView getImgHelpServiceIcon() {
            return imgHelpServiceIcon;
        }
    }

    public class HelpNumberAdapter extends RecyclerView.Adapter<HelpNumbersActivity.ViewHolder> {

        List<HelpNumber> helpNumberList;

        public HelpNumberAdapter(List<HelpNumber> helpNumberList) {
            this.helpNumberList = helpNumberList;
        }

        @NonNull
        @Override
        public HelpNumbersActivity.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.viewholder_help_number_item, parent, false);

            return new HelpNumbersActivity.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HelpNumbersActivity.ViewHolder holder, int position) {
            //Integer classNameId = helpNumberList.get(position).getClassNameId();
            //String number = helpNumberList.get(position).getNumber();
//            Drawable classIcon = ContextCompat.getDrawable(getApplicationContext(), helpNumberList.get(position).getClassIconId());

//            holder.getTxtHelpNumberLabel().setText(helpNumberList.get(position).getClassNameId());
            holder.getTxtHelpNumberLabel().setText(helpNumberList.get(position).getName());
            holder.getTxtHelpNumber().setText(helpNumberList.get(position).getNumber());
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    InputStream in = new URL(helpNumberList.get(position).getImageUrl()).openStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    runOnUiThread(() -> holder.getImgHelpServiceIcon().setImageBitmap(bitmap));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
//            if (classIcon != null) holder.getImgHelpServiceIcon().setImageDrawable(classIcon);
        }

        @Override
        public int getItemCount() {
            return helpNumberList.size();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setContextLocale(newBase));
    }
}