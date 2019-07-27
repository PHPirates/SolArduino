module Main where

import           Criterion.Main
import           Data.Astro.Time.JulianDate
import           Data.Time.Calendar

import           AngleFunctions
import           PowerFunctions
import           SunPosition
import           TimeConverters

--main :: IO ()
--main = print (directPower 10 0.5)
-- | Benchmark functions using Criterion.
main = defaultMain [
    bgroup "benchmarking..." [ -- would usually be function name here
    -- whnf wants two arguments: a function of one argument and the argument
--        bench "2018 7 30 10 29 0" $ whnf (toLocalTime 2018 7 30 10 29) 0
--        bench "2018 8 11 22 0 0" $ whnf (bestAngle (toUniversalTime 2018 8 11 3 0 0) (toUniversalTime 2018 8 11 22 0 0)) 100 -- 5 ms
--      , bench "2018 8 11 22 0 0" $ whnf (bestAngle (toUniversalTime 2018 8 11 3 0 0) (toUniversalTime 2018 8 11 22 0 0)) 1000 -- 60 ms
        bench "writing angles to file" $ whnf (writeBestAnglesToFile "angles.times" (fromGregorian 2018 8 15) (fromGregorian 2018 9 1) 1000) 10
        ]
    ]
