package app.chatapp.actor

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import app.chatapp.service.MessageService
import app.chatapp.MainApp.clientActor
import app.chatapp.model.{User, ChatMessage}
import javafx.application.Platform

import scala.language.postfixOps


object UserActor {

    // Ключ для регистрации актора в Receptionist
    val clientServiceKey: ServiceKey[Event] = ServiceKey[UserActor.Event]("Client")

    // Переменные для хранения данных о клиенте и списке клиентов в кластере
    var clientInCluster: User = _
    var currentCountClients: IndexedSeq[ActorRef[UserActor.Event]] = _

    trait JSer

    sealed trait Event extends JSer

    private final case class ClientsUpdated(newClients: Set[ActorRef[UserActor.Event]]) extends Event

    case class NewClient(clientPort: Int, clientNickName: String) extends Event

    case class MyInfo(client: User) extends Event

    case class PostMessage(message: ChatMessage, friend: User) extends Event

    case class PostMessageToGeneral(message: ChatMessage, generalRoom: User) extends Event

    case class StopActor() extends Event

    case class DeleteStoppedActor(friend: User) extends Event


    def apply(controller: MessageService): Behavior[Event] = Behaviors.setup { ctx =>
        init(ctx, controller)
    }

    private def init(ctx: ActorContext[Event], controller: MessageService): Behavior[Event] = {
        Behaviors.receiveMessage {
            case NewClient(clientPort, clientNickName) =>
                println("------IN NEW_CLIENT CASE------" + "\nport: " + clientPort + "\nnick: " + clientNickName)
                clientActor = ctx.self
                clientInCluster = new User(clientPort, clientNickName, ctx.self)
                Platform.runLater(() => controller.setMySelf(clientInCluster))

                ctx.system.receptionist ! Receptionist.Register(clientServiceKey, ctx.self)

                val subscriptionAdapter = ctx.messageAdapter[Receptionist.Listing] {
                    case UserActor.clientServiceKey.Listing(clients) =>
                        ClientsUpdated(clients)
                }
                // Подписываемся на изменения списка клиентов
                ctx.system.receptionist ! Receptionist.Subscribe(UserActor.clientServiceKey, subscriptionAdapter)

                running(ctx, controller, clientPort, clientNickName)
        }
    }

    private def running(ctx: ActorContext[Event], controller: MessageService,
                        clientPort: Int, clientNickName: String): Behavior[Event] =
        Behaviors.receiveMessage {
            case ClientsUpdated(newClients) =>
                currentCountClients = newClients.toIndexedSeq
                println("Size newClients: " + newClients.size)
                ctx.log.info("///// List of services registered with the receptionist changed: {}", newClients)
                newClients.foreach(actor => if (actor != ctx.self) actor ! MyInfo(new User(clientPort, clientNickName, ctx.self)))
                running(ctx, controller, clientPort, clientNickName)

            case MyInfo(receivedClient) =>
                println("------OTHER ACTOR TAKE NEW ACTOR------")
                Platform.runLater(() => controller.newUser(receivedClient))
                Behaviors.same

            case PostMessage(message, friend) =>
                Platform.runLater(() => controller.setMessageFromOtherActor(message, friend))
                Behaviors.same

            case PostMessageToGeneral(message, generalRoom) =>
                currentCountClients.foreach(actor => if (actor != ctx.self) actor ! PostMessage(message, generalRoom))
                Behaviors.same

            case StopActor() =>
                println("---CASE STOPaCTOR---")
                currentCountClients.foreach(actor => if (actor != ctx.self) actor ! DeleteStoppedActor(clientInCluster))
                Behaviors.stopped

            case DeleteStoppedActor(friend) =>
                Platform.runLater(() => controller.deleteUser(friend))
                Behaviors.same
        }
}
