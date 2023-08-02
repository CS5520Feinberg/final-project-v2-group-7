package edu.northeastern.pawsomepals.ui.chat;

import static edu.northeastern.pawsomepals.ui.chat.ChatFirebaseUtil.allUserCollectionReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.adapters.ChatMessageRecyclerAdapter;
import edu.northeastern.pawsomepals.models.ChatMessageModel;
import edu.northeastern.pawsomepals.models.ChatRoomModel;
import edu.northeastern.pawsomepals.models.ChatStyle;
import edu.northeastern.pawsomepals.models.GroupChatModel;
import edu.northeastern.pawsomepals.models.Users;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class ChatRoomActivity extends AppCompatActivity {
    private EditText messageInput;
    private ImageButton sendMessageBtn;
    private ImageButton functionBtn;
    private ImageButton backBtn;
    private ImageButton infoBtn;
    private TextView chatRoomName;
    private RecyclerView chatRoomRecyclerView;
    private List<Users> otherGroupUsers;
    private List<Users> groupUsers;

    private Users otherUser;
    private String chatRoomId;
    private ChatRoomModel chatRoomModel;
    private ChatMessageRecyclerAdapter adapter;

    private GroupChatModel group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        initialView();
        otherGroupUsers = new ArrayList<>();
        groupUsers = new ArrayList<>();
        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageInput.getText().toString().trim();
                if (message.isEmpty()) return;
                sendMessageToUser(message);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditChatRoomInfoActivity.class);
                //check
                ChatFirebaseUtil.passGroupChatModelAsIntent(intent, groupUsers);
                startActivity(intent);
            }
        });

        functionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        }
        );

        if (ChatFirebaseUtil.getChatStyleFromIntent(getIntent()).equals("oneOnOne")) {
            otherUser = ChatFirebaseUtil.getUserModelFromIntent(getIntent());
            chatRoomId = ChatFirebaseUtil.getChatroomId(ChatFirebaseUtil.currentUserId(), otherUser.getUserId());
            chatRoomName.setText(otherUser.getName());
            getOrCreateChatRoomModel();

        } else if (ChatFirebaseUtil.getChatStyleFromIntent(getIntent()).equals("group")) {
            group = ChatFirebaseUtil.getGroupChatModelFromIntent(getIntent());
            chatRoomId = ChatFirebaseUtil.getGroupRoomId(group.getGroupMembers());
            chatRoomName.setText(group.getGroupName());

            List<DocumentReference> references = ChatFirebaseUtil.getGroupFromChatRoom(group.getGroupMembers());
            for (DocumentReference reference : references) {
                reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        Users user = snapshot.toObject(Users.class);
                        if (user != null)
                            if (!user.getUserId().equals(ChatFirebaseUtil.currentUserId())) {
                                otherGroupUsers.add(user);
                                groupUsers.add(user);
                            }
                        if (user.getUserId().equals(ChatFirebaseUtil.currentUserId())) {
                            groupUsers.add(user);
                        }
                    }
                });
            }

            getOrCreateGroupChatModel();
        }

        setupChatRecyclerView();
    }

    private void initialView() {
        chatRoomName = findViewById(R.id.userName);
        messageInput = findViewById(R.id.message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        functionBtn = findViewById(R.id.function_btn);
        infoBtn = findViewById(R.id.chatroom_more_button);
        chatRoomRecyclerView = findViewById(R.id.message_recycler_view);
        backBtn = findViewById(R.id.message_back_button);
    }

    private void showDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.chat_bottom_sheet_layout);

        LinearLayout cameraLayout = dialog.findViewById(R.id.layoutCamera);
        LinearLayout galleryLayout = dialog.findViewById(R.id.layoutGallery);
        LinearLayout locationLayout = dialog.findViewById(R.id.layoutLocation);

        cameraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        galleryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        locationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }


    private void setupChatRecyclerView() {
        Query query = ChatFirebaseUtil.getChatroomMessageReference(chatRoomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class).build();

        adapter = new ChatMessageRecyclerAdapter(options, getApplicationContext());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        chatRoomRecyclerView.setLayoutManager(manager);
        chatRoomRecyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                chatRoomRecyclerView.smoothScrollToPosition(0);
            }
        });
    }

    private void sendMessageToUser(String message) {
        chatRoomModel.setLastMessageTimestamp(Timestamp.now());
        chatRoomModel.setLastMessageSenderId(ChatFirebaseUtil.currentUserId());
        chatRoomModel.setLastMessage(message);
        ChatFirebaseUtil.getChatroomReference(chatRoomId).set(chatRoomModel);

        ChatMessageModel chatMessageModel = new ChatMessageModel(message, ChatFirebaseUtil.currentUserId(), Timestamp.now());
        ChatFirebaseUtil.getChatroomMessageReference(chatRoomId).add(chatMessageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            messageInput.setText("");
                            sendNotification(message);
                        }
                    }
                });
    }

    private void getOrCreateChatRoomModel() {
        ChatFirebaseUtil.getChatroomReference(chatRoomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatRoomModel = task.getResult().toObject(ChatRoomModel.class);

                if (chatRoomModel == null) {
                    //first time chat
                    chatRoomModel = new ChatRoomModel(
                            chatRoomId,
                            Arrays.asList(ChatFirebaseUtil.currentUserId(), otherUser.getUserId()),
                            Timestamp.now(),
                            ""
                    );
                    chatRoomModel.setChatStyle(ChatStyle.ONEONONE);
                    ChatFirebaseUtil.getChatroomReference(chatRoomId).set(chatRoomModel);
                }
            }
        });
    }

    private void getOrCreateGroupChatModel() {
        ChatFirebaseUtil.getChatroomReference(chatRoomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatRoomModel = task.getResult().toObject(ChatRoomModel.class);

                if (chatRoomModel == null) {
                    //first time chat
                    chatRoomModel = new ChatRoomModel(
                            chatRoomId,
                            group.getGroupMembers(),
                            Timestamp.now(),
                            ""
                    );
                    chatRoomModel.setChatStyle(ChatStyle.GROUP);
                    ChatFirebaseUtil.getChatroomReference(chatRoomId).set(chatRoomModel);
                }
            }
        });
    }

    private void sendNotification(String message) {
        if (otherGroupUsers.size() == 0) {
            otherGroupUsers.add(otherUser);
        }

        //current username, message, currentuserid,otheruserid
        ChatFirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Users currentUser = task.getResult().toObject(Users.class);

                    try {
                        for (Users user : otherGroupUsers) {
                            JSONObject jasonObject = new JSONObject();
                            JSONObject notificationObj = new JSONObject();
                            notificationObj.put("title", currentUser.getName());
                            notificationObj.put("body", message);

                            JSONObject dataObj = new JSONObject();
                            dataObj.put("userId", currentUser.getUserId());

                            jasonObject.put("notification", notificationObj);
                            jasonObject.put("data", dataObj);

                            if (user != null) {
                                jasonObject.put("to", user.getFcmToken());
                            }
                            callApi(jasonObject);
                        }
                    } catch (Exception e) {
                        Log.i("info", "exeption");
                    }
                }
            }
        });
    }

    private void callApi(JSONObject jasonObject) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();

        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jasonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer AAAA7LggfNU:APA91bHUj7USk9d2fCoErjjeekUeYJ7LM1JHYAvqX1SeBxyuKYVXn0yl4onyw2hRt8TUo7Pd_Q0LfaqmUNpl5X8--ylQb5qvBoFTCxNHwBfBwQ901LkGbGGkofPpOizcy-Vo74Nfa8CU")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                if (response.isSuccessful()){
//                    try {
//                        if (response.body() != null) {
//                            JSONObject responseJson = new JSONObject();
//                            JSONArray results = responseJson.getJSONArray("results");
//                            if(responseJson.getInt("failure")==1){
//                                JSONObject error = (JSONObject) results.get(0);
//                                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
//                                return;
//                            }
//                        }
//                    } catch (JSONException e){
//                        e.printStackTrace();
//                    }
//                    showToast("Notification sent successfully");
////                }
            }
        });
    }
}