package bootstrap

import cats.{Applicative, Monad}
import cats.effect.{Async, Resource}
import cats.effect.std.Dispatcher
import org.http4s.HttpApp
import org.http4s.server.Router
import routes.{AppRoutes, GraphQLRoutes}

object AppRouter {
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

  def buildAsResource[F[_]: Applicative: Async: Dispatcher](
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
}
