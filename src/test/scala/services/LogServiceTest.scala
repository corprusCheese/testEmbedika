package services

import cats.MonadError
import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import domain.RequestUrl
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
    when(logRepositoryDsl.getRequestStats).thenReturn(IO(List()))
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
    "can get request stats" in {
      logService.getRequestStats.asserting(_ shouldBe List())
    }
    "can't get requests if repository is not working" in {
      when(logRepositoryDsl.getAllRequests)
        .thenReturn(MonadError[IO, Throwable].raiseError(t))

      logService.getAllRequests
        .assertThrows[Throwable]
    }
    "can't get request stats if repository is not working" in {
      when(logRepositoryDsl.getRequestStats)
        .thenReturn(MonadError[IO, Throwable].raiseError(t))

      logService.getRequestStats
        .assertThrows[Throwable]
    }
    "can't save if repository is not working" in {
      when(logRepositoryDsl.saveRequest(requestForLogCreate))
        .thenReturn(MonadError[IO, Throwable].raiseError(t))

      logService
        .saveRequest(requestForLogCreate)
        .assertThrows[Throwable]
    }
  }
}
