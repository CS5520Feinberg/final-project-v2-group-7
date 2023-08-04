package edu.northeastern.pawsomepals.ui.map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.Event;
import edu.northeastern.pawsomepals.models.FeedItem;
import edu.northeastern.pawsomepals.ui.feed.FirestoreDataLoader;

public class MapFragment extends Fragment implements OnMapReadyCallback, FirestoreDataLoader.FirestoreDataListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private MapView mapView;
    private GoogleMap googleMap;

    private FusedLocationProviderClient fusedLocationClient;
    private Activity activity;
    private FeedItem selectedFeedItem;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = requireActivity();
        selectedFeedItem = null;
        if (getArguments() != null) {
            selectedFeedItem = (FeedItem) requireArguments().getSerializable("feedItem");
        }

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void moveMapToCurrentLocation(double latitude, double longitude, int zoom) {
        if (googleMap == null) {
            return;
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        loadMarkersOnMap();
        if (selectedFeedItem != null) {
            moveMapToCurrentLocation(selectedFeedItem.getLatLng().getLatitude(), selectedFeedItem.getLatLng().getLongitude(), 12);
//            selectedFeedItem = null;
        }
        Log.d("yoo", "onMapReady = " + googleMap.toString());
    }

    private void loadMarkersOnMap() {
        List<CollectionReference> collections = new ArrayList<>();
        collections.add(FirebaseFirestore.getInstance().collection("events"));
        collections.add(FirebaseFirestore.getInstance().collection("posts"));
        collections.add(FirebaseFirestore.getInstance().collection("services"));
        collections.add(FirebaseFirestore.getInstance().collection("photovideo"));

        FirestoreDataLoader firestoreDataLoader = new FirestoreDataLoader(this, collections, "createdAt");
        firestoreDataLoader.loadDataFromCollections();
    }

    @Override
    public void onDataLoaded(List<FeedItem> feedItems) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (googleMap != null) {
                    for (FeedItem item : feedItems) {
                        if (item.getLatLng() != null) {
                            MarkerOptions marker = new MarkerOptions()
                                    .title(((Event) item).getEventName())
                                    .icon(bitmapFromVector(
                                            activity.getApplicationContext(),
                                            R.drawable.dogpawheart,
                                            80))
                                    .snippet(((Event) item).getEventDetails())
                                    .position(new LatLng(item.getLatLng().getLatitude(), item.getLatLng().getLongitude()));
                            Marker m = googleMap.addMarker(marker);
                            if (selectedFeedItem != null && item.getFeedItemId().equals(selectedFeedItem.getFeedItemId())) {
                                m.setZIndex(9);
                                m.showInfoWindow();
                            }
                            m.setTag(item.getFeedItemId());
                        }
                    }
                }
            }
        });
    }

    private BitmapDescriptor bitmapFromVector(Context context, int vectorResId, int sizeOfMarker) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(
                context, vectorResId);

        // below line is use to set bounds to our vector
        // drawable.
        vectorDrawable.setBounds(
                0, 0, sizeOfMarker,
                sizeOfMarker);

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(
                sizeOfMarker,
                sizeOfMarker,
                Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our
        // bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (selectedFeedItem == null) {
            requestLocationPermission();
        }
        mapView.onResume();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(requireActivity(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getCurrentLocation(new CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY).build(), null)
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.d("yoo", "location success = " + location.toString());
                            moveMapToCurrentLocation(location.getLatitude(), location.getLongitude(), 9);
                        }
                    }
                });
    }
}
