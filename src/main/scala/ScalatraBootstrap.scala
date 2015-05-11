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

import javax.servlet.ServletContext

import _root_.akka.actor.{ActorRef, Props}
import com.bowlingx.commentp.akka.{ProtocolActor, AkkaBroadcaster, AkkaCluster, ClusterSystem}
import com.bowlingx.commentp.controllers.Backend
import com.bowlingx.commentp.util.Logging
import com.bowlingx.commentp.{Environment, ServletEnvironment, WebSocketServlet}
import com.google.inject.{Guice, Injector, Provides}
import com.tzavellas.sse.guice.ScalaModule
import org.atmosphere.cpr._
import org.scalatra._

import scala.concurrent.duration._
import scala.language.postfixOps

class ScalatraBootstrap extends LifeCycle with Logging {

  lazy val system = ClusterSystem("commentp", 1 minute, Some("akka-cluster.conf"), None)

  override def init(context: ServletContext) {

    context.setAttribute(AkkaBroadcaster.CLUSTER_SYSTEM, system.cluster)

    val atmosphereServlet = context.createServlet(classOf[MeteorServlet])
    val framework = atmosphereServlet.framework()

    val actionActor = system.cluster.actorOf(Props[ProtocolActor])

    val injector = Guice.createInjector(new ScalaModule() {
      def configure(): Unit = {}
      @Provides
      def provideEnvironment(): Environment = {
        new ServletEnvironment(system, framework.getBroadcasterFactory, actionActor)
      }
    })

    context.setAttribute(classOf[Injector].getName, injector)

    framework.setDefaultBroadcasterClassName(classOf[AkkaBroadcaster].getName)
    val reg = context.addServlet("WebsocketServlet", atmosphereServlet)
    reg.setAsyncSupported(true)
    reg.setInitParameter("org.atmosphere.servlet", classOf[WebSocketServlet].getName)
    reg.addMapping("/sock/*")


    context.mount(injector.getInstance(classOf[Backend]), "/*")
  }

  override def destroy(context: ServletContext): Unit = {
    super.destroy(context)
    system.shutdown()
  }
}
