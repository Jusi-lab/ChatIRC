package app.chatapp.service

import app.chatapp.controller.MessageController
import app.chatapp.actor.UserActor.{PostMessage, PostMessageToGeneral}
import app.chatapp.model.{User, ChatMessage}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.control.{ListCell, ListView}
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.text.Font
import javafx.application.Platform

import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.ResourceBundle

// Сервис для обработки логики чата и работы с UI
class MessageService extends MessageController with Initializable {

    private var myself: User = _  // Текущий пользователь
    private var currentFriend: User = _  // пОльзователь с которым ведется чат
    private val generalRoom: User = new User(-1, "Group chat", null)  // Общий чат

    // Инициализация компонента и обработка UI-элементов
    override def initialize(location: URL, resources: ResourceBundle): Unit = {
        sendButton.setVisible(false)
        messagesTextField.setVisible(false)

        // Настройка отображения списка друзей в ListView
        friendsListView.setCellFactory((_: ListView[User]) => new ListCell[User]() {
            setStyle("-fx-control-inner-background: #000000; -fx-text-fill: #FFFFFF; -fx-selection-bar-non-focused: #2F70AF;")
            setFont(Font.font("Corbel Light", 20))
            override def updateItem(item: User, empty: Boolean): Unit = {
                super.updateItem(item, empty)
                if (empty || item == null) {
                    setText(null)
                } else {
                    setText(item.getNickName)
                }
            }
        })

        // Обработчик выбора друга из списка
        friendsListView.getSelectionModel.selectedItemProperty().addListener(new ChangeListener[User] {
            override def changed(observable: ObservableValue[_ <: User], oldValue: User, newValue: User): Unit = {
                currentFriend = friendsListView.getSelectionModel.getSelectedItem
                // Если выбран скрываем UI элементы чата
                if (currentFriend.equals(myself)){
                    sendButton.setVisible(false)
                    messagesTextField.setVisible(false)
                    chatListView.getItems.clear()
                } else {
                    // Иначе показываем элементы и загружаем историю сообщений
                    sendButton.setVisible(true)
                    messagesTextField.setVisible(true)
                    chatListView.getItems.clear()
                    myself.getListMessagesWithFriends(currentFriend).foreach(msg => chatListView.getItems.add(msg))
                }
            }
        })

        // Настройка отображения сообщений в ListView чата
        chatListView.setCellFactory((_: ListView[ChatMessage]) => new ListCell[ChatMessage]() {
            override def updateItem(item: ChatMessage, empty: Boolean): Unit = {
                super.updateItem(item, empty)
                setStyle("-fx-control-inner-background: #000000; -fx-text-fill: #FFFFFF;")
                setFont(Font.font("Impact", 16))
                if (empty || item == null) {
                    setText(null)
                } else {
                    // Отображаем имя отправителя, текст сообщения и время отправки
                    val senderName = item.senderName
                    val messageText = item.textBody
                    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

                    if (item.from == myself.getRef) {
                        setText(s"($timestamp) $senderName: $messageText")
                        setAlignment(Pos.TOP_RIGHT)
                    } else {
                        setAlignment(Pos.TOP_LEFT)
                        setText(s"$senderName: $messageText ($timestamp)")
                    }
                }
            }
        })

        // Изменения стиля кнопки при наведении
        sendButton.setOnMouseEntered(_ => sendButton.setStyle("-fx-background-color: #643a7e"))
        sendButton.setOnMouseExited(_ => sendButton.setStyle("-fx-background-color: #806491"))
        sendButton.setOnAction(_ => sendMessage())

        // Обработчик нажатия Enter
        messagesTextField.setOnKeyPressed((t: KeyEvent) => {
            if (t.getCode.equals(KeyCode.ENTER)) sendMessage()
        })
    }

    // Метод для отправки сообщения
    def sendMessage(): Unit = {
        if (messagesTextField.getText.nonEmpty) {
            // Создаем новое сообщение
            val message = new ChatMessage(myself.getRef, messagesTextField.getText, myself.getNickName)
            messagesTextField.clear()

            // Сохраняем сообщение в истории чата
            myself.saveMessagesInChat(message, currentFriend)

            // Добавляем сообщение в ListView чата
            Platform.runLater(() => {
                chatListView.getItems.add(message)
                chatListView.scrollTo(chatListView.getItems.size - 1)
            })

            // Отправляем сообщение в общий чат или user
            if (currentFriend.getNickName == "Group chat") {
                myself.getRef ! PostMessageToGeneral(message, generalRoom)
            } else {
                currentFriend.getRef ! PostMessage(message, myself)
            }
        }
    }

    //  Информацию о текущем пользователе
    def setMySelf(receivedMySelf: User): Unit = {
        Platform.runLater(() => {
            // Добавляем общий чат в список
            friendsListView.getItems.add(generalRoom)
            myNameLabel.setText(receivedMySelf.getNickName)
        })
        myself = receivedMySelf
    }

    // Добавляем нового пользователя в список друзей
    def newUser(client: User): Unit = {
        var check: Boolean = true
        // Проверяем, есть ли уже этот пользователь в списке
        friendsListView.getItems.forEach(friend => if(friend.equals(client)) check = false)
        if (check) {
            Platform.runLater(() => {
                friendsListView.getItems.add(client)
            })
        }
    }

    // Обработка получения нового сообщения от другого пользователя
    def setMessageFromOtherActor(message: ChatMessage, friend: User): Unit = {
        myself.saveMessagesInChat(message, friend)
        if (currentFriend.equals(friend)) {
            Platform.runLater(() => {
                chatListView.getItems.add(message)
                chatListView.scrollTo(chatListView.getItems.size - 1)
            })
        }
    }

    // Удаляем пользователя из списка друзей
    def deleteUser(friend: User): Unit = {
        var check: Boolean = false
        // Проверяем, есть ли этот друг в списке
        friendsListView.getItems.forEach(friend => if(friend.equals(friend)) check = true)
        if (check) {
            Platform.runLater(() => {
                friendsListView.getItems.remove(friend)
            })
        }
    }
}
