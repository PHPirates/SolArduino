module Main where

import Criterion.Main
import SunPosition

-- | Benchmark functions using Criterion.
main :: IO ()
main = defaultMain [
    bgroup "getSunPosition" [
        bench "2018 7 30 10 29 0" $ whnf (getSunPosition 2018 7 30 10 29) 0
        ]
    ]
