module com.fourlab.demo1 {
  requires javafx.controls;
  requires javafx.fxml;


  opens com.fourlab.demo1 to javafx.fxml;
  exports com.fourlab.demo1;
}