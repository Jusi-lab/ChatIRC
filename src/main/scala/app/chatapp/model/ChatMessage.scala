package app.chatapp.model

import akka.actor.typed.ActorRef
import app.chatapp.actor.UserActor.{Event, JSer}

// Класс для представления сообщения чата
class ChatMessage(
                   val from: ActorRef[Event], // Отправитель
                   val textBody: String, // Текст
                   val senderName: String // Имя
                 ) extends JSer
