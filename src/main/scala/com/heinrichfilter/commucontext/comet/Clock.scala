package com.heinrichfilter.commucontext.comet

import java.util.Date
import net.liftweb.actor.LiftActor
import net.liftweb.common.Full
import net.liftweb.http._
import net.liftweb.http.js.JsCmds._
import net.liftweb.util.Helpers._
import net.liftweb.util._
import scala.xml.Text

object ClockMaster extends LiftActor {
  private var clocks : List[Clock] = Nil

  protected def messageHandler = {
    case SubscribeClock(clk) => {
        clocks ::= clk
      }
    case UnsubClock(clk) => {
        clocks -= clk
      }
    case Tick => {
        clocks.foreach(_ ! Tick)
      }
  }
}

class Clock extends CometActor {
  override def defaultPrefix = Full("clk")
  def render = bind("tick" -> SHtml.ajaxButton(Text("Tick!"), {() => ClockMaster ! Tick
                                                               Noop}),
                    "time" -> timeSpan)
  def timeSpan = (<span id="time">{timeNow} - {uniqueId}</span>)

  override def lowPriority : PartialFunction[Any, Unit] = {
    case Tick => {
        println("Got tick " + new Date());
        partialUpdate(SetHtml("time", Text(timeNow.toString + " - " + this.uniqueId)))
      }
  }
  
  override def localSetup {
    ClockMaster ! SubscribeClock(this)
    super.localSetup()
  }

  override def localShutdown {
    ClockMaster ! UnsubClock(this)
    super.localShutdown()
  }

}

case class SubscribeClock(clock : Clock)
case class UnsubClock(clock : Clock)
case object Tick
