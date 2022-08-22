import caliban.CalibanError.ExecutionError
import caliban.schema.{ArgBuilder, Schema}
import caliban.schema.Schema.{field, obj}
import domain._
import domain.derivatives._
import org.joda.time.DateTime

package object caliban {
  case class Queries[F[_]](
      listFilterCars: CarFilter => F[List[Car]],
      carsStats: F[CarRepositoryStats],
      logs: F[List[RequestForLog]],
      logsStats: F[List[RequestStats]]
  )

  case class Mutations[F[_]](
      addCar: CarCreate => F[Unit],
      deleteCar: CarId => F[Unit]
  )

  implicit val yearOfReleaseSchema: Schema[Any, YearOfRelease] =
    Schema.intSchema.contramap(_.value)

  implicit val carIdSchema: Schema[Any, CarId] =
    Schema.intSchema.contramap(_.value)

  implicit val carNumberSchema: Schema[Any, CarNumber] =
    Schema.stringSchema.contramap(_.value)

  implicit val carStampSchema: Schema[Any, CarStamp] =
    Schema.stringSchema.contramap(_.value)

  implicit val carColorSchema: Schema[Any, CarColor] =
    Schema.stringSchema.contramap(_.value)

  implicit lazy val carSchema: Schema[Any, Car] =
    obj("Car", Some("A car with description"))(implicit ft =>
      List(
        field("id")(_.id),
        field("number")(_.number),
        field("stamp")(_.stamp),
        field("color")(_.color),
        field("yearOfRelease")(_.yearOfRelease)
      )
    )

  implicit lazy val requestSchema: Schema[Any, RequestForLog] =
    obj("RequestForLog", Some("Request in log"))(implicit ft =>
      List(
        field("id")(_.id),
        field("url")(_.url),
        field("startProcessingAt")(_.startProcessingAt),
        field("endProcessingAt")(_.endProcessingAt)
      )
    )

  implicit lazy val requestStatsSchema: Schema[Any, RequestStats] =
    obj("RequestStats", Some("Request in log grouped"))(implicit ft =>
      List(
        field("url")(_.url),
        field("numberOfRecords")(_.numberOfRecords),
        field("maxRequestTime")(_.maxRequestTime.getMillis)
      )
    )

  implicit val requestIdSchema: Schema[Any, RequestId] =
    Schema.intSchema.contramap(_.value)

  implicit val numberOfRecordsSchema: Schema[Any, NumberOfRecords] =
    Schema.intSchema.contramap(_.value)

  implicit val dateTimeSchema: Schema[Any, DateTime] =
    Schema.instantSchema.contramap(d =>
      java.time.Instant.ofEpochMilli(d.toInstant.getMillis)
    )

  implicit val requestUrlSchema: Schema[Any, RequestUrl] =
    Schema.stringSchema.contramap(_.value)

  implicit val carNumberArgBuilder: ArgBuilder[CarNumber] = {
    case Value.StringValue(value) =>
      Right(CarNumber(value))
    case other =>
      Left(ExecutionError(s"Can't build a CarNumber from input $other"))
  }

  implicit val carStampArgBuilder: ArgBuilder[CarStamp] = {
    case Value.StringValue(value) =>
      Right(CarStamp(value))
    case other =>
      Left(ExecutionError(s"Can't build a CarStamp from input $other"))
  }

  implicit val carColorArgBuilder: ArgBuilder[CarColor] = {
    case Value.StringValue(value) =>
      Right(CarColor(value))
    case other =>
      Left(ExecutionError(s"Can't build a CarColor from input $other"))
  }

  implicit val yearOfReleaseArgBuilder: ArgBuilder[YearOfRelease] = {
    case Value.IntValue.IntNumber(value) =>
      Right(YearOfRelease(value))
    case other =>
      Left(ExecutionError(s"Can't build a YearOfRelease from input $other"))
  }

  implicit val carIdArgBuilder: ArgBuilder[CarId] = {
    case Value.IntValue.IntNumber(value) =>
      Right(CarId(value))
    case other =>
      Left(ExecutionError(s"Can't build a CarId from input $other"))
  }
}
