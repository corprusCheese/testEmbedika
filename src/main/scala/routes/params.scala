package routes

import domain._
import org.http4s.QueryParamDecoder
import org.http4s.dsl.impl._

object params {
  object cars {
    implicit val idQueryParamDecoder: QueryParamDecoder[CarId] =
      QueryParamDecoder[Int].map(CarId.apply)
    implicit val numberQueryParamDecoder: QueryParamDecoder[CarNumber] =
      QueryParamDecoder[String].map(CarNumber.apply)
    implicit val stampQueryParamDecoder: QueryParamDecoder[CarStamp] =
      QueryParamDecoder[String].map(CarStamp.apply)
    implicit val colorQueryParamDecoder: QueryParamDecoder[CarColor] =
      QueryParamDecoder[String].map(CarColor.apply)
    implicit val yearQueryParamDecoder: QueryParamDecoder[YearOfRelease] =
      QueryParamDecoder[Int].map(YearOfRelease.apply)

    // optional params for filter
    case object OptParamNumberMatcher
        extends OptionalQueryParamDecoderMatcher[CarNumber]("number")
    case object OptParamStampMatcher
        extends OptionalQueryParamDecoderMatcher[CarStamp]("stamp")
    case object OptParamColorMatcher
        extends OptionalQueryParamDecoderMatcher[CarColor]("color")
    case object OptParamYearFromMatcher
        extends OptionalQueryParamDecoderMatcher[YearOfRelease]("yearFrom")
    case object OptParamYearToMatcher
        extends OptionalQueryParamDecoderMatcher[YearOfRelease]("yearTo")

    // for deleting
    case object ParamCarIdMatcher extends QueryParamDecoderMatcher[CarId]("id")
  }
}
