package storages

import cats.effect._
import cats.implicits._
import domain._
import domain.derivatives._
import circe._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.implicits.legacy.instant._
import doobie.postgres.circe.jsonb.implicits._
import dsls._
import io.circe._
import io.circe.syntax._
import org.postgresql.util.PGobject

final case class CarRepository[F[_]: MonadCancelThrow](tx: Transactor[F])
    extends CarRepositoryDsl[F] {

  override def carList(carFilter: CarFilter): F[List[Car]] =
    CarRepository.queries.queryForFilterCars(carFilter).to[List].transact(tx)

  override def addCar(newCar: CarCreate): F[Boolean] =
    CarRepository.queries
      .queryForAddingCar(newCar)
      .run
      .transact(tx)
      .map(_ > 0)

  override def deleteById(id: CarId): F[Boolean] =
    CarRepository.queries
      .queryForDeletingByCarId(id)
      .run
      .transact(tx)
      .map(_ > 0)

  override def getStats: F[CarRepositoryStats] =
    CarRepository.queries
      .queryForStats()
      .option
      .transact(tx)
      .map(_.get)
}

object CarRepository {

  def resource[F[_]: MonadCancelThrow](
      tx: Transactor[F]
  ): Resource[F, CarRepository[F]] =
    Resource.pure[F, CarRepository[F]](CarRepository(tx))

  object queries {
    def queryForAddingCar(newCar: CarCreate): doobie.Update0 =
      sql"""|INSERT INTO cars (number, stamp, color, year_of_release) 
            |VALUES (${newCar.number}, ${newCar.stamp}, ${newCar.color}, ${newCar.yearOfRelease})""".stripMargin.update

    def queryForDeletingByCarId(id: CarId): doobie.Update0 =
      sql"""DELETE FROM cars WHERE id = $id""".stripMargin.update

    def queryForFilterCars(carFilter: CarFilter): doobie.Query0[Car] =
      (fr"""SELECT id, number, stamp, color, year_of_release FROM cars WHERE (1 = 1) """.stripMargin ++ fragmentForCarFilter(
        carFilter
      )).stripMargin
        .query[Car]

    def queryForStats(): doobie.Query0[CarRepositoryStats] =
      sql"""|SELECT COUNT(id), MIN(created_at), MAX(created_at) FROM cars""".stripMargin
        .query[CarRepositoryStats]

    private def fragmentForCarFilter(carFilter: CarFilter): doobie.Fragment =
      // numbers
      carFilter.numberOpt
        .map(x => fr" AND number = $x ")
        .getOrElse(Fragment.empty) ++
        // stamps
        carFilter.stampOpt
          .map(x => fr" AND stamp = $x ")
          .getOrElse(Fragment.empty) ++
        // color
        carFilter.colorOpt
          .map(x => fr" AND color = $x ")
          .getOrElse(Fragment.empty) ++
        // from
        carFilter.yearFrom
          .map(x => fr" AND year_of_release >= $x ")
          .getOrElse(Fragment.empty) ++
        // to
        carFilter.yearTo
          .map(x => fr" AND year_of_release <= $x ")
          .getOrElse(Fragment.empty)
  }
}
