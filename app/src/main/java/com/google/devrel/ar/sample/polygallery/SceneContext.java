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
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.collision.Box;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.Locale;
import java.util.Objects;

/**
 * The context of a Scene.  This class handles
 * the nodes of the Sceneform scene and provides some helper functions.
 */
public class SceneContext {
  private final Context context;
  private AnchorNode anchorNode;
  private Node modelNode;
  private Node infoCard;
  private Scene scene;

  /**
   * Create a new context for the scene
   * @param context - the activity context.  This is used for loading assets.
   */
  public SceneContext(Context context) {
    this.context = context;
  }

  /**
   * Limits the modelNode size by scaling.
   * @param minSize  the min size in meters for the model.
   * @param maxSize the max size in meters for the model.
   */
  public  void limitSize(float minSize, float maxSize) {
    Box modelBox = (Box) modelNode.getCollisionShape();
    Vector3 size = modelBox.getSize();
    float maxDim = Math.max(size.x, Math.max(size.y, size.z));
    float currentScale = modelNode.getWorldScale().x;

    if (infoCard != null) {
      Box infoBox = (Box)infoCard.getCollisionShape();
      size = infoBox.getSize();
      float infoMaxDim = Math.max(size.x, Math.max(size.y, size.z));
      if (infoMaxDim > maxDim) {
        maxDim = infoMaxDim;
        currentScale = infoCard.getWorldScale().x;
      }
    }

    // Assume all dimensions have the same scale.
    float currentSize = maxDim * currentScale;
    float newScale = currentScale;
    if (currentSize < minSize) {
      newScale = newScale * (minSize/currentSize);
    } else if (currentSize > maxSize) {
      newScale = newScale * (maxSize/currentSize);
    }
    modelNode.setWorldScale(new Vector3(newScale, newScale, newScale));

  }

  /**
   * Sets the scene object for this context.  This simplifies the differences between an AR based
   * scene and non-AR based scene.
   * @param scene
   */
  public void setScene(Scene scene) {
    this.scene = scene;
  }

  /**
   * Returns the scene's camera.
   */
  public Camera getCamera() {
    return scene != null ? scene.getCamera() : null;
  }

  /**
   * Sets the min and max scale values based on size.
   * The API for TransformableNode allows limiting the scale of the node by value.
   * This sets those values based on the size of the renderable.
   * @param node the TransformableNode.
   * @param minSize the min size in meters to allow when scaling the node.
   * @param maxSize the max size in meters to allow when scaling the node.
   */
  public static void setScaleRange(TransformableNode node, float minSize, float maxSize) {
    // Set the min/max scale based on size not factors
    Box box = (Box) node.getRenderable().getCollisionShape();
    Vector3 size = Objects.requireNonNull(box).getSize();
    // use the largest dimension
    float maxDim = Math.max(size.x, Math.max(size.y, size.z));

    float minScale = node.getScaleController().getMinScale();
    float maxScale = node.getScaleController().getMaxScale();
    // min is 1cm
    minScale = Math.min(minSize / maxDim, minScale);
    /// max is 3m
    maxScale = Math.max(maxSize / maxDim, maxScale);

    node.getScaleController().setMinScale(minScale);
    node.getScaleController().setMaxScale(maxScale);
  }

  /**
   * Resets the context by removing the nodes from the scene.
   */
  public void resetContext() {
    if (modelNode != null) {
      modelNode.setParent(null);
      modelNode = null;
    }

    if (anchorNode != null) {
      anchorNode.getAnchor().detach();
      anchorNode.setParent(null);
      anchorNode = null;
    }
    if (infoCard != null) {
      infoCard.setParent(null);
      infoCard = null;
    }
  }

  /**
   * Returns true if the the model node is available.
   */
  public boolean hasModelNode() {
    return modelNode != null;
  }

  /**
   * Generates a string for describing the node's scale and rotation.
   * @return string for model info, or null if the node is not available.
   */
  public String generateNodeInfo() {
    if (scene == null) {
      return null;
    }
    Camera camera = scene.getCamera();
    String msg = null;
    if (modelNode != null && modelNode.getRenderable() != null) {
      Vector3 scale = modelNode.getLocalScale();
      Vector3 size = ((Box) modelNode.getCollisionShape()).getSize();
      size.x *= scale.x;
      size.y *= scale.y;
      size.z *= scale.z;
      Vector3 dir = Vector3.subtract(modelNode.getForward(), camera.getForward());
      msg = String.format(Locale.getDefault(), "%s\n%s\n%s",
              String.format(Locale.getDefault(), "scale: (%.02f, %.02f, %.02f)",
                      scale.x,
                      scale.y,
                      scale.z),
              String.format(Locale.getDefault(), "size: (%.02f, %.02f, %.02f)",
                      size.x,
                      size.y,
                      size.z),
              String.format(Locale.getDefault(), "dir: (%.02f, %.02f, %.02f)",
                      dir.x,
                      dir.y,
                      dir.z)
      );

    }
    return msg;
  }

  /**
   * Rotates the info card in the  scene to face the camera.
   */
  public void rotateInfoCardToCamera() {
    if (scene == null) {
      return;
    }
    Camera camera = scene.getCamera();
    // Rotate the card to look at the camera.
    if (infoCard != null) {
      Vector3 cameraPosition = camera.getWorldPosition();
      Vector3 cardPosition = infoCard.getWorldPosition();
      Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);
      Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
      infoCard.setWorldRotation(lookRotation);
    }
  }

  /**
   * Resets the model node and positions it.
   * @param position the world position of the node.
   */
  public void resetModelNode(Vector3 position) {
    if (modelNode != null) {
      modelNode.setParent(null);
    }
    //Create the model node each time so scale and rotation is reset.
    modelNode = new Node();
    modelNode.setParent(scene);
    modelNode.setWorldPosition(position);
    modelNode.setWorldRotation(Quaternion.identity());
    modelNode.setWorldScale(Vector3.one());
  }

  /**
   * Attaches the info card displaying the information for the given item to the
   * model node.
   * @param selectedItem the Poly gallery item describing the renderable.
   */
  public void attachInfoCardNode(GalleryItem selectedItem) {
    if (infoCard == null) {
      infoCard = new Node();
      ViewRenderable.builder()
              .setView(context, R.layout.model_info)
              .build()
              .thenAccept(
                      (renderable) -> {
                        infoCard.setRenderable(renderable);
                        setModelLabel(renderable, selectedItem);
                      })
              .exceptionally(
                      (throwable) -> {
                        throw new AssertionError(
                                "Could not load plane card view.", throwable);
                      });
    } else {
      setModelLabel((ViewRenderable) infoCard.getRenderable(), selectedItem);
    }
    infoCard.setParent(modelNode);
    float height = .5f;
    if (modelNode.getRenderable() instanceof ModelRenderable) {
      height = getRenderableHeight((ModelRenderable) modelNode.getRenderable());
    }
    infoCard.setLocalPosition(new Vector3(0, height, 0));
  }

  /**
   * Returns the height of the renderable in local scale in meters.
   */
  private float getRenderableHeight(ModelRenderable renderable) {
    Box box = (Box) renderable.getCollisionShape();
    return Objects.requireNonNull(box).getCenter().y + box.getExtents().y;
  }

  /**
   * Sets the information from the selected item on the view renderable which
   * is expected to have a single text vew.
   */
  private void setModelLabel(@NonNull ViewRenderable viewRenderable,
                             @NonNull GalleryItem selectedItem) {
    TextView textView = (TextView) viewRenderable.getView();
    textView.setText(String.format("%s by %s\n%s",
            selectedItem.getDisplayName(), selectedItem.getAuthor(), selectedItem.getLicense()));
  }

  /**
   * Sets the renderable on the model node and repositions the info card node accordingly.
   */
  public void setModelRenderable(ModelRenderable renderable) {
    modelNode.setRenderable(renderable);
    infoCard.setLocalPosition(new Vector3(0, getRenderableHeight(renderable), 0));
  }

  /**
   * Resets the anchor node used in AR mode.
   * This detaches any existing anchor and sets the anchor to the value passed in and
   * sets the parent as the scene.
   */
  public void resetAnchorNode(Anchor anchor) {
    // Clean up old anchor
    if (anchorNode != null && anchorNode.getAnchor() != null) {
      anchorNode.getAnchor().detach();
    } else {
      anchorNode = new AnchorNode();
    }
    anchorNode.setParent(scene);
    anchorNode.setAnchor(anchor);
  }

  /**
   * Connects the node to the anchor node.
   */
  public void attachModelNodeToAnchorNode(TransformableNode node) {
    if (modelNode != null) {
      modelNode.setParent(null);
    }

    modelNode = node;
    modelNode.setParent(anchorNode);
  }
}
