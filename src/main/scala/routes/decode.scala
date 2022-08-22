package routes

import cats.MonadThrow
import cats.implicits.{catsSyntaxApplicativeError, toFlatMapOps}
import io.circe.Decoder
import org.http4s.{Request, Response}
import org.http4s.circe.{JsonDecoder, toMessageSyntax}
import org.http4s.dsl.Http4sDsl

object decode {
  implicit class RequestDecoder[F[_]: JsonDecoder: MonadThrow](
      ar: Request[F]
  ) extends Http4sDsl[F] {
    def decodeWithParseHandling[A: Decoder](
        f: A => F[Response[F]]
    ): F[Response[F]] = {
      ar.asJsonDecode[A]
        .attempt
        .flatMap({
          case Left(_) =>
            UnprocessableEntity("parsing error")
          case Right(value) =>
            f(value)
        })
    }
  }
}
