package bootstrap

import cats.{Monad, MonadThrow}
import cats.effect.Resource
import dsls.{CarServiceDsl, LogServiceDsl}
import services.{CarService, LogService}

final case class Services[F[_]](
    carServiceDsl: CarServiceDsl[F],
    logServiceDsl: LogServiceDsl[F]
)

object Services {
  def resource[F[_]: Monad: MonadThrow](
      storages: Storages[F]
  ): Resource[F, Services[F]] =
    for {
      carService <-
        CarService.resource[F](storages.carRepositoryDsl, storages.cacheDsl)
      logService <- LogService.resource[F](storages.logRepositoryDsl)
    } yield Services(carService, logService)
}
