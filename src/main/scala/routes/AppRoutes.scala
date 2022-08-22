package routes

import cats._
import cats.effect._
import cats.syntax.all._
import domain._
import circe._
import domain.derivatives._
import dsls._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s._
import org.joda.time.DateTime
import routes.params.cars._
import services.errors._
import routes.json._

final case class AppRoutes[F[_]: Applicative: Sync: JsonDecoder](
    carServiceDsl: CarServiceDsl[F],
    logServiceDsl: LogServiceDsl[F]
) extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case ar @ POST -> Root / "cars" / "delete" :? ParamCarIdMatcher(id) =>
      carsDeleteResponses(ar, id)

    case ar @ POST -> Root / "cars" / "create" =>
      carsCreateResponsesWithParsingError(ar)

    case ar @ GET -> Root / "cars" / "stats" =>
      carsStatsResponses(ar)

    case ar @ GET -> Root / "cars"
        :? (OptParamNumberMatcher(numberOpt)
        +& OptParamStampMatcher(stampOpt)
        +& OptParamColorMatcher(colorOpt)
        +& OptParamYearFromMatcher(yearFromOpt)
        +& OptParamYearToMatcher(yearToOpt)) =>
      val carFilter =
        CarFilter(numberOpt, stampOpt, colorOpt, yearFromOpt, yearToOpt)
      carsFilterResponses(ar, carFilter)

    case ar @ GET -> Root / "logs" =>
      logsResponses(ar)

    case ar @ GET -> Root / "logs" / "stats" =>
      logsStatsResponses(ar)
  }

  private def logsStatsResponses(ar: Request[F]): F[Response[F]] =
    saveRequest(ar, logServiceDsl.getRequestStats)
      .flatMap(list => Ok(list.asJson))
      .handleErrorWith(e => BadRequest(e.getMessage))

  private def logsResponses(ar: Request[F]): F[Response[F]] =
    saveRequest(ar, logServiceDsl.getAllRequests)
      .flatMap(list => Ok(list.asJson))
      .handleErrorWith(e => BadRequest(e.getMessage))

  private def carsStatsResponses(ar: Request[F]): F[Response[F]] =
    saveRequest(ar, carServiceDsl.getStats)
      .flatMap(stats => Ok(stats.asJson))
      .handleErrorWith(e => BadRequest(e.getMessage))

  private def carsFilterResponses(
      ar: Request[F],
      carFilter: CarFilter
  ): F[Response[F]] =
    saveRequest(ar, carServiceDsl.filterCars(carFilter))
      .flatMap(list => Ok(list.asJson))
      .handleErrorWith(e => BadRequest(e.getMessage))

  private def carsCreateResponses(
      ar: Request[F],
      car: CarCreate
  ): F[Response[F]] =
    saveRequest(ar, carServiceDsl.createCar(car))
      .flatMap(_ => Ok(s"car ${car.number} is created"))
      .handleErrorWith({
        case CarNumberAlreadyExistsInDatabase =>
          BadRequest(s"car ${car.number} already exists")
        case e: Throwable => BadRequest(e.getMessage)
      })

  private def carsDeleteResponses(ar: Request[F], id: CarId): F[Response[F]] =
    saveRequest(ar, carServiceDsl.deleteCar(id))
      .flatMap(_ => Ok(s"car $id is deleted"))
      .handleErrorWith({
        case CantFindACarForDeletingInDatabase =>
          BadRequest(s"car with = $id is not found")
        case e: Throwable => BadRequest(e.getMessage)
      })

  private def carsCreateResponsesWithParsingError(
      ar: Request[F]
  ): F[Response[F]] =
    ar.decodeWithParseHandling[CarCreate](carsCreateResponses(ar, _))

  private def saveRequest[A](
      ar: Request[F],
      action: => F[A],
      createdAt: DateTime = DateTime.now()
  ): F[A] =
    for {
      result <- action
      _ <- logServiceDsl.saveRequest(
        RequestForLogCreate(
          RequestUrl(ar.uri.renderString),
          createdAt,
          DateTime.now()
        )
      )
    } yield result
}

object AppRoutes {
  def resource[F[_]: Applicative: Sync: JsonDecoder](
      carServiceDsl: CarServiceDsl[F],
      logServiceDsl: LogServiceDsl[F]
  ): Resource[F, AppRoutes[F]] =
    Resource.pure[F, AppRoutes[F]](AppRoutes(carServiceDsl, logServiceDsl))
}
