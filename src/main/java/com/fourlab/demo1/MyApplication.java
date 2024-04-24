package com.fourlab.demo1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Camera;
import javafx.scene.DepthTest;
import javafx.scene.SceneAntialiasing;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.util.Objects;

public class MyApplication extends Application {

  static final private int DEFAULT_SCREEN_WIDTH = 640;
  static final private int DEFAULT_SCREEN_HEIGHT = 480;

  @Override
  public void start(Stage stage) {
    var subScene = makeSubScene(null, null);
    var cock = (Group) subScene.getRoot().getChildrenUnmodifiable().getFirst();
    var balls = new Box[] {
        (Box) cock.getChildren().get(0),
        (Box) cock.getChildren().get(1),
        (Box) cock.getChildren().get(3),
    };
    var light = (PointLight) subScene.getRoot().getChildrenUnmodifiable().getLast();
    var controlPanel = makeControlPanel(light, balls);
    var root = new VBox(controlPanel, subScene);
    root.setPadding(new Insets(10));
    root.setSpacing(10);

    var scene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
    stage.setResizable(false);
    stage.setTitle("3D example");
    stage.setScene(scene);
    stage.show();
  }

  private static VBox makeControlPanel(PointLight light, Box[] balls) {
    var fixedCol = new ColumnConstraints();
    fixedCol.setMinWidth(200);
    var growCol = new ColumnConstraints();
    growCol.setHgrow(Priority.ALWAYS);

    // Attenuation
    var attenuationLabel = new Label("Attenuation");
    attenuationLabel.setAlignment(Pos.CENTER);
    var cAttSlider = new Slider(0.0, 1.0, 1.0);
    cAttSlider.valueProperty().bindBidirectional(light.constantAttenuationProperty());
    var cAttLabel = new Label("Constant attenuation [%.2f-%.2f]".formatted(cAttSlider.getMin(), cAttSlider.getMax()));
    var lAttSlider = new Slider(0.0, 0.5, 0.0);
    lAttSlider.valueProperty().bindBidirectional(light.linearAttenuationProperty());
    var lAttLabel = new Label("Linear attenuation [%.2f-%.2f]".formatted(lAttSlider.getMin(), lAttSlider.getMax()));
    var qAttSlider = new Slider(0.0, 0.25, 0.0);
    qAttSlider.valueProperty().bindBidirectional(light.quadraticAttenuationProperty());
    var qAttLabel = new Label("Quadratic attenuation [%.2f-%.2f]".formatted(qAttSlider.getMin(), qAttSlider.getMax()));
    var attenuation = new GridPane();
    attenuation.getColumnConstraints().addAll(fixedCol, growCol);
    attenuation.setHgap(10);
    attenuation.setVgap(3);
    attenuation.add(cAttLabel, 0, 0);
    attenuation.add(lAttLabel, 0, 1);
    attenuation.add(qAttLabel, 0, 2);
    attenuation.add(cAttSlider, 1, 0);
    attenuation.add(lAttSlider, 1, 1);
    attenuation.add(qAttSlider, 1, 2);

    var spacing1 = new Region();
    spacing1.setPrefHeight(10);

    // Texture mix
    var mixLabel = new Label("Attenuation");
    mixLabel.setAlignment(Pos.CENTER);
    var cmMixSlider = new Slider(0.0, 1.0, 0.5);
    var cmMixLabel = new Label("Mix [color <-> material]");
    var cmnMixSlider = new Slider(0.0, 1.0, 0.5);
    var cmnMixLabel = new Label("Mix [color/material <-> number]");
    var mix = new GridPane();
    mix.getColumnConstraints().addAll(fixedCol, growCol);
    mix.setHgap(10);
    mix.setVgap(3);
    mix.add(cmMixLabel, 0, 0);
    mix.add(cmnMixLabel, 0, 1);
    mix.add(cmMixSlider, 1, 0);
    mix.add(cmnMixSlider, 1, 1);

    var spacing2 = new Region();
    spacing2.setPrefHeight(5);

    Button applyMixButton = new Button("Apply mix");
    applyMixButton.setOnAction(event -> {
      for (var ball: balls) {
        var matData = (MaterialData) ball.getUserData();
        var mat = (PhongMaterial) ball.getMaterial();
        mat.setDiffuseMap(
            mixTextures(
                matData.number,
                mixTextures(matData.color, matData.material, cmMixSlider.getValue()),
                cmnMixSlider.getValue()
            )
        );
      }
    });
    applyMixButton.fire();
    var applyMixButtonWrapper = new HBox(applyMixButton);
    applyMixButtonWrapper.setAlignment(Pos.TOP_RIGHT);

    var controlPanel = new VBox(attenuationLabel, attenuation, spacing1, mixLabel, mix, spacing2, applyMixButtonWrapper);
    controlPanel.setPadding(new Insets(10));
    controlPanel.setBorder(Border.stroke(Color.BLACK));

    return controlPanel;
  }

  private static Group getDefaultScene() {
    var matData1 = new MaterialData();
    matData1.color = Color.RED;
    matData1.material = new Image(Objects.requireNonNull(MyApplication.class.getResourceAsStream("/ice.jpg")));
    matData1.number = new Image(Objects.requireNonNull(MyApplication.class.getResourceAsStream("/2.png")));
    var matData2 = new MaterialData();
    matData2.color = Color.GREEN;
    matData2.material = new Image(Objects.requireNonNull(MyApplication.class.getResourceAsStream("/metal.jpg")));
    matData2.number = new Image(Objects.requireNonNull(MyApplication.class.getResourceAsStream("/1.png")));
    var matData3 = new MaterialData();
    matData3.color = Color.BLUE;
    matData3.material = new Image(Objects.requireNonNull(MyApplication.class.getResourceAsStream("/wood.png")));
    matData3.number = new Image(Objects.requireNonNull(MyApplication.class.getResourceAsStream("/3.png")));
    var mat1 = new PhongMaterial(); var mat2 = new PhongMaterial(); var mat3 = new PhongMaterial();
    var b1 = makeBox(-1, 0, 0, mat1); b1.setUserData(matData1);
    var b2_1 = makeBox(0, 0, 0, mat2); b2_1.setUserData(matData2);
    var b2_2 = makeBox(0, -1, 0, mat2); b2_2.setUserData(matData2);
    var b3 = makeBox(1, 0, 0, mat3); b3.setUserData(matData3);
    var light = new PointLight(Color.WHITE);
    light.getTransforms().setAll(new Translate(-5, -5, -3));
    return new Group(new Group(b1, b2_1, b2_2, b3), light);
  }

  static private Camera getDefaultCamera() {
    var camera = new PerspectiveCamera(true);
    var rot = getRotationFromXyz(0, 0, 0);
    camera.getTransforms().addAll(new Translate(-2, -1, -5), rot[0], rot[1], rot[2]);
    camera.setFieldOfView(75.0);
    camera.setVerticalFieldOfView(true);
    camera.setDepthTest(DepthTest.ENABLE);
    return camera;
  }

  private SubScene makeSubScene(Group _tree, Camera _camera) {
    var tree = _tree == null ? getDefaultScene() : _tree;
    var camera = _camera == null ? getDefaultCamera() : _camera;
    var subScene = new SubScene(tree, DEFAULT_SCREEN_WIDTH, DEFAULT_SCREEN_HEIGHT, true, SceneAntialiasing.BALANCED);
    subScene.setCamera(camera);
    subScene.setFill(Color.DARKGRAY);
    subScene.setUserData(new TreeData());
    subScene.setOnMousePressed(event -> {
      if (event.isPrimaryButtonDown()) {
        var treeData = (TreeData) subScene.getUserData();
        treeData.lastMousePos = new Double[] { event.getSceneX(), event.getSceneY() };
      }
    });
    subScene.setOnMouseDragged(event -> {
      if (event.isPrimaryButtonDown()) {
        var group = subScene.getRoot().getChildrenUnmodifiable().getFirst();
        var treeData = (TreeData) subScene.getUserData();
        treeData.rotation[1] -= (event.getSceneX() - treeData.lastMousePos[0]) * 0.5;
        treeData.rotation[0] += (event.getSceneY() - treeData.lastMousePos[1]) * 0.5;
        treeData.lastMousePos = new Double[] { event.getSceneX(), event.getSceneY() };
        var rot = getRotationFromXyz(treeData.rotation[0], treeData.rotation[1], 0);
        group.getTransforms().setAll(rot[0], rot[1]);
      }
    });
    return subScene;
  }

  private static Box makeBox(double x, double y, double z, Material mat) {
    var box = new Box(1, 1, 1);
    box.setTranslateX(x); box.setTranslateY(y); box.setTranslateZ(z);
    box.setMaterial(mat);
    box.setCullFace(CullFace.BACK);
    box.setDrawMode(DrawMode.FILL);
    return box;
  }

  static private Rotate[] getRotationFromXyz(double x, double y, double z) {
    return new Rotate[] {
        new Rotate(x, Rotate.X_AXIS),
        new Rotate(y, Rotate.Y_AXIS),
        new Rotate(z, Rotate.Z_AXIS)
    };
  }

  static private Image mixTextures(Image texture1, Image texture2, double mix) {
    int width = (int) texture1.getWidth();
    int height = (int) texture1.getHeight();
    WritableImage writableImage = new WritableImage(width, height);
    PixelReader pixelReader1 = texture1.getPixelReader();
    PixelReader pixelReader2 = texture2.getPixelReader();
    PixelWriter pixelWriter = writableImage.getPixelWriter();

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        Color color1 = pixelReader1.getColor(x, y);
        Color color2 = pixelReader2.getColor(x, y);
        if (color1.getOpacity() < 1.0) {
          double r = color2.getRed();
          double g = color2.getGreen();
          double b = color2.getBlue();
          pixelWriter.setColor(x, y, Color.color(r, g, b));
        } else if (color2.getOpacity() < 1.0) {
          double r = color1.getRed();
          double g = color1.getGreen();
          double b = color1.getBlue();
          pixelWriter.setColor(x, y, Color.color(r, g, b));
        } else {
          double r = color1.getRed() * mix + color2.getRed() * (1 - mix);
          double g = color1.getGreen() * mix + color2.getGreen() * (1 - mix);
          double b = color1.getBlue() * mix + color2.getBlue() * (1 - mix);
          pixelWriter.setColor(x, y, Color.color(r, g, b));
        }
      }
    }

    return writableImage;
  }

  static private Image mixTextures(Color color, Image texture, double mix) {
    int width = (int) texture.getWidth();
    int height = (int) texture.getHeight();
    WritableImage writableImage = new WritableImage(width, height);
    PixelReader pixelReader = texture.getPixelReader();
    PixelWriter pixelWriter = writableImage.getPixelWriter();

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        Color texColor = pixelReader.getColor(x, y);
        pixelWriter.setColor(x, y, color);
        if (texColor.getOpacity() >= 1.0) {
          double r = texColor.getRed() * mix + color.getRed() * (1 - mix);
          double g = texColor.getGreen() * mix + color.getGreen() * (1 - mix);
          double b = texColor.getBlue() * mix + color.getBlue() * (1 - mix);
          pixelWriter.setColor(x, y, Color.color(r, g, b));
        }
      }
    }

    return writableImage;
  }

  public static void main(String[] args) {
    launch();
  }
}

class TreeData {

  public Double[] rotation = new Double[] { 0.0, 0.0 };
  public Double[] lastMousePos = new Double[] { 0.0, 0.0 };

}

class MaterialData {

  public Image material;
  public Image number;
  public Color color;

}