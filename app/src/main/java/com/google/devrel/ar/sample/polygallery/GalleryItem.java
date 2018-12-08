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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.concurrent.CompletableFuture;

public class GalleryItem {
  private static final String TAG = "GalleryItem";

  // Used to identify the model.  For example it is used as the sceneform registryId.
  private final String key;

  // Fields from the Poly asset data.
  private String displayName;
  private String authorName;
  private String license;
  private String description;
  private String thumbnail;
  private String modelUrl;

  // A completableFuture to hold the thumbnail bitmap.  It is loaded asynchronously, thus the
  // future.
  private CompletableFuture<Bitmap> thumbnailHolder;

  // Reference to the viewHolder holding this item, can be null.
  private RecyclerView.ViewHolder viewHolder;

  // Future used when loading the model.
  private CompletableFuture<ModelRenderable> renderableHolder;

  /**
   * Constructor.
   *
   * @param key a string used to identify this asset.
   */
  public GalleryItem(String key) {
    this.key = key;
  }

  /**
   * The display name of the asset.
   */
  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Sets the author name and the license of the asset.
   */
  public void setAuthorInfo(String authorName, String license) {
    this.authorName = authorName;
    this.license = license;
  }

  public String getAuthor() {
    return authorName;
  }

  public String getLicense() {
    return license;
  }

  /**
   * The description of the asset, if any.
   */
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * The URL to the thumbnail for this asset.
   */
  public String getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(String thumbnail) {
    this.thumbnail = thumbnail;
  }

  /**
   * Starts the asynchronous loading of the thumbnail bitmap.
   * The handler is used to process the request. The results of loading are accessed via
   *
   * @param handler
   * @see #getThumbnailHolder()
   */
  public void loadThumbnail(Handler handler) {
    // Send an asynchronous request.
    if (thumbnailHolder == null) {
      thumbnailHolder = new CompletableFuture<>();
      AsyncHttpRequest request = new AsyncHttpRequest(getThumbnail(),
              handler, new AsyncHttpRequest.CompletionListener() {

        @Override
        public void onHttpRequestSuccess(byte[] responseBody) {
          thumbnailHolder.complete(BitmapFactory.decodeByteArray(responseBody,
                  0, responseBody.length));
        }

        @Override
        public void onHttpRequestFailure(int statusCode, String message, Exception exception) {
          Log.e(TAG, "Cannot load thumbnail: " + statusCode + " " + message, exception);
          thumbnailHolder.completeExceptionally(exception);
        }
      });
      request.send();
    }
  }

  /**
   * Sets the model URL.  This is loaded by calling {link #getRenderableHolder()}
   *
   * @param modelUrl
   */
  public void setModelUrl(String modelUrl) {
    this.modelUrl = modelUrl;
  }

  /**
   * Returns the currently associated ViewHolder for this item.
   *
   * @return null if no view holder.
   */
  public RecyclerView.ViewHolder getViewHolder() {
    return viewHolder;
  }

  public void setViewHolder(RecyclerView.ViewHolder viewHolder) {
    this.viewHolder = viewHolder;
  }

  /**
   * Returns the future for the ModelRenderable.  This starts the loading process if not
   * already started.
   */
  public CompletableFuture<ModelRenderable> getRenderableHolder() {

    if (renderableHolder == null) {
      Context context = viewHolder.itemView.getContext();
      RenderableSource source = RenderableSource.builder().setSource(context,
              Uri.parse(modelUrl), RenderableSource.SourceType.GLTF2)
              .setRecenterMode(RenderableSource.RecenterMode.ROOT)
              .build();


      renderableHolder = ModelRenderable.builder().setRegistryId(key)
              .setSource(context, source)
              .build();
    }
    return renderableHolder;
  }

  /**
   * Returns the future for the thumbnail bitmap.  The loading is done via {link #loadThumbnail()}.
   *
   * @return the future, or null if loadThumbnail has not been called.
   */
  public CompletableFuture<Bitmap> getThumbnailHolder() {
    return thumbnailHolder;
  }
}