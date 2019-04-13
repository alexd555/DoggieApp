package com.example.android.doggie.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.android.doggie.R;
import com.example.android.doggie.UserClient;
import com.example.android.doggie.models.ClusterMarker;
import com.example.android.doggie.models.User;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.facebook.FacebookSdk.getApplicationContext;


public class MyClusterManagerRenderer extends DefaultClusterRenderer<ClusterMarker> {
    private static final String TAG = " ClusterManagerRenderer";
    private final IconGenerator iconGenerator;
    private final ImageView imageView;
    private final int markerWidth;
    private final int markerHeight;
    private final int padding;
    private Map<String, Marker> markersMap;
//    private Marker marker;
    private Context mContext;
//    private DownloadImageTask dit;
//    Thread thread1;
//    Thread thread2;

    public MyClusterManagerRenderer(Context context, GoogleMap googleMap,
                                    ClusterManager<ClusterMarker> clusterManager) {

        super(context, googleMap, clusterManager);

        // initialize cluster item icon generator
        iconGenerator = new IconGenerator(context.getApplicationContext());
        imageView = new ImageView(context.getApplicationContext());
        markerWidth = (int) context.getResources().getDimension(R.dimen.custom_marker_image);
        markerHeight = (int) context.getResources().getDimension(R.dimen.custom_marker_image);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(markerWidth, markerHeight));
        padding = (int) context.getResources().getDimension(R.dimen.custom_marker_padding);
        imageView.setPadding(padding, padding, padding, padding);
        iconGenerator.setContentView(imageView);
        markersMap = new HashMap<>();
        mContext = context;

    }

    @Override
    protected void onClusterItemRendered(final ClusterMarker clusterItem, final Marker marker) {

        final String imageUrl = clusterItem.getIconPath();

        if (imageView != null && imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(mContext.getApplicationContext())
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .optionalCircleCrop()
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher_round)
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            imageView.setImageDrawable(resource);
                            Bitmap icon = iconGenerator.makeIcon();
                            try {
                                marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
                                Log.d(TAG, "icon changed on onClusterItemRendered" + imageUrl);
//                                markersMap.put(clusterItem.getUser().getUserId(), marker);
                            } catch (Exception e) {
                                Log.d("TAG", "onResourceReady: " + e.getMessage());
                            }
                        }
                    });
        }
    }
//    @Override
//    protected void onBeforeClusterItemRendered(ClusterMarker item, MarkerOptions markerOptions) {
//        super.onBeforeClusterItemRendered(item, markerOptions);
//        Bitmap mIcon = null ;
//        try {
//            InputStream in = new URL(item.getIconPath()).openStream();
//            mIcon = BitmapFactory.decodeStream(in);
//            imageView.setImageBitmap(mIcon);
//            Bitmap icon = iconGenerator.makeIcon();
//            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.getTitle());
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//////        imageView.setLayoutParams(new ViewGroup.LayoutParams(markerWidth, markerHeight));
//////        imageView.setPadding(padding, padding, padding, padding);
//////        Bitmap icon = iconGenerator.makeIcon();
//////        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.getTitle());
////
////
//    }


        @Override
        protected boolean shouldRenderAsCluster (Cluster cluster){
            return false;
        }

        /**
         * Update the GPS coordinate of a ClusterItem
         * @param clusterMarker
         */
        public void setUpdateMarker (final ClusterMarker clusterMarker) {
            final String imageUrl = clusterMarker.getIconPath();
//            User user = ((UserClient)(getApplicationContext())).getUser();
//
//            Log.d("TAG", "icon changed: " + imageUrl);

            if (imageView != null && imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(getApplicationContext())
                        .load(imageUrl)
                        .apply(new RequestOptions()
                                .optionalCircleCrop()
                                .placeholder(R.mipmap.ic_launcher)
                                .error(R.mipmap.ic_launcher_round)
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(new SimpleTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                imageView.setImageDrawable(resource);
                                Bitmap icon = iconGenerator.makeIcon();
                                try {
//                                    Log.d("TAG", "icon changed: " + imageUrl + clusterMarker.getIconPath());

                                    if (imageUrl.equals(clusterMarker.getIconPath()))
                                    {
                                        String userId = clusterMarker.getUser().getUserId();
                                        Marker marker = getMarker(clusterMarker);// markersMap.get(userId);
//                                        marker.setPosition(clusterMarker.getPosition());
                                        if (marker!=null) {
                                            marker.setPosition(clusterMarker.getPosition());
                                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
                                            Log.d("TAG", "icon changed on updated marker " + marker.getId() + imageUrl);
                                        }
                                    }
                                }catch (Exception e) {
                                    Log.d("TAG", "onResourceReady: " + e.getMessage());
                                }
                            }
                        });
            }
        }
//        public void setUpdateMarker (ClusterMarker clusterMarker){
//            marker = getMarker(clusterMarker);
//            if (marker != null) {
//                marker.setPosition(clusterMarker.getPosition());
//                String imageUrl = clusterMarker.getIconPath();
//
//                if (imageView != null && imageUrl != null && !imageUrl.isEmpty()) {
//                    Glide.with(getApplicationContext())
//                            .load(imageUrl)
//                            .apply(new RequestOptions()
//                                    .optionalCircleCrop()
//                                    .placeholder(R.mipmap.ic_launcher)
//                                    .error(R.mipmap.ic_launcher_round)
//                                    .diskCacheStrategy(DiskCacheStrategy.ALL))
//                            .into(new SimpleTarget<Drawable>() {
//                                @Override
//                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                                    imageView.setImageDrawable(resource);
//                                    Bitmap icon = iconGenerator.makeIcon();
//                                    try {
//                                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
//                                    } catch (Exception e) {
//                                        Log.d("TAG", "onResourceReady: " + e.getMessage());
//                                    }
//                                }
//                            });
//                }
//            dit = new DownloadImageTask();
//            dit.execute(clusterMarker.getIconPath());
//            }
//        }
//    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
//        @Override
//        protected Bitmap doInBackground(String... urls) {
//            Bitmap mIcon = null;
//            try {
//                InputStream in = new URL(urls[0]).openStream();
//                mIcon = BitmapFactory.decodeStream(in);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return mIcon;
//        }
//        @Override
//        protected void onPostExecute(Bitmap result) {
//            if (marker!=null && result != null) {
//                Bitmap resizedBitmap = Bitmap.createScaledBitmap(result, markerWidth, markerHeight,
//                        false);
//                marker.setIcon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));
//
//            }
//        }
//    }
}

















