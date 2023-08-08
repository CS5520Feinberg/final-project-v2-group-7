package edu.northeastern.pawsomepals.utils;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;

public class FirebaseUtil {


    public static void fetchUserInfoFromFirestore(String userId, DataCallback dataCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").whereEqualTo("userId", userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().size() > 0) {
                Users user = task.getResult().getDocuments().get(0).toObject(Users.class);
                dataCallback.onUserReceived(user);
            } else {
            }
        });
    }

    public static Map<String, Users> fetchUserInfoFromFirestoreBlocking(List<String> userIds) {
        Map<String, Users> map = new HashMap<>();
        if(userIds.isEmpty()){
            return map;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Task<QuerySnapshot> task = db.collection("user").whereIn("userId", userIds).get();
        try {
            Tasks.await(task);
            if (task.isSuccessful()) {
                for (DocumentSnapshot d: task.getResult().getDocuments()) {
                    Users users = d.toObject(Users.class);
                    map.put(users.getUserId(), users);
                }
                return map;
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public static void uploadImageToStorage(Uri cameraImageUri, Uri galleryImageUri, String postType, DataCallback dataCallback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        Uri uploadImageUri = null;
        if (cameraImageUri != null) {
            uploadImageUri = cameraImageUri;
        } else if (galleryImageUri != null) {
            uploadImageUri = galleryImageUri;
        }

        if (uploadImageUri != null) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageName = postType + "_image_" + timestamp + ".jpg";
            StorageReference imageRef = storageRef.child(postType + "_images/" + imageName);
            UploadTask uploadTask = imageRef.putFile(uploadImageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        if (downloadUri != null) {
                            dataCallback.onImageUriReceived(downloadUri.toString());
                        }
                    } else {
                        dataCallback.onError(task.getException());
                    }
                }
            });
        } else {
            dataCallback.onDismiss();
        }
    }

    public static void updateFeedWithCommentCount(String postType, String postId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(postType).whereEqualTo("feedItemId", postId).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                DocumentSnapshot documentSnapshot = null;
                if (!queryDocumentSnapshots.isEmpty()) {
                    documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                    int currentCommentCount = documentSnapshot.getLong("commentCount") != null ? documentSnapshot.getLong("commentCount").intValue() : 0;
                    int newCommentCount = currentCommentCount + 1;
                    documentSnapshot.getReference().update("commentCount", newCommentCount).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });

    }

    public static void createCollectionInFirestore(Map<String, Object> feedTypeObj, String feedType, DataCallback dataCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //Add a new document with a generated ID
        db.collection(feedType).add(feedTypeObj).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                dataCallback.onDismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    public static void addLikeToFirestore(String feedItemId, String createdBy, String postType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> likeData = new HashMap<>();
        likeData.put("feedItemId", feedItemId);

        db.collection("user").document(createdBy).collection("likes").add(likeData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d("likes", "Like added successfully to " + feedItemId);
                updateFeedWithLikeCount(feedItemId, 1, postType);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("likes", "Like not added to " + feedItemId);
            }
        });
    }

    public static void removeLikeFromFirestore(String feedItemId, String createdBy, String postType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(createdBy).collection("likes").whereEqualTo("feedItemId", feedItemId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                    documentSnapshot.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("likes", "Like removed successfully from " + feedItemId);
                            updateFeedWithLikeCount(feedItemId, -1, postType);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("likes", "Failed to remove like from " + feedItemId);
                        }
                    });
                }
            }
        });

    }

    private static void updateFeedWithLikeCount(String feedItemId, int likeChange, String postType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(postType) // Replace "your_feed_collection" with your actual feed collection name
                .whereEqualTo("feedItemId", feedItemId).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        DocumentSnapshot documentSnapshot = null;
                        if (!queryDocumentSnapshots.isEmpty()) {
                            documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            int currentLikeCount = documentSnapshot.getLong("likeCount") != null ? documentSnapshot.getLong("likeCount").intValue() : 0;
                            int newLikeCount = currentLikeCount + likeChange;
                            documentSnapshot.getReference().update("likeCount", newLikeCount).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    public static void addFavToFirestore(String feedItemId, String createdBy, String postType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> favData = new HashMap<>();
        favData.put("feedItemId", feedItemId);

        db.collection("user").document(createdBy).collection("favorites").add(favData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d("fav", "fav added successfully to " + feedItemId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("fav", "fav not added to " + feedItemId);
            }
        });
    }

    public static void removeFavFromFirestore(String feedItemId, String createdBy, String postType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(createdBy).collection("favorites").whereEqualTo("feedItemId", feedItemId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                    documentSnapshot.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("favorites", "favorites removed successfully from " + feedItemId);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("favorites", "Failed to remove favorites from " + feedItemId);
                        }
                    });
                }
            }
        });

    }

    public static void fetchRecipeFromFirestore(String recipeId, DataCallback dataCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("recipes").whereEqualTo("recipeId", recipeId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().size() > 0) {
                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                Recipe recipe = documentSnapshot.toObject(Recipe.class);
                dataCallback.onRecipeReceived(recipe);
            } else {
                Exception exception = task.getException();
            }
        });
    }

    public static void getFollowData(String userId, DataCallback dataCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference followingRef = db.collection("follow").document(userId);
        followingRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                List<String> followingUserIds = (List<String>) documentSnapshot.get("following");
                dataCallback.onFollowingUserIdListReceived(followingUserIds);
            }
        });
    }

    public interface DataCallback {
        void onUserReceived(Users user);

        void onImageUriReceived(String imageUrl);

        void onError(Exception exception);

        void onDismiss();

        void onRecipeReceived(Recipe recipe);

        void onFollowingUserIdListReceived(List<String> followingUserIds);
    }
}