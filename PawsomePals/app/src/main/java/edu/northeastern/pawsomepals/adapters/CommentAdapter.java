package edu.northeastern.pawsomepals.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Comment;

public class CommentAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<Comment> comments;
    private String postId;

    public CommentAdapter(Context context, List<Comment> comments, String postId) {
        this.context = context;
        this.comments = comments;
        this.postId = postId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.comment_item , parent , false);

        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Comment comment = comments.get(position);


    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image_profile;
        public TextView username;
        public TextView comment;
        public FirebaseFirestore db = FirebaseFirestore.getInstance();
        public FirebaseAuth auth = FirebaseAuth.getInstance();
        public FirebaseUser currentUser = auth.getCurrentUser();

        private String loggedInUserId = currentUser.getUid();

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
        }
    }

    private void getUserInfo (final ImageView imageView , final TextView username , String publisherid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

    }



    }