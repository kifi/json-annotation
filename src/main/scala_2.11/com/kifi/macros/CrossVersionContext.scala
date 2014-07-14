package com.kifi.macros

import scala.reflect.macros._
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation

/**
 * Context has been deprecated in Scala 2.11, blackbox.Context is used instead
 */
object CrossVersionDefs {
  type CrossVersionContext = blackbox.Context
}
