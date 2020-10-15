/*
 * Copyright (c) 2017 - 2020 CiBO Technologies - All Rights Reserved
 * You may use, distribute, and modify this code under the
 * terms of the BSD 3-Clause license.
 *
 * A copy of the license can be found on the root of this repository,
 * at https://github.com/cibotech/ScalaStan/blob/master/LICENSE,
 * or at https://opensource.org/licenses/BSD-3-Clause
 */

package com.cibo.scalastan.transform

import com.cibo.scalastan.{StanContext, StanType}
import com.cibo.scalastan.ast._

trait StanTransform[STATE] {

  case class State[+T](run: STATE => (T, STATE)) {
    def flatMap[B](f: T => State[B]): State[B] = State { s =>
      val (a, s2) = run(s)
      f(a).run(s2)
    }

    def map[B](f: T => B): State[B] = State { s =>
      val (a, s2) = run(s)
      f(a) -> s2
    }
  }

  object State {
    def pure[T](t: T): State[T] = State(s => (t, s))

    def traverse[T](vs: Seq[T])(f: T => State[T]): State[Seq[T]] = State { state =>
      vs.foldLeft(Vector.empty[T] -> state) { case ((lst, oldState), v) =>
        val (nv, newState) = f(v).run(oldState)
        (lst :+ nv) -> newState
      }
    }

    def get: State[STATE] = State { s => (s, s) }

    def put(v: STATE): State[Unit] = State { s => ((), v) }

    def modify(f: STATE => STATE): State[Unit] = State { s => ((), f(s)) }
  }

  def initialState: STATE

  def run(program: StanProgram)(implicit context: StanContext): StanProgram = {
    handleProgram(program).run(initialState)._1
  }

  def handleProgram(program: StanProgram)(implicit context: StanContext): State[StanProgram] = {
    for {
      newFuncs <- State.traverse(program.functions)(handleFunction)
      newTransData <- State.traverse(program.transformedData)(handleTransformedData)
      newTransParams <- State.traverse(program.transformedParameters)(handleTransformedParameter)
      newGenQuants <- State.traverse(program.generatedQuantities)(handleGeneratedQuantity)
      newModel <- handleModel(program.model)
    } yield program.copy(
      functions = newFuncs,
      transformedData = newTransData,
      transformedParameters = newTransParams,
      generatedQuantities = newGenQuants,
      model = newModel
    )
  }

  def handleFunction(
    function: StanFunctionDeclaration
  )(implicit context: StanContext): State[StanFunctionDeclaration] = {
    handleRoot(function.code).map(newCode => function.copy(code = newCode))
  }

  def handleTransformedData(
    transform: StanTransformedData
  )(implicit context: StanContext): State[StanTransformedData] = {
    handleRoot(transform.code).map(newCode => transform.copy(code = newCode))
  }

  def handleTransformedParameter(
    transform: StanTransformedParameter
  )(implicit context: StanContext): State[StanTransformedParameter] = {
    handleRoot(transform.code).map(newCode => transform.copy(code = newCode))
  }

  def handleGeneratedQuantity(g: StanGeneratedQuantity)(implicit context: StanContext): State[StanGeneratedQuantity] = {
    handleRoot(g.code).map(newCode => g.copy(code = newCode))
  }

  def handleModel(statement: StanStatement)(implicit context: StanContext): State[StanStatement] = handleRoot(statement)

  def handleRoot(statement: StanStatement)(implicit context: StanContext): State[StanStatement] = dispatch(statement)

  def dispatch(statement: StanStatement)(implicit context: StanContext): State[StanStatement] = statement match {
    case t: StanBlock                 => handleBlock(t)
    case v: StanValueStatement        => handleValue(v)
    case a: StanAssignment            => handleAssignment(a)
    case f: StanForLoop               => handleFor(f)
    case w: StanWhileLoop             => handleWhile(w)
    case i: StanIfStatement           => handleIf(i)
    case b: StanBreakStatement        => handleBreak(b)
    case c: StanContinueStatement     => handleContinue(c)
    case s: StanSampleStatement[_, _] => handleSample(s)
    case r: StanReturnStatement       => handleReturn(r)
    case d: StanInlineDeclaration     => handleDecl(d)
  }

  def dispatchOption(statementOpt: Option[StanStatement])(implicit context: StanContext): State[Option[StanStatement]] = {
    statementOpt match {
      case Some(statement) => dispatch(statement).map(x => Some(x))
      case None            => State.pure(None)
    }
  }

  def handleRange(v: StanValueRange)(implicit context: StanContext): State[StanValueRange] = {
    for {
      newStart <- handleExpression(v.start)
      newEnd <- handleExpression(v.end)
    } yield v.copy(start = newStart, end = newEnd)
  }

  // Process the LHS of an assignment or sample.
  private def handleLHS[T <: StanType](v: StanValue[T])(implicit context: StanContext): State[StanValue[T]] = v match {
    case i: StanIndexOperator[_, T, _] =>
      for {
        newValue <- handleExpression(i.value)
        newIndices <- State.traverse(i.indices)(handleExpression(_))
      } yield i.copy(value = newValue, indices = newIndices)
    case s: StanSliceOperator[T, _]    =>
      for {
        newValue <- handleExpression(s.value)
        newSlices <- State.traverse(s.slices)(handleRange)
      } yield s.copy(value = newValue, slices = newSlices)
    case _                             => State.pure(v)
  }

  def handleBlock(b: StanBlock)(implicit context: StanContext): State[StanStatement] = {
    State.traverse(b.children)(dispatch).map { newChildren =>
      b.copy(children = newChildren)
    }
  }

  def handleValue(v: StanValueStatement)(implicit context: StanContext): State[StanStatement] = State.pure(v)

  def handleAssignment(a: StanAssignment)(implicit context: StanContext): State[StanStatement] = {
    for {
      newLhs <- handleExpression(a.lhs)
      newRhs <- handleExpression(a.rhs)
    } yield a.copy(lhs = newLhs, rhs = newRhs)
  }

  def handleFor(f: StanForLoop)(implicit context: StanContext): State[StanStatement] = {
    for {
      newRange <- handleRange(f.range)
      newBody <- dispatch(f.body)
    } yield f.copy(range = newRange, body = newBody)
  }

  def handleWhile(w: StanWhileLoop)(implicit context: StanContext): State[StanStatement] = {
    for {
      newCond <- handleExpression(w.cond)
      newBody <- dispatch(w.body)
    } yield w.copy(cond = newCond, body = newBody)
  }

  def handleIf(i: StanIfStatement)(implicit context: StanContext): State[StanStatement] = {
    for {
      newConds <- State.traverse(i.conds)(c => dispatch(c._2).map(x => c._1 -> x))
      newOtherwise <- dispatchOption(i.otherwise)
    } yield i.copy(conds = newConds, otherwise = newOtherwise)
  }

  def handleBreak(b: StanBreakStatement)(implicit context: StanContext): State[StanStatement] = State.pure(b)

  def handleContinue(c: StanContinueStatement)(implicit context: StanContext): State[StanStatement] = State.pure(c)

  def handleSample[T <: StanType, R <: StanType](
    s: StanSampleStatement[T, R]
  )(implicit context: StanContext): State[StanStatement] = {
    for {
      newLhs <- handleLHS(s.left)
      newRhs <- handleDistribution(s.right)
    } yield s.copy(left = newLhs, right = newRhs)
  }

  def handleReturn(r: StanReturnStatement)(implicit context: StanContext): State[StanStatement] = {
    handleExpression(r.result).map(newResult => r.copy(result = newResult))
  }

  def handleDecl(d: StanInlineDeclaration)(implicit context: StanContext): State[StanStatement] = State.pure(d)

  private def handleDistribution[T <: StanType, R <: StanType](
    dist: StanDistribution[T, R]
  )(implicit context: StanContext): State[StanDistribution[T, R]] = {
    State.traverse(dist.args)(handleExpression(_)).map { newArgs =>
      dist match {
        case c: StanContinuousDistribution[T, R]          => c.copy(args = newArgs)
        case dc: StanDiscreteDistributionWithCdf[T, R]    => dc.copy(args = newArgs)
        case dn: StanDiscreteDistributionWithoutCdf[T, R] => dn.copy(args = newArgs)
      }
    }
  }

  def handleExpression[T <: StanType](
    expr: StanValue[T]
  )(implicit context: StanContext): State[StanValue[T]] = expr match {
    case call: StanCall[T] => handleCall(call)
    case gt: StanGetTarget => handleGetTarget(gt)
    case tv: StanTargetValue => handleTargetValue(tv)
    case dn: StanDistributionNode[_] => handleDistributionNode(dn)
    case un: StanUnaryOperator[_, T] => handleUnaryOperator(un)
    case bn: StanBinaryOperator[T, _, _] => handleBinaryOperator(bn)
    case in: StanIndexOperator[_, T, _]  => handleIndexOperator(in)
    case sl: StanSliceOperator[T, _]     => handleSliceOperator(sl)
    case tr: StanTranspose[_, T]         => handleTranspose(tr)
    case to: StanTernaryOperator[_, T]   => handleTernaryOperator(to)
    case vr: StanDeclaration[T]          => handleVariable(vr)
    case cn: StanConstant[T]             => handleConstant(cn)
    case ar: StanArrayLiteral[_, _]      => handleArray(ar)
    case st: StanStringLiteral           => handleString(st)
    case lt: StanLiteral                 => handleLiteral(lt)
    case un: StanUnknown[T]              => handleUnknown(un)
  }

  def handleCall[T <: StanType](call: StanCall[T])(implicit context: StanContext): State[StanValue[T]] = {
    State.traverse(call.args)(handleExpression(_)).map(newArgs => call.copy(args = newArgs))
  }

  def handleGetTarget[T <: StanType](gt: StanGetTarget)(implicit context: StanContext): State[StanValue[T]] =
    State.pure(gt.asInstanceOf[StanValue[T]])

  def handleTargetValue[T <: StanType](tv: StanTargetValue)(implicit context: StanContext): State[StanValue[T]] =
    State.pure(tv.asInstanceOf[StanValue[T]])

  def handleDistributionNode[T <: StanType](
    d: StanDistributionNode[_ <: StanType]
  )(implicit context: StanContext): State[StanValue[T]] = {
    for {
      newY <- handleExpression(d.y)
      newArgs <- State.traverse(d.args)(handleExpression(_))
    } yield d.copy(y = newY, args = newArgs).asInstanceOf[StanValue[T]]
  }

  def handleUnaryOperator[T <: StanType, R <: StanType](
    op: StanUnaryOperator[T, R]
  )(implicit context: StanContext): State[StanValue[R]] = {
    handleExpression(op.right).map(newRight => op.copy(right = newRight))
  }

  def handleBinaryOperator[T <: StanType, L <: StanType, R <: StanType](
    op: StanBinaryOperator[T, L, R]
  )(implicit context: StanContext): State[StanValue[T]] = {
    for {
      newLeft <- handleExpression(op.left)
      newRight <- handleExpression(op.right)
    } yield op.copy(left = newLeft, right = newRight)
  }

  def handleIndexOperator[T <: StanType, N <: StanType, D <: StanDeclaration[_]](
    op: StanIndexOperator[T, N, D]
  )(implicit context: StanContext): State[StanValue[N]] = {
    for {
      newValue <- handleExpression(op.value)
      newIndices <- State.traverse(op.indices)(handleExpression(_))
    } yield op.copy(value = newValue, indices = newIndices)
  }

  def handleSliceOperator[T <: StanType, D <: StanDeclaration[_]](
    op: StanSliceOperator[T, D]
  )(implicit context: StanContext): State[StanValue[T]] = {
    for {
      newValue <- handleExpression(op.value)
      newSlices <- State.traverse(op.slices)(handleRange)
    } yield op.copy(value = newValue, slices = newSlices)
  }

  def handleTranspose[T <: StanType, R <: StanType](
    tr: StanTranspose[T, R]
  )(implicit context: StanContext): State[StanValue[R]] = {
    handleExpression(tr.value).map(newValue => tr.copy(value = newValue))
  }

  def handleTernaryOperator[C <: StanType, T <: StanType](
    to: StanTernaryOperator[C, T]
  )(implicit context: StanContext): State[StanValue[T]] = {
    for {
      newCond <- handleExpression(to.cond)
      newLeft <- handleExpression(to.left)
      newRight <- handleExpression(to.right)
    } yield to.copy(cond = newCond, left = newLeft, right = newRight)
  }

  def handleConstant[T <: StanType](cn: StanConstant[T])(implicit context: StanContext): State[StanValue[T]] = {
    State.pure(cn)
  }

  def handleArray[R <: StanType, N <: StanType](
    ar: StanArrayLiteral[N, _]
  )(implicit context: StanContext): State[StanValue[R]] = {
    State.traverse(ar.values)(handleExpression).map { newValues =>
      ar.copy(values = newValues).asInstanceOf[StanValue[R]]
    }
  }

  def handleString[T <: StanType](st: StanStringLiteral)(implicit context: StanContext): State[StanValue[T]] = {
    State.pure(st.asInstanceOf[StanValue[T]])
  }

  def handleLiteral[T <: StanType](l: StanLiteral)(implicit context: StanContext): State[StanValue[T]] = {
    State.pure(l.asInstanceOf[StanValue[T]])
  }

  def handleUnknown[T <: StanType](un: StanUnknown[T])(implicit context: StanContext): State[StanValue[T]] = {
    State.pure(un.asInstanceOf[StanValue[T]])
  }

  def handleVariable[T <: StanType](decl: StanDeclaration[T])(implicit context: StanContext): State[StanValue[T]] = {
    State.pure(decl)
  }

  def allInputs(statement: StanStatement): Seq[StanDeclaration[_]] = {
    statement.inputs ++ statement.children.flatMap(allInputs)
  }

  def allOutputs(statement: StanStatement): Seq[StanDeclaration[_]] = {
    statement.outputs ++ statement.children.flatMap(allOutputs)
  }

  def allValues(statement: StanStatement): Seq[StanValue[_ <: StanType]] = {
    def helper(v: StanValue[_ <: StanType]): Seq[StanValue[_ <: StanType]] = v +: v.children.flatMap(helper)
    statement.values.flatMap(helper) ++ statement.children.flatMap(allValues)
  }
}
