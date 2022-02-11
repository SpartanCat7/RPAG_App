package edu.integrator.rpagv2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.integrator.rpagv2.R;

import java.util.List;

import edu.integrator.rpagv2.Models.Comment;
import edu.integrator.rpagv2.Providers.CommentProvider;
import edu.integrator.rpagv2.Providers.UserProvider;

public class CommentActivity extends AppCompatActivity {

    CommentProvider mCommentProvider;
    UserProvider mUserProvider;
    ListenerRegistration mListenerRegistration;

    String idAlerta;

    RecyclerView commentsRecyclerView;
    TextView txtCommentsActivityTitle;

    CustomCommentAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        mCommentProvider = new CommentProvider();
        mUserProvider = new UserProvider();

        idAlerta = getIntent().getStringExtra("alertId");

        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        txtCommentsActivityTitle = findViewById(R.id.txtCommentsActivityTitle);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        mListenerRegistration = mCommentProvider.getCommentsByAlert(idAlerta).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                Log.d(MainActivity.LOG_TAG, "Comments recovered: " + value.size());

                listAdapter = new CustomCommentAdapter(value.toObjects(Comment.class));
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
                    .inflate(R.layout.comment_card, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.getCommentTextView().setText(commentList.get(position).getText());
            holder.getCommentDateTextView().setText(commentList.get(position).getDate().toString());
            mUserProvider.getUserById(commentList.get(position).getUserId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists())
                        holder.getCommentUsernameTextView().setText((String) documentSnapshot.get("username"));
                }
            });
        }

        @Override
        public int getItemCount() {
            return commentList.size();
        }
    }
}