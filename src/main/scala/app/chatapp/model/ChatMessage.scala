package app.chatapp.model

import akka.actor.typed.ActorRef
import app.chatapp.actor.UserActor.{Event, JSer}

// Класс для представления сообщения чата
class ChatMessage(outerFrom: ActorRef[Event], outerTo: ActorRef[Event], outerTextBody: String, senderNickName: String) extends JSer {

    // Переменные для хранения данных о том, кто отправил сообщение, кто получил и сам текст сообщения
    private var from: ActorRef[Event] = outerFrom
    private var to: ActorRef[Event] = outerTo
    private var textBody: String = outerTextBody
    private var senderName: String = senderNickName

    // Получить отправителя сообщения
    def getFrom: ActorRef[Event] = this.from

    // Установить отправителя сообщения
    def setFrom(from: ActorRef[Event]): Unit = {
        this.from = from
    }

    // Получить получателя сообщения
    def getTo: ActorRef[Event] = this.to

    // Установить получателя сообщения
    def setTo(to: ActorRef[Event]): Unit = {
        this.to = to
    }

    // Получить текст сообщения
    def getTextBody: String = this.textBody

    // Установить текст сообщения
    def setTextBody(textBody: String): Unit = {
        this.textBody = textBody
    }

    // Получить имя отправителя
    def getSenderName: String = this.senderName

    // Установить имя отправителя
    def setSenderName(senderName: String): Unit = {
        this.senderName = senderName
    }
}
