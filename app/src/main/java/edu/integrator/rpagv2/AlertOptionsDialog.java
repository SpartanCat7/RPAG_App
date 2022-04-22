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
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.integrator.rpagv2.R;
import com.mapbox.geojson.Point;
import com.mapbox.search.MapboxSearchSdk;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.ReverseGeoOptions;
import com.mapbox.search.ReverseGeocodingSearchEngine;
import com.mapbox.search.SearchCallback;
import com.mapbox.search.SearchRequestTask;
import com.mapbox.search.result.SearchAddress;
import com.mapbox.search.result.SearchResult;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.integrator.rpagv2.Models.ImageData;
import edu.integrator.rpagv2.Models.UIAlert;
import edu.integrator.rpagv2.Models.Vote;
import edu.integrator.rpagv2.Providers.ImageProvider;
import edu.integrator.rpagv2.Providers.VoteProvider;
import mobi.gspd.segmentedbarview.Segment;
import mobi.gspd.segmentedbarview.SegmentedBarView;
import mobi.gspd.segmentedbarview.SegmentedBarViewSideStyle;

public class AlertOptionsDialog extends DialogFragment {

    private AlertOptionsDialogInterface alertOptionsDialogInterface;

    TextView txtTitle;
    ImageView imgAlertIcon;
    TextView txtConfirmations, txtReports, txtAddress, txtDate;
    Button btnConfirm, btnReport;
    ImageView imgAlertPhoto;
    EditText editComment;
    Button btnComment;
    Button btnOpenComments;

    SegmentedBarView barView;

    UIAlert alert;
    int confirmationsCount;
    int reportsCount;
    private Vote voteCastedByUser;
    List<Vote> listVotes;
    //Bitmap imageBitmap;
    ImageData imageData;

    VoteProvider mVoteProvider;
    ListenerRegistration voteListenerRegistration;
    ImageProvider mImageProvider;

    private ReverseGeocodingSearchEngine reverseGeocoding;
    private SearchRequestTask searchRequestTask;

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
        //txtCoordinates = view.findViewById(R.id.txtAlertOptionsCoordinatesData);
        txtAddress = view.findViewById(R.id.txtAlertOptionsAddressData);
        txtDate = view.findViewById(R.id.txtAlertOptionsDateData);
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

        mVoteProvider = new VoteProvider();
        voteListenerRegistration = mVoteProvider.getVotesByAlert(alert.getAlertData().getId()).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                listVotes = value.toObjects(Vote.class);

                confirmationsCount = 0;
                reportsCount = 0;

                voteCastedByUser = null;

                for (Vote vote : listVotes) {
                    if (vote.isVoteTrue()) {
                        confirmationsCount += 1;
                    } else {
                        reportsCount += 1;
                    }

                    String userId = alertOptionsDialogInterface.getCurrentUserId();
                    if (userId != null) {
                        if (vote.getUserId().equals(userId)) {
                            voteCastedByUser = vote;
                        }
                    }

                }

                updateVoteButtonStates();
                //updateVoteButtonStatesNoLimits();
                updateSegmentGraph();

                txtConfirmations.setText(String.valueOf(confirmationsCount));
                txtReports.setText(String.valueOf(reportsCount));
            }
        });

        if (alert.getAlertClass().id == MainActivity.CUSTOM_CLASS_ID)
            txtTitle.setText(alert.getAlertData().getCustomName());
        else
            txtTitle.setText(alert.getAlertClass().name_string_ID);

        imgAlertIcon.setImageDrawable(getActivity().getDrawable(alert.getAlertClass().icon));

        //txtCoordinates.setText(alert.lat + " / " + alert.len);

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        txtDate.setText(dateFormat.format(alert.getAlertData().getDate()));

        btnConfirm.setOnClickListener(newPositiveVote);
        btnReport.setOnClickListener(newNegativeVote);

        imgAlertPhoto.setVisibility(View.GONE);
        mImageProvider = new ImageProvider();
        mImageProvider.getImagesByAlert(alert.getAlertData().getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
                    alertOptionsDialogInterface.sendAlertComment(alert, editComment.getText().toString(), AlertOptionsDialog.this);
                } else {
                    Toast.makeText(getActivity(), getText(R.string.comment_missing), Toast.LENGTH_LONG).show();
                }
            }
        });

        if (alertOptionsDialogInterface.getCurrentUserId() == null) {
            editComment.setEnabled(false);
            btnComment.setEnabled(false);
            btnConfirm.setEnabled(false);
            btnReport.setEnabled(false);
        }

        btnOpenComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertOptionsDialogInterface.openComments(alert);
            }
        });

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
        if (voteCastedByUser != null) {
            if (voteCastedByUser.isVoteTrue()) {
                btnConfirm.setOnClickListener(removeUserVote);
                btnConfirm.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.confirm_button_highlight, null));
                btnReport.setOnClickListener(updateNegativeVote);
                btnReport.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.report_button, null));
            } else {
                btnConfirm.setOnClickListener(updatePositiveVote);
                btnConfirm.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.confirm_button, null));
                btnReport.setOnClickListener(removeUserVote);
                btnReport.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.report_button_highlight, null));
            }
        } else {
            btnConfirm.setOnClickListener(newPositiveVote);
            btnConfirm.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.confirm_button, null));
            btnReport.setOnClickListener(newNegativeVote);
            btnReport.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.report_button, null));
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

    View.OnClickListener newPositiveVote = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Vote newVote = new Vote(null, alert.getAlertData().getId(), alertOptionsDialogInterface.getCurrentUserId(), new Date(), true);
            mVoteProvider.create(newVote).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    try {
                        Toast.makeText(getActivity(), getString(R.string.positive_vote_entered), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    View.OnClickListener newNegativeVote = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Vote newVote = new Vote(null, alert.getAlertData().getId(), alertOptionsDialogInterface.getCurrentUserId(), new Date(), false);
            mVoteProvider.create(newVote).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    try {
                        Toast.makeText(getActivity(), getString(R.string.negative_vote_entered), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    View.OnClickListener updatePositiveVote = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mVoteProvider.updateVote(voteCastedByUser.getId(), true).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    try {
                        Toast.makeText(getActivity(), getString(R.string.positive_vote_entered), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    View.OnClickListener updateNegativeVote = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mVoteProvider.updateVote(voteCastedByUser.getId(), false).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    try {
                        Toast.makeText(getActivity(), getString(R.string.negative_vote_entered), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    View.OnClickListener removeUserVote = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mVoteProvider.remove(voteCastedByUser.getId()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    try {
                        Toast.makeText(getActivity(), getString(R.string.vote_retired), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

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

    public interface AlertOptionsDialogInterface {
        void sendAlertComment(UIAlert alert, String comentario, AlertOptionsDialog dialog);
        void openComments(UIAlert alert);
        Context getActContext();
        String getCurrentUserId();
    }

    @Override
    public void onStop() {

        super.onStop();
    }
}
