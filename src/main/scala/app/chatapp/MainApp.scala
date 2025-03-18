package app.chatapp

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.typed.Cluster
import app.chatapp.actor.UserActor
import UserActor.{JSer, StopActor}
import com.typesafe.config.ConfigFactory
import javafx.application.{Application, Platform}
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage

import java.io.IOException


object MainApp {

    // Ссылка на актера клиента, используется для отправки сообщений
    var clientActor: ActorRef[UserActor.Event] = _

    // Определяем команду RootCmd, которая будет использоваться в качестве базовой команды для актера
    trait RootCmd extends JSer

    // Поведение для корневого актора
    object RootBehavior {
        def apply(): Behavior[RootCmd] = Behaviors.setup[RootCmd] { ctx =>
            val cluster = Cluster(ctx.system)
            Behaviors.empty
        }
    }

    // Функция для старта системы акторов с заданным портом
    def startup(port: Int): ActorSystem[RootCmd] = {

        // Конфигурация Akka с указанием порта для сетевого соединения
        val config = ConfigFactory
            .parseString(
                s"""
              akka.remote.artery.canonical.port=$port
              """)
            .withFallback(ConfigFactory.load())

        // Создание системы акторов с использованием конфигурации
        ActorSystem(RootBehavior(), "ClusterSystem", config)
    }

    def main(args: Array[String]): Unit = {
        Application.launch(classOf[MainApp])
    }

    // Метод для остановки клиента
    def stop(): Unit = {
        println("--In STOP--")
        clientActor ! StopActor()
    }
}

class MainApp extends Application {

    // Метод, который запускает окно приложения
    override def start(primaryStage: Stage): Unit = {
        val loader: FXMLLoader = new FXMLLoader()
        loader.setLocation(getClass.getResource("/login.fxml"))
        try {
            val scene: Scene = new Scene(loader.load(), 350, 200)
            primaryStage.setScene(scene)
            primaryStage.setResizable(false)
            primaryStage.show()
        } catch {
            case e: IOException =>
                e.printStackTrace()
        }
    }

    // Метод, который вызывается при закрытии приложения
    override def stop(): Unit = {
        MainApp.stop()
        Platform.exit()
        System.exit(0)

        super.stop()
    }
}
