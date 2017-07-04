package webSocket

import java.util.concurrent.atomic.AtomicReference
import java.util.function.UnaryOperator

import scala.collection.immutable.HashSet
import scala.collection.mutable

class AtomicSet[T] extends mutable.Set[T]{
  private val atomicHashSet = new AtomicReference[HashSet[T]](HashSet())

  def toUnary[R](f: R => R) = new UnaryOperator[R] {
    override def apply(t: R): R = f(t)
  }

  override def +=(elem: T): AtomicSet.this.type = {
    atomicHashSet.updateAndGet(toUnary(_ + elem))
    this
  }

  override def -=(elem: T): AtomicSet.this.type = {
    atomicHashSet.updateAndGet(toUnary(_ - elem))
    this
  }

  override def contains(elem: T): Boolean = {
    atomicHashSet.get().contains(elem)
  }

  override def iterator: Iterator[T] = {
    atomicHashSet.get().iterator
  }
}
