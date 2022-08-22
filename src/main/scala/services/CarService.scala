package services

import cats.{Monad, MonadError, MonadThrow}
import cats.effect.Resource
import cats.implicits._
import domain._
import domain.derivatives._
import dsls.{CacheDsl, CarRepositoryDsl, CarServiceDsl}
import services.errors._

final case class CarService[F[_]: Monad: MonadThrow](
    carRepositoryDsl: CarRepositoryDsl[F],
    cacheDsl: CacheDsl[F]
) extends CarServiceDsl[F] {

  override def createCar(car: CarCreate): F[Unit] = {
    for {
      result <-
        carRepositoryDsl
          .addCar(car)
          .handleErrorWith(_ =>
            MonadError[F, Throwable]
              .raiseError(CarNumberAlreadyExistsInDatabase)
          )
      _ <-
        if (result)
          cacheDsl
            .removeCache()
        else
          Monad[F].unit
    } yield ()
  }

  override def deleteCar(id: CarId): F[Unit] =
    for {
      result <- carRepositoryDsl.deleteById(id)
      _ <-
        if (result)
          cacheDsl
            .removeCache()
        else
          MonadError[F, Throwable]
            .raiseError(CantFindACarForDeletingInDatabase)
    } yield ()

  override def getStats: F[CarRepositoryStats] =
    for {
      cachedData <- cacheDsl.getStats
      result <- cachedData match {
        case Some(value) => value.pure[F]
        case None        => carRepositoryDsl.getStats.flatTap(cacheDsl.addStats)
      }
    } yield result

  override def filterCars(carFilter: CarFilter): F[List[Car]] =
    for {
      cachedData <- cacheDsl.getFilterResult(carFilter)
      result <- cachedData match {
        case Some(value) =>
          cacheDsl.updateExpireFilterResult(carFilter).as(value)
        case None =>
          carRepositoryDsl
            .carList(carFilter)
            .flatTap(cacheDsl.addFilterResult(carFilter, _))
      }
    } yield result
}

object CarService {
  def resource[F[_]: Monad: MonadThrow](
      carRepositoryDsl: CarRepositoryDsl[F],
      cacheDsl: CacheDsl[F]
  ): Resource[F, CarService[F]] =
    Resource.pure[F, CarService[F]](CarService(carRepositoryDsl, cacheDsl))
}
