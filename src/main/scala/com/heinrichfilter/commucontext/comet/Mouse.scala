package com.heinrichfilter.commucontext.comet

import net.liftweb.actor.LiftActor
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.jquery._
import net.liftweb.http.js.{JE,JsCmd,JsCmds}
import net.liftweb.util.Helpers._
import net.liftweb.util._
import JsCmds._ // For implicits
import JqJE._
import JE._
import scala.xml.Text

object MouseMaster extends LiftActor {
  private var mouseList : List[Mouse] = Nil

  protected def messageHandler = {
    case SubscribeMouse(mouse) => {
        mouseList ::= mouse
      }
    case UnsubscribeMouse(mouse) => {
        mouseList -= mouse
        this ! DeleteGhostMouseInAllViews(mouse.uniqueId)
      }
    case UpdateGhostMouseInAllViews(uniqueId, mouseCoords) => {
        mouseList.foreach(_ ! UpdateMouseCoords(uniqueId, mouseCoords))
      }
    case DeleteGhostMouseInAllViews(uniqueId) => {
        mouseList.foreach(_ ! DeleteGhostMouse(uniqueId))
    }
  }
}

class Mouse extends CometActor {
  var mouseCoords = new MouseCoords(0,0)
  override def defaultPrefix = Full("mouse")
  def render = bind("sendCoordsToServer" ->      Text(""+SHtml.ajaxCall(JsRaw("e.pageX + ',' + e.pageY"), onMouseMoveServer _)._2),
                    "sendUnbindEventToServer" -> Text(""+SHtml.ajaxCall(JsRaw("'noop'"), onUnbindServer _)._2)
  )

  override def lowPriority : PartialFunction[Any, Unit] = {
    case UpdateMouseCoords(id, mouseCoords) => {
        partialUpdate(Call("updateOrCreateMouseDiv", id, mouseCoords.x, mouseCoords.y))
      }
    case DeleteGhostMouse(id) => {
        partialUpdate(JqId("mouse_" + id) ~> JqRemove())
    }
  }
  
  def onMouseMoveServer (str: String) : JsCmd = {
    mouseCoords.setCoords(str)
    MouseMaster ! UpdateGhostMouseInAllViews(uniqueId, mouseCoords)
    println("Received " + str)
    Noop
  }

  def onUnbindServer (str: String) : JsCmd = {
    MouseMaster ! DeleteGhostMouseInAllViews(this.uniqueId)
    Noop
  }

  override def localSetup {
    MouseMaster ! SubscribeMouse(this)
    super.localSetup()
  }

  override def localShutdown {
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

case class SubscribeMouse(mouse : Mouse)
case class UnsubscribeMouse(mouse : Mouse)
case class UpdateMouseCoords(ghostMouseId:String, newMouseCoords:MouseCoords)
case class DeleteGhostMouse(ghostMouseId:String)
case class UpdateGhostMouseInAllViews(ghostMouseId:String, newMouseCoords:MouseCoords)
case class DeleteGhostMouseInAllViews(ghostMouseId:String)
