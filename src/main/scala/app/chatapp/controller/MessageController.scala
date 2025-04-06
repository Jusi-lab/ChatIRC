package app.chatapp.controller

import app.chatapp.model.{User, ChatMessage}
import javafx.fxml.FXML
import javafx.scene.control.{Button, ListView, TextField, Label}

// Контроллер для окна чата
class MessageController {

  // Список для сообщений чата
  @FXML
  protected var chatListView: ListView[ChatMessage] = new ListView[ChatMessage]()

  // Список для пользователей
  @FXML
  protected var friendsListView: ListView[User] = new ListView[User]()

  // Поле текста сообщения
  @FXML
  protected var messagesTextField: TextField = _

  // Кнопка для отправки
  @FXML
  protected var sendButton: Button = _

  // Отображения имени пользователя
  @FXML
  protected var myNameLabel: Label = _

}
