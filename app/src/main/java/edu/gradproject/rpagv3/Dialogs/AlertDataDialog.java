package edu.gradproject.rpagv3.Dialogs;

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
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ListenerRegistration;
import com.mapbox.geojson.Point;
import com.mapbox.search.MapboxSearchSdk;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.ReverseGeoOptions;
import com.mapbox.search.ReverseGeocodingSearchEngine;
import com.mapbox.search.SearchCallback;
import com.mapbox.search.SearchRequestTask;
import com.mapbox.search.result.SearchResult;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.gradproject.rpagv3.MainActivity;
import edu.gradproject.rpagv3.Models.AlertType;
import edu.gradproject.rpagv3.Models.Comment;
import edu.gradproject.rpagv3.Models.ImageData;
import edu.gradproject.rpagv3.Models.AlertSymbolBundle;
import edu.gradproject.rpagv3.Models.Vote;
import edu.gradproject.rpagv3.Providers.AlertProvider;
import edu.gradproject.rpagv3.Providers.CommentProvider;
import edu.gradproject.rpagv3.Providers.VoteProvider;
import edu.gradproject.rpagv3.R;
import mobi.gspd.segmentedbarview.Segment;
import mobi.gspd.segmentedbarview.SegmentedBarView;
import mobi.gspd.segmentedbarview.SegmentedBarViewSideStyle;

public class AlertDataDialog extends DialogFragment {

    private AlertDataDialogInterface alertDataDialogInterface;

    TextView txtTitle;
    ImageView imgAlertIcon;
    TextView txtConfirmations, txtReports, txtAddress, txtDate, txtDescription;
    Button btnConfirm, btnReport;
    ImageView imgAlertPhoto;
    EditText editComment;
    Button btnComment;
    Button btnOpenComments;

    SegmentedBarView barView;

    AlertType alertType;
    AlertSymbolBundle alert;
    DocumentReference alertRef;
    int confirmationsCount;
    int reportsCount;
    private Vote voteCastedByUser;
    List<Vote> listVotes;
    //Bitmap imageBitmap;
    ImageData imageData;
    ArrayList<ImageData> imagesList = new ArrayList<>();

    //VoteProvider mVoteProvider;
    ListenerRegistration voteListenerRegistration;
    AlertProvider mAlertProvider;

    private ReverseGeocodingSearchEngine reverseGeocoding;
    private SearchRequestTask searchRequestTask;

//    public AlertOptionsDialog(UIAlert alert) {
//        this.alert = alert;
//    }

    public AlertDataDialog(AlertDataDialogInterface alertDataDialogInterface, AlertSymbolBundle alert, AlertType alertType) {
        this.alertDataDialogInterface = alertDataDialogInterface;
        this.alert = alert;
        this.alertType = alertType;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_alert_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        txtTitle = view.findViewById(R.id.txtAlertOptionsTitle);
        imgAlertIcon = view.findViewById(R.id.imgAlertOptionsIcon);
        txtConfirmations = view.findViewById(R.id.txtAlertOptionsConfirmationsData);
        txtReports = view.findViewById(R.id.txtAlertOptionsReportsData);
        //txtCoordinates = view.findViewById(R.id.txtAlertOptionsCoordinatesData);
        txtAddress = view.findViewById(R.id.txtAlertOptionsAddressData);
        txtDate = view.findViewById(R.id.txtAlertOptionsDateData);
        txtDescription = view.findViewById(R.id.txtAlertOptionsDescriptionData);
        btnConfirm = view.findViewById(R.id.btnAlertOptionsVoteConfirm);
        btnReport = view.findViewById(R.id.btnAlertOptionsVoteReport);
        imgAlertPhoto = view.findViewById(R.id.imgAlertPhoto);
        editComment = view.findViewById(R.id.editAlertOptionsCommentField);
        btnComment = view.findViewById(R.id.btnAlertOptionsCommentButton);
        btnOpenComments = view.findViewById(R.id.btnAlertOptionsOpenCommentsButton);
        barView = view.findViewById(R.id.alertOptionsTrueFalseBar);

        barView.setSideStyle(SegmentedBarViewSideStyle.ROUNDED);
        /*
        barView = SegmentedBarView.builder(getContext())
                .segments(segments)
                .value(3f)
                .unit("ml<sup>2</sup>")
                .showDescriptionText(true)
                .sideStyle(SegmentedBarViewSideStyle.ROUNDED)
                .build();*/

        mAlertProvider = new AlertProvider();
        alertRef = mAlertProvider.getAlert(alert.getAlertData().getId());
        if (alert.getAlertData().getImages() != null) {
            for (String imageFileName : alert.getAlertData().getImages()) {
                loadImage(imageFileName);
            }
        }
        //mVoteProvider = new VoteProvider();
        voteListenerRegistration = VoteProvider.getVotes(alertRef).addSnapshotListener((value, error) -> {
            if (value != null) {
                listVotes = VoteProvider.DocSnapListToVoteArrayList(value.getDocuments());
                confirmationsCount = 0;
                reportsCount = 0;

                voteCastedByUser = null;

                for (Vote vote : listVotes) {
                    if (vote.isActive()) {
                        if (vote.isVoteTrue()) {
                            confirmationsCount += 1;
                        } else {
                            reportsCount += 1;
                        }
                    }
                    if (vote.getUserId().equals(alertDataDialogInterface.getCurrentUserId())) {
                        voteCastedByUser = vote;
                    }
                }

                updateVoteButtonStates();
                //updateVoteButtonStatesNoLimits();
                updateSegmentGraph();

                txtConfirmations.setText(String.valueOf(confirmationsCount));
                txtReports.setText(String.valueOf(reportsCount));
            }
        });

        txtTitle.setText(alertType.getName());

        imgAlertIcon.setImageBitmap(alertType.getIconBitmap());

        //txtCoordinates.setText(alert.lat + " / " + alert.len);

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        txtDate.setText(dateFormat.format(alert.getAlertData().getDate()));

        txtDescription.setText(alert.getAlertData().getDescription() != null ? alert.getAlertData().getDescription() : "");

        btnConfirm.setOnClickListener(newVote);
        btnReport.setOnClickListener(newVote);

        imgAlertPhoto.setVisibility(View.GONE);

        btnComment.setOnClickListener(v -> {
            if (alertDataDialogInterface.getCurrentUserId() != null) {
                if(editComment.getText().toString().length() > 0) {
                    //alertDataDialogInterface.sendAlertComment(alert, editComment.getText().toString(), AlertDataDialog.this);
                    CommentProvider.addComment(
                            new Comment(null, null, alertDataDialogInterface.getCurrentUserId(), new Date(), editComment.getText().toString(), true),
                            alertRef)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getActivity(), getText(R.string.comment_successful), Toast.LENGTH_LONG).show();
                                    clearCommentTextBox();
                                } else {
                                    Toast.makeText(getActivity(), getText(R.string.comment_error), Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    Toast.makeText(getActivity(), getText(R.string.comment_missing), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getActivity(), "Debe ingresar a su cuenta para enviar comentarios", Toast.LENGTH_LONG).show();
            }
        });

        if (alertDataDialogInterface.getCurrentUserId() == null) {
            editComment.setEnabled(false);
            btnComment.setEnabled(false);
            btnConfirm.setEnabled(false);
            btnReport.setEnabled(false);
        }

        btnOpenComments.setOnClickListener(v -> alertDataDialogInterface.openComments(alert));

        reverseGeocoding = MapboxSearchSdk.createReverseGeocodingSearchEngine();
        ReverseGeoOptions options = new ReverseGeoOptions.Builder(Point.fromLngLat(alert.getAlertData().getLongitude(), alert.getAlertData().getLatitude()))
                .limit(1)
                .build();

        searchRequestTask = reverseGeocoding.search(options, searchCallback);
    }

    private void updateSegmentGraph() {
        List<Segment> segments = new ArrayList<>();

        float trueSeg = confirmationsCount;
        float falseSeg = reportsCount;

        while (trueSeg + falseSeg > 10) {
            trueSeg = trueSeg / 2;
            falseSeg = falseSeg / 2;
        }

        for (int i = 0; i < trueSeg; i++) {
            segments.add(new Segment("", "", ContextCompat.getColor(getActivity(), R.color.colorGreen)));
        }
        for (int i = 0; i < falseSeg; i++) {
            segments.add(new Segment("", "", ContextCompat.getColor(getActivity(), R.color.colorRed)));
        }
        //if (!segments.isEmpty())
        //    barView.setSegments(segments);
        barView.setSegments(segments);
    }

    private void updateVoteButtonStates() {
        if (voteCastedByUser == null) {
            btnConfirm.setOnClickListener(newVote);
            btnConfirm.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.confirm_button, null));
            btnReport.setOnClickListener(newVote);
            btnReport.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.report_button, null));
        } else {
            if (voteCastedByUser.isActive()) {
                if (voteCastedByUser.isVoteTrue()) {
                    btnConfirm.setOnClickListener(setVoteInactive);
                    btnConfirm.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.confirm_button_highlight, null));
                    btnReport.setOnClickListener(setVoteActive);
                    btnReport.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.report_button, null));
                } else {
                    btnConfirm.setOnClickListener(setVoteActive);
                    btnConfirm.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.confirm_button, null));
                    btnReport.setOnClickListener(setVoteInactive);
                    btnReport.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.report_button_highlight, null));
                }
            } else {
                btnConfirm.setOnClickListener(setVoteActive);
                btnConfirm.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.confirm_button, null));
                btnReport.setOnClickListener(setVoteActive);
                btnReport.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.report_button, null));
            }
        }
    }

    /**
     * Only for adding votes without limits
     */

//    private void updateVoteButtonStatesNoLimits() {
//        btnConfirm.setOnClickListener(newPositiveVote);
//        btnConfirm.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.confirm_button, null));
//        btnReport.setOnClickListener(newNegativeVote);
//        btnReport.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.report_button, null));
//    }

    View.OnClickListener newVote = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Vote newVote = null;
            if (v.getId() == R.id.btnAlertOptionsVoteConfirm) {
                newVote = new Vote(alertDataDialogInterface.getCurrentUserId(), alert.getAlertData().getId(), alertDataDialogInterface.getCurrentUserId(), new Date(), true, true);
            } else if (v.getId() == R.id.btnAlertOptionsVoteReport) {
                newVote = new Vote(alertDataDialogInterface.getCurrentUserId(), alert.getAlertData().getId(), alertDataDialogInterface.getCurrentUserId(), new Date(), false, true);
            }
            if (newVote != null) {
                VoteProvider.addVote(newVote, alertRef).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        try {
                            Toast.makeText(getActivity(), getString(R.string.positive_vote_entered), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e(MainActivity.LOG_TAG, "New vote could not be created in DB");
                    }
                });
            }
        }
    };

    View.OnClickListener setVoteActive = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean isTrue = v.getId() == R.id.btnAlertOptionsVoteConfirm;
            VoteProvider.updateVote(alertRef, voteCastedByUser.getId(), isTrue, true).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    try {
                        Toast.makeText(getActivity(), getString(R.string.positive_vote_entered), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(MainActivity.LOG_TAG, "Vote could not be updated in DB");
                }
            });
        }
    };

    View.OnClickListener setVoteInactive = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean isTrue = v.getId() == R.id.btnAlertOptionsVoteConfirm;
            VoteProvider.updateVote(alertRef, voteCastedByUser.getId(), isTrue, false).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    try {
                        Toast.makeText(getActivity(), getString(R.string.vote_retired), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(MainActivity.LOG_TAG, "Vote could not be updated in DB");
                }
            });
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    private void loadImage(String imageFileName) {
        mAlertProvider.getImageFile(imageFileName).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Bitmap imageBitmap = BitmapFactory.decodeByteArray(task.getResult(), 0, task.getResult().length);
                imagesList.add(new ImageData(imageFileName, new Date(), imageBitmap, null));
                imgAlertPhoto.setImageBitmap(imageBitmap);
                imgAlertPhoto.setVisibility(View.VISIBLE);
            } else {
                Log.e(MainActivity.LOG_TAG, "Could not download image: " + imageFileName);
            }
        });
    }

    public void clearCommentTextBox() {
        editComment.setText("");
        editComment.clearFocus();
    }

    private final SearchCallback searchCallback = new SearchCallback() {
        @Override
        public void onResults(@NotNull List<? extends SearchResult> list, @NotNull ResponseInfo responseInfo) {
            if (list.isEmpty()) {
                Log.i(MainActivity.LOG_TAG, "No reverse geocoding results");
            } else {
                //Log.i(MainActivity.LOG_TAG, "First Address: " + list.get(0).getAddress().formattedAddress());
                //Log.i(MainActivity.LOG_TAG, "Reverse geocoding results: " + list);
                txtAddress.setText(list.get(0).getAddress().formattedAddress());
            }
        }

        @Override
        public void onError(@NotNull Exception e) {
            Log.i("SearchApiExample", "Reverse geocoding error", e);
        }
    };

    public interface AlertDataDialogInterface {
        //void sendAlertComment(AlertSymbolBundle alert, String comment, AlertDataDialog dialog);
        void openComments(AlertSymbolBundle alert);
        String getCurrentUserId();
    }

    @Override
    public void onStop() {

        super.onStop();
    }
}
