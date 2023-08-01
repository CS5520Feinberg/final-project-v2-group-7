package edu.northeastern.pawsomepals.ui.chat;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.northeastern.pawsomepals.models.GroupChatModel;
import edu.northeastern.pawsomepals.models.Users;

public class ChatFirebaseUtil {
    public static String currentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

    public static boolean isLoggedIn() {
        if (currentUserId() != null) {
            return true;
        }
        return false;
    }

    public static void passUserModelAsIntent(Intent intent, Users model) {
        intent.putExtra("name", model.getName());
        intent.putExtra("userId", model.getUserId());
        intent.putExtra("email", model.getEmail());
        intent.putExtra("fcmToken", model.getFcmToken());
    }

    public static void passGroupChatModelAsIntent(Intent intent, List<Users> users) {
        StringBuilder nameBuilder = new StringBuilder();
        StringBuilder idBuilder = new StringBuilder();

        for (Users user : users) {
            nameBuilder.append(user.getName() + " ");
            idBuilder.append(user.getUserId() + " ");
        }
        intent.putExtra("name", nameBuilder.toString());
        intent.putExtra("ids", idBuilder.toString());
        Log.i("info", nameBuilder.toString());
        Log.i("info", idBuilder.toString());
    }

    public static DocumentReference currentUserDetails() {
        return FirebaseFirestore.getInstance().collection("user").document(currentUserId());
    }

    public static CollectionReference allUserCollectionReference() {
        return FirebaseFirestore.getInstance().collection("user");
    }

    public static CollectionReference allRecipeCollectionReference() {
        Log.i("coll", FirebaseFirestore.getInstance().collection("recipes").getClass().toString());
        return FirebaseFirestore.getInstance().collection("recipes");
    }

    public static DocumentReference getChatroomReference(String chatroomId) {
        return FirebaseFirestore.getInstance().collection("chatroom").document(chatroomId);
    }

    public static CollectionReference getChatroomMessageReference(String chatroomId) {
        return getChatroomReference(chatroomId).collection("chats");
    }

    public static String getChatroomId(String userId1, String userId2) {
        if (userId1.hashCode() < userId2.hashCode()) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    public static String getGroupRoomId(List<String> userIds) {
        StringBuilder groupChatId = new StringBuilder();
        Collections.sort(userIds, new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                return o1.hashCode() - o2.hashCode();
            }
        });

        for (int i = 0; i < userIds.size(); i++) {
            groupChatId.append(userIds.get(i));
            if (i != userIds.size() - 1) {
                groupChatId.append("_");
            }

        }
        return "group" + groupChatId;
    }

    public static Users getUserModelFromIntent(Intent intent) {
        String userName = intent.getStringExtra("name");
        String userId = intent.getStringExtra("userId");
        String email = intent.getStringExtra("email");
        String fcmToken = intent.getStringExtra("fcmToken");

        return new Users(userName, userId, email, fcmToken);
    }

    public static GroupChatModel getGroupChatModelFromIntent(Intent intent) {
        String groupName = intent.getStringExtra("name");
        String ids = intent.getStringExtra("ids");
        List idList = Arrays.asList(ids.split(" "));

        return new GroupChatModel(idList, groupName);
    }


    public static CollectionReference allChatRoomCollectionReference() {
        return FirebaseFirestore.getInstance().collection("chatroom");
    }

    public static DocumentReference getOtherUserFromChatroom(List<String> userIds) {
        if (userIds.get(0).equals(ChatFirebaseUtil.currentUserId())) {
            return allUserCollectionReference().document(userIds.get(1));
        } else {
            return allUserCollectionReference().document(userIds.get(0));
        }
    }

    public static String timestampToString(Timestamp timestamp) {
        return new SimpleDateFormat("HH:HH").format(timestamp.toDate());
    }
}
