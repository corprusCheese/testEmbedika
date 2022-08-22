package services

import cats.MonadError
import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import domain.{RequestForLog, RequestUrl}
import domain.derivatives.RequestForLogCreate
import dsls.LogRepositoryDsl
import org.joda.time.DateTime
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfter
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class LogServiceTest
    extends AsyncFreeSpec
    with AsyncIOSpec
    with MockitoSugar
    with BeforeAndAfter {

  private val logRepositoryDsl = mock[LogRepositoryDsl[IO]]
  private val requestForLogCreate =
    RequestForLogCreate(RequestUrl("/api"), DateTime.now(), DateTime.now())

  after {
    reset(logRepositoryDsl)
  }

  before {
    when(logRepositoryDsl.getAllRequests).thenReturn(IO(List()))
    when(logRepositoryDsl.saveRequest(requestForLogCreate)).thenReturn(IO(true))
  }

  "LogService" - {
    val logService = LogService[IO](logRepositoryDsl)
    val t = new Throwable("my message")

    "can save" in {
      logService.saveRequest(requestForLogCreate).asserting(_ shouldBe ())
    }
    "can get requests" in {
      logService.getAllRequests.asserting(_ shouldBe List())
    }
    "can't get requests if repository is not working" in {
      when(logRepositoryDsl.getAllRequests)
        .thenReturn(MonadError[IO, Throwable].raiseError(t))

      logService.getAllRequests
        .handleErrorWith(e => IO(t))
        .asserting(_ shouldBe t)
    }
    "can't save if repository is not working" in {
      when(logRepositoryDsl.saveRequest(requestForLogCreate))
        .thenReturn(MonadError[IO, Throwable].raiseError(t))

      logService
        .saveRequest(requestForLogCreate)
        .handleErrorWith(_ => IO(t))
        .asserting(_ shouldBe t)
    }
  }
}
