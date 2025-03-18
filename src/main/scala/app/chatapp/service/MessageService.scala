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

import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.ResourceBundle

// Сервис для обработки логики чата и взаимодействия с пользовательским интерфейсом
class MessageService extends MessageController with Initializable {

    private var myself: User = _
    private var currentFriend: User = _
    private val generalRoom: User = new User(-1, "Group chat", null)

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
                if (currentFriend.equals(myself)){
                    sendButton.setVisible(false)
                    messagesTextField.setVisible(false)
                    chatListView.getItems.clear()
                } else {
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
                    if (item.getFrom == myself.getRef) {
                        setText("(" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + ") " + item.getTextBody)
                        setAlignment(Pos.TOP_RIGHT)
                    } else {
                        setAlignment(Pos.TOP_LEFT)
                        setText(item.getTextBody + " (" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + ")")
                    }
                }
            }
        })

        // Обработчик изменения стиля кнопки отправки сообщения при наведении
        sendButton.setOnMouseEntered(_ => {
            sendButton.setStyle("-fx-background-color: #643a7e")
        })

        sendButton.setOnMouseExited(_ => {
            sendButton.setStyle("-fx-background-color: #806491")
        })

        sendButton.setOnAction(_ => {
            sendMessage()
        })

        messagesTextField.setOnKeyPressed((t: KeyEvent) => {
            if(t.getCode.equals(KeyCode.ENTER)) sendMessage()
        })
    }


    // Метод для отправки сообщения
    def sendMessage(): Unit = {
        if (messagesTextField.getText.nonEmpty) {
            println("--Button is pressed--")
            val message: ChatMessage = new ChatMessage(myself.getRef, currentFriend.getRef, messagesTextField.getText)
            messagesTextField.clear()
            myself.saveMessagesInChat(message, currentFriend)
            chatListView.getItems.clear()
            myself.getListMessagesWithFriends(currentFriend).foreach(msg => chatListView.getItems.add(msg))
            if (currentFriend.getNickName == "Group chat"){
                println("---SEND MESSAGE TO ACTOR---")
                myself.getRef ! PostMessageToGeneral(message, generalRoom)
            } else {
                currentFriend.getRef ! PostMessage(message, myself)
            }
        }
    }

    // Устанавливаем информацию о текущем пользователе
    def setMySelf(receivedMySelf: User): Unit = {
        println("------SET MYSELF------")
        friendsListView.getItems.add(generalRoom)
        myNameLabel.setText(receivedMySelf.getNickName)
        myself = receivedMySelf
    }

    // Добавляем нового пользователя в список друзей
    def newUser(client: User): Unit = {
        println("------TRYING TO WRITE NEW USER INTO LIST------")
        var check: Boolean = true
        friendsListView.getItems.forEach(friend => if(friend.equals(client)) check = false)
        if (check) {
            friendsListView.getItems.add(client)
        }
    }

    // Обработка получения нового сообщения от другого пользователя
    def setMessageFromOtherActor(message: ChatMessage, friend: User): Unit = {
        myself.saveMessagesInChat(message, friend)
        println("------CURRENT_FRIEND: " + currentFriend + "; FRIEND: " + friend)
        if (currentFriend.equals(friend)) {
            println("--I'M HERE--")
            chatListView.getItems.clear()
            myself.getListMessagesWithFriends(friend).foreach(msg => chatListView.getItems.add(msg))
        }
    }

    // Удаляем пользователя из списка друзей
    def deleteUser(friend: User): Unit ={
        println("------TRYING TO DELETE USER FROM LIST------")
        var check: Boolean = false
        friendsListView.getItems.forEach(friend => if(friend.equals(friend)) check = true)
        if (check) {
            friendsListView.getItems.remove(friend)
        }
    }
}
