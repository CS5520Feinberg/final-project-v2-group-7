package edu.northeastern.pawsomepals.models;


import com.google.firebase.Timestamp;

import java.util.List;

public class ChatRoomModel {
    private ChatStyle chatStyle;
    private String chatRoomId;
    private List<String> userIds;
    private Timestamp lastMessageTimestamp;
    private String lastMessageSenderId;
    private String lastMessage;

    public ChatRoomModel(){

    }

    public ChatRoomModel(String chatRoomId, List<String> userIds, Timestamp lastMessageTimestamp, String lastMessageSenderId) {
        this.chatRoomId = chatRoomId;
        this.userIds = userIds;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessageSenderId = lastMessageSenderId;
        this.chatStyle = null;
    }

    public ChatStyle getChatStyle() {
        return chatStyle;
    }

    public void setChatStyle(ChatStyle chatStyle) {
        this.chatStyle = chatStyle;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public Timestamp getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(Timestamp lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
