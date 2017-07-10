package com.fang.segment

import com.fang.ImplicitConvert._
import com.thoughtworks.binding.Binding.{BindingSeq, Vars}
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.Node

object HeadNavBar {
  val NOOP = "javascript:;"

  case class NavItem(title: String, href: String = NOOP)

  @dom def apply(vars: BindingSeq[NavItem]): Binding[Node] = {
    <nav class="navbar navbar-default navbar-fixed-top">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed"
                  data:data-toggle="collapse" data:data-target="#bs-example-navbar-collapse-1">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <div class="navbar-brand">
            <b>OnlineGo</b>
          </div>
        </div>
        <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
          <ul class="nav navbar-nav navbar-right">
            {for (item <- vars) yield <li><a href={item.href}>{item.title}</a></li>}
          </ul>
        </div>
      </div>
    </nav>
  }
}
