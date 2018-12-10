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

import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/**
 * Methods that call the Poly API.
 */
public class PolyApi {
  private static final String TAG = "PolyAPI";

  // The API host.
  private static final String HOST = "poly.googleapis.com";

  private final String apiKey;

  public PolyApi(String apiKey) {
    this.apiKey = apiKey;
  }

  /**
   * Gets the asset with the given ID.
   *
   * @param assetId            The ID of the asset to get.
   * @param handler            The handler on which to call the listener.
   * @param completionListener The listener to call when the asset request is completed.
   */
  public void GetAsset(String assetId, Handler handler,
                              AsyncHttpRequest.CompletionListener completionListener) {
    // Build the URL to the asset. It should be something like:
    //   https://poly.googleapis.com/v1/assets/ASSET_ID_HERE?key=YOUR_API_KEY_HERE
    String url = new Uri.Builder()
            .scheme("https")
            .authority(HOST)
            .appendPath("v1")
            .appendPath("assets")
            .appendPath(assetId)
            .appendQueryParameter("key", apiKey)
            .build().toString();

    // Send an asynchronous request.
    AsyncHttpRequest request = new AsyncHttpRequest(url, handler, completionListener);
    request.send();
  }

  public  void ListAssets(String keywords, boolean curatedOnly, String category, Handler handler,
                                AsyncHttpRequest.CompletionListener completionListener) {
    // Build the URL to the asset. It should be something like:
    //   https://poly.googleapis.com/v1/assets?key=YOUR_API_KEY_HERE
    Uri.Builder urlBuilder = new Uri.Builder()
            .scheme("https")
            .authority(HOST)
            .appendPath("v1")
            .appendPath("assets")
            .appendQueryParameter("key", apiKey)
            .appendQueryParameter("curated", Boolean.toString(curatedOnly))
            .appendQueryParameter("format", "GLTF2")
            .appendQueryParameter("pageSize", "100");


    if (keywords != null && !keywords.isEmpty()) {
      urlBuilder.appendQueryParameter("keywords", keywords);
    }
    if (category != null && !category.isEmpty()) {
      urlBuilder.appendQueryParameter("category", category);
    }
    String url = urlBuilder.build().toString();
    // Send an asynchronous request.
    AsyncHttpRequest request = new AsyncHttpRequest(url, handler, completionListener);
    request.send();

  }
}
