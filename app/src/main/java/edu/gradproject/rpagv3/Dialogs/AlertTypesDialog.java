package edu.gradproject.rpagv3.Dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import edu.gradproject.rpagv3.Models.AlertType;
import edu.gradproject.rpagv3.R;

public class AlertTypesDialog extends DialogFragment {
    RecyclerView recyclerView;
    Button btnCancel;

    AlertTypeAdapter alertTypeAdapter;
    AlertTypesDialogInterface mInterface;

    public AlertTypesDialog(AlertTypesDialogInterface alertTypesDialogInterface) {
        this.mInterface = alertTypesDialogInterface;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_alert_type_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.alertTypesRecyclerView);
        btnCancel = view.findViewById(R.id.btnCancelAlertTypeDialog);

        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        alertTypeAdapter = new AlertTypeAdapter(mInterface);
        recyclerView.setAdapter(alertTypeAdapter);
    }

    private static class AlertTypeViewHolder extends RecyclerView.ViewHolder {
        private final Button btnSelectAlertType;
        private final ImageView imgAlertTypeIcon;
        private String typeId;

        public AlertTypeViewHolder(@NonNull View itemView) {
            super(itemView);

            btnSelectAlertType = itemView.findViewById(R.id.btnSelectAlertType);
            imgAlertTypeIcon = itemView.findViewById(R.id.imgAlertTypeIcon);
        }

        public Button getBtnSelectAlertType() {
            return btnSelectAlertType;
        }

        public ImageView getImgAlertTypeIcon() {
            return imgAlertTypeIcon;
        }

        public String getTypeId() {
            return typeId;
        }

        public void setTypeId(String typeId) {
            this.typeId = typeId;
        }
    }

    private static class AlertTypeAdapter extends RecyclerView.Adapter<AlertTypeViewHolder> {

        AlertTypesDialogInterface mInterface;

        public AlertTypeAdapter(AlertTypesDialogInterface mInterface) {
            this.mInterface = mInterface;
        }

        @NonNull
        @Override
        public AlertTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_alert_type_button, parent, false);
            return new AlertTypeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AlertTypeViewHolder holder, int position) {
            Button btn = holder.getBtnSelectAlertType();
            ImageView img = holder.getImgAlertTypeIcon();
            holder.setTypeId(mInterface.getAlertTypes().get(holder.getBindingAdapterPosition()).getId());

            btn.setText(mInterface.getAlertTypes().get(position).getName());
            btn.setOnClickListener(view -> {
                Toast.makeText(view.getContext(), "Selected: " + mInterface.getAlertTypes().get(holder.getBindingAdapterPosition()).getName(), Toast.LENGTH_SHORT).show();
                mInterface.onAlertTypeClicked(holder.getTypeId());
            });
            if (mInterface.getAlertTypes().get(position).getIconBitmap() != null) {
                img.setImageBitmap(mInterface.getAlertTypes().get(position).getIconBitmap());
            }
        }

        @Override
        public int getItemCount() {
            return mInterface.getAlertTypes().size();
        }
    }

    public interface AlertTypesDialogInterface {
        void onAlertTypeClicked(String typeId);
        ArrayList<AlertType> getAlertTypes();
    }
}
