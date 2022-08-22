package services

import cats.MonadError
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits.catsSyntaxOptionId
import domain._
import domain.derivatives.{CarCreate, CarFilter}
import dsls.{CacheDsl, CarRepositoryDsl}
import org.joda.time.DateTime
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.BeforeAndAfter
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import services.errors.CarNumberAlreadyExistsInDatabase

class CarServiceTest
    extends AsyncFreeSpec
    with AsyncIOSpec
    with ArgumentMatchersSugar
    with MockitoSugar
    with BeforeAndAfter {

  private val carRepositoryDsl = mock[CarRepositoryDsl[IO]]
  private val cacheDsl = mock[CacheDsl[IO]]

  private val stats = CarRepositoryStats(
    NumberOfRecords(1),
    DateTime.now().some,
    DateTime.now().some
  )
  private val carCreate = CarCreate(
    CarNumber("A45"),
    CarStamp("BMW"),
    CarColor("red"),
    YearOfRelease(2020)
  )
  after {
    reset(carRepositoryDsl, cacheDsl)
  }

  before {
    when(cacheDsl.removeCache()).thenReturn(IO.unit)
    when(cacheDsl.updateExpireFilterResult(*)).thenReturn(IO.unit)
    when(cacheDsl.addStats(*)).thenReturn(IO.unit)
    when(cacheDsl.addFilterResult(*, *)).thenReturn(IO.unit)
    when(carRepositoryDsl.getStats).thenReturn(IO(stats))
    when(carRepositoryDsl.deleteById(*)).thenReturn(IO(true))
    when(carRepositoryDsl.addCar(*)).thenReturn(IO(true))
    when(carRepositoryDsl.carList(*)).thenReturn(IO(List()))
  }

  "CarService" - {
    val carService = CarService[IO](carRepositoryDsl, cacheDsl)

    "can save" in {
      carService
        .createCar(carCreate)
        .asserting(_ shouldBe ())
    }
    "throws custom error if repository throws an error when saving" in {
      when(carRepositoryDsl.addCar(*))
        .thenReturn(
          MonadError[IO, Throwable].raiseError(new Throwable("violates unique"))
        )

      carService
        .createCar(carCreate)
        .assertThrows[CarNumberAlreadyExistsInDatabase.type]
    }
    "can get filtered cars from cache" in {
      when(cacheDsl.getFilterResult(*)).thenReturn(IO(List().some))
      carService.filterCars(CarFilter()).asserting(_ shouldBe List())
    }
    "can get filtered cars from db if can't get from cache" in {
      when(cacheDsl.getFilterResult(*)).thenReturn(IO(None))
      carService.filterCars(CarFilter()).asserting(_ shouldBe List())
    }
    "can get stats from cache" in {
      when(cacheDsl.getStats).thenReturn(IO(stats.some))
      carService.getStats.asserting(_ shouldBe stats)
    }
    "can get stats from db" in {
      when(cacheDsl.getStats).thenReturn(IO(None))
      carService.getStats.asserting(_ shouldBe stats)
    }
    "can delete" in {
      carService.deleteCar(CarId(1)).asserting(_ shouldBe ())
    }
  }
}
