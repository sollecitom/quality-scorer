package sollecitom.quality.scorer.model

/** The user's input can't be graded (e.g. no Kotlin files, unreadable coverage report): a usage error, not a bug. */
class InvalidInputException(message: String) : RuntimeException(message)
