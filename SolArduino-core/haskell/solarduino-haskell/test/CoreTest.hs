import           Data.Astro.Coordinate
import           Data.Astro.Time.JulianDate
import           Data.Astro.Types
import           Data.Maybe                 (fromMaybe)
import           Data.Time.Calendar
import           Data.Tuple.Select
import           GoldenSection              (goldenSectionSearch)
import           System.Directory
import           Test.Tasty
import           Test.Tasty.HUnit

import           AngleFunctions
import           PowerFunctions
import           SummerTime                 (isSummerTime)
import           SunPosition
import           TimeConverters

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
      , sunriseTest
      , sunsetTest
      , bestAnglesDayTestAug
      , bestAnglesDayTestOct
      , utilTest
      , bestAnglesMoreDaysTest
      , toUnixTimeTest
--      , testWriteBestAnglesToFile
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
        , testCase "Test altitude via JulianDate"
                    (abs (DD 14.77 - hAltitude (getSunPosition (toUniversalTime 2019 1 1 12 0 0))) `compare` 0.02 @?= LT )
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

sunriseTest =
    testGroup
        "Test sunrise"
        [ testCase "Test year of sunrise" $ sel1 sunriseTuple @?= 2018
        , testCase "Test month of sunrise" $ sel2 sunriseTuple @?= 8
        , testCase "Test day of sunrise" $ sel3 sunriseTuple @?= 13
        , testCase "Test hour of sunrise" $ sel4 sunriseTuple @?= 6
        , testCase "Test minute of sunrise" $ abs (sel5 sunriseTuple - 25) `compare` 5 @?= LT
        , testCase "Test altitude at sunrise" $ abs sunriseAltitude `compare` 1 @?= LT
        ]
        where date = toLocalDate 2018 8 13
              defaultTime = toUniversalTime 2018 8 13 10 0 0
              sunrise = fromMaybe defaultTime $ getSunrise date
              sunriseTuple = gmtToCET $ toYMDHMS sunrise
              sunriseAltitude = hAltitude $ getSunPosition sunrise

sunsetTest =
    testGroup
        "Test sunset"
        [ testCase "Test year of sunset" $ sel1 sunsetTuple @?= 2018
        , testCase "Test month of sunset" $ sel2 sunsetTuple @?= 8
        , testCase "Test day of sunset" $ sel3 sunsetTuple @?= 13
        , testCase "Test hour of sunset" $ sel4 sunsetTuple @?= 21
        , testCase "Test minute of sunset" $ abs (sel5 sunsetTuple - 7) `compare` 5 @?= LT
        , testCase "Test altitude at sunset" $ abs sunsetAltitude `compare` 1 @?= LT
        ]
        where date = toLocalDate 2018 8 13
              defaultTime = toUniversalTime 2018 8 13 10 0 0
              sunset = fromMaybe defaultTime $ getSunset date
              sunsetTuple = gmtToCET $ toYMDHMS sunset
              sunsetAltitude = hAltitude $ getSunPosition sunset

bestAnglesDayTestAug =
    testGroup
        "Test optimal angles for a day"
        [ testCase ("Test 2018 8 14 element nr " ++ show n) $ abs (fst (angles !! n) - expectedAngles !! n ) `compare` threshold @?= LT | n <- [0..9]
        ]
        where angles = bestAnglesDay (toLocalDate 2018 8 14) 1000 10
              expectedAngles = [47.8, 48.3, 46.3, 43.1, 38.6, 31.8, 20.0, 0.0, 0.0, 0.0]
              threshold = 3.0 -- amount of degrees it's allowed to be off compared to the Mathematica implementation

bestAnglesDayTestOct =
    testGroup
        "Test optimal angles for a day"
        [ testCase ("Test 2018 10 13 element nr " ++ show n) $ abs (fst (angles !! n) - expectedAngles !! n ) `compare` threshold @?= LT | n <- [3..9] -- skip the first ones as above 60 degrees inaccuracies are bigger but not so important
        ]
        where angles = bestAnglesDay (toLocalDate 2018 10 13) 1000 10
              expectedAngles = [88.0, 76.9, 68.7, 63.2, 59.7, 56.6, 53.3, 47.7, 36.0, 0.0]
              threshold = 3.0 -- amount of degrees it's allowed to be off compared to the Mathematica implementation

utilTest =
    testGroup
        "Test time/date converters"
        [ testCase "Test localdate 2018 8 15" $ toLocalDate 2018 8 15 == julianToLocalDate (fromYMDHMS 2018 8 15 0 0 0) @?= True
        , testCase "Test localdate 2018 2 15" $ toLocalDate 2018 2 15 == julianToLocalDate (fromYMDHMS 2018 2 15 0 0 0) @?= True
        , testCase "Test julianToLocalTime" $ localToJulianDate (julianToLocalTime (JD 2458345.5)) @?= JD 2458345.5
        , testCase "Test gmtToCET" $ gmtToCET (2018, 8, 15, 13, 22, 0) @?= (2018, 8, 15, 15, 22, 0)
        , testCase "Test gmtToCET" $ gmtToCET (2018, 2, 15, 13, 22, 0) @?= (2018, 2, 15, 14, 22, 0)
        ]

bestAnglesMoreDaysTest =
    testGroup
        "Test angles multiple days"
        [ testCase "Test 2018 8 15 only" $
          bestAnglesMoreDays (fromGregorian 2018 8 15) (fromGregorian 2018 8 15) 1000 10 @?= bestAnglesDay (toLocalDate 2018 8 15) 1000 10
        , testCase "Test number of angles" $ length (bestAnglesMoreDays (fromGregorian 2018 8 15) (fromGregorian 2018 8 17) 1000 10) @?= 30
        ]

toUnixTimeTest =
    testGroup
        "Test converting JulianDate to Unix time"
        [ testCase "Test 2018 8 15 15 34 0" $ abs (julianDateToUnixTime (toUniversalTime 2018 8 15 15 34 0) - 1534340040) `compare` 0.01 @?= LT
        ]

-- Ugly, but Haskell and IO...

--class Monad m => FSMonad m where
--    readFile' :: FilePath -> m String
--
--data MockFS = SingleFile FilePath String
--
--instance FSMonad (State MockFS) where
--               -- ^ Reader would be enough in this particular case though
--    readFile' pathRequested = do
--        (SingleFile pathExisting contents) <- get
--        if pathExisting == pathRequested
--            then return contents
--            else fail "file not found"
--
--testWriteBestAnglesToFile =
--    testGroup
--        "Test writing angles to file"
--        [ testCase "Test file length" $ evalState (lengthOfFileIsFifty "angles.test") (SingleFile "test.txt" "hello world") @?= 50
--        , testCase "Remove the file" $ removeFile filePath
--        ]
--  where
--    filePath = "angles.test"
--    written = writeBestAnglesToFile "angles.test" (fromGregorian 2018 8 15) (fromGregorian 2018 8 19) 1000 10
--    length' string = length $ lines string
--    compareWithFifty x = x @?= 50
--    lengthOfFileIsFifty = do
--        contents <- readFile' filePath
--        return $ compareWithFifty $ length' contents
--    lengthOfFile :: FSMonad m => FilePath -> m Int
--    lengthOfFile file = do
--        contents <- readFile' file
--        return $ length' contents
