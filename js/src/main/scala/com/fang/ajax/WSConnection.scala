package com.fang.ajax

import org.scalajs.dom._

abstract class WSConnection[In, Out](val url: String) {
  val socket = new WebSocket(url)
  socket.onerror = (event: ErrorEvent) => onError(event.message)
  socket.onclose = onClose(_:CloseEvent)
  socket.onopen = onOpen(_:Event)
  socket.onmessage = onMessage(_:MessageEvent)

  private def onMessage(event: MessageEvent) = {
    val data: String = event.data.asInstanceOf[String]
    try {
      val transformed: In = decode(data)
      onReceive(transformed)
    } catch {
      case error: Exception => onError(error.getMessage)
    }
  }

  def sendMessage(out: Out): Unit = {
    try{
      val data: String = encode(out)
      socket.send(data)
    }catch {
      case exception: Exception => onError(exception.getMessage)
    }
  }

  def close(): Unit = socket.close()

  def decode(input: String): In

  def encode(output: Out): String

  def onClose(event: CloseEvent): Unit

  def onError(message: String): Unit

  def onOpen(event: Event): Unit

  def onReceive(data: In): Unit
}