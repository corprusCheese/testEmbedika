import io.estatico.newtype.macros.newtype
import org.joda.time.DateTime

package object domain {
  @newtype
  case class CarId(value: Int)

  @newtype
  case class CarNumber(value: String)

  @newtype
  case class CarStamp(value: String)

  @newtype
  case class CarColor(value: String)

  @newtype
  case class YearOfRelease(value: Int)

  case class Car(
      id: CarId,
      number: CarNumber,
      stamp: CarStamp,
      color: CarColor,
      yearOfRelease: YearOfRelease
  )

  @newtype
  case class NumberOfRecords(value: Int)

  case class CarRepositoryStats(
      numberOfRecords: NumberOfRecords,
      firstRecordDateTime: Option[DateTime],
      lastRecordDateTime: Option[DateTime]
  )

  @newtype
  case class RequestId(value: Int)

  @newtype
  case class RequestUrl(value: String)

  case class RequestForLog(
      id: RequestId,
      url: RequestUrl,
      startProcessingAt: DateTime,
      endProcessingAt: DateTime
  )

  case class RequestStats(
      url: RequestUrl,
      numberOfRecords: NumberOfRecords,
      maxRequestTime: DateTime
  )
}
