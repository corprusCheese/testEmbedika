package dsls

import domain.{Car, CarRepositoryStats}
import domain.derivatives.CarFilter

trait CacheDsl[F[_]] {
  def addStats(stats: CarRepositoryStats): F[Unit]
  def addFilterResult(carFilter: CarFilter, result: List[Car]): F[Unit]
  def updateExpireFilterResult(carFilter: CarFilter): F[Unit]
  def getStats: F[Option[CarRepositoryStats]]
  def getFilterResult(carFilter: CarFilter): F[Option[List[Car]]]
  def removeCache(): F[Unit]
}
