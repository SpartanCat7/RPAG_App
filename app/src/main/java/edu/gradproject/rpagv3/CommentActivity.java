package edu.gradproject.rpagv3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

import edu.gradproject.rpagv3.Models.Comment;
import edu.gradproject.rpagv3.Providers.AlertProvider;
import edu.gradproject.rpagv3.Providers.CommentProvider;
import edu.gradproject.rpagv3.Providers.UserProvider;

public class CommentActivity extends AppCompatActivity {

    AlertProvider mAlertProvider;
    //CommentProvider mCommentProvider;
    UserProvider mUserProvider;
    ListenerRegistration mListenerRegistration;

    String alertId;

    RecyclerView commentsRecyclerView;
    TextView txtCommentsActivityTitle;

    CustomCommentAdapter listAdapter;

    DocumentReference alertRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        mAlertProvider = new AlertProvider();
        //mCommentProvider = new CommentProvider();
        mUserProvider = new UserProvider();

        alertId = getIntent().getStringExtra("alertId");
        alertRef = mAlertProvider.getAlert(alertId);

        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        txtCommentsActivityTitle = findViewById(R.id.txtCommentsActivityTitle);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        mListenerRegistration = CommentProvider.getComments(alertRef).addSnapshotListener((value, error) -> {
            if (value != null) {
                Log.d(MainActivity.LOG_TAG, "Comments recovered: " + value.size());

                listAdapter = new CustomCommentAdapter(CommentProvider.DocSnapListToCommentArrayList(value.getDocuments()));
                commentsRecyclerView.setAdapter(listAdapter);
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView commentTextView;
        private final TextView commentDateTextView;
        private final TextView commentUsernameTextView;

        public ViewHolder(View view) {
            super(view);

            commentTextView = view.findViewById(R.id.comment_text);
            commentDateTextView = view.findViewById(R.id.comment_date);
            commentUsernameTextView = view.findViewById(R.id.comment_username);
        }

        public TextView getCommentTextView() {
            return commentTextView;
        }

        public TextView getCommentDateTextView() {
            return commentDateTextView;
        }

        public TextView getCommentUsernameTextView() {
            return commentUsernameTextView;
        }
    }

    public class CustomCommentAdapter extends RecyclerView.Adapter<ViewHolder> {

        List<Comment> commentList;

        public CustomCommentAdapter(List<Comment> commentList) {
            this.commentList = commentList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.viewholder_comment_item, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.getCommentTextView().setText(commentList.get(position).getText());
            holder.getCommentDateTextView().setText(commentList.get(position).getDate().toString());
            String userId = commentList.get(position).getUserId();
            if (userId != null) {
                mUserProvider.getUserById(userId).addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists())
                        holder.getCommentUsernameTextView().setText((String) documentSnapshot.get("username"));
                });
            } else {
                holder.getCommentUsernameTextView().setText("???");
            }

        }

        @Override
        public int getItemCount() {
            return commentList.size();
        }
    }
}