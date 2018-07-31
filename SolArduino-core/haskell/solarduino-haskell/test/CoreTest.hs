import Test.Tasty
import Test.Tasty.HUnit

import PowerFunctions
import SunPosition (getSunPosition)
import Data.Astro.Time.JulianDate (lctFromYMDHMS)
import Data.Astro.Types
import Data.Astro.Coordinate

-- to run tests in terminal: stack test
main = defaultMain tests

-- Output is compared with the original Mathematica implementation
tests :: TestTree
tests = testGroup "Tests" [directPowerTest, sunPositionTest]

directPowerTest =
    testGroup
        "directPower Tests"
        [ testCase "Test directPower 0.5 10" $ compare (abs (directPower 0.5 10 - 489.607)) 0.001 @?= LT
        , testCase "Test directPower 0.34 253" $ compare (abs (directPower 0.34 253 - 526.962)) 0.001 @?= LT
        , testCase "Test directPower 0 0" $ compare (abs (directPower 0 0 - 576.186)) 0.001 @?= LT
        , testCase "Test directPower 3.14 10" $ compare (abs (directPower 3.14 10)) 0.001 @?= LT
        ]

sunPositionTest =
    testGroup
        "sunPosition tests"
        [ testCase "Test sunPosition 2018 7 30 10 29 0" $
          getSunPosition 2018 7 30 10 29 0 @?= HC (DD 38.89) (DD 111.15)
        ]
