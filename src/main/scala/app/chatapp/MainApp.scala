package app.chatapp

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import app.chatapp.actor.UserActor
import UserActor.{Event, StopActor}
import com.typesafe.config.ConfigFactory
import javafx.application.{Application, Platform}
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage
import java.io.IOException

object MainApp {

    // Ссылка на актера клиента
    var clientActor: ActorRef[UserActor.Event] = _

    // Метод для старта системы акторов с заданным портом
    def startup(port: Int): ActorSystem[RootCmd] = {
        // Конфигурация Akka с указанием порта для сетевого соединения
        val config = ConfigFactory
          .parseString(s"akka.remote.artery.canonical.port=$port")
          .withFallback(ConfigFactory.load())

        // Создание системы акторов с использованием конфигурации
        ActorSystem(RootBehavior(), "ClusterSystem", config)
    }

    // Поведение для корневого актора
    trait RootCmd
    object RootBehavior {
        def apply(): Behavior[RootCmd] = Behaviors.setup { ctx =>
            val cluster = akka.cluster.typed.Cluster(ctx.system)
            Behaviors.empty
        }
    }

    // Метод для остановки клиента
    def stop(): Unit = {
        println("--In STOP--")
        if (clientActor != null) {
            clientActor ! StopActor()
        } else {
            println("Client actor is not initialized.")
        }
        Platform.exit()
        System.exit(0)
    }

    def main(args: Array[String]): Unit = {
        Application.launch(classOf[MainApp])
    }
}

class MainApp extends Application {

    // Метод, который запускает окно приложения
    override def start(primaryStage: Stage): Unit = {
        val loader: FXMLLoader = new FXMLLoader()
        loader.setLocation(getClass.getResource("/login.fxml"))

        try {
            val root: Parent = loader.load()
            val scene: Scene = new Scene(root)
            primaryStage.setScene(scene)
            primaryStage.setResizable(false)
            primaryStage.setTitle("Login")
            primaryStage.show()
        } catch {
            case e: IOException =>
                e.printStackTrace()
        }
    }

    // Метод, который вызывается при закрытии приложения
    override def stop(): Unit = {
        MainApp.stop()
        super.stop()
    }
}
