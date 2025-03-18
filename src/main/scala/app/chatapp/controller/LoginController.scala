package app.chatapp.controller

import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.{Button, PasswordField, TextField}
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

import java.io.IOException

// Контроллер для окна входа в приложение
class LoginController {

  // Поле для кнопки "Войти"
  @FXML
  protected var signInButton: Button = _

  // Поле для ввода никнейма пользователя
  @FXML
  protected var nickNameField: TextField = _

  // Поле для ввода порта
  @FXML
  protected  var portField: TextField = _

  @FXML
  def initialize(): Unit = {

  }
}
