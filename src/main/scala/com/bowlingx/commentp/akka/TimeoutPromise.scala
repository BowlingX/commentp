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

package com.bowlingx.commentp.akka

import java.util.concurrent.TimeoutException

import akka.actor.ActorSystem

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Promise}
import scala.language.postfixOps

/**
 * A Promise that uses Akka to handle a timeout
 * If Promise is not resolved within specified timeout, it will fail
 * This is a non-blocking operation
 */
object TimeoutPromise {
  def apply[A](implicit timeout: FiniteDuration, executionContext: ExecutionContext, system: ActorSystem): Promise[A] = {

    val prom = Promise[A]()

    // timeout logic
    system.scheduler.scheduleOnce(timeout) {
      val timeoutInSeconds = timeout.toSeconds.toString
      prom tryFailure new TimeoutException(s"Failed to resolve promise, timeout exceeded $timeoutInSeconds seconds")
    }

    prom
  }
}
