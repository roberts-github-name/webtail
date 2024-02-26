package webtail.utils

import scala.util.control.Breaks

object FnUtils {

  implicit class TwoArgs[A, B](f: (A, B) => Unit) {
    def compose(g: (A, B) => Unit): (A, B) => Unit = {
      (a, b) => {
        f(a, b)
        g(a, b)
      }
    }

    /** Does [[f]], then g1, g2, ... while p returns true */
    def composeCond(p: (A, B) => Boolean, gs: ((A, B) => Unit)*): (A, B) => Unit = {
      (a, b) => {
        f(a, b)
        val br = new Breaks
        br.breakable {
          gs.foreach { g =>
            if (!p(a, b)) br.break()
            g(a, b)
          }
        }
      }
    }
  }
}
