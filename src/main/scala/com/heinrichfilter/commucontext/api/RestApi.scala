package com.heinrichfilter.commucontext.api

import com.heinrichfilter.commucontext.comet.ClockMaster
import com.heinrichfilter.commucontext.comet.Tick
import net.liftweb.common.{Box, Full}
import net.liftweb.http.{GetRequest, LiftResponse, LiftRules, NotFoundResponse, Req}
import net.liftweb.http.rest.XMLApiHelper
import scala.xml.NodeSeq

object RestApi extends XMLApiHelper {
  def dispatch: LiftRules.DispatchPF = {
    case Req("api" :: "clock" :: "tick" :: Nil, "", GetRequest) => () => sendTickToClockMaster()
    case Req("api" :: x :: Nil, "", _) => failure _
  }

  def createTag(in: NodeSeq) = <api>{in}</api>

  def failure(): LiftResponse = {
    val ret: Box[NodeSeq] = Full(<op id="FAILURE"></op>)
    NotFoundResponse()
  }

  def sendTickToClockMaster(): LiftResponse = {
    ClockMaster ! Tick
    Full(<span>Completed!</span>)
  }
}
