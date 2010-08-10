package com.heinrichfilter.commucontext.comet

import java.util.Date
import net.liftweb.actor.LiftActor
import net.liftweb.common.Box
import net.liftweb.common.Full
import net.liftweb.http._
import net.liftweb.http.js.JE.Call
import net.liftweb.http.js.JsCmds._
import net.liftweb.util.Helpers._
import net.liftweb.util._
import net.liftweb.http.js.jquery._
import net.liftweb.http.js.{JE,JsCmd,JsCmds, Jx}
import JsCmds._ // For implicits
import JqJE._
import net.liftweb.http.js.JE._
import JE.{JsRaw,Str}
import scala.xml.NodeSeq
import scala.xml.Text
import scala.xml.Unparsed

object MouseMaster extends LiftActor {
  private var mouseList : List[Mouse] = Nil

  protected def messageHandler = {
    case SubscribeMouse(mouse) => {
        mouseList ::= mouse
        println("------SubscribeMouse. Mousecount:" + mouseList.length)
      }
    case UnsubscribeMouse(mouse) => {
        mouseList -= mouse
        println("------UnsubscribeMouse. Mousecount:" + mouseList.length)
      }
    case UpdateAllGhostMouseCoords(uniqueId, mouseCoords) => {
        mouseList.foreach(_ ! UpdateMouseCoords(uniqueId, mouseCoords))
      }
  }
}

class Mouse extends CometActor {
  var mouseCoords = new MouseCoords(0,0)
  override def defaultPrefix = Full("mouse")
  //def render = bind("sendCoordsToServer" -> Text(SHtml.ajaxCall(Str("Button-2"), onMouseMoveServer _)._2 +""),
  def render = bind("sendCoordsToServer" ->      Text(""+SHtml.ajaxCall(JsRaw("e.pageX + ',' + e.pageY"), onMouseMoveServer _)._2),
                    "sendUnbindEventToServer" -> Text(""+SHtml.ajaxCall(JsRaw("'noop'"), onUnbindServer _)._2),
                    "otherPointers" -> <span id="text">otherPointers</span>
  )

  override def lowPriority : PartialFunction[Any, Unit] = {
    case Tick => {
        println("Got tick " + new Date());
        partialUpdate(SetHtml("text", Text(timeNow.toString)))
      }
    case UpdateMouseCoords(uniqueId, mouseCoords) => {
        println("##################################### UpdateMouseCoords Start " + uniqueId)
        partialUpdate(Call("updateOrCreateMouseDiv", uniqueId, mouseCoords.x + "," + mouseCoords.y))
        println("##################################### UpdateMouseCoords End " + uniqueId)
    }
    case CreateDivForMouse(uniqueId) => {
        println("##################################### CreateDivForMouse Start")
        //partialUpdate(Jq("body") ~> JqAppend(<div id = "{uniqueId}">CreateDivForMouse{uniqueId}</div>))
        //partialUpdate(JsRaw("$('body').append('<div id=" + uniqueId + ">Test</p>');Alert('test');"))
        partialUpdate(Call("updateOrCreateMouseDiv", uniqueId, "test"))
        println("##################################### CreateDivForMouse End")
    }
  }
  
  def onMouseMoveServer (str: String) : JsCmd = {
    mouseCoords.setCoords(str)
    MouseMaster ! UpdateAllGhostMouseCoords(this.uniqueId, this.mouseCoords)
    println("Received " + str)
    //partialUpdate(SetHtml("text", Text(str + " - " +timeNow.toString)))
    Noop
    //JsRaw("alert('Button2 clicked')")
  }

  def onUnbindServer (str: String) : JsCmd = {
    this.localShutdown
    //unWatch ???
    println("-------onUnbindServer")
    Noop
  }


  override def localSetup {
    MouseMaster ! SubscribeMouse(this)
    super.localSetup()
  }

  override def localShutdown {
    println("-------localShutdown")
    MouseMaster ! UnsubscribeMouse(this)
    super.localShutdown()
  }

}

class MouseCoords(var x : Int, var y : Int) {
  def setCoords(xCommaY: String) {
    val xyCoordList = List.fromString(xCommaY, ',')
    this.x = xyCoordList(0).toInt
    this.y = xyCoordList(1).toInt
  }
}

case class CreateDivForMouse(uniqueId : String)
case class SubscribeMouse(mouse : Mouse)
case class UnsubscribeMouse(mouse : Mouse)
case class UpdateMouseCoords(uniqueId:String, newMouseCoords:MouseCoords)
case class UpdateAllGhostMouseCoords(uniqueId:String, newMouseCoords:MouseCoords)
