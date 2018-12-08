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

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * ViewHolder for showing a thumbnail of a model from Poly.
 */
public class GalleryItemHolder extends RecyclerView.ViewHolder {
  private static final int SELECTED_COLOR = Color.WHITE;
  private static final int DESELECTED_COLOR = Color.DKGRAY;
  private final GalleryAdapter adapter;
  private GalleryItem item;

  /**
   * Constructs a ViewHolder.
   *
   * @param itemView       the view for the item.
   * @param galleryAdapter the adapter used to create this holder.
   */
  public GalleryItemHolder(View itemView, GalleryAdapter galleryAdapter) {
    super(itemView);
    this.adapter = galleryAdapter;
  }

  /**
   * Sets the item for this view holder.  The view is updated to
   * handle selection and selection state.
   *
   * @param item - from the adapter associated with this view holder.
   */
  public void setItem(GalleryItem item) {
    this.item = item;
    itemView.setOnClickListener(this::onClick);
    if (!item.equals(adapter.getSelected())) {
      itemView.setBackgroundColor(DESELECTED_COLOR);
      itemView.setSelected(false);
    } else {
      itemView.setSelected(true);
      itemView.setBackgroundColor(SELECTED_COLOR);
    }
  }

  /**
   * Handles the click to select an item.
   */
  private void onClick(View view) {
    GalleryItem selected = adapter.getSelected();
    if (!item.equals(selected)) {
      if (selected != null) {
        selected.getViewHolder().itemView.setBackgroundColor(DESELECTED_COLOR);
      }
      adapter.setSelected(item);
      itemView.setSelected(true);
      itemView.setBackgroundColor(SELECTED_COLOR);
    } else {
      adapter.setSelected(null);
      itemView.setSelected(false);
      itemView.setBackgroundColor(DESELECTED_COLOR);
    }
  }


}
