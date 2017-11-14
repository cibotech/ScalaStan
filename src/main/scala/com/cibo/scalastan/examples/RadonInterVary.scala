package com.cibo.scalastan.examples

import com.cibo.scalastan.ScalaStan

object RadonInterVary extends App with ScalaStan {

  val N = data(int(lower = 0))
  val county = data(int(lower = 1, upper = 85)(N))
  val u = data(vector(N))
  val x = data(vector(N))
  val y = data(vector(N))

  val a = parameter(vector(85))
  val b = parameter(vector(85))
  val beta = parameter(vector(2))
  val muA = parameter(real())
  val muB = parameter(real())
  val muBeta = parameter(real())
  val sigmaA = parameter(real(lower = 0, upper = 100))
  val sigmaB = parameter(real(lower = 0, upper = 100))
  val sigmaBeta = parameter(real(lower = 0, upper = 100))
  val sigmaY = parameter(real(lower = 0, upper = 100))

  val inter = new DataTransform(vector(N)) {
    result := u :* x
  }

  val yhat = new ParameterTransform(vector(N)) {
    for (i <- range(1, N)) {
      result(i) := a(county(i)) + x(i) * b(county(i)) + beta(1) * u(i) + beta(2) * inter(i)
    }
  }

  val model = new Model {
    muBeta ~ Normal(0, 1)
    beta ~ Normal(100.0 * muBeta, sigmaBeta)
    muA ~ Normal(0, 1)
    a ~ Normal(muA, sigmaA)
    muB ~ Normal(0, 1)
    b ~ Normal(0.1 * muB, sigmaB)
    y ~ Normal(yhat, sigmaY)
  }

  val xData = Vector[Double](
    1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
    0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0,
    1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 0,
    0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1,
    0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
    1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1,
    1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 1, 1, 1, 0, 1, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0,
    1, 0, 1, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0,
    1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 0,
    1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0,
    1, 1, 0, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    1, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0,
    0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0,
    0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
    1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0,
    0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0,
    0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0
  )

  val yData = Vector[Double](
    0.78845736, 0.78845736, 1.06471074, 0, 1.13140211,
    0.91629073, 0.40546511, 0, -0.35667494, 0.18232156,
    0.18232156, 0.26236426, 0.33647224, -0.91629073, 0.09531018,
    1.5040774, 0.26236426, 0.74193734, 1.77495235, 1.19392247,
    0.58778666, 1.68639895, 1.84054963, 0.64185389, 1.88706965,
    1.13140211, 1.91692261, 1.94591015, 2.04122033, 1.64865863,
    1.5040774, 1.48160454, 1.02961942, 2.09186406, 0.47000363,
    1.43508453, 1.68639895, 1.38629436, 0.83290912, 1.06471074,
    0.33647224, 1.19392247, 1.06471074, 0.58778666, -1.60943791,
    0.87546874, 0.09531018, 0.78845736, -0.51082562, 0.53062825,
    1.06471074, 0.78845736, 0.53062825, 0.33647224, 0.64185389,
    0.58778666, 0.18232156, 1.45861502, 1.5040774, 1.84054963,
    1.5260563, 1.74046617, 0.78845736, -0.91629073, 1.5260563,
    1.48160454, 1.88706965, 0.99325177, 1.06471074, 1.06471074,
    1.97408103, 1.60943791, 0.95551145, 1.60943791, 2.56494936,
    1.97408103, 1.91692261, 2.54944517, 1.75785792, 2.2512918,
    1.79175947, 1.33500107, 2.66025954, 0.58778666, 1.93152141,
    1.54756251, 2.2512918, 0.91629073, 1.90210753, 1.38629436,
    2.31253542, 0.78845736, 0.58778666, 1.22377543, 1.7227666,
    1.45861502, 1.36097655, 0.26236426, 1.43508453, -0.22314355,
    0.69314718, 0.47000363, 2.55722731, 2.68784749, 1.54756251,
    2.2617631, -2.30258509, 1.30833282, 2.00148, 0.64185389,
    1.66770682, 1.38629436, 2.04122033, 0.33647224, 2.30258509,
    2.24070969, -0.22314355, 1.48160454, 1.60943791, 0.74193734,
    0.53062825, 2.09186406, -0.10536052, 2.55722731, 0.95551145,
    1.25276297, 3.28091122, 0.40546511, 2.56494936, 2.17475172,
    2.97041447, 0.91629073, 2.19722458, 2.57261223, 1.28093385,
    1.93152141, 1.56861592, 1.22377543, -0.10536052, 1.22377543,
    0.99325177, 0.33647224, 1.91692261, 2.40694511, -2.30258509,
    0.91629073, 0.58778666, 0.47000363, 0, -0.10536052,
    1.06471074, 1.48160454, 0.40546511, 1.41098697, 0.91629073,
    1.90210753, 1.45861502, 1.70474809, 1.28093385, 1.02961942,
    2.68102153, 1.90210753, 2.07944154, 0.95551145, 1.02961942,
    1.48160454, 0.53062825, 0.69314718, 0.69314718, 0.40546511,
    2.2617631, 2.09186406, 1.25276297, -0.22314355, 1.62924054,
    1.16315081, 2.37954613, 2.10413415, 1.84054963, 1.56861592,
    1.79175947, 0.09531018, 2.16332303, 2.17475172, 1.91692261,
    0.83290912, 0.47000363, 1.02961942, 1.87180218, 0.53062825,
    1.5260563, 1.19392247, 1.48160454, 3.05400118, 2.20827441,
    -0.10536052, 1.58923521, 1.60943791, 0.09531018, 2.02814825,
    1.68639895, 1.28093385, 1.58923521, 1.54756251, 0.33647224,
    1.22377543, 1.43508453, 0.91629073, 0.33647224, 0.33647224,
    0.64185389, 1.56861592, 0.33647224, 1.33500107, 2.17475172,
    1.45861502, 1.48160454, 1.5040774, 0.78845736, -0.69314718,
    1.75785792, 1.68639895, 1.97408103, 1.74046617, 2.00148,
    1.56861592, 1.91692261, 1.85629799, 1.30833282, 1.70474809,
    2.05412373, 1.48160454, 0.99325177, 1.22377543, 1.43508453,
    0.83290912, 0.26236426, 1.64865863, -2.30258509, 0.91629073,
    1.16315081, 1.16315081, 2.2617631, 1.43508453, 2.19722458,
    1.84054963, 3.48431229, 2.58021683, 0.78845736, 1.7227666,
    2.66025954, 1.93152141, 2.02814825, 2.28238239, 0.95551145,
    3.77276094, 1.58923521, 1.58923521, 1.25276297, 1.56861592,
    1.7227666, 1.25276297, 1.36097655, 1.90210753, 2.06686276,
    1.19392247, 0.74193734, 0.47000363, 1.38629436, 0.58778666,
    0.91629073, 2.41591378, 0.95551145, 1.36097655, 2.00148,
    0.26236426, -0.10536052, -0.91629073, 0.91629073, 1.79175947,
    0.69314718, 1.68639895, 1.09861229, 1.06471074, 1.70474809,
    1.41098697, 1.36097655, 2.70136121, 1.97408103, 0.83290912,
    1.02961942, 1.48160454, 0.40546511, 2.1517622, 1.7227666,
    2.1517622, 1.33500107, 0.58778666, 0.64185389, 1.70474809,
    0.91629073, -0.22314355, 0.74193734, 1.02961942, 1.36097655,
    1.45861502, 1.54756251, 1.02961942, 1.41098697, 0.47000363,
    1.45861502, -0.35667494, 1.70474809, 1.19392247, 1.70474809,
    0.91629073, 0.99325177, 2.12823171, 1.19392247, 1.16315081,
    2.1517622, 0.53062825, 1.74046617, 2.56494936, 0.99325177,
    1.54756251, 1.7227666, 2.62466859, 2.02814825, 1.74046617,
    1.5260563, 2.02814825, 0.95551145, 1.5040774, 1.77495235,
    0.78845736, 0.87546874, 1.38629436, 1.5260563, 1.5260563,
    2.38876279, 2.02814825, 1.09861229, 0.40546511, 0.47000363,
    2.80336038, 1.13140211, 1.62924054, 1.58923521, 1.79175947,
    -0.10536052, 0.58778666, 1.36097655, 1.7227666, -0.91629073,
    0.95551145, 1.28093385, 1.82454929, 3.16124671, 1.36097655,
    1.06471074, 1.09861229, 1.54756251, 1.09861229, 1.43508453,
    1.33500107, 1.09861229, 1.45861502, 1.06471074, 1.22377543,
    2.14006616, 2.19722458, 1.56861592, 1.28093385, 0.78845736,
    1.02961942, -0.22314355, 0.40546511, 1.5260563, 1.30833282,
    1.28093385, 1.09861229, 0.78845736, 0.64185389, 0.95551145,
    0.58778666, 0.87546874, 1.45861502, 0.95551145, 0.09531018,
    1.19392247, 0.91629073, 2.24070969, 0.26236426, 2.12823171,
    1.60943791, 1.06471074, 2.57261223, 2.72785283, 0.58778666,
    1.33500107, 2.06686276, 0.95551145, 2.42480273, 1.41098697,
    2.50959926, 1.90210753, 1.93152141, 1.5040774, -0.10536052,
    0.53062825, 0.33647224, 0.69314718, 0, 0,
    1.02961942, 0.26236426, 2.42480273, 2.77258872, 0.26236426,
    0.26236426, 0.47000363, -0.10536052, 1.02961942, -0.69314718,
    0.40546511, 1.96009478, -0.69314718, 2.31253542, 1.45861502,
    1.19392247, 1.06471074, 2.52572864, 1.43508453, 1.5040774,
    1.36097655, 1.19392247, 2.86220088, 2.360854, 2.06686276,
    1.25276297, 1.87180218, 1.93152141, 1.62924054, 2.48490665,
    1.62924054, 2.18605128, 1.75785792, 1.5260563, 1.36097655,
    0.40546511, 3.16968558, -0.10536052, 0.33647224, 0.09531018,
    1.02961942, 3.87535902, -0.10536052, 2.11625551, 1.41098697,
    -0.69314718, 1.90210753, 2.01490302, 2.21920348, -0.69314718,
    0.40546511, 2.3321439, 1.36097655, 0.58778666, 2.29253476,
    0.83290912, 1.48160454, 1.02961942, 0.09531018, 0.18232156,
    0.47000363, 3.23474917, -2.30258509, 2.360854, 0.83290912,
    1.36097655, 1.97408103, 0.74193734, 1.16315081, -0.69314718,
    1.74046617, 0.33647224, 0.74193734, 1.48160454, 0.87546874,
    1.58923521, 1.09861229, 1.09861229, 1.02961942, 1.36097655,
    2.38876279, 1.85629799, 0.69314718, 1.09861229, 1.5040774,
    0.74193734, 2.07944154, 0.26236426, 2.21920348, 0.09531018,
    2.360854, 3.17805383, 2.20827441, 2.49320545, 2.09186406,
    2.37954613, 1.43508453, 2.75366071, 1.68639895, 1.82454929,
    2.27212589, 2.09186406, 0.47000363, 0.47000363, 1.85629799,
    1.48160454, 2.41591378, 2.30258509, 1.5040774, 2.07944154,
    0.83290912, 1.16315081, 1.60943791, 1.41098697, 0.09531018,
    0.69314718, 0.09531018, 1.06471074, 0.74193734, 2.05412373,
    1.33500107, 0.91629073, 1.06471074, 0.53062825, 0.91629073,
    2.24070969, -0.51082562, 0.99325177, 0.09531018, 0.74193734,
    2.48490665, 2.53369681, 1.16315081, 1.43508453, 1.33500107,
    1.30833282, 1.75785792, -1.2039728, 1.41098697, 1.02961942,
    0.64185389, 0.18232156, 0.18232156, 0.40546511, 2.24070969,
    0.53062825, 2.49320545, 1.45861502, 1.93152141, 0.33647224,
    0.91629073, 2.2617631, 1.33500107, 1.22377543, 1.91692261,
    1.28093385, 0.78845736, 0.95551145, 0.74193734, 1.94591015,
    0.18232156, 1.33500107, 1.25276297, 1.43508453, 0.47000363,
    1.02961942, 2.1517622, 1.82454929, 1.64865863, 0.99325177,
    0.18232156, 1.25276297, 1.70474809, 2.31253542, 1.70474809,
    0.18232156, 1.58923521, 1.38629436, 1.25276297, 0.91629073,
    0.18232156, 0.99325177, 0.53062825, 1.13140211, -0.35667494,
    0, 0.64185389, 1.33500107, 2.18605128, 2.00148,
    3.0301337, 1.79175947, 0.74193734, 1.75785792, 2.27212589,
    1.85629799, 1.5260563, 1.7227666, 2.94443898, 0.87546874,
    1.09861229, 1.62924054, 2.04122033, 2.09186406, 1.54756251,
    2.12823171, 0.47000363, 1.79175947, 0.09531018, 2.43361336,
    1.45861502, 1.28093385, 2.3321439, 1.22377543, 1.13140211,
    1.28093385, 0.99325177, 1.38629436, 0.18232156, 0.53062825,
    1.43508453, 2.9601051, 2.20827441, 0.69314718, 2.43361336,
    2.32238772, 0.74193734, 0.18232156, 1.16315081, 0.69314718,
    1.45861502, 0.78845736, 1.68639895, 3.22684399, 1.62924054,
    0.83290912, 1.16315081, 0.91629073, 1.02961942, 1.13140211,
    0.47000363, 1.54756251, 1.38629436, 1.60943791, 0.40546511,
    1.56861592, -0.22314355, -0.69314718, 0.87546874, 0.83290912,
    1.5260563, 2.39789527, 2.70136121, 2.1517622, 1.5040774,
    0.40546511, 1.36097655, 0.58778666, 0.47000363, -0.69314718,
    -0.91629073, -0.69314718, 2.16332303, 0.47000363, 0.33647224,
    2.16332303, 2.40694511, 0.40546511, 0.09531018, -0.10536052,
    -0.35667494, 1.43508453, 1.22377543, 0.74193734, 1.06471074,
    0.58778666, 0.58778666, 0.87546874, 0.53062825, -0.22314355,
    2.45958884, 0.58778666, 1.02961942, 1.25276297, 1.28093385,
    1.25276297, 1.09861229, 1.16315081, 1.13140211, 1.19392247,
    0.53062825, 1.7227666, 1.22377543, 0.40546511, 3.47196645,
    0.09531018, 0.74193734, -0.22314355, 0.40546511, 0.26236426,
    1.13140211, 1.97408103, 0.33647224, 0.26236426, 0.40546511,
    1.60943791, 0.83290912, 0.87546874, 0.18232156, 1.68639895,
    0.09531018, 0.33647224, 1.97408103, 0.09531018, 1.19392247,
    1.16315081, 0.40546511, 1.28093385, -0.22314355, 0.47000363,
    0.33647224, 0.99325177, 1.19392247, -0.10536052, -0.51082562,
    0.69314718, 0.64185389, -0.10536052, 1.68639895, 0.40546511,
    1.13140211, 0.58778666, -0.10536052, 1.19392247, 0.53062825,
    1.13140211, -0.35667494, 1.45861502, 0.33647224, 0.58778666,
    0.40546511, 0.78845736, 0.87546874, 0.99325177, 0.53062825,
    0.09531018, 0.58778666, -1.60943791, 0.78845736, 1.5260563,
    0.74193734, 0.69314718, -0.35667494, 1.85629799, 1.09861229,
    0.69314718, -0.10536052, 1.19392247, 0.58778666, 0.58778666,
    0.78845736, 1.45861502, 2.01490302, 1.85629799, 2.11625551,
    0.74193734, 1.19392247, 0.26236426, 1.60943791, 0,
    1.94591015, 1.74046617, 2.31253542, 1.88706965, 0.95551145,
    1.19392247, 0.40546511, 1.60943791, 2.00148, 2.67414865,
    0.58778666, 2.00148, 0.95551145, 1.30833282, 0.64185389,
    0.78845736, 1.60943791, 1.98787435, 1.30833282, 1.06471074,
    1.48160454, 2.12823171, 1.62924054, 1.28093385, 0.40546511,
    2.1517622, 2.360854, 2.07944154, 1.5040774, 1.09861229,
    0.87546874, 0.40546511, 1.56861592, 1.91692261, 0.74193734,
    1.79175947, 1.06471074, 1.90210753, 2.9601051, 1.38629436,
    1.77495235, 2.19722458, 2.12823171, 0.09531018, 1.13140211,
    2.44234704, 2.2617631, 1.06471074, -0.35667494, 1.16315081,
    1.54756251, 1.56861592, -0.91629073, 2.2300144, 0.53062825,
    -0.10536052, 2.32238772, 2.04122033, 0.78845736, 1.87180218,
    2.50143595, 1.5260563, 1.82454929, 1.87180218, 1.02961942,
    0.64185389, 0.18232156, 0.87546874, 0, 0.18232156,
    0.47000363, -0.22314355, 0.53062825, 1.54756251, 0.53062825,
    1.19392247, -0.22314355, 2.28238239, 1.66770682, 2.14006616,
    0.64185389, 1.88706965, 1.33500107, 1.77495235, 1.58923521,
    0.91629073, 2.37024374, 0.87546874, 0.74193734, 1.54756251,
    1.30833282, 2.59525471, 1.06471074, 1.45861502, 1.33500107,
    0.58778666, 0.40546511, 0.58778666, 0.26236426, 1.88706965,
    3.0155349, 1.79175947, 2.62466859, 2.32238772, 1.74046617,
    2.2300144, 1.22377543, 1.41098697, 2.4510051, 1.97408103,
    1.54756251, 0.58778666, -0.35667494, 1.54756251, 2.32238772,
    2.42480273, 2.02814825, 2.46809953, -0.69314718, 1.90210753,
    1.66770682, 1.13140211, 0.74193734, 1.98787435, 1.62924054,
    0.78845736, 0.83290912, 2.76631911, 2.2512918, 1.85629799,
    1.5040774, 1.60943791, 1.30833282, 1.06471074
  )

  val countyData = Vector[Int](
    1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
    2, 2, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6,
    6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8,
    8, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 11,
    11, 11, 11, 11, 12, 12, 12, 12, 13, 13, 13, 13, 13, 13, 14, 14, 14, 14,
    14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 15, 15, 15, 15, 16, 16, 17, 17,
    17, 17, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 19, 19, 19, 19,
    19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19,
    19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19,
    19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19,
    19, 19, 19, 19, 19, 20, 20, 20, 21, 21, 21, 21, 21, 21, 21, 21, 21, 22,
    22, 22, 22, 22, 22, 23, 23, 24, 24, 24, 24, 24, 24, 24, 24, 24, 25, 25,
    25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 26, 26, 26, 26, 26, 26,
    26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,
    26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,
    26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,
    26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,
    26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,
    26, 26, 26, 26, 26, 26, 26, 26, 26, 27, 27, 27, 27, 27, 27, 28, 28, 28,
    28, 28, 29, 29, 29, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 31, 31,
    31, 31, 31, 32, 32, 32, 32, 33, 33, 33, 33, 34, 34, 34, 35, 35, 35, 35,
    35, 35, 35, 36, 36, 37, 37, 37, 37, 37, 37, 37, 37, 37, 38, 38, 38, 38,
    39, 39, 39, 39, 39, 40, 40, 40, 40, 41, 41, 41, 41, 41, 41, 41, 41, 42,
    43, 43, 43, 43, 43, 43, 43, 43, 43, 44, 44, 44, 44, 44, 44, 44, 45, 45,
    45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 45, 46, 46, 46, 46, 46, 47, 47,
    48, 48, 48, 48, 48, 48, 48, 48, 48, 49, 49, 49, 49, 49, 49, 49, 49, 49,
    49, 49, 49, 49, 50, 51, 51, 51, 51, 52, 52, 52, 53, 53, 53, 54, 54, 54,
    54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54,
    54, 54, 55, 55, 55, 55, 55, 55, 55, 55, 56, 56, 56, 57, 57, 57, 57, 57,
    57, 58, 58, 58, 58, 59, 59, 59, 59, 60, 60, 61, 61, 61, 61, 61, 61, 61,
    61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61, 61,
    61, 61, 61, 61, 61, 61, 61, 62, 62, 62, 62, 62, 63, 63, 63, 64, 64, 64,
    64, 64, 64, 64, 64, 64, 64, 64, 65, 65, 66, 66, 66, 66, 66, 66, 66, 66,
    66, 66, 66, 66, 66, 66, 67, 67, 67, 67, 67, 67, 67, 67, 67, 67, 67, 67,
    67, 68, 68, 68, 68, 68, 68, 68, 68, 69, 69, 69, 69, 70, 70, 70, 70, 70,
    70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70,
    70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70,
    70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70,
    70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70,
    70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70,
    70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70,
    70, 70, 70, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71,
    71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 72, 72, 72, 72, 72, 72, 72, 72,
    72, 72, 73, 73, 74, 74, 74, 74, 75, 75, 75, 76, 76, 76, 76, 77, 77, 77,
    77, 77, 77, 77, 78, 78, 78, 78, 78, 79, 79, 79, 79, 80, 80, 80, 80, 80,
    80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80,
    80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80, 80,
    80, 80, 80, 80, 80, 81, 81, 81, 82, 83, 83, 83, 83, 83, 83, 83, 83, 83,
    83, 83, 83, 83, 84, 84, 84, 84, 84, 84, 84, 84, 84, 84, 84, 84, 84, 85,
    85
  )

  val uData = Vector[Double](
    -0.689047595, -0.689047595, -0.689047595, -0.689047595, -0.84731286,
    -0.84731286, -0.84731286, -0.84731286, -0.84731286, -0.84731286,
    -0.84731286, -0.84731286, -0.84731286, -0.84731286, -0.84731286,
    -0.84731286, -0.84731286, -0.84731286, -0.84731286, -0.84731286,
    -0.84731286, -0.84731286, -0.84731286, -0.84731286, -0.84731286,
    -0.84731286, -0.84731286, -0.84731286, -0.84731286, -0.84731286,
    -0.84731286, -0.84731286, -0.84731286, -0.84731286, -0.84731286,
    -0.84731286, -0.84731286, -0.84731286, -0.84731286, -0.84731286,
    -0.84731286, -0.84731286, -0.84731286, -0.84731286, -0.84731286,
    -0.84731286, -0.84731286, -0.84731286, -0.84731286, -0.84731286,
    -0.84731286, -0.84731286, -0.84731286, -0.84731286, -0.84731286,
    -0.84731286, -0.113458774, -0.113458774, -0.113458774, -0.593352526,
    -0.593352526, -0.593352526, -0.593352526, -0.593352526, -0.593352526,
    -0.593352526, -0.142890481, -0.142890481, -0.142890481, -0.142890481,
    0.387056708, 0.387056708, 0.387056708, 0.271613664, 0.271613664,
    0.271613664, 0.271613664, 0.271613664, 0.271613664, 0.271613664,
    0.271613664, 0.271613664, 0.271613664, 0.271613664, 0.271613664,
    0.271613664, 0.271613664, 0.277578705, 0.277578705, 0.277578705,
    0.277578705, -0.332315488, -0.332315488, -0.332315488, -0.332315488,
    -0.332315488, -0.332315488, -0.332315488, -0.332315488, -0.332315488,
    -0.332315488, 0.095864572, 0.095864572, 0.095864572, 0.095864572,
    0.095864572, 0.095864572, -0.608219807, -0.608219807, -0.608219807,
    -0.608219807, -0.608219807, 0.273684563, 0.273684563, 0.273684563,
    0.273684563, -0.735320087, -0.735320087, -0.735320087, -0.735320087,
    -0.735320087, -0.735320087, 0.343781175, 0.343781175, 0.343781175,
    0.343781175, 0.343781175, 0.343781175, 0.343781175, 0.343781175,
    0.343781175, 0.343781175, 0.343781175, 0.343781175, 0.343781175,
    0.343781175, -0.059860414, -0.059860414, -0.059860414, -0.059860414,
    -0.504995983, -0.504995983, 0.339560321, 0.339560321, 0.339560321,
    0.339560321, -0.6333907, -0.6333907, -0.6333907, -0.6333907,
    -0.6333907, -0.6333907, -0.6333907, -0.6333907, -0.6333907,
    -0.6333907, -0.6333907, -0.6333907, -0.024145162, -0.024145162,
    -0.024145162, -0.024145162, -0.024145162, -0.024145162, -0.024145162,
    -0.024145162, -0.024145162, -0.024145162, -0.024145162, -0.024145162,
    -0.024145162, -0.024145162, -0.024145162, -0.024145162, -0.024145162,
    -0.024145162, -0.024145162, -0.024145162, -0.024145162, -0.024145162,
    -0.024145162, -0.024145162, -0.024145162, -0.024145162, -0.024145162,
    -0.024145162, -0.024145162, -0.024145162, -0.024145162, -0.024145162,
    -0.024145162, -0.024145162, -0.024145162, -0.024145162, -0.024145162,
    -0.024145162, -0.024145162, -0.024145162, -0.024145162, -0.024145162,
    -0.024145162, -0.024145162, -0.024145162, -0.024145162, -0.024145162,
    -0.024145162, -0.024145162, -0.024145162, -0.024145162, -0.024145162,
    -0.024145162, -0.024145162, -0.024145162, -0.024145162, -0.024145162,
    -0.024145162, -0.024145162, -0.024145162, -0.024145162, -0.024145162,
    -0.024145162, 0.26385546, 0.26385546, 0.26385546, 0.155712317,
    0.155712317, 0.155712317, 0.155712317, 0.155712317, 0.155712317,
    0.155712317, 0.155712317, 0.155712317, 0.295025047, 0.295025047,
    0.295025047, 0.295025047, 0.295025047, 0.295025047, 0.414913663,
    0.414913663, 0.224206986, 0.224206986, 0.224206986, 0.224206986,
    0.224206986, 0.224206986, 0.224206986, 0.224206986, 0.224206986,
    0.196610646, 0.196610646, 0.196610646, 0.196610646, 0.196610646,
    0.196610646, 0.196610646, 0.196610646, 0.196610646, 0.196610646,
    0.196610646, 0.196610646, 0.196610646, 0.196610646, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, -0.096520812,
    -0.096520812, -0.096520812, -0.096520812, -0.096520812, 0.503529069,
    0.503529069, 0.503529069, 0.503529069, 0.503529069, 0.503529069,
    -0.400596977, -0.400596977, -0.400596977, -0.400596977, -0.400596977,
    -0.751872233, -0.751872233, -0.751872233, -0.663347631, -0.663347631,
    -0.663347631, -0.663347631, -0.663347631, -0.663347631, -0.663347631,
    -0.663347631, -0.663347631, -0.663347631, -0.663347631, 0.309020285,
    0.309020285, 0.309020285, 0.309020285, 0.309020285, -0.053386009,
    -0.053386009, -0.053386009, -0.053386009, 0.109732943, 0.109732943,
    0.109732943, 0.109732943, -0.007803367, -0.007803367, -0.007803367,
    -0.881828921, -0.881828921, -0.881828921, -0.881828921, -0.881828921,
    -0.881828921, -0.881828921, 0.311029879, 0.311029879, -0.691596384,
    -0.691596384, -0.691596384, -0.691596384, -0.691596384, -0.691596384,
    -0.691596384, -0.691596384, -0.691596384, -0.681708848, -0.681708848,
    -0.681708848, -0.681708848, 0.194447737, 0.194447737, 0.194447737,
    0.194447737, 0.194447737, 0.444903746, 0.444903746, 0.444903746,
    0.444903746, 0.394734406, 0.394734406, 0.394734406, 0.394734406,
    0.394734406, 0.394734406, 0.394734406, 0.394734406, 0.149600343,
    0.013764829, 0.013764829, 0.013764829, 0.013764829, 0.013764829,
    0.013764829, 0.013764829, 0.013764829, 0.013764829, 0.165861836,
    0.165861836, 0.165861836, 0.165861836, 0.165861836, 0.165861836,
    0.165861836, 0.140422594, 0.140422594, 0.140422594, 0.140422594,
    0.140422594, 0.140422594, 0.140422594, 0.140422594, 0.140422594,
    0.140422594, 0.140422594, 0.140422594, 0.140422594, 0.023950874,
    0.023950874, 0.023950874, 0.023950874, 0.023950874, -0.210059522,
    -0.210059522, -0.093226652, -0.093226652, -0.093226652, -0.093226652,
    -0.093226652, -0.093226652, -0.093226652, -0.093226652, -0.093226652,
    0.260932471, 0.260932471, 0.260932471, 0.260932471, 0.260932471,
    0.260932471, 0.260932471, 0.260932471, 0.260932471, 0.260932471,
    0.260932471, 0.260932471, 0.260932471, 0.398849943, 0.248046873,
    0.248046873, 0.248046873, 0.248046873, 0.405451775, 0.405451775,
    0.405451775, 0.265221717, 0.265221717, 0.265221717, 0.243150079,
    0.243150079, 0.243150079, 0.243150079, 0.243150079, 0.243150079,
    0.243150079, 0.243150079, 0.243150079, 0.243150079, 0.243150079,
    0.243150079, 0.243150079, 0.243150079, 0.243150079, 0.243150079,
    0.243150079, 0.243150079, 0.243150079, 0.243150079, 0.243150079,
    0.243150079, 0.243150079, -0.204730369, -0.204730369, -0.204730369,
    -0.204730369, -0.204730369, -0.204730369, -0.204730369, -0.204730369,
    -0.074027668, -0.074027668, -0.074027668, -0.16329217, -0.16329217,
    -0.16329217, -0.16329217, -0.16329217, -0.16329217, 0.478604039,
    0.478604039, 0.478604039, 0.478604039, 0.266111083, 0.266111083,
    0.266111083, 0.266111083, 0.281148274, 0.281148274, -0.418053511,
    -0.418053511, -0.418053511, -0.418053511, -0.418053511, -0.418053511,
    -0.418053511, -0.418053511, -0.418053511, -0.418053511, -0.418053511,
    -0.418053511, -0.418053511, -0.418053511, -0.418053511, -0.418053511,
    -0.418053511, -0.418053511, -0.418053511, -0.418053511, -0.418053511,
    -0.418053511, -0.418053511, -0.418053511, -0.418053511, -0.418053511,
    -0.418053511, -0.418053511, -0.418053511, -0.418053511, -0.418053511,
    -0.418053511, 0.366322259, 0.366322259, 0.366322259, 0.366322259,
    0.366322259, 0.380577977, 0.380577977, 0.380577977, 0.193146093,
    0.193146093, 0.193146093, 0.193146093, 0.193146093, 0.193146093,
    0.193146093, 0.193146093, 0.193146093, 0.193146093, 0.193146093,
    0.528024865, 0.528024865, -0.212045365, -0.212045365, -0.212045365,
    -0.212045365, -0.212045365, -0.212045365, -0.212045365, -0.212045365,
    -0.212045365, -0.212045365, -0.212045365, -0.212045365, -0.212045365,
    -0.212045365, 0.063115634, 0.063115634, 0.063115634, 0.063115634,
    0.063115634, 0.063115634, 0.063115634, 0.063115634, 0.063115634,
    0.063115634, 0.063115634, 0.063115634, 0.063115634, -0.683436482,
    -0.683436482, -0.683436482, -0.683436482, -0.683436482, -0.683436482,
    -0.683436482, -0.683436482, 0.237212123, 0.237212123, 0.237212123,
    0.237212123, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, -0.474673717, -0.474673717, -0.474673717,
    -0.474673717, -0.474673717, 0.116395407, 0.116395407, 0.116395407,
    0.116395407, 0.116395407, 0.116395407, 0.116395407, 0.116395407,
    0.116395407, 0.116395407, 0.116395407, 0.116395407, 0.116395407,
    0.116395407, 0.116395407, 0.116395407, 0.116395407, 0.116395407,
    0.116395407, 0.116395407, 0.116395407, 0.116395407, 0.116395407,
    0.116395407, 0.116395407, 0.269805739, 0.269805739, 0.269805739,
    0.269805739, 0.269805739, 0.269805739, 0.269805739, 0.269805739,
    0.269805739, 0.269805739, 0.470778329, 0.470778329, 0.316028976,
    0.316028976, 0.316028976, 0.316028976, -0.046840067, -0.046840067,
    -0.046840067, 0.497594477, 0.497594477, 0.497594477, 0.497594477,
    0.150082416, 0.150082416, 0.150082416, 0.150082416, 0.150082416,
    0.150082416, 0.150082416, -0.672029732, -0.672029732, -0.672029732,
    -0.672029732, -0.672029732, 0.212414197, 0.212414197, 0.212414197,
    0.212414197, -0.147484283, -0.147484283, -0.147484283, -0.147484283,
    -0.147484283, -0.147484283, -0.147484283, -0.147484283, -0.147484283,
    -0.147484283, -0.147484283, -0.147484283, -0.147484283, -0.147484283,
    -0.147484283, -0.147484283, -0.147484283, -0.147484283, -0.147484283,
    -0.147484283, -0.147484283, -0.147484283, -0.147484283, -0.147484283,
    -0.147484283, -0.147484283, -0.147484283, -0.147484283, -0.147484283,
    -0.147484283, -0.147484283, -0.147484283, -0.147484283, -0.147484283,
    -0.147484283, -0.147484283, -0.147484283, -0.147484283, -0.147484283,
    -0.147484283, -0.147484283, -0.147484283, -0.147484283, -0.147484283,
    -0.147484283, -0.147484283, 0.183237804, 0.183237804, 0.183237804,
    0.236036084, 0.463211867, 0.463211867, 0.463211867, 0.463211867,
    0.463211867, 0.463211867, 0.463211867, 0.463211867, 0.463211867,
    0.463211867, 0.463211867, 0.463211867, 0.463211867, -0.090024275,
    -0.090024275, -0.090024275, -0.090024275, -0.090024275, -0.090024275,
    -0.090024275, -0.090024275, -0.090024275, -0.090024275, -0.090024275,
    -0.090024275, -0.090024275, 0.355286981, 0.355286981
  )

  val result = model
    .withData(x, xData)
    .withData(y, yData)
    .withData(u, uData)
    .withData(county, countyData)
    .run()

  println("yhat: " + result.best(yhat.result))

}