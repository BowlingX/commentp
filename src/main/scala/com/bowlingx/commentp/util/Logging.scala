// The MIT License (MIT)
//
// Copyright (c) 2015 David Heidrich, BowlingX <me@bowlingx.com>
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
package com.bowlingx.commentp.util

import org.slf4j.{Logger => SLF4JLogger, LoggerFactory}

trait Logger {

  protected val slf4jLogger: SLF4JLogger

  def error(message: => String) {
    if (slf4jLogger.isErrorEnabled) slf4jLogger.error(message)
  }

  def error(message: => String, t: Throwable) {
    if (slf4jLogger.isErrorEnabled) slf4jLogger.error(message, t)
  }

  def warn(message: => String) {
    if (slf4jLogger.isWarnEnabled) slf4jLogger.warn(message)
  }

  def warn(message: => String, t: Throwable) {
    if (slf4jLogger.isWarnEnabled) slf4jLogger.warn(message, t)
  }

  def info(message: => String) {
    if (slf4jLogger.isInfoEnabled) slf4jLogger.info(message)
  }

  def info(message: => String, t: Throwable) {
    if (slf4jLogger.isInfoEnabled) slf4jLogger.info(message, t)
  }

  def trace(message: => String) {
    if (slf4jLogger.isTraceEnabled) slf4jLogger.trace(message)
  }

  def trace(message: => String, t: Throwable) {
    if (slf4jLogger.isTraceEnabled) slf4jLogger.trace(message, t)
  }

  def debug(message: => String) {
    if (slf4jLogger.isDebugEnabled) slf4jLogger.debug(message)
  }

  def debug(message: => String, t: Throwable) {
    if (slf4jLogger.isDebugEnabled) slf4jLogger.debug(message, t)
  }

}

/**
 * Simple logging trait, mixin for logging capabilities
 */
trait Logging {
  self =>
  protected lazy val logger = new Logger {
    override protected val slf4jLogger = LoggerFactory getLogger self.getClass.getName
  }
}

