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

package com.bowlingx.commentp.atmosphere

import java.util.concurrent.ConcurrentHashMap
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import com.bowlingx.commentp.util.Logging
import org.atmosphere.cpr.AtmosphereResource.TRANSPORT._
import org.atmosphere.cpr.BroadcastFilter.BroadcastAction
import org.atmosphere.cpr.{AtmosphereResource, Broadcaster, BroadcasterFactory, Meteor, PerRequestBroadcastFilter}
import org.eclipse.jetty.http.HttpStatus
import org.scalatra._

import scala.collection.JavaConverters._
import scala.collection.concurrent.{Map => ConcurrentMap}
import scala.util.{Failure, Success, Try}

/**
 * Action Parameters
 * @param req req
 * @param resp response
 * @param routeParams route parameters
 */
case class ActionParams(req: HttpServletRequest, resp: HttpServletResponse, routeParams: MultiParams)

case class RequestParams(req: HttpServletRequest, resp: HttpServletResponse)

/**
 * This is a simple Servlet that supports Pattern matching style REST urls
 */
trait AtmosphereServlet extends HttpServlet with Logging {

  type ActionBlock = ActionParams => Any

  type AtmosphereMatch = PartialFunction[(RequestParams, Any), Option[Any]]

  case class Action(pattern: String, action: ActionBlock)

  private[this] val _routes: ConcurrentMap[HttpMethod, Seq[Action]] =
    new ConcurrentHashMap[HttpMethod, Seq[Action]].asScala

  val CHANNEL_PARAMETER = 'channel

  val broadcasterFactory: BroadcasterFactory

  /**
   * Handle all GET Requests
   */
  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) {
    handle(Get, req, resp)
  }

  /**
   * Handle all POST Requests
   */
  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    handle(Post, req, resp)
  }

  // scalastyle:off

  /**
   * Handles Route dispatching
   * @param m HTTP Method
   * @param req request
   * @param resp response
   */
  private[this] def handle(m: HttpMethod, req: HttpServletRequest, resp: HttpServletResponse) {
    _routes.foreach {
      case (t, actionSeq) if t == m =>
        val matchingRoutes = actionSeq map {
          case (Action(pattern, action)) =>
            val calcPattern = "%s%s" format(getServletContext.getContextPath, pattern)
            val extractedMultiParams = SinatraPathPatternParser(calcPattern)(req.getRequestURI)
            extractedMultiParams map {
              params =>
                (params, action)
            }
        }
        // If Routes are found, select first matching route that was found and execute action block
        matchingRoutes.find(_.isDefined) map {
          case Some((params, action)) =>
            Try(action(ActionParams(req, resp, params))) match {
              case Success(r) => r match {
                case s: Int =>
                  resp.setStatus(s)
                case Unit =>
                case request =>
                  // Explicitly set encoding and content type
                  resp.setCharacterEncoding("UTF-8")
                  resp.setContentType("application/json;charset=UTF-8")
                  resp.getWriter.print(request)
              }
              case Failure(error) =>
                logger.error("Fatal error during servlet processing: %s" format error.getMessage, error)
                resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500)
            }
          case _ => Unit
        } orElse {
          resp.setStatus(HttpStatus.NOT_FOUND_404)
          None
        }

      case _ =>
    }
  }

  // scalastyle:on


  private[this] def addHandler(method: HttpMethod, pattern: String, action: ActionBlock) {
    val el = Action(pattern, action)
    _routes.put(method, _routes.get(method).map(s => s :+ el).getOrElse(Seq(el)))
  }

  /**
   * Adds a new GET route
   * @param pattern pattern to match
   * @param action calling action
   */
  def get(pattern: String)(action: ActionBlock) {
    addHandler(Get, pattern, action)
  }

  /**
   * Adds a new POST route
   * @param pattern pattern to match
   * @param action calling action
   * @return
   */
  def post(pattern: String)(action: ActionBlock) {
    addHandler(Post, pattern, action)
  }


  /**
   * Creates a new Request filter for an atmosphere Result
   * @param block
   * @return
   */
  private def createFilterForBlock(block: AtmosphereMatch) = {
    new PerRequestBroadcastFilter() {
      def filter(broadcasterId: String, r: AtmosphereResource,
                 originalMessage: scala.Any, message: scala.Any): BroadcastAction = {
        // Bind request and response to scope
        block.lift.apply((RequestParams(r.getRequest, r.getResponse), originalMessage)).flatMap {
          result =>
            result.map(new org.atmosphere.cpr.BroadcastFilter.BroadcastAction(_))
        } getOrElse {
          new org.atmosphere.cpr.BroadcastFilter.BroadcastAction(BroadcastAction.ACTION.ABORT, message)
        }
      }

      def filter(broadcasterId: String, originalMessage: scala.Any, message: scala.Any): BroadcastAction = {
        new org.atmosphere.cpr.BroadcastFilter.BroadcastAction(message)
      }
    }
  }


  /**
   * Listens to a given channel, a parameter `:channel` must be present in the URL
   * @param pattern
   * @param block
   * @return
   */
  def channel(pattern: String)(block: AtmosphereMatch): Unit = {
    get(pattern) {
      a =>
        a.routeParams.get(CHANNEL_PARAMETER).flatMap(_.headOption).foreach { channel =>
          log(channel)
          val m = createMeteor(channel, a, block)
          m suspend -1
        }
        Unit
    }
  }

  /**
   * Creates a new Meteor
   * @param id
   * @param action
   * @param atmosphereResult
   * @return
   */
  private[this] def createMeteor(id: String, action: ActionParams, atmosphereResult: AtmosphereMatch): Meteor = {


    val m: Meteor = Meteor.build(action.req)
    val b = broadcasterFactory.lookup(id, true).asInstanceOf[Broadcaster]
    b.setScope(Broadcaster.SCOPE.APPLICATION)
    m.setBroadcaster(b)

    if (!b.getBroadcasterConfig.hasPerRequestFilters) {
      b.getBroadcasterConfig.addFilter(createFilterForBlock(atmosphereResult))
    }

    m resumeOnBroadcast (m.transport() == LONG_POLLING)
    m
  }
}

