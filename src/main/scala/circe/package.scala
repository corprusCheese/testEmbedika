import domain.derivatives.CarCreate
import domain._
import doobie.Meta
import io.circe._
import io.estatico.newtype.Coercible
import org.joda.time.DateTime
import org.postgresql.util.PGobject
import doobie.implicits.legacy.instant._
import cats.implicits._
import io.circe.syntax._

import java.time.Instant

package object circe {

  // for json handling
  def deriveCodec[T, R](implicit
      e: Encoder[R],
      d: Decoder[R],
      ev1: Coercible[T, R],
      ev2: Coercible[R, T]
  ): Codec[T] =
    Codec.from(
      d.map(ev2.apply),
      e.contramap(ev1.apply)
    )

  // for doobie implicits
  def deriveMeta[N, R](implicit
      ev1: Meta[R],
      ev2: Coercible[N, R],
      ev3: Coercible[R, N]
  ): Meta[N] =
    ev1.imap(ev3.apply)(ev2.apply)

  // car
  implicit val carIdCodec: Codec[CarId] = deriveCodec[CarId, Int]
  implicit val carNumberCodec: Codec[CarNumber] = deriveCodec[CarNumber, String]
  implicit val carColorCodec: Codec[CarColor] = deriveCodec[CarColor, String]
  implicit val carStampCodec: Codec[CarStamp] = deriveCodec[CarStamp, String]
  implicit val yearOfReleaseCodec: Codec[YearOfRelease] =
    deriveCodec[YearOfRelease, Int]

  implicit val carIdMeta: Meta[CarId] = deriveMeta[CarId, Int]
  implicit val carNumberMeta: Meta[CarNumber] = deriveMeta[CarNumber, String]
  implicit val carColorMeta: Meta[CarColor] = deriveMeta[CarColor, String]
  implicit val carStampMeta: Meta[CarStamp] = deriveMeta[CarStamp, String]
  implicit val yearOfReleaseMeta: Meta[YearOfRelease] =
    deriveMeta[YearOfRelease, Int]

  // stats
  implicit val numberOfRecordsCodec: Codec[NumberOfRecords] =
    deriveCodec[NumberOfRecords, Int]
  implicit val numberOfRecordsMeta: Meta[NumberOfRecords] =
    deriveMeta[NumberOfRecords, Int]

  // requests for log
  implicit val requestIdCodec: Codec[RequestId] =
    deriveCodec[RequestId, Int]
  implicit val requestUrlCodec: Codec[RequestUrl] =
    deriveCodec[RequestUrl, String]
  implicit val requestIdMeta: Meta[RequestId] =
    deriveMeta[RequestId, Int]
  implicit val requestUrlMeta: Meta[RequestUrl] =
    deriveMeta[RequestUrl, String]

  implicit val dateTimeEncoder: Encoder[DateTime] =
    (a: DateTime) => {
      a.toString().asJson
    }
  implicit val dateTimeDecoder: Decoder[DateTime] =
    Decoder[String].map(DateTime.parse)

  implicit val jodaTimeMeta: Meta[DateTime] =
    Meta[Instant].timap(x => new DateTime(x.toEpochMilli))(x =>
      Instant.ofEpochMilli(x.getMillis)
    )

  implicit val carEncoder: Encoder[Car] = (a: Car) =>
    Json.obj(
      ("id", a.id.asJson),
      ("number", a.number.asJson),
      ("stamp", a.stamp.asJson),
      ("color", a.color.asJson),
      ("yearOfRelease", a.yearOfRelease.asJson)
    )

  implicit val statsEncoder: Encoder[CarRepositoryStats] =
    (a: CarRepositoryStats) =>
      Json.obj(
        ("numberOfRecords", a.numberOfRecords.asJson),
        ("firstRecordDateTime", a.firstRecordDateTime.asJson),
        ("lastRecordDateTime", a.lastRecordDateTime.asJson)
      )

  implicit val carDecoder: Decoder[Car] = (c: HCursor) => {
    (
      c.downField("id").as[CarId],
      c.downField("number").as[CarNumber],
      c.downField("stamp").as[CarStamp],
      c.downField("color").as[CarColor],
      c.downField("yearOfRelease").as[YearOfRelease]
    ).mapN(Car.apply)
  }

  implicit val statsDecoder: Decoder[CarRepositoryStats] = (c: HCursor) => {
    (
      c.downField("numberOfRecords").as[NumberOfRecords],
      c.downField("firstRecordDateTime").as[Option[DateTime]],
      c.downField("lastRecordDateTime").as[Option[DateTime]]
    ).mapN(CarRepositoryStats.apply)
  }

  implicit val requestForLogEncoder: Encoder[RequestForLog] =
    (a: RequestForLog) =>
      Json.obj(
        ("id", a.id.asJson),
        ("url", a.url.asJson),
        ("startProcessingAt", a.startProcessingAt.asJson),
        ("endProcessingAt", a.endProcessingAt.asJson)
      )

  implicit val requestStatsEncoder: Encoder[RequestStats] =
    (a: RequestStats) =>
      Json.obj(
        ("url", a.url.asJson),
        ("numberOfRecords", a.numberOfRecords.asJson),
        ("maxRequestTime", a.maxRequestTime.getMillis.asJson)
      )

  implicit val requestForLogDecoder: Decoder[RequestForLog] =
    (c: HCursor) => {
      (
        c.downField("id").as[RequestId],
        c.downField("url").as[RequestUrl],
        c.downField("startProcessingAt").as[DateTime],
        c.downField("endProcessingAt").as[DateTime]
      ).mapN(RequestForLog.apply)
    }

  implicit val requestStatsDecoder: Decoder[RequestStats] =
    (c: HCursor) => {
      (
        c.downField("url").as[RequestUrl],
        c.downField("numberOfRecords").as[NumberOfRecords],
        c.downField("maxRequestTime").as[DateTime]
      ).mapN(RequestStats.apply)
    }

  implicit val jsonMeta: Meta[Json] =
    Meta.Advanced
      .other[PGobject]("jsonb")
      .timap[Json](a =>
        io.circe.jawn.parse(a.getValue).leftMap[Json](e => throw e).merge
      ) { a =>
        val o = new PGobject
        o.setType("jsonb")
        o.setValue(a.noSpaces)
        o
      }

  implicit val carCreateDecoder: Decoder[CarCreate] = (c: HCursor) => {
    (
      c.downField("number").as[CarNumber],
      c.downField("stamp").as[CarStamp],
      c.downField("color").as[CarColor],
      c.downField("yearOfRelease").as[YearOfRelease]
    ).mapN(CarCreate.apply)
  }
}
