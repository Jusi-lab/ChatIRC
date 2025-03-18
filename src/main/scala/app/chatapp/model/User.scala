package app.chatapp.model

import akka.actor.typed.ActorRef
import app.chatapp.actor.UserActor
import UserActor.JSer

// Класс, представляющий пользователя чата
class User(constructPort: Int, constructNickName: String, constructRef: ActorRef[UserActor.Event]) extends JSer {

    // Порт пользователя (используется для идентификации)
    private val port: Int = constructPort

    // Никнейм пользователя
    private val nickName: String = constructNickName

    // Ссылка на актера пользователя для взаимодействия с системой
    private val refOnActor: ActorRef[UserActor.Event] = constructRef

    // Словарь для хранения сообщений между пользователем и его друзьями
    private var mapMessagesWithFriends: Map[Int, List[ChatMessage]] = Map.empty[Int, List[ChatMessage]]

    // Получить порт пользователя
    def getPort: Int = port

    // Получить никнейм пользователя
    def getNickName: String = nickName

    // Получить ссылку на актера пользователя
    def getRef: ActorRef[UserActor.Event] = refOnActor

    // Получить список сообщений с конкретным другом
    def getListMessagesWithFriends(currentFriend: User): List[ChatMessage] = {
        if (mapMessagesWithFriends.contains(currentFriend.getPort)) {
            mapMessagesWithFriends.apply(currentFriend.getPort)
        } else {
            List.empty[ChatMessage]
        }
    }

    // Добавить нового друга в список сообщений
    def setNewFriendToMap(friend: User): Unit = {
        mapMessagesWithFriends += (friend.getPort -> List())
    }

    // Сохранить новое сообщение в чат
    def saveMessagesInChat(message: ChatMessage, friend: User): Unit = {

        if (mapMessagesWithFriends.contains(friend.getPort)) {
            val list: List[ChatMessage] = mapMessagesWithFriends.apply(friend.getPort)
            mapMessagesWithFriends = mapMessagesWithFriends.updated(friend.getPort, list :+ message)

            // Если сообщений нет, создаем новый список с текущим сообщением
        } else {
            mapMessagesWithFriends += (friend.getPort -> List(message))
        }
    }

// Метод для проверки равенства двух объектов User
    def canEqual(a: Any) = a.isInstanceOf[User]

    // Переопределение метов для правильного сравнения объектов User
    override def equals(that: Any): Boolean =
        that match {
            case that: User => that.canEqual(this) && this.hashCode == that.hashCode
            case _ => false
        }

    override def hashCode: Int = {
        val prime = 31
        var result = 1
        result = prime * result + port;
        result = prime * result + (if (nickName == null) 0 else nickName.hashCode)
        result
    }
}
