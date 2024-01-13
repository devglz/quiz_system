package com.goncalves.project.controller;

import com.auth.Auth;
import com.auth.SHA256Hashing;
import com.conf.GlobalSettings;
import com.goncalves.project.model.User;
import com.goncalves.project.service.UserService;
import com.goncalves.project.util.FormUtils;
import com.goncalves.project.util.FormValidation;
import com.goncalves.project.util.alertMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class loginController implements Initializable {

    @FXML
    private AnchorPane login_form;
    @FXML
    private TextField login_username;
    @FXML
    private PasswordField login_password;
    @FXML
    private TextField login_showPassword;
    @FXML
    private CheckBox login_selectShowPassword;
    @FXML
    private Button login_btn;
    @FXML
    private Button login_createAccount;
    @FXML
    private Hyperlink login_forgotPassword;

    @FXML
    private AnchorPane signup_form;
    @FXML
    private TextField signup_email;
    @FXML
    private TextField signup_username;
    @FXML
    private PasswordField signup_password;
    @FXML
    private PasswordField signup_cPassword;
    @FXML
    private ComboBox<String> signup_selectQuestion;
    @FXML
    private TextField signup_answer;
    @FXML
    private Button signup_loginAccount;

    @FXML
    private AnchorPane forgot_form;
    @FXML
    private TextField forgot_username;
    @FXML
    private ComboBox<String> forgot_selectQuestion;
    @FXML
    private TextField forgot_answer;
    @FXML
    private Button forgot_backBtn;

    @FXML
    private AnchorPane changePass_form;
    @FXML
    private PasswordField changePass_password;
    @FXML
    private PasswordField changePass_cPassword;
    @FXML
    private Button changePass_backBtn;
    @FXML
    private ComboBox<String> comboBox;

    private final UserService userService = UserService.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize combo boxes
        List<String> questionList = Arrays.asList("Pytanie 1", "Pytanie 2");
        FormUtils.fillComboBox(signup_selectQuestion, questionList.toArray(new String[0]));
        FormUtils.fillComboBox(forgot_selectQuestion, questionList.toArray(new String[0]));
    }


    public void login() throws IOException, SQLException {
        alertMessage alert = new alertMessage();

        String password = login_selectShowPassword.isSelected() ? login_showPassword.getText() : login_password.getText();

        if (FormValidation.isEmpty(login_username.getText()) || FormValidation.isEmpty(password)) {
            alert.errorMessage("Wypełnij wszytski pola");
        } else {
            Auth loggedUser = new Auth();
            boolean isOK = false;
            try {
                isOK = loggedUser.login1step(login_username.getText(), SHA256Hashing.hashStringToSHA256(password), GlobalSettings.socket);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (isOK == true) {
                alert.successMessage("Successfully Login!");

                // Load home form
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/views/homeForm.fxml"));
                Parent mainForm = loader.load();
                Scene mainFormScene = new Scene(mainForm);

                // Get the Stage from the current scene
                Stage window = (Stage) login_btn.getScene().getWindow();

                // Set the scene to the stage
                window.setScene(mainFormScene);

                // Display the stage
                window.show();

            } else {
                // ELSE, THEN ERROR MESSAGE WILL APPEAR
                alert.errorMessage("Incorrect Username/Password");
            }
        }
    }

    @FXML
    public void showPassword() {
        if (login_selectShowPassword.isSelected()) {
            login_showPassword.setText(login_password.getText());
            login_showPassword.setVisible(true);
            login_password.setVisible(false);
        } else {
            login_password.setText(login_showPassword.getText());
            login_showPassword.setVisible(false);
            login_password.setVisible(true);
        }
    }

    @FXML
    public void register() throws SQLException {
        alertMessage alert = new alertMessage();
        String username = signup_username.getText();
        String password = signup_password.getText();
        String email = signup_email.getText();
        String question = signup_selectQuestion.getSelectionModel().getSelectedItem();
        String answer = signup_answer.getText();

        if (FormValidation.isEmpty(email)
                || FormValidation.isEmpty(username)
                || FormValidation.isEmpty(password)
                || FormValidation.isEmpty(signup_cPassword.getText())
                || FormValidation.isComboBoxSelected(signup_selectQuestion)
                || FormValidation.isEmpty(answer)) {
            alert.errorMessage("All fields are necessary to be filled");
        } else if (FormValidation.isPasswordMatch(password, signup_cPassword.getText())) {
            alert.errorMessage("Password does not match");
        } else if (FormValidation.isPasswordLengthValid(password)) {
            alert.errorMessage("Invalid Password, at least 8 characters needed");
        } else {
            User newUser = new User(email, username, password, question, answer);
            boolean registerSuccess = userService.registerUser(newUser);

            if (registerSuccess) {
                alert.successMessage("Registered Successfully!");

                // TO CLEAR ALL FIELDS OF REGISTRATION FORM
                FormUtils.clearTextField(signup_email);
                FormUtils.clearTextField(signup_username);
                FormUtils.clearPasswordField(signup_password);
                FormUtils.clearPasswordField(signup_cPassword);
                FormUtils.clearComboBox(signup_selectQuestion);
                FormUtils.clearTextField(signup_answer);

                FormUtils.hide(signup_form);
                FormUtils.show(login_form);
            }
        }
    }

    @FXML
    public void forgotPassword() {
        alertMessage alert = new alertMessage();

        if (FormValidation.isEmpty(forgot_username.getText())
                || FormValidation.isComboBoxSelected(forgot_selectQuestion)
                || FormValidation.isEmpty(forgot_answer.getText())) {
            alert.errorMessage("Please fill all blank fields");
        } else {
            User forgotPasswordSuccess = userService.forgotPassword(forgot_username.getText(),
                    forgot_selectQuestion.getSelectionModel().getSelectedItem(),
                    forgot_answer.getText());

            if (forgotPasswordSuccess != null) {
                // PROCEED TO CHANGE PASSWORD
                FormUtils.hide(signup_form);
                FormUtils.hide(login_form);
                FormUtils.hide(forgot_form);
                FormUtils.show(changePass_form);
            } else {
                alert.errorMessage("Incorrect information");
            }
        }
    }

    @FXML
    public void changePassword() {
        alertMessage alert = new alertMessage();

        if (FormValidation.isEmpty(changePass_password.getText()) || FormValidation.isEmpty(changePass_cPassword.getText())) {
            alert.errorMessage("Please fill all blank fields");
        } else if (FormValidation.isPasswordMatch(changePass_password.getText(), changePass_cPassword.getText())) {
            alert.errorMessage("Password does not match");
        } else if (FormValidation.isPasswordLengthValid(changePass_password.getText())) {
            alert.errorMessage("Invalid Password, at least 8 characters needed");
        } else {
            boolean changePasswordSuccess = userService.changePassword(forgot_username.getText(), changePass_password.getText());

            if (changePasswordSuccess) {
                alert.successMessage("Successfully changed Password");

                // LOGIN FORM WILL APPEAR
                FormUtils.hide(signup_form);
                FormUtils.show(login_form);
                FormUtils.hide(forgot_form);
                FormUtils.hide(changePass_form);

                FormUtils.clearTextField(login_username);
                FormUtils.clearPasswordField(login_password);
                FormUtils.clearTextField(login_showPassword);

                login_password.setVisible(true);
                login_showPassword.setVisible(false);
                login_selectShowPassword.setSelected(false);

                FormUtils.clearPasswordField(changePass_password);
                FormUtils.clearPasswordField(changePass_cPassword);
            }
        }
    }

    @FXML
    public void switchForm(ActionEvent event) {
        // THE REGISTRATION FORM WILL BE VISIBLE
        if (event.getSource() == signup_loginAccount || event.getSource() == forgot_backBtn) {
            signup_form.setVisible(false);
            login_form.setVisible(true);
            forgot_form.setVisible(false);
            changePass_form.setVisible(false);
        } else if (event.getSource() == login_createAccount) { // THE LOGIN FORM WILL BE VISIBLE
            signup_form.setVisible(true);
            login_form.setVisible(false);
            forgot_form.setVisible(false);
            changePass_form.setVisible(false);
        } else if (event.getSource() == login_forgotPassword) {
            signup_form.setVisible(false);
            login_form.setVisible(false);
            forgot_form.setVisible(true);
            changePass_form.setVisible(false);
            // TO SHOW THE DATA TO OUR COMBOBOX
            forgotListQuestion();
        } else if (event.getSource() == changePass_backBtn) {
            signup_form.setVisible(false);
            login_form.setVisible(false);
            forgot_form.setVisible(true);
            changePass_form.setVisible(false);
        }
    }

    private final String[] questionList = {"What is your favorite food?", "What is your favorite color?",
            "What is the name of your pet?", "What is your most favorite sport?"};

    public void questions() {
        FormUtils.fillComboBox(signup_selectQuestion, questionList);
    }

    public void forgotListQuestion() {
        FormUtils.fillComboBox(forgot_selectQuestion, questionList);
    }
}
