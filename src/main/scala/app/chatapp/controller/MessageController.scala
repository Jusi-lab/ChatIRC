package app.chatapp.controller

import app.chatapp.model.{User, ChatMessage}
import javafx.fxml.FXML
import javafx.scene.control.{Button, ListView, TextField, Label}
import javafx.scene.control

// Контроллер для окна чата
class MessageController {

  // Список для отображения сообщений чата
  @FXML
  protected var chatListView: ListView[ChatMessage] = new ListView[ChatMessage]()

  // Список для отображения пользователей
  @FXML
  protected var friendsListView: ListView[User] = new ListView[User]()

  // Поле для ввода текста сообщения
  @FXML
  protected var messagesTextField: TextField = _

  // Кнопка для отправки сообщения
  @FXML
  protected var sendButton: Button = _

  // Метка для отображения имени пользователя
  @FXML
  protected var myNameLabel: Label = _

  @FXML
  def initialize(): Unit = {

  }
}
