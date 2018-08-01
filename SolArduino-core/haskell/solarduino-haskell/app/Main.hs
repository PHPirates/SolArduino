module Main where

--import Criterion.Main
import PowerFunctions

main :: IO ()
main = print (directPower 0.5 10)
-- | Benchmark functions using Criterion.
--main = defaultMain [
--    bgroup "getSunPosition" [
--        bench "2018 7 30 10 29 0" $ whnf (getSunPosition 2018 7 30 10 29) 0
--        ]
--    ]
