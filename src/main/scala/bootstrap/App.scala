package bootstrap

import cats._
import cats.effect._
import cats.effect.std.Dispatcher
import cats.implicits._
import config._
import dev.profunktor.redis4cats.effect.Log.NoOp.instance
import org.http4s.HttpApp
import org.http4s.circe.JsonDecoder
import org.http4s.server.{Router, Server}
import routes._

object App {

  private def makeHttpApp[F[_]: Monad](
      appRoutes: AppRoutes[F],
      graphQLRoutes: GraphQLRoutes[F]
  ): Resource[F, HttpApp[F]] =
    Resource.pure[F, HttpApp[F]](
      Router(
        "/api/" -> appRoutes.routes,
        "/graphql/" -> graphQLRoutes.routes
      ).orNotFound
    )

  private def buildHttpAppAsResource[F[_]: Applicative: Async: Dispatcher](
      services: Services[F]
  ): Resource[F, HttpApp[F]] =
    for {
      appRoutes <-
        AppRoutes.resource[F](services.carServiceDsl, services.logServiceDsl)
      graphQLRoutes <-
        GraphQLRoutes
          .resource[F](services.carServiceDsl, services.logServiceDsl)
      httpApp <- makeHttpApp(appRoutes, graphQLRoutes)
    } yield httpApp

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
      httpApp <- buildHttpAppAsResource(services)
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
