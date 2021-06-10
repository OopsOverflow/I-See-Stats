module i_see_stats {
	requires javafx.controls;
	requires javafx.graphics;
	requires javafx.fxml;
	requires jimObjModelImporterJFX;
	
	opens main to javafx.graphics, javafx.fxml;
}