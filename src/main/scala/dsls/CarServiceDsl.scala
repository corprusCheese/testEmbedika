package dsls

import domain.derivatives.{CarCreate, CarFilter}
import domain.{Car, CarId, CarRepositoryStats}

trait CarServiceDsl[F[_]] {
  def createCar(car: CarCreate): F[Unit]
  def deleteCar(car: CarId): F[Unit]
  def getStats: F[CarRepositoryStats]
  def filterCars(carFilter: CarFilter): F[List[Car]]
}
