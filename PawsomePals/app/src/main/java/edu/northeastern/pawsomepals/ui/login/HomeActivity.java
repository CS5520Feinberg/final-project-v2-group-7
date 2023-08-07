

package edu.northeastern.pawsomepals.ui.login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.ui.chat.ChatFragment;
import edu.northeastern.pawsomepals.ui.feed.FeedFragment;
import edu.northeastern.pawsomepals.ui.map.MapFragment;
import edu.northeastern.pawsomepals.ui.profile.ProfileFragment;
import edu.northeastern.pawsomepals.ui.search.SearchFragment;

public class HomeActivity extends AppCompatActivity implements LogoutDialogListener {
    BottomNavigationView bottomNavigationView;
    private int currentSelectedItemIndex = 0;
    private Toolbar toolbar;

    private ImageView logoutImageView;
    public static int PROFILE_ACTIVITY_REQUEST_CODE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = findViewById(R.id.chatToolBar);

        logoutImageView = findViewById(R.id.logoutImageView);

        logoutImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogoutDialogFragment dialog = new LogoutDialogFragment();
                dialog.setLogoutDialogListener((LogoutDialogListener) HomeActivity.this);
                dialog.show(getSupportFragmentManager(), "logout_dialog");
//                Log out delete messaging token to avoid receiving messages
//                FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                       if (task.isSuccessful()){
//                           LogoutDialogFragment dialog = new LogoutDialogFragment();
//                           dialog.setLogoutDialogListener((LogoutDialogListener) HomeActivity.this);
//                           dialog.show(getSupportFragmentManager(), "logout_dialog");
//                       }
//                    }
//                });
            }
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                currentSelectedItemIndex = item.getItemId();
                String fragmentTag = getFragmentTagBasedOnId(item.getItemId());
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);

                if (fragment == null) {
                    fragment = getFragmentBasedOnId(currentSelectedItemIndex);
                }

                // Check if the selected fragment is the ProfileFragment
                if (fragment instanceof ProfileFragment) {
                    // Set the profileId to the current user's userId
                    SharedPreferences sharedPreferences = getSharedPreferences("ProfileId", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("profileId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    editor.apply();
                }

                replaceFragment(fragment, item.getItemId());
                return true;
            }
        });

        if (savedInstanceState == null) {
            currentSelectedItemIndex = R.id.feed;
        } else {
            currentSelectedItemIndex = savedInstanceState.getInt("current_selected_item_index");
        }
        showInitialFragment();
    }

    private Fragment getFragmentBasedOnId(int itemId) {
        if (itemId == R.id.feed) {
            return new FeedFragment();
        } else if (itemId == R.id.search) {
            toolbar.setTitle("Search");
            return new SearchFragment();
        } else if (itemId == R.id.chat) {
            toolbar.setTitle("Chat");
            return new ChatFragment();
        } else if (itemId == R.id.profile) {
            toolbar.setTitle("Profile");
            ProfileFragment profileFragment = new ProfileFragment();
            Bundle args = new Bundle();
            args.putString("profileId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            profileFragment.setArguments(args);
            return profileFragment;
        } else if (itemId == R.id.map) {
            toolbar.setTitle("Map");
            return new MapFragment();
        }
        throw  new IllegalArgumentException("Fragment not found");
    }

    private void showInitialFragment() {
        String fragmentTag = getFragmentTagBasedOnId(currentSelectedItemIndex);
        Fragment initialFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
        if (initialFragment == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, FeedFragment.class, null,
                            fragmentTag)
                    .commit();
        } else {
            replaceFragment(initialFragment, currentSelectedItemIndex);
        }
    }

    private String getFragmentTagBasedOnId(int itemId) {
        if (itemId == R.id.feed) {
            toolbar.setTitle("Feed");
            return "feed_fragment";
        } else if (itemId == R.id.search) {
            toolbar.setTitle("Search");
            return "search_fragment";
        } else if (itemId == R.id.chat) {
            toolbar.setTitle("Chat");
            return "chat_fragment";
        } else if (itemId == R.id.profile) {
            toolbar.setTitle("Profile");
            return "profile_fragment";
        } else if (itemId == R.id.map) {
            toolbar.setTitle("Map");
            return "map_fragment";
        }
        return null;
    }

    private void replaceFragment(Fragment fragment, int id) {
        // Check if the selected fragment is the ProfileFragment
        if (fragment instanceof ProfileFragment) {
            // Set the profileId to the current user's userId
            SharedPreferences sharedPreferences = getSharedPreferences("ProfileId", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("profileId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            editor.apply();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view, fragment, getFragmentTagBasedOnId(id))
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (currentSelectedItemIndex != R.id.feed) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(getFragmentTagBasedOnId(R.id.feed));
            replaceFragment(fragment, currentSelectedItemIndex);
            bottomNavigationView.setSelectedItemId(R.id.feed);
            currentSelectedItemIndex = R.id.feed;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("current_selected_item_index", currentSelectedItemIndex);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLogoutConfirmed() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PROFILE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String resultData = data.getStringExtra("profileId");
                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragment_container_view, ProfileFragment.class, data.getExtras(),
                                "profile_fragment")
                        .commit();
            }
        }
        if (requestCode == 9) {


        }
    }
}
