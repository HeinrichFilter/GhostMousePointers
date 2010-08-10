package com.heinrichfilter.commucontext {
package snippet {

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.java.util.Date
import com.heinrichfilter.commucontext.comet.ClockMaster
import com.heinrichfilter.commucontext.comet.Tick
import com.heinrichfilter.commucontext.lib._
import Helpers._

class HelloWorld {
  lazy val date: Box[Date] = DependencyFactory.inject[Date] // inject the date

  def howdy(in: NodeSeq): NodeSeq =
  Helpers.bind("b", in, "time" -> date.map(d => Text(d.toString)))

  /*
   lazy val date: Date = DependencyFactory.time.vend // create the date via factory

   def howdy(in: NodeSeq): NodeSeq = Helpers.bind("b", in, "time" -> date.toString)
   */

   def tick(xhtml: NodeSeq): NodeSeq = {
     ClockMaster ! Tick
     return Text("test")
   }
}

}
}
