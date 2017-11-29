/*
 * Copyright (c) 2017 CiBO Technologies - All Rights Reserved
 * You may use, distribute, and modify this code under the
 * terms of the BSD 3-Clause license.
 *
 * A copy of the license can be found on the root of this repository,
 * at https://github.com/cibotech/ScalaStan/blob/master/LICENSE,
 * or at https://opensource.org/licenses/BSD-3-Clause
 */

package com.cibo.scalastan

sealed abstract class StanDeclaration[T <: StanType](implicit ss: ScalaStan) extends StanValue[T] with NameLookup {
  private[scalastan] val typeConstructor: T

  protected val internalNameFunc: Function0[Option[String]]
  protected def _userName: Option[String] = internalNameFunc().orElse(NameLookup.lookupName(this))
  protected val _ss: ScalaStan = ss

  private[scalastan] def emit: String = name
  private[scalastan] def emitDeclaration: String = typeConstructor.emitDeclaration(name)

  def size(implicit ev: T <:< StanCompoundType): StanValue[StanInt] = dims.head

  def range(implicit ev: T <:< StanCompoundType): ValueRange = ValueRange(1, size)

  def dims: Seq[StanValue[StanInt]] = typeConstructor.getIndices
}

case class StanDataDeclaration[T <: StanType] private[scalastan] (
  private[scalastan] val typeConstructor: T,
  protected val internalNameFunc: () => Option[String] = () => None
)(implicit ss: ScalaStan) extends StanDeclaration[T] with ReadOnlyIndex[T] {
  require(typeConstructor.isDerivedFromData,
    "data declaration bounds must be derived from other data declarations or constant")
  private[scalastan] type DECL_TYPE = StanDataDeclaration[T]
  private[scalastan] def isDerivedFromData: Boolean = true
}

case class StanParameterDeclaration[T <: StanType] private[scalastan] (
  private[scalastan] val typeConstructor: T,
  protected val internalNameFunc: () => Option[String] = () => None,
  private[scalastan] val indices: Seq[Int] = Seq.empty
)(implicit ss: ScalaStan) extends StanDeclaration[T] with Assignable[T] with Updatable[T] {
  require(typeConstructor.isDerivedFromData,
    "parameter declaration bounds must be derived from data declarations or constant")
  private[scalastan] type DECL_TYPE = StanParameterDeclaration[T]
  private[scalastan] def isDerivedFromData: Boolean = false

  override private[scalastan] def emit: String = {
    val baseName = super.emit
    if (indices.nonEmpty) {
      s"$baseName[" + indices.mkString(",") + "]"
    } else {
      baseName
    }
  }

  def apply(
    index: Int
  ): StanParameterDeclaration[T#NEXT_TYPE] = {
    StanParameterDeclaration(typeConstructor.next, () => Some(name), indices :+ index)
  }

  def apply(
    index1: Int,
    index2: Int
  ): StanParameterDeclaration[T#NEXT_TYPE#NEXT_TYPE] = apply(index1).apply(index2)

  def apply(
    index1: Int,
    index2: Int,
    index3: Int
  ): StanParameterDeclaration[T#NEXT_TYPE#NEXT_TYPE#NEXT_TYPE] = apply(index1, index2).apply(index3)

  def apply(
    index1: Int,
    index2: Int,
    index3: Int,
    index4: Int
  ): StanParameterDeclaration[T#NEXT_TYPE#NEXT_TYPE#NEXT_TYPE#NEXT_TYPE] = apply(index1, index2).apply(index3, index4)
}

case class StanLocalDeclaration[T <: StanType] private[scalastan] (
  private[scalastan] val typeConstructor: T,
  protected val internalNameFunc: () => Option[String] = () => None
)(implicit ss: ScalaStan) extends StanDeclaration[T] with Assignable[T] with Updatable[T] {
  private[scalastan] type DECL_TYPE = StanLocalDeclaration[T]
  private[scalastan] def isDerivedFromData: Boolean = false
}

case class StanInlineDeclaration[T <: StanType](
  protected val decl: StanLocalDeclaration[T]
) extends StanValue[T] {
  private[scalastan] def emit: String = decl.emitDeclaration
  private[scalastan] def isDerivedFromData: Boolean = false
}


