
package org.papamitra.pasori

import com.sun.jna._
import java.nio.IntBuffer
import java.nio.ByteBuffer
import java.util.GregorianCalendar

object Libpafe {
  private val libpafe = NativeLibrary.getInstance("/usr/lib64/libpafe.so")
  lazy val pasori_open = libpafe.getFunction("pasori_open")
  lazy val pasori_close = libpafe.getFunction("pasori_close")
  lazy val pasori_init = libpafe.getFunction("pasori_init")
  lazy val pasori_version = libpafe.getFunction("pasori_version")

  lazy val felica_polling = libpafe.getFunction("felica_polling")
  lazy val felica_read_single = libpafe.getFunction("felica_read_single")

  private val libc = NativeLibrary.getInstance("c")
  lazy val free = libc.getFunction("free")

  def toArgs(args: Any*) = args.map(_.asInstanceOf[AnyRef]).toArray
}

class Pasori {
  import Libpafe._
  private lazy val pasori = pasori_open.invokePointer(Array())
  init

  private[this] def init {
    pasori_init.invokeInt(Array(pasori)) match {
      case 0 => ;
      case _ => throw new Exception("pasori init")
    }
  }

  def close {
    pasori_close.invokeVoid(Array(pasori))
  }

  def version = {
    val v1 = IntBuffer.allocate(8)
    val v2 = IntBuffer.allocate(8)
    val ret = pasori_version.invokeInt(Array(pasori, v1, v2))
    (ret, v1, v2)
  }

  def felicaPolling(polType: Int): Option[Felica] = {
    def loop(cnt: Int): Option[Pointer] = {
      if (cnt == 0) return None
      val pt = polType.asInstanceOf[Short]
      felica_polling.invokePointer(toArgs(pasori, pt, 0, 0)) match {
        case Pointer.NULL => loop(cnt - 1)
        case p => Some(p)
      }
    }
    loop(5).map(p => new Felica(new tag_felica(p)))
  }
}

class Felica(felica: tag_felica) {
  import Libpafe._

  def readBlock(serviceCode: Int, blockNo: Int) = {
    val data = ByteBuffer.allocate(16)
    val ret = felica_read_single.invokeInt(toArgs(felica, serviceCode, 0, blockNo, data))
    (ret, data)
  }

  def close {
    free.invokeInt(Array(felica.getPointer))
  }
}

class SuicaHistory(data: ByteBuffer) {

  val term = data.get(0)
  val proc = data.get(1)
  val date = parseDate(data.getShort(4))
  val inLine = data.get(6)
  val inStation = data.get(7)
  val outLine = data.get(8)
  val outStation = data.get(9)
  val region = data.get(15)

  def parseDate(date:Short) = 
    new GregorianCalendar( 2000 + (date >> 9),     /*year*/
			  ((date >> 5) & 0xf) - 1, /*month(0 origin)*/
			  date & 0x1f)             /*day*/
}

object Main {
  def using[A <: { def close }, B](p: A)(f: A => B) =
    try {
      f(p)
    } finally {
      p.close
    }

  def main(args: Array[String]) {
    using(new Pasori) { pasori =>
      val (ret, v1, v2) = pasori.version
      println(v1.get, v2.get)
      pasori.felicaPolling(LibpafeLibrary.FELICA_POLLING_ANY) match {
        case None => throw new Exception("felica polling");
        case Some(felica) =>
          using(felica) { f =>
            val (ret, data) = f.readBlock(
              LibpafeLibrary.FELICA_SERVICE_SUICA_HISTORY, 0)
            print(new SuicaHistory(data))
            ()
          }
      }
    }
  }
}
