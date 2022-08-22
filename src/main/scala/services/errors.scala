package services

import scala.util.control.NoStackTrace

object errors {
  sealed trait ServiceError extends NoStackTrace

  sealed trait CarServiceError extends ServiceError

  case object CarNumberAlreadyExistsInDatabase extends CarServiceError
  case object CantFindACarForDeletingInDatabase extends CarServiceError
}
