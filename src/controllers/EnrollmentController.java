package controllers;

import dao.CourseDAO;
import dao.EnrollmentDAO;
import dao.StudentDAO;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Enrollment;

public class EnrollmentController implements Initializable {

    @FXML
    private ComboBox<Integer> studentsCombobox;

    @FXML
    private ComboBox<Integer> coursesCombobox;

    @FXML
    private DatePicker enrollmentDate;

    @FXML
    private TableView<Enrollment> table;

    @FXML
    private TableColumn<Enrollment, Integer> enrollmentIdTC;

    @FXML
    private TableColumn<Enrollment, Integer> studentIdTC;

    @FXML
    private TableColumn<Enrollment, Integer> courseIdTC;

    @FXML
    private TableColumn<Enrollment, String> enrollmentDateTC;

    private StudentDAO studentdao = new StudentDAO();
    private CourseDAO coursedao = new CourseDAO();
    private EnrollmentDAO enrollmentdao = new EnrollmentDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        enrollmentIdTC.setCellValueFactory(new PropertyValueFactory<Enrollment, Integer>("enrollmentId"));
        studentIdTC.setCellValueFactory(new PropertyValueFactory<Enrollment, Integer>("studentId"));
        courseIdTC.setCellValueFactory(new PropertyValueFactory<Enrollment, Integer>("courseId"));
        enrollmentDateTC.setCellValueFactory(new PropertyValueFactory<Enrollment, String>("enrollmentDate"));

        List<Integer> studentIds = studentdao.getAllStudentsIds();
        studentsCombobox.getItems().addAll(studentIds);

        List<Integer> courseIds = coursedao.getAllCoursesIds();
        coursesCombobox.getItems().addAll(courseIds);

        table.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        return;
                    }

                    studentsCombobox.setValue(newValue.getStudentId());
                    coursesCombobox.setValue(newValue.getCourseId());

                    if (newValue.getEnrollmentDate() != null
                            && !newValue.getEnrollmentDate().isEmpty()) {
                        enrollmentDate.setValue(LocalDate.parse(newValue.getEnrollmentDate()));
                    } else {
                        enrollmentDate.setValue(null);
                    }
                }
        );
    }

    @FXML
    private void viewHandle(ActionEvent event) {
        List<Enrollment> enrollments = enrollmentdao.findAll();
        table.getItems().setAll(enrollments);
    }

    @FXML
    private void addHandle(ActionEvent event) {
        if (!enrollmentValidator()) {
            showWarningAlert(
                    "Invalid Input",
                    "Missing Data",
                    "Please select student ID, course ID, and enrollment date."
            );
            return;
        }

        int studentId = studentsCombobox.getValue();
        int courseId = coursesCombobox.getValue();
        String date = enrollmentDate.getValue().toString();

        if (enrollmentdao.isDuplicateEnrollment(studentId, courseId)) {
            showWarningAlert(
                    "Duplicate Enrollment",
                    "Enrollment Already Exists",
                    "This student is already enrolled in this course."
            );
            return;
        }

        Enrollment enrollment = new Enrollment(studentId, courseId, date);

        boolean success = enrollmentdao.insertOne(enrollment);

        if (success) {
            clear();
            viewHandle(event);
            showInfoAlert("Success", "Enrollment added successfully.");
        } else {
            showWarningAlert(
                    "Error",
                    "Add Failed",
                    "Enrollment could not be added."
            );
        }
    }

    @FXML
    private void updateHandle(ActionEvent event) {
        Enrollment selectedEnrollment = table.getSelectionModel().getSelectedItem();

        if (selectedEnrollment == null) {
            showWarningAlert(
                    "No Selection",
                    "No Enrollment Selected",
                    "Please select an enrollment record from the table."
            );
            return;
        }

        if (!enrollmentValidator()) {
            showWarningAlert(
                    "Invalid Input",
                    "Missing Data",
                    "Please select student ID, course ID, and enrollment date."
            );
            return;
        }

        int studentId = studentsCombobox.getValue();
        int courseId = coursesCombobox.getValue();
        String date = enrollmentDate.getValue().toString();

        Enrollment enrollment = new Enrollment(
                selectedEnrollment.getEnrollmentId(),
                studentId,
                courseId,
                date
        );

        if (enrollmentdao.isDuplicateEnrollmentForUpdate(
                enrollment.getEnrollmentId(),
                enrollment.getStudentId(),
                enrollment.getCourseId())) {

            showWarningAlert(
                    "Duplicate Enrollment",
                    "Enrollment Already Exists",
                    "Another record already has the same student ID and course ID."
            );
            return;
        }

        boolean success = enrollmentdao.updateOne(enrollment);

        if (success) {
            clear();
            viewHandle(event);
            showInfoAlert("Success", "Enrollment updated successfully.");
        } else {
            showWarningAlert(
                    "Error",
                    "Update Failed",
                    "Enrollment could not be updated."
            );
        }
    }

    @FXML
    private void deleteHandle(ActionEvent event) {
        Enrollment selectedEnrollment = table.getSelectionModel().getSelectedItem();

        if (selectedEnrollment == null) {
            showWarningAlert(
                    "No Selection",
                    "No Enrollment Selected",
                    "Please select an enrollment record from the table."
            );
            return;
        }

        boolean confirmed = showConfirmationAlert(
                "Delete Confirmation",
                "Are you sure?",
                "Do you want to delete this enrollment record?"
        );

        if (confirmed) {
            boolean success = enrollmentdao.deleteOne(selectedEnrollment);

            if (success) {
                clear();
                viewHandle(event);
                showInfoAlert("Success", "Enrollment deleted successfully.");
            } else {
                showWarningAlert(
                        "Error",
                        "Delete Failed",
                        "Enrollment could not be deleted."
                );
            }
        }
    }

    @FXML
    private void clearHandle(ActionEvent event) {
        clear();
    }

    private boolean enrollmentValidator() {
        return studentsCombobox.getValue() != null
                && coursesCombobox.getValue() != null
                && enrollmentDate.getValue() != null;
    }

    private void clear() {
        studentsCombobox.setValue(null);
        coursesCombobox.setValue(null);
        enrollmentDate.setValue(null);
        table.getSelectionModel().clearSelection();
    }

    private void showWarningAlert(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmationAlert(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();

        return result.isPresent() && result.get() == ButtonType.OK;
    }
}