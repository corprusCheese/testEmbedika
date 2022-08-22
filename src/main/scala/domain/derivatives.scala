package domain

import org.joda.time.DateTime

object derivatives {

  case class CarFilter(
      numberOpt: Option[CarNumber] = None,
      stampOpt: Option[CarStamp] = None,
      colorOpt: Option[CarColor] = None,
      yearFrom: Option[YearOfRelease] = None,
      yearTo: Option[YearOfRelease] = None
  )

  case class CarCreate(
      number: CarNumber,
      stamp: CarStamp,
      color: CarColor,
      yearOfRelease: YearOfRelease
  )

  case class RequestForLogCreate(
      url: RequestUrl,
      startProcessingAt: DateTime,
      endProcessingAt: DateTime
  )
}
