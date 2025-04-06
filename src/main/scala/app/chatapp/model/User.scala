package app.chatapp.model

import akka.actor.typed.ActorRef
import app.chatapp.actor.UserActor
import UserActor.JSer

// Класс пользователя чата
case class User(constructPort: Int, constructNickName: String, constructRef: ActorRef[UserActor.Event]) extends JSer {

  // Порт
  private val port: Int = constructPort

  // Никнейм
  private val nickName: String = constructNickName

  // Ссылка на актера пользователя для взаимодействия с системой
  private val refOnActor: ActorRef[UserActor.Event] = constructRef

  // Хранения сообщений между пользователем и его друзьями
  private var mapMessagesWithFriends: Map[Int, List[ChatMessage]] = Map.empty[Int, List[ChatMessage]]
  def getPort: Int = port
  def getNickName: String = nickName

  // Получить ссылку на актера пользователя
  def getRef: ActorRef[UserActor.Event] = refOnActor

  // Получить список сообщений с конкретным пользователем
  def getListMessagesWithFriends(currentFriend: User): List[ChatMessage] = {
    // Проверяем, есть ли уже сообщения
    if (mapMessagesWithFriends.contains(currentFriend.getPort)) {
      mapMessagesWithFriends.apply(currentFriend.getPort) // Возвращаем список сообщений с другом
    } else {
      List.empty[ChatMessage] // Если сообщений нет, возвращаем пустой список
    }
  }

  // Добавить нового друга в список сообщений
  def setNewFriendToMap(friend: User): Unit = {
    mapMessagesWithFriends += (friend.getPort -> List())
  }

  // Сохранить новое сообщение в чат
  def saveMessagesInChat(message: ChatMessage, friend: User): Unit = {
    // Проверяем, есть ли уже сообщения
    if (mapMessagesWithFriends.contains(friend.getPort)) {
      val list: List[ChatMessage] = mapMessagesWithFriends.apply(friend.getPort)
      // Обновляем список сообщений добавляя новое сообщение
      mapMessagesWithFriends = mapMessagesWithFriends.updated(friend.getPort, list :+ message)
    } else {
      // Если сообщений еще нет, создаем новый список с текущим сообщением
      mapMessagesWithFriends += (friend.getPort -> List(message))
    }
  }
}
