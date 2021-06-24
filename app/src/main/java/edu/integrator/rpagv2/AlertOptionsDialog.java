package edu.integrator.rpagv2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.integrator.rpagv2.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.integrator.rpagv2.Models.ImageData;
import edu.integrator.rpagv2.Models.UIAlert;
import edu.integrator.rpagv2.Models.Vote;
import edu.integrator.rpagv2.Providers.ImageProvider;
import edu.integrator.rpagv2.Providers.VoteProvider;

public class AlertOptionsDialog extends DialogFragment {

    private AlertOptionsDialogInterface alertOptionsDialogInterface;

    TextView txtTitle;
    ImageView imgAlertIcon;
    TextView txtConfirmations, txtReports, txtCoordinates;
    Button btnConfirm, btnReport;
    ImageView imgAlertPhoto;
    EditText editComment;
    Button btnComment;
    Button btnOpenComments;

    UIAlert alert;
    int confirmationsCount;
    int reportsCount;
    List<Vote> listVotes;
    Bitmap imageBitmap;
    ImageData imageData;

    VoteProvider mVoteProvider;
    ListenerRegistration voteListenerRegistration;
    ImageProvider mImageProvider;

    public AlertOptionsDialog(UIAlert alert) {
        this.alert = alert;
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
        imgAlertPhoto = view.findViewById(R.id.imgAlertPhoto);
        editComment = view.findViewById(R.id.editAlertOptionsCommentField);
        btnComment = view.findViewById(R.id.btnAlertOptionsCommentButton);
        btnOpenComments = view.findViewById(R.id.btnAlertOptionsOpenCommentsButton);

        mVoteProvider = new VoteProvider();
        voteListenerRegistration = mVoteProvider.getVotesByAlert(alert.id).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                listVotes = value.toObjects(Vote.class);

                confirmationsCount = 0;
                reportsCount = 0;

                for (Vote vote : listVotes) {
                    if (vote.isVoteTrue()) {
                        confirmationsCount += 1;
                    } else {
                        reportsCount += 1;
                    }
                }

                txtConfirmations.setText(String.valueOf(confirmationsCount));
                txtReports.setText(String.valueOf(reportsCount));
            }
        });

        txtTitle.setText(alert.alertClass.name_string_ID);

        imgAlertIcon.setImageDrawable(getContext().getDrawable(alert.alertClass.icon));

        txtCoordinates.setText(alert.lat + " / " + alert.len);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vote newVote = new Vote(null, alert.id, alertOptionsDialogInterface.getCurrentUserId(), new Date(), true);

                mVoteProvider.create(newVote).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(MainActivity.LOG_TAG, "Positive vote registered");
                    }
                });
            }
        });
        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vote newVote = new Vote(null, alert.id, alertOptionsDialogInterface.getCurrentUserId(), new Date(), false);

                mVoteProvider.create(newVote).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(MainActivity.LOG_TAG, "Negative vote registered");
                    }
                });
            }
        });

        imgAlertPhoto.setVisibility(View.GONE);
        mImageProvider = new ImageProvider();
        mImageProvider.getImagesByAlert(alert.id).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.size() > 0) {
                    Log.d(MainActivity.LOG_TAG, "Image found for alert");

                    imageData = queryDocumentSnapshots.toObjects(ImageData.class).get(0);
                    loadImage(imageData);
                } else {
                    imgAlertPhoto.setVisibility(View.GONE);
                }
            }
        });

        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editComment.getText().toString().length() > 0) {
                    alertOptionsDialogInterface.sendAlertComment(alert, editComment.getText().toString());
                } else {
                    Toast.makeText(getContext(), getText(R.string.comment_missing), Toast.LENGTH_LONG).show();
                }
            }
        });
        btnOpenComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertOptionsDialogInterface.openComments(alert);
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        alertOptionsDialogInterface = (MainActivity) context;
    }

    private void loadImage(final ImageData mImage) {
        mImageProvider.getFile(mImage.getFileName()).addOnCompleteListener(new OnCompleteListener<byte[]>() {
            @Override
            public void onComplete(@NonNull Task<byte[]> task) {
                if (task.isSuccessful()) {
                    Log.d(MainActivity.LOG_TAG, "File recovered (" + mImage.getFileName() + ")");
                    Bitmap bitmapImage = BitmapFactory.decodeByteArray(task.getResult(), 0, task.getResult().length);
                    imgAlertPhoto.setImageBitmap(bitmapImage);
                    imgAlertPhoto.setVisibility(View.VISIBLE);
                } else {
                    Log.d(MainActivity.LOG_TAG, "File not recovered (" + mImage.getFileName() + ")");
                }
            }
        });
    }

    public interface AlertOptionsDialogInterface {
        void sendAlertComment(UIAlert alert, String comentario);
        void openComments(UIAlert alert);
        String getCurrentUserId();
    }

    @Override
    public void onStop() {

        super.onStop();
    }
}
