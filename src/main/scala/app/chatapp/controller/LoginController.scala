package app.chatapp.controller

import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.{Button, PasswordField, TextField}

// Контроллер для окна входа в приложение
class LoginController {

  // Поле для кнопки Логин
  @FXML
  protected var signInButton: Button = _

  // Поле для ввода никнейма пользователя
  @FXML
  protected var nickNameField: TextField = _


}
