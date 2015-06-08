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

package com.bowlingx.commentp

import javax.servlet.ServletContext

import _root_.akka.actor.ActorSystem
import com.bowlingx.commentp.akka.AkkaBroadcaster
import com.google.inject.Injector
import org.atmosphere.cpr.{AtmosphereFramework, MeteorServlet}

object Bootstrap {
  def prepareContext(context: ServletContext, system: ActorSystem, injector: Injector): Unit = {
    context.setAttribute(AkkaBroadcaster.clusterSystem, system)
    context.setAttribute(classOf[Injector].getName, injector)
  }

  def createAtmosphere(context: ServletContext): AtmosphereFramework = {

    val atmosphereServlet = context.createServlet(classOf[MeteorServlet])
    val framework = atmosphereServlet.framework()

    framework.setDefaultBroadcasterClassName(classOf[AkkaBroadcaster].getName)
    val reg = context.addServlet("WebsocketServlet", atmosphereServlet)
    reg.setAsyncSupported(true)
    reg.setInitParameter("org.atmosphere.servlet", classOf[WebSocketServlet].getName)
    reg.addMapping("/sock/*")

    framework
  }
}
