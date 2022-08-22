package bootstrap

import cats._
import cats.effect._
import cats.effect.std.Dispatcher
import cats.implicits._
import config._
import dev.profunktor.redis4cats.effect.Log.NoOp.instance
import org.http4s.circe.JsonDecoder
import org.http4s.server.Server

object App {

  private def buildAsResource[F[
      _
  ]: Applicative: MonadCancelThrow: Async: JsonDecoder: Dispatcher](
      config: AppConfig
  ): Resource[F, Server] =
    for {
      storages <- Storages.resource[F](
        config.postgresConfig,
        config.redisConfig,
        config.redisSettings
      )
      services <- Services.resource[F](storages)
      httpApp <- AppRouter.buildAsResource[F](services)
      server <- Server.buildAsResource[F](config.httpServerConfig, httpApp)
    } yield server

  def run[F[_]: Applicative: MonadCancelThrow: Async: JsonDecoder](
      config: AppConfig
  ): F[ExitCode] =
    Dispatcher[F]
      .use(implicit dispatcher =>
        App
          .buildAsResource[F](config)
          .use(_ => Async[F].never.as(()))
          .as(ExitCode.Success)
          .handleErrorWith(e => {
            println(s"ERROR: ${e.toString}")
            ExitCode.Error.pure[F]
          })
      )
}
