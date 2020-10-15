/*
 * Copyright (c) 2017 - 2020 CiBO Technologies - All Rights Reserved
 * You may use, distribute, and modify this code under the
 * terms of the BSD 3-Clause license.
 *
 * A copy of the license can be found on the root of this repository,
 * at https://github.com/cibotech/ScalaStan/blob/master/LICENSE,
 * or at https://opensource.org/licenses/BSD-3-Clause
 */

package com.cibo.scalastan.ast

import com.cibo.scalastan._

import scala.language.existentials

// Base class for value types.
abstract class StanValue[T <: StanType] extends StanNode with Implicits {

  // The declaration type used to declare this value.
  // This is used to determine if assignment is allowed.
  type DECL_TYPE <: StanDeclaration[_]

  val returnType: T

  def inputs: Seq[StanDeclaration[_ <: StanType]]
  def outputs: Seq[StanDeclaration[_ <: StanType]]
  def children: Seq[StanValue[_ <: StanType]]

  def export(builder: StanProgramBuilder): Unit

  // Check if this value is derived from data only.
  def isDerivedFromData: Boolean

  // Emit the Stan representation of this value.
  def emit: String

  def unary_-(): StanValue[T] = StanUnaryOperator(returnType, StanUnaryOperator.Negate, this)

  // Logical functions.
  def ===[R <: StanScalarType](right: StanValue[R])(implicit ev: LogicalAllowed[T]): StanValue[StanInt] =
    StanBinaryOperator(StanBinaryOperator.EqualTo, StanInt(), this, right)
  def =/=[R <: StanScalarType](right: StanValue[R])(implicit ev: LogicalAllowed[T]): StanValue[StanInt] =
    StanBinaryOperator(StanBinaryOperator.NotEqualTo, StanInt(), this, right)
  def <[R <: StanScalarType](right: StanValue[R])(implicit ev: LogicalAllowed[T]): StanValue[StanInt] =
    StanBinaryOperator(StanBinaryOperator.LessThan, StanInt(), this, right)
  def <=[R <: StanScalarType](right: StanValue[R])(implicit ev: LogicalAllowed[T]): StanValue[StanInt] =
    StanBinaryOperator(StanBinaryOperator.LessThanOrEqualTo, StanInt(), this, right)
  def >[R <: StanScalarType](right: StanValue[R])(implicit ev: LogicalAllowed[T]): StanValue[StanInt] =
    StanBinaryOperator(StanBinaryOperator.GreaterThan, StanInt(), this, right)
  def >=[R <: StanScalarType](right: StanValue[R])(implicit ev: LogicalAllowed[T]): StanValue[StanInt] =
    StanBinaryOperator(StanBinaryOperator.GreaterThanOrEqualTo, StanInt(), this, right)

  // Boolean operators.
  def unary_!()(implicit ev: LogicalAllowed[T]): StanValue[StanInt] =
    StanUnaryOperator(StanInt(), StanUnaryOperator.LogicalNot, this)
  def ||[R <: StanScalarType](right: StanValue[R])(implicit ev: LogicalAllowed[T]): StanValue[StanInt] =
    StanBinaryOperator(StanBinaryOperator.LogicalOr, StanInt(), this, right)
  def &&[R <: StanScalarType](right: StanValue[R])(implicit ev: LogicalAllowed[T]): StanValue[StanInt] =
    StanBinaryOperator(StanBinaryOperator.LogicalAnd, StanInt(), this, right)

  def +[B <: StanType, R <: StanType](
    right: StanValue[B]
  )(
    implicit ev: AdditionAllowed[R, T, B]
  ): StanValue[R] =
    StanBinaryOperator(StanBinaryOperator.Add, ev.newType(returnType, right.returnType), this, right)

  def -[B <: StanType, R <: StanType](
    right: StanValue[B]
  )(
    implicit ev: AdditionAllowed[R, T, B]
  ): StanValue[R] = StanBinaryOperator(StanBinaryOperator.Subtract, ev.newType(returnType, right.returnType), this, right)

  def *[B <: StanType, R <: StanType](
    right: StanValue[B]
  )(
    implicit ev: MultiplicationAllowed[R, T, B]
  ): StanValue[R] = StanBinaryOperator(StanBinaryOperator.Multiply, ev.newType(returnType, right.returnType), this, right)

  def /[B <: StanType, R <: StanType](
    right: StanValue[B]
  )(
    implicit ev: DivisionAllowed[R, T, B]
  ): StanValue[R] = StanBinaryOperator(StanBinaryOperator.Divide, ev.newType(returnType, right.returnType), this, right)

  def \[B <: StanType, R <: StanType](
    right: StanValue[B]
  )(implicit ev: LeftDivisionAllowed[R, T, B]): StanValue[R] =
    StanBinaryOperator(StanBinaryOperator.LeftDivide, ev.newType(returnType, right.returnType), this, right)

  def %(right: StanValue[T])(implicit ev: ModulusAllowed[T]): StanValue[T] =
    StanBinaryOperator(StanBinaryOperator.Modulus, ev.newType(returnType, right.returnType), this, right)

  def ^[R <: StanScalarType](
    right: StanValue[R]
  )(
    implicit ev: IsScalarType[R]
  ): StanValue[StanReal] = StanBinaryOperator(StanBinaryOperator.Power, StanReal(), this, right)

  // Element-wise operators.
  def *:*(right: StanValue[T])(implicit ev: IsCompoundType[T]): StanValue[T] =
    StanBinaryOperator(StanBinaryOperator.ElementWiseMultiply, returnType, this, right)
  def /:/[B <: StanType, R <: StanType](
    right: StanValue[B]
  )(implicit ev: ElementWiseDivisionAllowed[R, T, B]): StanValue[R] =
    StanBinaryOperator(StanBinaryOperator.ElementWiseDivide, ev.newType(returnType, right.returnType), this, right)

  def ~[SUPPORT <: StanType](dist: StanDistribution[T, SUPPORT])(
    implicit code: StanProgramBuilder,
    ev: SampleAllowed[T, SUPPORT]
  ): Unit = {
    code.append(StanSampleStatement[T, SUPPORT](this, dist))
  }

  def t[R <: StanType](implicit e: TransposeAllowed[T, R]): StanValue[R] = StanTranspose(e.newType(returnType), this)

  def :=[R <: StanType](right: StanValue[R])(
    implicit code: StanProgramBuilder,
    ev1: AssignmentAllowed[DECL_TYPE],
    ev2: CanConvert[R, T]
  ): StanAssignment = {
    val assignment = StanAssignment(this, right)
    code.append(assignment)
    assignment
  }

  def apply[I <: StanType, N <: StanType](
    index: StanValue[I]
  )(
    implicit ev: IndexAllowed[T, I, N]
  ): StanIndexOperator[T, N, DECL_TYPE] =
    StanIndexOperator(ev.nextType(returnType, index.returnType), this, Seq(index))

  def apply(
    index1: StanValue[StanInt],
    index2: StanValue[StanInt]
  ): StanIndexOperator[T, T#NEXT_TYPE#NEXT_TYPE, DECL_TYPE] = {
    StanIndexOperator(returnType.next.next.asInstanceOf[T#NEXT_TYPE#NEXT_TYPE], this, Seq(index1, index2))
  }

  def apply(
    index1: StanValue[StanInt],
    index2: StanValue[StanInt],
    index3: StanValue[StanInt]
  ): StanIndexOperator[T, T#NEXT_TYPE#NEXT_TYPE#NEXT_TYPE, DECL_TYPE] = {
    StanIndexOperator(
      returnType.next.next.next.asInstanceOf[T#NEXT_TYPE#NEXT_TYPE#NEXT_TYPE], this, Seq(index1, index2, index3))
  }

  def apply(
    index1: StanValue[StanInt],
    index2: StanValue[StanInt],
    index3: StanValue[StanInt],
    index4: StanValue[StanInt]
  ): StanIndexOperator[T, T#NEXT_TYPE#NEXT_TYPE#NEXT_TYPE#NEXT_TYPE, DECL_TYPE] = {
    StanIndexOperator(
      returnType.next.next.next.next.asInstanceOf[T#NEXT_TYPE#NEXT_TYPE#NEXT_TYPE#NEXT_TYPE],
      this, Seq(index1, index2, index3, index4))
  }

  def apply(
    slice: StanValueRange
  )(implicit ev: IsCompoundType[T]): StanSliceOperator[T, DECL_TYPE] = StanSliceOperator(this, Seq(slice))

  def apply(
    slice1: StanValueRange,
    slice2: StanValueRange
  )(implicit ev: IsMatrix[T]): StanSliceOperator[T, DECL_TYPE] = StanSliceOperator(this, Seq(slice1, slice2))
}

trait Incrementable[T <: StanType] { self: StanValue[T] =>
  def +=[B <: StanType](right: StanValue[B])(
    implicit ev: AdditionAllowed[T, T, B],
    code: StanProgramBuilder
  ): Unit = {
    code.append(StanAssignment(this, right, StanAssignment.Add))
  }
}

trait Updatable[T <: StanType] extends Incrementable[T] { self: StanValue[T] =>
  def -=[B <: StanType](right: StanValue[B])(
    implicit ev: AdditionAllowed[T, T, B], code: StanProgramBuilder
  ): Unit = {
    code.append(StanAssignment(this, right, StanAssignment.Subtract))
  }

  def *=[B <: StanType](right: StanValue[B])(
    implicit ev: MultiplicationAllowed[T, T, B], code: StanProgramBuilder
  ): Unit = {
    code.append(StanAssignment(this, right, StanAssignment.Multiply))
  }

  def /=[B <: StanScalarType](right: StanValue[B])(
    implicit code: StanProgramBuilder
  ): Unit = {
    code.append(StanAssignment(this, right, StanAssignment.Divide))
  }
}

trait StanFunction {
  def name: String
  def export(builder: StanProgramBuilder): Unit
}

case class BuiltinFunction(name: String) extends StanFunction {
  def export(bulder: StanProgramBuilder): Unit = ()
}

case class StanCall[T <: StanType](
  returnType: T,
  function: StanFunction,
  args: Seq[StanValue[_ <: StanType]],
  id: Int
) extends StanValue[T] {
  def inputs: Seq[StanDeclaration[_ <: StanType]] = args.flatMap(_.inputs)
  def outputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def children: Seq[StanValue[_ <: StanType]] = args
  def isDerivedFromData: Boolean = args.forall(_.isDerivedFromData)
  def export(builder: StanProgramBuilder): Unit = {
    args.foreach(_.export(builder))
    function.export(builder)
  }
  def emit: String = {
    val argStr = args.map(_.emit).mkString(",")
    s"${function.name}($argStr)"
  }
}

object StanCall {
  def apply[T <: StanType](
    returnType: T,
    function: StanFunction,
    args: Seq[StanValue[_ <: StanType]]
  ): StanCall[T] = new StanCall(returnType, function, args, StanNode.getNextId)

  def apply[T <: StanType](
    returnType: T,
    name: String,
    args: Seq[StanValue[_ <: StanType]] = Seq.empty
  ): StanCall[T] = apply(returnType, BuiltinFunction(name), args)
}

case class StanGetTarget(
  id: Int = StanNode.getNextId
) extends StanValue[StanReal] {
  val returnType: StanReal = StanReal()
  def inputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def outputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def children: Seq[StanValue[_ <: StanType]] = Seq.empty
  def isDerivedFromData: Boolean = false
  def export(builder: StanProgramBuilder): Unit = ()
  def emit: String = "target()"
}

case class StanTargetValue(
  id: Int = StanNode.getNextId
) extends StanValue[StanReal] with Incrementable[StanReal] {
  val returnType: StanReal = StanReal()
  def inputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def outputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def children: Seq[StanValue[_ <: StanType]] = Seq.empty
  def isDerivedFromData: Boolean = false
  def export(builder: StanProgramBuilder): Unit = ()
  def emit: String = "target"
  def apply(): StanGetTarget = StanGetTarget()
}

case class StanDistributionNode[T <: StanType](
  name: String,
  y: StanValue[T],
  sep: String,
  args: Seq[StanValue[_ <: StanType]],
  id: Int = StanNode.getNextId
) extends StanValue[StanReal] {
  val returnType: StanReal = StanReal()
  def inputs: Seq[StanDeclaration[_ <: StanType]] = y.inputs ++ args.flatMap(_.inputs)
  def outputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def children: Seq[StanValue[_ <: StanType]] = y +: args
  def isDerivedFromData: Boolean = false
  def export(builder: StanProgramBuilder): Unit = {
    args.foreach(_.export(builder))
    y.export(builder)
  }
  def emit: String = {
    val argStr = args.map(_.emit).mkString(",")
    s"$name(${y.emit} $sep $argStr)"
  }
}

case class StanUnaryOperator[T <: StanType, R <: StanType](
  returnType: R,
  op: StanUnaryOperator.Operator,
  right: StanValue[T],
  id: Int = StanNode.getNextId
) extends StanValue[R] {
  def inputs: Seq[StanDeclaration[_ <: StanType]] = right.inputs
  def outputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def children: Seq[StanValue[_ <: StanType]] = Seq(right)
  def isDerivedFromData: Boolean = right.isDerivedFromData
  def export(builder: StanProgramBuilder): Unit = right.export(builder)
  def emit: String = s"(${op.name}${right.emit})"
}

object StanUnaryOperator {
  sealed abstract class Operator(val name: String)
  case object Negate extends Operator("-")
  case object LogicalNot extends Operator("!")
}

case class StanBinaryOperator[T <: StanType, L <: StanType, R <: StanType](
  op: StanBinaryOperator.Operator,
  returnType: T,
  left: StanValue[L],
  right: StanValue[R],
  id: Int = StanNode.getNextId
) extends StanValue[T] {
  def inputs: Seq[StanDeclaration[_ <: StanType]] = left.inputs ++ right.inputs
  def outputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def children: Seq[StanValue[_ <: StanType]] = Seq[StanValue[_ <: StanType]](left, right)
  def isDerivedFromData: Boolean = left.isDerivedFromData && right.isDerivedFromData
  def export(builder: StanProgramBuilder): Unit = {
    left.export(builder)
    right.export(builder)
  }
  def emit: String = s"(${left.emit} ${op.name} ${right.emit})"
}

object StanBinaryOperator {
  sealed abstract class Operator(val name: String)
  case object Add extends Operator("+")
  case object Subtract extends Operator("-")
  case object Multiply extends Operator("*")
  case object Divide extends Operator("/")
  case object LeftDivide extends Operator("\\")
  case object Modulus extends Operator("%")
  case object Power extends Operator("^")
  case object ElementWiseMultiply extends Operator(".*")
  case object ElementWiseDivide extends Operator("./")
  case object EqualTo extends Operator("==")
  case object NotEqualTo extends Operator("!=")
  case object LessThan extends Operator("<")
  case object LessThanOrEqualTo extends Operator("<=")
  case object GreaterThan extends Operator(">")
  case object GreaterThanOrEqualTo extends Operator(">=")
  case object LogicalOr extends Operator("||")
  case object LogicalAnd extends Operator("&&")
}

case class StanIndexOperator[T <: StanType, N <: StanType, D <: StanDeclaration[_]](
  returnType: N,
  value: StanValue[_ <: StanType],
  indices: Seq[StanValue[_ <: StanType]],
  id: Int = StanNode.getNextId
) extends StanValue[N] {
  type DECL_TYPE = D
  def inputs: Seq[StanDeclaration[_ <: StanType]] = value.inputs ++ indices.flatMap(_.inputs)
  def outputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def children: Seq[StanValue[_ <: StanType]] = value +: indices
  def isDerivedFromData: Boolean = value.isDerivedFromData && indices.forall(_.isDerivedFromData)
  def export(builder: StanProgramBuilder): Unit = {
    value.export(builder)
    indices.foreach(_.export(builder))
  }
  def emit: String = value.emit + indices.map(_.emit).mkString("[", ",", "]")
}

case class StanSliceOperator[T <: StanType, D <: StanDeclaration[_]](
  value: StanValue[T],
  slices: Seq[StanValueRange],
  id: Int = StanNode.getNextId
) extends StanValue[T] {
  type DECL_TYPE = D
  val returnType: T = value.returnType.asInstanceOf[T]
  def inputs: Seq[StanDeclaration[_ <: StanType]] = value.inputs ++ slices.flatMap(s => s.start.inputs ++ s.end.inputs)
  def outputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def children: Seq[StanValue[_ <: StanType]] = slices.flatMap(s => s.start.children ++ s.end.children) :+ value
  def isDerivedFromData: Boolean =
    value.isDerivedFromData && slices.forall(s => s.start.isDerivedFromData && s.end.isDerivedFromData)
  def export(builder: StanProgramBuilder): Unit = {
    value.export(builder)
    slices.foreach(_.export(builder))
  }
  def emit: String = value.emit + slices.map(s => s"${s.start.emit}:${s.end.emit}").mkString("[", ",", "]")
}

case class StanTranspose[T <: StanType, R <: StanType](
  returnType: R,
  value: StanValue[T],
  id: Int = StanNode.getNextId
) extends StanValue[R] {
  def inputs: Seq[StanDeclaration[_ <: StanType]] = value.inputs
  def outputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def children: Seq[StanValue[_ <: StanType]] = Seq(value)
  def isDerivedFromData: Boolean = value.isDerivedFromData
  def export(builder: StanProgramBuilder): Unit = {
    value.export(builder)
  }
  def emit: String = s"${value.emit}'"
}

case class StanTernaryOperator[C <: StanType, T <: StanType](
  cond: StanValue[C],
  left: StanValue[T],
  right: StanValue[T],
  id: Int = StanNode.getNextId
) extends StanValue[T] {
  val returnType: T = left.returnType
  def inputs: Seq[StanDeclaration[_ <: StanType]] = cond.inputs ++ left.inputs ++ right.inputs
  def outputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def children: Seq[StanValue[_ <: StanType]] = Seq[StanValue[_ <: StanType]](cond, left, right)
  def isDerivedFromData: Boolean = cond.isDerivedFromData && left.isDerivedFromData && right.isDerivedFromData
  def export(builder: StanProgramBuilder): Unit = {
    cond.export(builder)
    left.export(builder)
    right.export(builder)
  }
  def emit: String = s"(${cond.emit} ? ${left.emit} : ${right.emit})"
}

case class StanConstant[T <: StanType](
  returnType: T,
  value: T#SCALA_TYPE,
  id: Int = StanNode.getNextId
) extends StanValue[T] {
  def inputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def outputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def children: Seq[StanValue[_ <: StanType]] = Seq.empty
  def isDerivedFromData: Boolean = true
  def export(builder: StanProgramBuilder): Unit = ()
  def emit: String = value.toString
}

case class StanArrayLiteral[N <: StanType, T <: StanArray[N]](
  values: Seq[StanValue[N]],
  id: Int = StanNode.getNextId
) extends StanValue[T] {
  val returnType: T = StanArray(StanConstant[StanInt](StanInt(), values.length), values.head.returnType).asInstanceOf[T]
  def inputs: Seq[StanDeclaration[_ <: StanType]] = values.flatMap(_.inputs)
  def outputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def children: Seq[StanValue[_ <: StanType]] = values
  def isDerivedFromData: Boolean = true
  def export(builder: StanProgramBuilder): Unit = {
    values.foreach(_.export(builder))
  }
  def emit: String = values.map(_.emit).mkString("{", ",", "}")
}

case class StanStringLiteral(
  value: String,
  id: Int = StanNode.getNextId
) extends StanValue[StanString] {
  val returnType: StanString = StanString()
  def inputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def outputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def children: Seq[StanValue[_ <: StanType]] = Seq.empty
  def isDerivedFromData: Boolean = true
  def export(builder: StanProgramBuilder): Unit = ()
  def emit: String = s""""$value""""
}

case class StanLiteral(
  value: String,
  id: Int = StanNode.getNextId
) extends StanValue[StanVoid] {
  val returnType: StanVoid = StanVoid()
  def inputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def outputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def children: Seq[StanValue[_ <: StanType]] = Seq.empty
  def isDerivedFromData: Boolean = true
  def export(builder: StanProgramBuilder): Unit = ()
  def emit: String = value.toString
}

sealed trait StanUnknown[T <: StanType] extends StanValue[T] {
  val id: Int = StanNode.getNextId
  def inputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def outputs: Seq[StanDeclaration[_ <: StanType]] = Seq.empty
  def children: Seq[StanValue[_ <: StanType]] = Seq.empty
  def isDerivedFromData: Boolean = true
  def export(builder: StanProgramBuilder): Unit = ()
  def emit: String = ""
}

case object StanUnknownInt extends StanUnknown[StanInt] {
  val returnType: StanInt = StanInt(None, None)
}

case object StanUnknownReal extends StanUnknown[StanReal] {
  val returnType: StanReal = StanReal(None, None)
}

object StanUnknown {
  def boundOpt[T <: StanType](v: StanValue[T]): Option[StanValue[T]] = v match {
    case _: StanUnknown[T] => None
    case _                 => Some(v)
  }
}
