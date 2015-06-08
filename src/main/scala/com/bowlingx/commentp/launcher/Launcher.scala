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

package com.bowlingx.commentp.launcher

import com.bowlingx.commentp.util.Logging
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext

import scala.util.Try

/**
 * Main Entry point, launches embedded jetty
 */
object Launcher extends App with Logging {
  val defaultPort = 8080

  import logger._

  // read the port from environment
  val port = Option(System.getenv("PORT")) flatMap(
    r => Try(r.toInt).toOption) getOrElse Launcher.defaultPort

  info("Booting application")

  val server = new Server(port)
  val context = new WebAppContext()
  context.setContextPath("/")
  context.setResourceBase("webapp")

  // Disable Directory listing
  context.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false")
  // Disable showing stack trace
  context.getErrorHandler.setShowStacks(false)

  server.setHandler(context)

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run() {
      info("Stopping server")
      server.stop()
    }
  })

  server.start
  server.join
}
