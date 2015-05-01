package com.bowlingx.commentp

import com.bowlingx.commentp.controllers.Website
import org.scalatra.test.specs2._

// For more on Specs2, see http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html
class CommentPSpec extends ScalatraSpec { def is =
  "GET / on CommentP"                     ^
    "should return status 200"                  ! root200^
                                                end

  addServlet(classOf[Website], "/*")

  def root200 = get("/") {
    status must_== 200
  }
}
