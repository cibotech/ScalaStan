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

import com.cibo.scalastan.ast.StanDeclaration

case class DataMapping[T <: StanType](
  decl: StanDeclaration[T],
  values: T#SCALA_TYPE
) {
  def emit: String = {
    val nameStr = decl.emit
    val dataStr = decl.returnType.emitData(values.asInstanceOf[decl.returnType.SCALA_TYPE])
    s"$nameStr <- $dataStr"
  }
}
