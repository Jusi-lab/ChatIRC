package app.chatapp.service

import akka.actor.typed.Scheduler
import akka.util.Timeout
import app.chatapp.actor.UserActor
import app.chatapp.controller.LoginController
import app.chatapp.MainApp.startup
import UserActor.NewClient
import javafx.fxml.{FXMLLoader, Initializable}
import javafx.scene.image.Image
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

import java.io.IOException
import java.net.{ServerSocket, URL}
import java.util.ResourceBundle
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.util.Using

// Сервис для обработки логики окна входа
class LoginService extends LoginController with Initializable {

  // Переменная для хранения свободного порта
    private val userPort: Int = freePorts

  // Переменная для хранения имени пользователя
    private var userNickName: String = _

  // Инициализация компонента
    override def initialize(location: URL, resources: ResourceBundle): Unit = {

        portField.setText(userPort.toString)

        signInButton.setOnMouseEntered(_ => {
            signInButton.setStyle("-fx-background-color: #643a7e")
        })

        signInButton.setOnMouseExited(_ => {
            signInButton.setStyle("-fx-background-color: #806491")
        })

        signInButton.setOnAction(_ => {
            signIn()
        })

        nickNameField.setOnKeyPressed((t: KeyEvent) => {
            if(t.getCode.equals(KeyCode.ENTER)) signIn()
        })

        portField.setOnKeyPressed((t: KeyEvent) => {
            if(t.getCode.equals(KeyCode.ENTER)) signIn()
        })
    }

  // Метод для обработки входа в систему
    def signIn(): Unit = {
        if (nickNameField.getText.nonEmpty || portField.getText.nonEmpty) {
            userNickName = nickNameField.getText
            signInButton.getScene.getWindow.hide()

            val system = startup(userPort)                          //RUN CLUSTER
            implicit val timeout: Timeout = Timeout(20.seconds)
            implicit val scheduler: Scheduler = system.scheduler
            implicit val context: ExecutionContextExecutor = system.executionContext
            val loader: FXMLLoader = new FXMLLoader()
            loader.setLocation(getClass.getResource("/chatwindow.fxml"))
            try {
                loader.load()
            } catch {
                case exception: IOException =>
                    exception.printStackTrace()
            }
            val root: Parent = loader.getRoot
            val stage: Stage = new Stage()
            val receivedController: MessageService = loader.getController
            stage.setScene(new Scene(root))
            stage.setTitle("Chat")
            stage.show()

            val clientActor = system.systemActorOf(UserActor.apply(controller = receivedController), "myself")
            clientActor ! NewClient(userPort, userNickName)
        }
    }

  // Метод для получения свободного порта для клиента
    def freePorts: Int = {
        var freePort: Int = 0
        try{
            new ServerSocket(25251).close()
            freePort = 25251
            freePort
        } catch {
            case _: IOException =>
                Using(new ServerSocket(0)) (_.getLocalPort).foreach(port => freePort = port)
                freePort
        }
    }
}
