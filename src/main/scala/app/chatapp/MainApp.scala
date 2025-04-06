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

  // Метод для старта системы
  def startup(port: Int): ActorSystem[RootCmd] = {
    // Конфигурация Akka с указанием порта для сетевого соединения
    val config = ConfigFactory
      .parseString(s"akka.remote.artery.canonical.port=$port")  // Используем порт из конфигурации
      .withFallback(ConfigFactory.load())  // Загружаем настройки из конфигурации по умолчанию

    // Создание системы акторов с использованием конфигурации
    ActorSystem(RootBehavior(), "ClusterSystem", config)
  }

  // Поведение для корневого актора
  trait RootCmd
  object RootBehavior {
    def apply(): Behavior[RootCmd] = Behaviors.setup { ctx =>
      // Инициализация кластера Akka
      val cluster = akka.cluster.typed.Cluster(ctx.system)
      Behaviors.empty
    }
  }

  // Метод для остановки актора
  def stop(): Unit = {
    println("--In STOP--")
    // Если клиентский актер был инициализирован, отправляем команду остановки
    if (clientActor != null) {
      clientActor ! StopActor()
    } else {
      println("Client actor is not initialized.")  // Если актер не инициализирован
    }
    Platform.exit()
    System.exit(0)
  }

  // Метод для запуска JavaFX приложения
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[MainApp])
  }
}

class MainApp extends Application {

  // Метод запускает окна приложения
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

  // Метод при закрытии приложения
  override def stop(): Unit = {
    MainApp.stop()
    super.stop()
  }
}
