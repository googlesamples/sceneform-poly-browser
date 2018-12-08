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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.SceneView;

/**
 * Fragment for holding a non-AR Sceneform SceneView.
 */
public class SceneformFragment extends android.support.v4.app.Fragment {
    private final ViewTreeObserver.OnWindowFocusChangeListener focusHandler = this::onWindowFocusChanged;

    public SceneformFragment() {
    super();
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.sceneform_fragment, container, false);
    view.getViewTreeObserver().addOnWindowFocusChangeListener(focusHandler);

    return view;
  }

    @Override
    public void onDestroyView() {
      getView().getViewTreeObserver().removeOnWindowFocusChangeListener(focusHandler);
      super.onDestroyView();

    }

    @Override
  public void onResume() {
    super.onResume();
    /*
     *
     *  N O T E:  It is important to call resume and pause on the SceneView.  If you don't, then
     *  nothing will render.
     */
    try {
      getSceneView().resume();
      // This will never happen - but ARCore functionality is still not completely refactored out.
    } catch (CameraNotAvailableException e) {
      throw new RuntimeException("ARException in non-AR mode?", e);
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    getSceneView().pause();
  }

  public void onWindowFocusChanged(boolean hasFocus) {
    if (hasFocus && getActivity() != null) {
      // Standard Android full-screen functionality.
      getActivity().getWindow().getDecorView().setSystemUiVisibility(
              View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                      | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                      | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                      | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                      | View.SYSTEM_UI_FLAG_FULLSCREEN
                      | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
      getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
  }

  public SceneView getSceneView() {
    return getActivity() == null ? null : getActivity().findViewById(R.id.scene_view);
  }
}
