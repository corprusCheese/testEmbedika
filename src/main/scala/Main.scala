import bootstrap.App
import cats.effect.{ExitCode, IO, IOApp}
import config.AppConfig

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    println("building service")
    AppConfig.resource[IO].use(App.run[IO])
  }
}
