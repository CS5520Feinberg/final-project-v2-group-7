package edu.northeastern.pawsomepals.ui.feed.viewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.ui.feed.FeedFragmentViewType;
import edu.northeastern.pawsomepals.ui.feed.layout.FeedActionsLayout;
import edu.northeastern.pawsomepals.utils.DialogHelper;
import edu.northeastern.pawsomepals.utils.OnItemActionListener;
import edu.northeastern.pawsomepals.utils.TimeUtil;

public class RecipeFeedViewHolder extends RecyclerView.ViewHolder {
    ImageView recipeImage;
    TextView username;
    ImageView userProfilePic,moreOptionImageView;
    TextView recipeName, timestampTextView;

    FeedActionsLayout feedActionsLayout;
    private OnItemActionListener onItemActionListener;

    public RecipeFeedViewHolder(@NonNull View itemView) {
        super(itemView);
        recipeImage = itemView.findViewById(R.id.recipeImageView);
        userProfilePic = itemView.findViewById(R.id.userProfilePic);
        recipeName = itemView.findViewById(R.id.recipeNameTextView);
        username = itemView.findViewById(R.id.usernameTextView);
        timestampTextView = itemView.findViewById(R.id.timestampTextView);
        feedActionsLayout = itemView.findViewById(R.id.feed_action);
        moreOptionImageView = itemView.findViewById(R.id.moreOptionImageView);
    }

    public void bindData(AppCompatActivity activity, FeedFragmentViewType feedFragmentViewType, Recipe recipe, OnItemActionListener onItemActionListener) {
        feedActionsLayout.bindView(activity, recipe);
        recipeName.setText(recipe.getTitle());
        username.setText(recipe.getUsername());
        timestampTextView.setText(TimeUtil.formatTime(recipe.getCreatedAt()));

        Glide.with(itemView.getContext())
                .load(recipe.getImg())
                .transform(new FitCenter(), new GranularRoundedCorners(0, 0, 25, 25))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(recipeImage);

        Glide.with(itemView.getContext())
                .load(recipe.getUserProfileImage())
                .into(userProfilePic);

        recipeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onRecipeClick(recipe);
            }
        });

        recipeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onRecipeClick(recipe);
            }
        });

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onUserClick(recipe.getCreatedBy());
            }
        });
        userProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onUserClick(recipe.getCreatedBy());
            }
        });

        PopupMenu popupMenu = DialogHelper.getPopupMenu(activity, moreOptionImageView, recipe, feedFragmentViewType);
        if (!popupMenu.getMenu().hasVisibleItems()) {
            moreOptionImageView.setVisibility(View.GONE);
        }

        moreOptionImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMenu.show();
            }
        });
    }
}
