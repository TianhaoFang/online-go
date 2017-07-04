package webSocket

import java.util.concurrent.atomic.AtomicBoolean

import org.reactivestreams.{Processor, Subscriber, Subscription}

abstract class WebSocketProcessor[In, Out] extends Processor[In, Out]{
  type SubscriberType = Subscriber[_ >: Out]
  private val atomicSet = new AtomicSet[SubscriberType]
  private val notInit = new AtomicBoolean(true)

  override def subscribe(s: SubscriberType): Unit = {
    s.onSubscribe(new Subscription {
      override def cancel(): Unit = {
        atomicSet -= s
      }
      override def request(n: Long): Unit = {}
    })
    atomicSet += s
    if(notInit.getAndSet(false)){
      onConnected()
    }
  }

  override def onError(t: Throwable): Unit = {
    throw t
  }

  override def onComplete(): Unit = {
    atomicSet.foreach(s => s.onComplete())
    onCloseClient()
  }

  override def onNext(t: In): Unit = onReceive(t)

  override def onSubscribe(s: Subscription): Unit = {
    s.request(Long.MaxValue)
  }

  def sendMessage(message: Out): Unit = {
    atomicSet.foreach(s => s.onNext(message))
  }

  def closeClient(): Unit = {
    atomicSet.foreach(s => s.onComplete())
  }

  def onConnected(){}
  def onCloseClient(){}
  def onReceive(message: In)
}
