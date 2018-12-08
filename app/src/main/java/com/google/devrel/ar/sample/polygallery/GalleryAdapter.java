/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devrel.ar.sample.polygallery;

import android.app.ActionBar;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Recycler view adapter for displaying thumbnails of Poly models.
 */
class GalleryAdapter extends RecyclerView.Adapter {
    private static final String TAG = "GalleryAdapter";

    private final List<GalleryItem> items;
    private int selected;

    public GalleryAdapter(List<GalleryItem> items) {
        this.items = items;
        selected = -1;
    }

    /**
     * Parses the response from the Poly API and creates GalleryItems for  the results.
     *
     * @param responseBody            - the response data.
     * @param backgroundThreadHandler - a background handler thread used to load thumbnails.
     * @return List of gallery items.
     * @throws IOException if there is a problem.
     */
    public static List<GalleryItem> parseListResults(
            byte[] responseBody, Handler backgroundThreadHandler) throws IOException {
        Log.d(TAG, "Got asset response (" + responseBody.length + " bytes). Parsing.");
        String assetBody = new String(responseBody, Charset.forName("UTF-8"));
        Log.d(TAG, assetBody);

        try {
            JSONObject response = new JSONObject(assetBody);

            List<GalleryItem> items = new ArrayList<>();

            // See https://developers.google.com/poly/reference/api/rest/v1/assets/list
            // for available fields.
            if (!response.has("assets")) {
                throw new IOException("No assets found");
            }
            JSONArray assets = response.getJSONArray("assets");

            for (int i = 0; i < assets.length(); i++) {
                JSONObject obj = assets.getJSONObject(i);
                // Use the name as the key.
                GalleryItem item = new GalleryItem(
                        obj.getString("name"));
                item.setDisplayName(obj.getString("displayName"));
                item.setAuthorInfo(obj.getString("authorName"), obj.getString("license"));

                if (obj.has("description")) {
                    item.setDescription(obj.getString("description"));
                }

                String url = obj.getJSONObject("thumbnail").getString("url");
                item.setThumbnail(url);
                item.loadThumbnail(backgroundThreadHandler);

                // Find the glTF URL.
                JSONArray formats = obj.getJSONArray("formats");
                for (int j = 0; j < formats.length(); j++) {
                    JSONObject format = formats.getJSONObject(j);
                    if (format.getString("formatType").equals("GLTF2")) {
                        item.setModelUrl(format.getJSONObject("root").getString("url"));
                        break;
                    }
                }
                items.add(item);
            }

            return items;

        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error while processing response: " + e);
            throw new IOException("JSON parsing error", e);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view.
        ImageView v = new ImageView(parent.getContext());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        lp.setMargins(4, 4, 4, 4);
        v.setPadding(8, 8, 8, 8);
        v.setImageResource(R.drawable.model_placeholder);
        v.setCropToPadding(true);
        v.setScaleType(ImageView.ScaleType.CENTER_CROP);
        v.setImageResource(R.drawable.model_placeholder);
        v.setLayoutParams(lp);
        return new GalleryItemHolder(v, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemId() != position) {
            ((GalleryItemHolder) holder).setItem(items.get(position));
            items.get(position).setViewHolder(holder);
            items.get(position).getThumbnailHolder().thenAccept(bitmap -> {
                ImageView imageView = (ImageView) holder.itemView;
                imageView.setImageBitmap(bitmap);
                imageView.requestLayout();
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Returns the selected item
     *
     * @return null if nothing is selected.
     */
    public GalleryItem getSelected() {
        return selected >= 0 ? items.get(selected) : null;
    }

    /**
     * Sets the selected item.  The item must already be in the list of items for this adapter,
     * otherwise it is ignored.
     */
    public void setSelected(GalleryItem item) {
        selected = item == null ? -1 : items.indexOf(item);
    }
}