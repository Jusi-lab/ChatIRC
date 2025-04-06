package app.chatapp.service

import akka.actor.typed.{Scheduler, ActorRef, ActorSystem}
import akka.util.Timeout
import app.chatapp.actor.UserActor.{NewClient}
import app.chatapp.controller.LoginController
import app.chatapp.MainApp.startup
import javafx.fxml.{FXMLLoader, Initializable}
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage
import com.typesafe.config.ConfigFactory
import java.net.URL
import java.util.ResourceBundle
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import app.chatapp.actor.UserActor

// Сервис для обработки логики окна входа
class LoginService extends LoginController with Initializable {

  private var userNickName: String = _

  // Инициализация кнопки входа
  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    // Изменяем стиль кнопки при наведении
    signInButton.setOnMouseEntered(_ => setButtonStyle("#643a7e"))
    signInButton.setOnMouseExited(_ => setButtonStyle("#806491"))
    // Обработка клика
    signInButton.setOnAction(_ => signIn())

    // Обработка Enter
    nickNameField.setOnKeyPressed((t: KeyEvent) => {
      if (t.getCode == KeyCode.ENTER) signIn()
    })
  }

  // Метод входа в систему
  def signIn(): Unit = {
    // Получаем текст из поля
    val nickName = nickNameField.getText.trim
    if (nickName.isEmpty) {
      // Ошибка пустое
      println("The login field should not be empty.")
    } else {
      userNickName = nickName
      signInButton.getScene.getWindow.hide()
      // Инициализируем систему актеров
      initializeActorSystem()
    }
  }

  // Инициализации ActorSystem
  private def initializeActorSystem(): Unit = {
    try {
      // Загружаем конфигурацию для получения порта
      val config = ConfigFactory.load()
      val userPort = config.getInt("akka.remote.artery.canonical.port")
      val system = startup(userPort)
      implicit val timeout: Timeout = Timeout(20.seconds)
      implicit val scheduler: Scheduler = system.scheduler
      implicit val context: ExecutionContextExecutor = system.executionContext

      // Загрузка главное окна
      val loader = new FXMLLoader(getClass.getResource("/chatwindow.fxml"))
      val root: Parent = loader.load()
      val stage: Stage = new Stage()
      val receivedController = loader.getController[MessageService]
      stage.setScene(new Scene(root))
      stage.setTitle("Chat")
      stage.show()

      // Создаем актера для текущего пользователя
      val clientActor = system.systemActorOf(UserActor.apply(receivedController), "myself")
      // Отправляем сообщение с никнеймом и портом пользователя
      clientActor ! NewClient(userPort, userNickName)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        println(s"Ошибка при старте клиента или загрузке окна: ${e.getMessage}")
    }
  }

  //  стиль для кнопки
  private def setButtonStyle(color: String): Unit = {
    signInButton.setStyle(s"-fx-background-color: $color")
  }
}
