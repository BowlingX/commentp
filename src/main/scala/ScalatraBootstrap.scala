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

import com.bowlingx.commentp.akka.{AkkaBroadcaster, AkkaCluster, ClusterSystem}
import com.bowlingx.commentp.controllers.Website
import com.google.inject.{Guice, Provides}
import com.tzavellas.sse.guice.ScalaModule
import org.scalatra._

import scala.concurrent.duration._
import scala.language.postfixOps

class ScalatraBootstrap extends LifeCycle {

  lazy val system = ClusterSystem("commentp", 1 minute, Some("akka-cluster.conf"), None)

  override def init(context: ServletContext) {

    context.setAttribute(AkkaBroadcaster.CLUSTER_SYSTEM, system.cluster)
    val injector = Guice.createInjector(new ScalaModule() {
      def configure(): Unit = {
      }

      @Provides
      def provideActorSystem(): AkkaCluster = {
        system
      }
    })

    context.mount(injector.getInstance(classOf[Website]), "/*")

  }

  override def destroy(context: ServletContext): Unit = {
    super.destroy(context)
    system.shutdown()
  }
}
