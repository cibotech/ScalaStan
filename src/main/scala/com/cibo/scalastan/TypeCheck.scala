/*
 * Copyright (c) 2017 - 2020 CiBO Technologies - All Rights Reserved
 * You may use, distribute, and modify this code under the
 * terms of the BSD 3-Clause license.
 *
 * A copy of the license can be found on the root of this repository,
 * at https://github.com/cibotech/ScalaStan/blob/master/LICENSE,
 * or at https://opensource.org/licenses/BSD-3-Clause
 */

package com.cibo.scalastan

import com.cibo.scalastan.ast.{StanLocalDeclaration, StanNode, StanParameterDeclaration, StanValue}

import scala.annotation.implicitNotFound

@implicitNotFound("${T} not a array/vector/matrix type")
sealed class IsCompoundType[T <: StanType]

object IsCompoundType {
  implicit def isCompound[T <: StanCompoundType] = new IsCompoundType[T]
}

@implicitNotFound("${T} not an int/real type")
sealed class IsScalarType[T <: StanType]

object IsScalarType {
  implicit def IsScalarType[T <: StanScalarType] = new IsScalarType[T]
}

@implicitNotFound("multiplication not allowed for ${R} = ${A} * ${B}")
sealed abstract class MultiplicationAllowed[R <: StanType, A <: StanType, B <: StanType] {
  def newType(left: A, right: B): R
}

object MultiplicationAllowed {
  implicit val riMultiplication = new MultiplicationAllowed[StanReal, StanReal, StanInt] {
    def newType(left: StanReal, right: StanInt): StanReal = StanReal()
  }
  implicit val irMultiplication = new MultiplicationAllowed[StanReal, StanInt, StanReal] {
    def newType(left: StanInt, right: StanReal): StanReal = StanReal()
  }
  implicit def scalarMultiplication[T <: StanScalarType] = new MultiplicationAllowed[T, T, T] {
    def newType(left: T, right: T): T = left
  }
  implicit def scalarVectorMultiplication[S <: StanScalarType, V <: StanCompoundType] = new MultiplicationAllowed[V, S, V] {
    def newType(left: S, right: V): V = right
  }
  implicit def vectorScalarMultiplication[S <: StanScalarType, V <: StanCompoundType] = new MultiplicationAllowed[V, V, S] {
    def newType(left: V, right: S): V = left
  }
  implicit val matVecMultiplication = new MultiplicationAllowed[StanVector, StanMatrix, StanVector] {
    def newType(left: StanMatrix, right: StanVector): StanVector = StanVector(left.rows)
  }
  implicit val rvMatMultiplication = new MultiplicationAllowed[StanRowVector, StanRowVector, StanMatrix] {
    def newType(left: StanRowVector, right: StanMatrix): StanRowVector = StanRowVector(right.cols)
  }
  implicit val rvVecMultiplication = new MultiplicationAllowed[StanReal, StanRowVector, StanVector] {
    def newType(left: StanRowVector, right: StanVector): StanReal = StanReal()
  }
  implicit val vecRvMultiplication = new MultiplicationAllowed[StanMatrix, StanVector, StanRowVector] {
    def newType(left: StanVector, right: StanRowVector): StanMatrix = StanMatrix(left.dim, right.dim)
  }
  implicit val matMatMultiplication = new MultiplicationAllowed[StanMatrix, StanMatrix, StanMatrix] {
    def newType(left: StanMatrix, right: StanMatrix): StanMatrix = StanMatrix(left.rows, right.cols)
  }
}

@implicitNotFound("division not allowed for ${R} = ${A} / ${B}")
sealed abstract class DivisionAllowed[R <: StanType, A <: StanType, B <: StanType] {
  def newType(left: A, right: B): R
}

object DivisionAllowed {
  implicit val riDivision = new DivisionAllowed[StanReal, StanReal, StanInt] {
    def newType(left: StanReal, right: StanInt): StanReal = StanReal()
  }
  implicit val irDivision = new DivisionAllowed[StanReal, StanInt, StanReal] {
    def newType(left: StanInt, right: StanReal): StanReal = StanReal()
  }
  implicit def scalarDivision[T <: StanScalarType] = new DivisionAllowed[T, T, T] {
    def newType(left: T, right: T): T = left
  }
  implicit def vecScalarDivision[V <: StanVectorLike, T <: StanScalarType] = new DivisionAllowed[V, V, T] {
    def newType(left: V, right: T): V = left
  }
  implicit def matScalarDivision[T <: StanScalarType] = new DivisionAllowed[StanMatrix, StanMatrix, T] {
    def newType(left: StanMatrix, right: T): StanMatrix = left
  }
  implicit val vecMatDivision = new DivisionAllowed[StanRowVector, StanRowVector, StanMatrix] {
    def newType(left: StanRowVector, right: StanMatrix): StanRowVector = left
  }
  implicit val matMatDivision = new DivisionAllowed[StanMatrix, StanMatrix, StanMatrix] {
    def newType(left: StanMatrix, right: StanMatrix): StanMatrix = left
  }
}

@implicitNotFound("left division not allowed for ${R} = ${A} \\ ${B}")
sealed abstract class LeftDivisionAllowed[R <: StanType, A <: StanType, B <: StanType] {
  def newType(left: A, right: B): R
}

object LeftDivisionAllowed {
  implicit val matVecDivision = new LeftDivisionAllowed[StanVector, StanMatrix, StanVector] {
    def newType(left: StanMatrix, right: StanVector): StanVector = StanVector(left.cols)
  }
  implicit val matMatDivision = new LeftDivisionAllowed[StanMatrix, StanMatrix, StanMatrix] {
    def newType(left: StanMatrix, right: StanMatrix): StanMatrix = right
  }
}

@implicitNotFound("element-wise division not allowed for ${R} = ${A} /:/ ${B}")
sealed abstract class ElementWiseDivisionAllowed[R <: StanType, A <: StanType, B <: StanType] {
  def newType(left: A, right: B): R
}

object ElementWiseDivisionAllowed {
  implicit def sameTypeDivision[T <: StanCompoundType] = new ElementWiseDivisionAllowed[T, T, T] {
    def newType(left: T, right: T): T = left
  }
  implicit def csDivision[C <: StanCompoundType, S <: StanScalarType] = new ElementWiseDivisionAllowed[C, C, S] {
    def newType(left: C, right: S): C = left
  }
  implicit def scDivision[C <: StanCompoundType, S <: StanScalarType] = new ElementWiseDivisionAllowed[C, S, C] {
    def newType(left: S, right: C): C = right
  }
}

@implicitNotFound("addition not allowed for ${R} = ${A} + ${B}")
sealed abstract class AdditionAllowed[R <: StanType, A <: StanType, B <: StanType] {
  def newType(left: A, right: B): R
}

 object AdditionAllowed {
  implicit val riAddition = new AdditionAllowed[StanReal, StanReal, StanInt] {
    def newType(left: StanReal, right: StanInt): StanReal = StanReal()
  }
  implicit val irAddition = new AdditionAllowed[StanReal, StanInt, StanReal] {
    def newType(left: StanInt, right: StanReal): StanReal = StanReal()
  }
  implicit def sameTypeAddition[T <: StanType] = new AdditionAllowed[T, T, T] {
    def newType(left: T, right: T): T = left
  }
  implicit def scalarVectorAddtion[V <: StanCompoundType, S <: StanScalarType] = new AdditionAllowed[V, S, V] {
    def newType(left: S, right: V): V = right
  }
  implicit def vectorScalarAddtion[V <: StanCompoundType, S <: StanScalarType] = new AdditionAllowed[V, V, S] {
    def newType(left: V, right: S): V = left
  }
}

@implicitNotFound("modulus not allowed for ${T} (only int)")
 sealed abstract class ModulusAllowed[T <: StanType] {
  def newType(left: T, right: T): T
}

 object ModulusAllowed {
  implicit def intModulus[T <: StanInt] = new ModulusAllowed[T] {
    def newType(left: T, right: T): T = left
  }
}

@implicitNotFound("logical not allowed for type ${T}")
 sealed class LogicalAllowed[T <: StanType]

 object LogicalAllowed {
  implicit def intLogical[T <: StanInt] = new LogicalAllowed[T]
  implicit def realLogical[T <: StanReal] = new LogicalAllowed[T]
}

@implicitNotFound("distance not allowed between types ${A} and ${B}")
 sealed class DistanceAllowed[A <: StanType, B <: StanType]

 object DistanceAllowed {
  implicit val vvDistance = new DistanceAllowed[StanVector, StanVector]
  implicit val vrDistance = new DistanceAllowed[StanVector, StanRowVector]
  implicit val rvDistance = new DistanceAllowed[StanRowVector, StanVector]
  implicit val rrDistance = new DistanceAllowed[StanRowVector, StanRowVector]
}

@implicitNotFound("transpose not allowed for ${R} = ${T}.t")
 sealed abstract class TransposeAllowed[T <: StanType, R <: StanType] {
  def newType(x: T): R
}

 object TransposeAllowed {
  implicit val matrixTranspose = new TransposeAllowed[StanMatrix, StanMatrix] {
    def newType(x: StanMatrix): StanMatrix = StanMatrix(x.cols, x.rows)
  }
  implicit val vectorTranspose = new TransposeAllowed[StanRowVector, StanVector] {
    def newType(x: StanRowVector): StanVector = StanVector(x.dim)
  }
  implicit val rowVectorTranspose = new TransposeAllowed[StanVector, StanRowVector] {
    def newType(x: StanVector): StanRowVector = StanRowVector(x.dim)
  }
}

@implicitNotFound("rng only allowed in GeneratedQuantity or TransformedData")
sealed trait RngAvailable
object RngAvailable extends RngAvailable

@implicitNotFound("only allowed in a ParameterTransform")
 sealed trait InParameterTransform

 object InParameterTransform extends InParameterTransform

@implicitNotFound("assignment not allowed")
 sealed class AssignmentAllowed[N <: StanNode]

 object AssignmentAllowed {
  implicit def paramAssignment[T <: StanType](
    implicit ev: InParameterTransform
  ): AssignmentAllowed[StanParameterDeclaration[T]] = new AssignmentAllowed[StanParameterDeclaration[T]]

  implicit def localAssignment[T <: StanType]: AssignmentAllowed[StanLocalDeclaration[T]] =
    new AssignmentAllowed[StanLocalDeclaration[T]]

  implicit def generatedQuantityAssignment[T <: StanType](
    implicit ev: RngAvailable
  ): AssignmentAllowed[StanParameterDeclaration[T]] = new AssignmentAllowed[StanParameterDeclaration[T]]
}

@implicitNotFound("implicit conversion from ${FROM} to ${TO} not allowed")
 sealed class CanConvert[FROM <: StanType, TO <: StanType]

 object CanConvert {
  implicit def compoundType[T <: StanCompoundType] = new CanConvert[T, T]
  implicit def scalar2real[T <: StanScalarType] = new CanConvert[T, StanReal]
  implicit val int2int = new CanConvert[StanInt, StanInt]
}

@implicitNotFound("continuous type required, got ${T}")
 sealed class ContinuousType[T <: StanType]

 object ContinuousType {
  implicit def hasRealElement[T <: StanType](implicit ev: T#ELEMENT_TYPE =:= StanReal) = new ContinuousType[T]

  // Because Stan auto-converts ints to reals, we allow bare ints to be treated as continuous.
  implicit val canConvertToReal = new ContinuousType[StanInt]
}

@implicitNotFound("discrete type required, got ${T}")
 sealed class DiscreteType[T <: StanType]

 object DiscreteType {
  implicit def hasIntElement[T <: StanType](implicit ev: T#ELEMENT_TYPE =:= StanInt) = new DiscreteType[T]
}

@implicitNotFound("${T} not a vector, row vector, or array")
 sealed abstract class IsVectorLikeOrArray[T <: StanType] {
  def dim(x: T): StanValue[StanInt]
}

 object IsVectorLikeOrArray {
  implicit val isVector = new IsVectorLikeOrArray[StanVector] {
    def dim(x: StanVector): StanValue[StanInt] = x.dim
  }
  implicit val isRowVector = new IsVectorLikeOrArray[StanRowVector] {
    def dim(x: StanRowVector): StanValue[StanInt] = x.dim
  }
  implicit def isArray[T <: StanType] = new IsVectorLikeOrArray[StanArray[T]] {
    def dim(x: StanArray[T]): StanValue[StanInt] = x.dim
  }
}

@implicitNotFound("${T} not a vector or matrix")
 sealed class IsVectorOrMatrix[T <: StanType]

 object IsVectorOrMatrix {
  implicit val isVector = new IsVectorOrMatrix[StanVector]
  implicit val isMatrix = new IsVectorOrMatrix[StanMatrix]
}

@implicitNotFound("${T} not a row vector or matrix")
 sealed class IsRowVectorOrMatrix[T <: StanType]

 object IsRowVectorOrMatrix {
  implicit val isRowVector = new IsRowVectorOrMatrix[StanRowVector]
  implicit val isMatrix = new IsRowVectorOrMatrix[StanMatrix]
}

@implicitNotFound("${T} not a vector, row vector, or matrix")
 sealed class IsVectorLikeOrMatrix[T <: StanType]

 object IsVectorLikeOrMatrix {
  implicit val isVector = new IsVectorLikeOrMatrix[StanVector]
  implicit val isRowVector = new IsVectorLikeOrMatrix[StanRowVector]
  implicit val isMatrix = new IsVectorLikeOrMatrix[StanMatrix]
}

@implicitNotFound("${T} not a vector, row vector, or an array of vector or row vector")
 sealed class IsVectorLikeOrArrayVectorLike[T <: StanType]

 object IsVectorLikeOrArrayVectorLike {
  implicit def isVectorLike[T <: StanVectorLike] = new IsVectorLikeOrArrayVectorLike[T]
  implicit def isArray[T <: StanVectorLike] = new IsVectorLikeOrArrayVectorLike[StanArray[T]]
}

@implicitNotFound("${T} not a matrix")
 sealed class IsMatrix[T <: StanType]

 object IsMatrix {
  implicit val isMatrix = new IsMatrix[StanMatrix]
}

@implicitNotFound("${T} must be 0 or 1 dimensional")
 sealed class Is0or1Dimensional[T <: StanType]

 object Is0or1Dimensional {
  implicit def isScalar[T <: StanScalarType] = new Is0or1Dimensional[T]
  implicit def isVectorLike[T <: StanVectorLike] = new Is0or1Dimensional[T]
  implicit val isIntArray = new Is0or1Dimensional[StanArray[StanInt]]
  implicit val isRealArray = new Is0or1Dimensional[StanArray[StanReal]]
  implicit val isCatArray = new Is0or1Dimensional[StanArray[StanCategorical]]
}

@implicitNotFound("${T} must be 1 or 2 dimensional")
 sealed class Is1or2Dimensional[T <: StanType]

 object Is1or2Dimensional {
  implicit def isVectorLike[T <: StanVectorLike] = new Is1or2Dimensional[T]
  implicit def isMatrix[T <: StanMatrix] = new Is1or2Dimensional[T]
  implicit def isArray0or1[T <: StanType: Is0or1Dimensional] = new Is1or2Dimensional[StanArray[T]]
}

@implicitNotFound("toMatrix not supported for type ${T}")
 sealed abstract class ToMatrixAllowed[T <: StanType] {
  def newType(x: T): StanMatrix
}

 object ToMatrixAllowed {
  implicit val isVector = new ToMatrixAllowed[StanVector] {
    def newType(x: StanVector): StanMatrix = StanMatrix(x.dim, 1)
  }
  implicit val isRowVector = new ToMatrixAllowed[StanRowVector] {
    def newType(x: StanRowVector): StanMatrix = StanMatrix(1, x.dim)
  }
  implicit val isMatrix = new ToMatrixAllowed[StanMatrix] {
    def newType(x: StanMatrix): StanMatrix = x
  }
  implicit val isIntArrayArray = new ToMatrixAllowed[StanArray[StanArray[StanInt]]] {
    def newType(x: StanArray[StanArray[StanInt]]): StanMatrix = StanMatrix(x.dim, x.inner.dim)
  }
  implicit val isRealArrayArray = new ToMatrixAllowed[StanArray[StanArray[StanReal]]] {
    def newType(x: StanArray[StanArray[StanReal]]): StanMatrix = StanMatrix(x.dim, x.inner.dim)
  }
}

@implicitNotFound("toVector not supported for type ${T}")
 sealed abstract class ToVectorAllowed[T <: StanType] {
  def dim(x: T): StanValue[StanInt]
}

 object ToVectorAllowed {
  implicit val isVector = new ToVectorAllowed[StanVector] {
    def dim(x: StanVector): StanValue[StanInt] = x.dim
  }
  implicit val isRowVector = new ToVectorAllowed[StanRowVector] {
    def dim(x: StanRowVector): StanValue[StanInt] = x.dim
  }
  implicit val isMatrix = new ToVectorAllowed[StanMatrix] {
    def dim(x: StanMatrix): StanValue[StanInt] = x.cols * x.rows
  }
  implicit val isIntArray= new ToVectorAllowed[StanArray[StanInt]] {
    def dim(x: StanArray[StanInt]): StanValue[StanInt] = x.dim
  }
  implicit val isRealArray= new ToVectorAllowed[StanArray[StanReal]] {
    def dim(x: StanArray[StanReal]): StanValue[StanInt] = x.dim
  }
}

@implicitNotFound("appendCol not allowed for ${R} = appendCol(${X}, ${Y})")
 sealed abstract class AppendColAllowed[X <: StanType, Y <: StanType, R <: StanType] {
  def newType(left: X, right: Y): R
}

 object AppendColAllowed {
  implicit val appendColMM = new AppendColAllowed[StanMatrix, StanMatrix, StanMatrix] {
    def newType(left: StanMatrix, right: StanMatrix): StanMatrix = StanMatrix(left.cols + right.cols, left.rows)
  }
  implicit val appendColMV = new AppendColAllowed[StanMatrix, StanVector, StanMatrix] {
    def newType(left: StanMatrix, right: StanVector): StanMatrix = StanMatrix(left.cols + 1, right.dim)
  }
  implicit val appendColVM = new AppendColAllowed[StanVector, StanMatrix, StanMatrix] {
    def newType(left: StanVector, right: StanMatrix): StanMatrix = StanMatrix(right.cols + 1, left.dim)
  }
  implicit val appendColVV = new AppendColAllowed[StanVector, StanVector, StanMatrix] {
    def newType(left: StanVector, right: StanVector): StanMatrix = StanMatrix(left.dim, 2)
  }
  implicit val appendColRR = new AppendColAllowed[StanRowVector, StanRowVector, StanRowVector] {
    def newType(left: StanRowVector, right: StanRowVector): StanRowVector = StanRowVector(left.dim + right.dim)
  }
  implicit val appendColDR = new AppendColAllowed[StanReal, StanRowVector, StanRowVector] {
    def newType(left: StanReal, right: StanRowVector): StanRowVector = StanRowVector(right.dim + 1)
  }
  implicit val appendColRD = new AppendColAllowed[StanRowVector, StanReal, StanRowVector] {
    def newType(left: StanRowVector, right: StanReal): StanRowVector = StanRowVector(left.dim + 1)
  }
}

@implicitNotFound("appendRow not allowed for ${R} = appendCol(${X}, ${Y})")
 sealed abstract class AppendRowAllowed[X <: StanType, Y <: StanType, R <: StanType] {
  def newType(left: X, right: Y): R
}

 object AppendRowAllowed {
  implicit val appendRowMM = new AppendRowAllowed[StanMatrix, StanMatrix, StanMatrix] {
    def newType(left: StanMatrix, right: StanMatrix): StanMatrix = StanMatrix(left.cols, left.rows + right.rows)
  }
  implicit val appendRowMR = new AppendRowAllowed[StanMatrix, StanRowVector, StanMatrix] {
    def newType(left: StanMatrix, right: StanRowVector): StanMatrix = StanMatrix(left.cols, left.rows + 1)
  }
  implicit val appendRowRM = new AppendRowAllowed[StanRowVector, StanMatrix, StanMatrix] {
    def newType(left: StanRowVector, right: StanMatrix): StanMatrix = StanMatrix(right.cols, right.rows + 1)
  }
  implicit val appendRowRR = new AppendRowAllowed[StanRowVector, StanRowVector, StanMatrix] {
    def newType(left: StanRowVector, right: StanRowVector): StanMatrix = StanMatrix(2, left.dim)
  }
  implicit def appendRowVV = new AppendRowAllowed[StanVector, StanVector, StanVector] {
    def newType(left: StanVector, right: StanVector): StanVector = StanVector(left.dim + right.dim)
  }
  implicit val appendRowDV = new AppendRowAllowed[StanReal, StanVector, StanVector] {
    def newType(left: StanReal, right: StanVector): StanVector = StanVector(right.dim + 1)
  }
  implicit val appendRowVD = new AppendRowAllowed[StanVector, StanReal, StanVector] {
    def newType(left: StanVector, right: StanReal): StanVector = StanVector(left.dim + 1)
  }
}

@implicitNotFound("invalid vectorization: ${T}")
 sealed class Vectorized1[T <: StanType]

 object Vectorized1 {
  implicit def validVectorization1[T <: StanType: Is0or1Dimensional] = new Vectorized1[T]
}

@implicitNotFound("invalid vectorization")
 sealed class Vectorized2[A <: StanType, B <: StanType]

 object Vectorized2 {
  implicit def validVectorization2[
    A <: StanType: Is0or1Dimensional,
    B <: StanType: Is0or1Dimensional
  ] = new Vectorized2[A, B]
}

@implicitNotFound("invalid vectorization")
 sealed class Vectorized3[A <: StanType, B <: StanType, C <: StanType]

 object Vectorized3 {
  implicit def validVectorization3[
    A <: StanType: Is0or1Dimensional,
    B <: StanType: Is0or1Dimensional,
    C <: StanType: Is0or1Dimensional
  ] = new Vectorized3[A, B, C]
}

@implicitNotFound("invalid vectorization")
 sealed class Vectorized4[A <: StanType, B <: StanType, C <: StanType, D <: StanType]

 object Vectorized4 {
  implicit def validVectorizationScalar[
    A <: StanType: Is0or1Dimensional,
    B <: StanType: Is0or1Dimensional,
    C <: StanType: Is0or1Dimensional,
    D <: StanType: Is0or1Dimensional
  ] = new Vectorized4[A, B, C, D]
}

@implicitNotFound("index not allowed")
 sealed abstract class IndexAllowed[T <: StanType, I <: StanType, N <: StanType] {
  def nextType(valueType: T, indexType: I): N
}

 object IndexAllowed {
  implicit def dereference[T <: StanType, I <: StanInt] = new IndexAllowed[T, I, T#NEXT_TYPE] {
    def nextType(valueType: T, indexType: I): T#NEXT_TYPE = valueType.next
  }
  implicit def combine1[T <: StanType, I <: StanArray[StanInt]] = new IndexAllowed[T, I, T] {
    def nextType(valueType: T, indexType: I): T = valueType
  }
}

@implicitNotFound("quad_form not allowed for ${T}")
 sealed abstract class QuadFormAllowed[T <: StanType, R <: StanType] {
  def newType(t: T): R
}

 object QuadFormAllowed {
  implicit val qfMatrix = new QuadFormAllowed[StanMatrix, StanMatrix] {
    def newType(t: StanMatrix): StanMatrix = StanMatrix(t.rows, t.cols)
  }
  implicit def qfVectorReal = new QuadFormAllowed[StanVector, StanReal] {
    def newType(t: StanVector): StanReal = StanReal()
  }
}

@implicitNotFound("invalid sample statement")
 sealed class SampleAllowed[T <: StanType, SUPPORT <: StanType]

 object SampleAllowed {
  implicit def sameType[T <: StanType] = new SampleAllowed[T, T]
  implicit def vectorized[T <: StanVectorLike, SUPPORT <: StanScalarType] = new SampleAllowed[T, SUPPORT]
  implicit def arrayVectorized[T <: StanScalarType, SUPPORT <: StanScalarType] = new SampleAllowed[StanArray[T], SUPPORT]
  implicit def vectorVectorized1[T <: StanVectorLike, SUPPORT <: StanVectorLike] = new SampleAllowed[StanArray[T], SUPPORT]
  implicit def vectorVectorized2 = new SampleAllowed[StanVector, StanRowVector]
  implicit def vectorVectorized3 = new SampleAllowed[StanRowVector, StanVector]
}
