package com.example.hritik.googlemapsgoogleplaces;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoAdapter implements GoogleMap.InfoWindowAdapter {
    private View mView;
    private Context mContext;

    public CustomInfoAdapter(Context mContext) {
        this.mContext = mContext;
        mView = LayoutInflater.from(mContext).inflate(R.layout.custome_info_window, null);
        Log.d("Hritik", "CustomInfoAdapter: ");
    }

    private void renderWindowText(Marker marker, View view) {

        String title = marker.getTitle();
        TextView tvTitle = view.findViewById(R.id.idTitle);
        if (!title.equals("")) {

            tvTitle.setText(title);
            Log.d("Hritik", "renderWindowText: "+title);

        }
        String snippet = marker.getSnippet();
        Log.d("Hritik", "renderWindowText: "+snippet);
        TextView tvSnippet = view.findViewById(R.id.snippet);
        if (!snippet.equals("")) {

            tvSnippet.setText(snippet);

        }


    }

    @Override

    public View getInfoWindow(Marker marker) {

        renderWindowText(marker,mView);
        return mView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        renderWindowText(marker,mView);
        return mView;
    }
}
