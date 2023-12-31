package edu.northeastern.pawsomepals.ui.chat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ChatGroupMemberViewsAdapter;
import edu.northeastern.pawsomepals.models.ChatUserCardViewModel;
import edu.northeastern.pawsomepals.models.GroupChatModel;
import edu.northeastern.pawsomepals.models.Users;
import edu.northeastern.pawsomepals.ui.profile.ProfileFragment;

public class EditChatRoomInfoActivity extends AppCompatActivity {
    private GroupChatModel groupChatModel;
    private LinearLayout groupNameLayout, groupNoticeLayout;
    private List<String> usersImgs;
    private List<String> usersNames;
    private List<Users> groupUsers;
    private List<ChatUserCardViewModel> userCardViewModels;
    //    private String membersNamesTxt;
    private ImageButton backBtn;
    private TextView groupName, groupNotice, membersNames;
    private EditText editTextField;
    private String newGroupName;
    private RecyclerView membersRecyclerView;
    private ChatGroupMemberViewsAdapter adapter;
    private boolean isChatroomEdited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_chat_room_info);

        groupUsers = new ArrayList<>();
        userCardViewModels = new ArrayList<>();
        isChatroomEdited = false;
        initialView();
        groupChatModel = ChatFirebaseUtil.getGroupChatModelFromIntent(getIntent());
        groupName.setText(groupChatModel.getGroupName());

        usersImgs = Arrays.asList(ChatFirebaseUtil.getGroupMemberImgsFromChatRoom(getIntent()).split(" "));
        usersNames = Arrays.asList(ChatFirebaseUtil.getGroupMemberNameFromChatRoom(getIntent()).split(","));
        for (int i = 0; i < usersImgs.size(); i++) {
            userCardViewModels.add(new ChatUserCardViewModel(groupChatModel.getGroupMembers().get(i), usersImgs.get(i), usersNames.get(i)));
        }
        adapter = new ChatGroupMemberViewsAdapter(this, userCardViewModels, new ChatFragment.ProfilePicClickListener() {
            @Override
            public void onItemClicked(String userIDValue) {
                navigateToProfileFragment(userIDValue);
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        membersRecyclerView.setLayoutManager(linearLayoutManager);
        membersRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        List<DocumentReference> references = ChatFirebaseUtil.getGroupFromChatRoom(groupChatModel.getGroupMembers());
        for (DocumentReference reference : references) {
            reference.get().addOnSuccessListener(snapshot -> {
                Users user = snapshot.toObject(Users.class);
                if (user != null) {
                    groupUsers.add(user);
                }
            });
        }
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isChatroomEdited) {
                    Log.i("edie",groupUsers.toString());
                    Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                    ChatFirebaseUtil.passGroupChatModelAsIntent(intent, groupUsers, newGroupName);
                    ChatFirebaseUtil.passIntentFromEditRoomAsIntent(intent,true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                } else {
                    onBackPressed();
                }
            }
        });
        groupNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog();

                Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                ChatFirebaseUtil.passGroupNameAsIntent(intent, newGroupName);
            }
        });

    }

    private void navigateToProfileFragment(String userIDValue) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("profileId", userIDValue);
        setResult(RESULT_OK, resultIntent);

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("ProfileId", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profileId", userIDValue);
        editor.apply();

        //Navigate to Profile Fragment
        ProfileFragment profileFragment = new ProfileFragment();
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.editChatContainer, profileFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

//    private String createMembersDetails() {
//        StringBuilder builder = new StringBuilder();
//        for (Users user : groupUsers) {
//            Log.i("info", user.getName());
//            builder.append(user.getName());
//            builder.append("  ");
//        }
//        return builder.toString();
//    }

    private void initialView() {
        groupName = findViewById(R.id.group_name_text);
//        groupNotice = findViewById(R.id.group_notice_text);
//        membersNames = findViewById(R.id.group_members_text);
        backBtn = findViewById(R.id.info_back_button);
        groupNameLayout = findViewById(R.id.groupNameLayout);
//        groupNoticeLayout = findViewById(R.id.groupNoticeLayout);
        membersRecyclerView = findViewById(R.id.group_member_card_views);
    }

    //    private String getUserNames(List<Users> users){
//        StringBuilder builder = new StringBuilder();
//        for(Users theUser:groupUsers){
//            builder.append(theUser.getName()+"  ");
//        }
//        return membersNamesTxt = builder.toString();
//    }
    private void createDialog() {
        editTextField = new EditText(this.getApplicationContext());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Group Name")
                .setView(editTextField)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                        newGroupName = editTextField.getText().toString();
                        groupName.setText(newGroupName);
                        groupChatModel.setGroupName(newGroupName);
                        ChatFirebaseUtil.passGroupNameAsIntent(intent, newGroupName);
                        updateFirestoreData();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void updateFirestoreData() {
        if (newGroupName != null) {
            isChatroomEdited = true;
            String chatroomId = ChatFirebaseUtil.getGroupRoomId(groupChatModel.getGroupMembers());
            ChatFirebaseUtil.getChatroomReference(chatroomId)
                    .update("chatRoomName", newGroupName)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("Firestore", "Document successfully updated!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Firestore", "Error updating document", e);
                        }
                    });
        }
    }
}