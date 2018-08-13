import           Data.Astro.Coordinate
import           Data.Astro.Time.JulianDate (lctFromYMDHMS)
import           Data.Astro.Types
import           Data.Time.Calendar
import           GoldenSection              (goldenSectionSearch)
import           Test.Tasty
import           Test.Tasty.HUnit

import           AngleFunctions
import           PowerFunctions
import           SunPosition
import           Util

-- to run tests in terminal: stack test
main = defaultMain tests

-- Output is compared with the original Mathematica implementation
tests :: TestTree
tests = testGroup "Tests" [
        directPowerTest
      , sunPositionTest
      , sunMisalignmentTest
      , totalPowerTest
      , optimizationTest
      , dstTest
      , bestAngleTest
      ]

directPowerTest =
    testGroup
        "directPower Tests"
        [ testCase "Test directPower 10 0.5" $ abs (directPower 10 0.5 - 489.607) `compare` 1.0e-3 @?= LT
        , testCase "Test directPower 253 0.34" $ abs (directPower 253 0.34 - 526.962) `compare` 1.0e-3 @?= LT
        , testCase "Test directPower 0 0" $ abs (directPower 0 0 - 576.186) `compare` 1.0e-3 @?= LT
        , testCase "Test directPower 10 3.14" $ abs (directPower 10 3.14) `compare` 1.0e-3 @?= LT
        ]

sunPositionTest =
    testGroup
        "sunPosition tests"
        [ testCase "Test altitude sunPosition 2018 7 30 10 29 0"
            -- horizonCoords = HC (DD 38.89) (DD 111.15) -- altitude, azimuth
            (abs (DD 38.89 - hAltitude (getSunPositionYMDHMS 2018 7 30 10 29 0)) `compare` 0.02 @?= LT )
        , testCase "Test azimuth sunPosition 2018 7 30 10 29 0"
          (abs (DD 111.15 - hAzimuth (getSunPositionYMDHMS 2018 7 30 10 29 0)) `compare` 0.02 @?= LT )
        , testCase "Test altitude sunPosition 2019 1 1 6 0 0"
            (abs (DD (-24.80) - hAltitude (getSunPositionYMDHMS 2019 1 1 6 0 0)) `compare` 0.02 @?= LT )
        , testCase "Test azimuth sunPosition 2019 1 1 6 0 0"
          (abs (DD 96.37 - hAzimuth (getSunPositionYMDHMS 2019 1 1 6 0 0)) `compare` 0.02 @?= LT )
        , testCase "Test altitude sunPosition 2019 1 1 12 0 0"
            (abs (DD 14.77 - hAltitude (getSunPositionYMDHMS 2019 1 1 12 0 0)) `compare` 0.02 @?= LT )
        , testCase "Test azimuth sunPosition 2019 1 1 12 0 0"
          (abs (DD 169.12 - hAzimuth (getSunPositionYMDHMS 2019 1 1 12 0 0)) `compare` 0.02 @?= LT )
        ]

sunMisalignmentTest = testGroup "sunMisalignment tests" [
        testCase "Test (getSunPosition 2018 8 1 15 57 0) 42" (
        abs (0.7983 - sunMisalignment (getSunPositionYMDHMS 2018 8 1 15 57 0) 42) `compare` 0.001 @?= LT
        )
      , testCase "Test (getSunPosition 2020 11 24 17 15 0) 60" (
        abs (1.5407 - sunMisalignment (getSunPositionYMDHMS 2020 11 24 17 15 0) 60) `compare` 0.001 @?= LT
        )
    ]

totalPowerTest = testGroup "totalPower tests" [
        testCase "Test 145 24"
        (abs (237.324 -
        totalPower [getSunPositionYMDHMS 2018 8 1 18 35 0, getSunPositionYMDHMS 2018 3 3 16 35 0] 145 24
        ) `compare` 0.5 @?= LT)
      , testCase "Test 2 5"
        (abs (767.742 -
        totalPower [getSunPositionYMDHMS 2018 2 1 20 35 0, getSunPositionYMDHMS 2018 3 17 12 0 0, getSunPositionYMDHMS 2018 3 17 12 10 0] 2 5
        ) `compare` 0.5 @?= LT)
      , testCase "Test 212 0"
        (abs (619.392 -
        totalPower [getSunPositionYMDHMS 2018 8 1 19 0 0, getSunPositionYMDHMS 2018 8 1 19 10 0, getSunPositionYMDHMS 2018 8 1 19 16 0, getSunPositionYMDHMS 2018 8 1 19 18 0] 212 0
        ) `compare` 0.5 @?= LT)
    ]

optimizationTest =
    testGroup
        "Test the accuracy of the optimization method"
        [ testCase "Test optimizing directPower convergence" $
           (snd result - fst result) `compare` 1e-5 @?= LT
        , testCase "Test optimizing directPower fst result" $
          abs ( negate $ fst result) `compare` 1e-5 @?= LT
        , testCase "Test optimizing directPower snd result" $
          abs ( negate $ snd result) `compare` 1e-5 @?= LT
        ]
        where result = goldenSectionSearch (directPower 42) (-pi / 3) (pi / 3) 1e-5

dstTest =
    testGroup
        "Test summer/winter time (DST) check"
        [ testCase "Test last winter day" $ isSummerTime (fromGregorian 2018 3 25) @?= False
        , testCase "Test first summer day" $ isSummerTime (fromGregorian 2018 3 26) @?= True
        , testCase "Test last summer day" $ isSummerTime (fromGregorian 2018 10 27) @?= True
        , testCase "Test first winter day" $ isSummerTime (fromGregorian 2018 10 28) @?= False
        ]

bestAngleTest =
    testGroup
        "Test best angle over time period"
        [ testCase "Test 2018 8 11" $
          abs (bestAngle (toUniversalTime 2018 8 11 3 0 0) (toUniversalTime 2018 8 11 22 0 0) 100 - 28.5) `compare` 0.1 @?= LT
        ]
