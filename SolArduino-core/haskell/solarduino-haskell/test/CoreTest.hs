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
tests = testGroup "Tests" [
        directPowerTest
      , sunPositionTest
      , sunMisalignmentTest
      , totalPowerTest
      ]

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
        [ testCase "Test altitude sunPosition 2018 7 30 10 29 0"
            -- horizonCoords = HC (DD 38.89) (DD 111.15) -- altitude, azimuth
            (abs (DD 38.89 - hAltitude (getSunPosition 2018 7 30 10 29 0)) `compare` 0.02 @?= LT )
        , testCase "Test azimuth sunPosition 2018 7 30 10 29 0"
          (abs (DD 111.15 - hAzimuth (getSunPosition 2018 7 30 10 29 0)) `compare` 0.02 @?= LT )
        , testCase "Test altitude sunPosition 2019 1 1 6 0 0"
            (abs (DD (-34.11) - hAltitude (getSunPosition 2019 1 1 6 0 0)) `compare` 0.02 @?= LT )
        , testCase "Test azimuth sunPosition 2019 1 1 6 0 0"
          (abs (DD 84.65 - hAzimuth (getSunPosition 2019 1 1 6 0 0)) `compare` 0.02 @?= LT )
        , testCase "Test altitude sunPosition 2019 1 1 12 0 0"
            (abs (DD 11.92 - hAltitude (getSunPosition 2019 1 1 12 0 0)) `compare` 0.02 @?= LT )
        , testCase "Test azimuth sunPosition 2019 1 1 12 0 0"
          (abs (DD 155.24 - hAzimuth (getSunPosition 2019 1 1 12 0 0)) `compare` 0.02 @?= LT )
        ]

sunMisalignmentTest = testGroup "sunMisalignment tests" [
        testCase "Test (getSunPosition 2018 8 1 15 57 0) 42" (
        abs (0.7983 - sunMisalignment (getSunPosition 2018 8 1 15 57 0) 42) `compare` 0.001 @?= LT
        )
      , testCase "Test (getSunPosition 2020 11 24 17 15 0) 60" (
        abs (1.2964 - sunMisalignment (getSunPosition 2020 11 24 17 15 0) 60) `compare` 0.001 @?= LT
        )
    ]

totalPowerTest = testGroup "totalPower tests" [
        testCase "Test 24 145"
        (abs (386.334 -
        totalPower [getSunPosition 2018 8 1 18 35 0, getSunPosition 2018 3 3 16 35 0] 24 145
        ) `compare` 0.5 @?= LT)
      , testCase "Test 5 2"
        (abs (705.406 -
        totalPower [getSunPosition 2018 2 1 20 35 0, getSunPosition 2018 3 17 12 0 0, getSunPosition 2018 3 17 12 10 0] 5 2
        ) `compare` 0.5 @?= LT)
      , testCase "Test 0 212"
        (abs (619.392 -
        totalPower [getSunPosition 2018 8 1 19 0 0, getSunPosition 2018 8 1 19 10 0, getSunPosition 2018 8 1 19 16 0, getSunPosition 2018 8 1 19 18 0] 0 212
        ) `compare` 0.5 @?= LT)
    ]