module Main where

import           AngleFunctions
import           Criterion.Main
import           Data.Astro.Time.JulianDate
import           PowerFunctions
import           SunPosition
import           Util

--main :: IO ()
--main = print (directPower 10 0.5)
-- | Benchmark functions using Criterion.
main = defaultMain [
    bgroup "bechmarking..." [ -- would usually be function name here
    -- whnf wants two arguments: a function of one argument and the argument
--        bench "2018 7 30 10 29 0" $ whnf (toLocalTime 2018 7 30 10 29) 0
        bench "2018 8 11 22 0 0" $ whnf (bestAngle (toUniversalTime 2018 8 11 3 0 0) (toUniversalTime 2018 8 11 22 0 0)) 100 -- 5 ms
      , bench "2018 8 11 22 0 0" $ whnf (bestAngle (toUniversalTime 2018 8 11 3 0 0) (toUniversalTime 2018 8 11 22 0 0)) 1000 -- 60 ms
        ]
    ]
