package dsls

import domain.derivatives.{CarCreate, CarFilter}
import domain.{Car, CarId, CarRepositoryStats}

trait CarRepositoryDsl[F[_]] {
  def carList(carFilter: CarFilter): F[List[Car]]
  def addCar(newCar: CarCreate): F[Boolean]
  def deleteById(id: CarId): F[Boolean]
  def getStats: F[CarRepositoryStats]
}
