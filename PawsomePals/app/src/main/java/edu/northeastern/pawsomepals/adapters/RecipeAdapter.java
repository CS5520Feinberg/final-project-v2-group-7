package edu.northeastern.pawsomepals.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.utils.OnItemActionListener;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private List<Recipe> recipes;
    private OnItemActionListener onItemActionListener;



    public RecipeAdapter(List<Recipe> recipes,List<Users> user,OnItemActionListener onItemActionListener) {
        this.recipes = recipes;
        this.onItemActionListener = onItemActionListener;
    }

    @NonNull
    @Override
    public RecipeAdapter.RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_layout, parent, false);
        return new RecipeViewHolder(itemView);

    }


    @Override
    public void onBindViewHolder(@NonNull RecipeAdapter.RecipeViewHolder holder, int position) {

        Recipe recipe = recipes.get(position);
        String user = recipe.getUsername();
        holder.recipeName.setText(recipe.getTitle());
        holder.username.setText(recipe.getUsername());
        Glide.with(holder.itemView.getContext())
                .load(recipe.getImg())
                .into(holder.recipeImage);
            Glide.with(holder.itemView.getContext())
                    .load(recipe.getUserProfileImage())
                    .into(holder.userProfilePic);

        holder.recipeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onRecipeClick(recipes.get(holder.getBindingAdapterPosition()));
            }
        });

        holder.recipeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onRecipeClick(recipes.get(holder.getBindingAdapterPosition()));
            }
        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onUserClick(recipes.get(holder.getBindingAdapterPosition()));
            }
        });
        holder.userProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemActionListener.onUserClick(recipes.get(holder.getBindingAdapterPosition()));
            }
        });

    }
    public List<Recipe> getRecipes() {
        return recipes;
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void setRecipes(List<Recipe> recipeList) {
        recipes = recipeList;
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder{
        ImageView recipeImage;
        TextView username;
        ImageView userProfilePic;
        TextView recipeName;


        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            userProfilePic = itemView.findViewById(R.id.userProfilePic);
            recipeName = itemView.findViewById(R.id.recipeName);
            username = itemView.findViewById(R.id.username);


        }


    }
}
