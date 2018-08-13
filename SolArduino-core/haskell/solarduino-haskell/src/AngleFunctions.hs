module AngleFunctions (bestAngle, bestAnglesDay) where

import           GoldenSection
import           PowerFunctions
import           SunPosition

import           Data.Astro.Time.JulianDate
import           Data.Astro.Types
import           Data.Maybe
import           Data.Time.Calendar             (Day)
import           Data.Time.Calendar.MonthDay
import           Data.Time.Calendar.OrdinalDate
import           Data.Tuple.Select

-- | Find the optimal angle over a certain time period.
-- This method finds a certain number of sun positions and then optimizes the angle such that the sum of the total of power from the sun at each sun position is maximal.
-- Example:
-- import Data.Astro.Time.JulianDate
-- bestAngle (toUniversalTime 2018 8 11 3 0 0) (toUniversalTime 2018 8 11 22 0 0) 100
bestAngle :: JulianDate -- ^ Start time
             -> JulianDate -- ^ End time
             -> Int -- ^ Number of sun positions to sample for this interval. More is slower but more precise.
             -> Double -- ^ Optimal angle
bestAngle jdStart jdEnd nrSunPos = fst $ goldenSectionSearch (totalPower listOfSunPos dayOfYear ) 0 90 0.01
    where -- We need to convert to JulianDate to use the times
        intervalLengthInHours = 24 * numberOfDays jdStart jdEnd
        -- Time between sampled sun positions, such that the total number of sun positions will be as given.
        interval = intervalLengthInHours / (fromIntegral nrSunPos - 1)

        listOfSunPos = [getSunPosition (addHours (DH n) jdStart) | n <- take nrSunPos [0, interval..]]

        -- In general the start date may not be the same as the end date, but we take the first one to optimize with.
        -- In practice it will be the same day, as setting the solar panels on an angle for multiple days is uncommon.
        date = toYMDHMS jdStart
        year = sel1 date
        month = sel2 date
        day = sel3 date
        dayOfYear = monthAndDayToDayOfYear (isLeapYear year) month day

-- | Find a list of angles and corresponding times for a certain day.
-- The result is a list of pairs (a, t), the solar panels should be set at angle a at time t.
-- Example:
-- import Data.Astro.Time.JulianDate
-- bestAnglesDay (toLocalDate 2018 8 11) 1000 10
bestAnglesDay :: LocalCivilDate -- ^ Date for which to find the optimal angles
                -> Int -- ^ Number of sun positions to sample for this day. More is slower but more precise.
                -> Int -- ^ Number of times to adjust the solar panels
                -> [(Double, JulianDate)] -- ^ Angle, time
bestAnglesDay date nrSunPos nrAdjustments =
    [ ( bestAngle
            (addHours (DH n) sunriseDate)
            (addHours (DH (n + interval)) sunriseDate)
            (nrSunPos `div` nrAdjustments) -- Use integer division to get an integer number of samples
      , addHours (DH n) sunriseDate)
    | n <- [0,interval .. sunShineHours]
    ]
  where
    -- Hour of the day at which to start, by default 0:00
    defaultSunrise = lcdDate date
    sunriseDate = fromMaybe defaultSunrise $ getSunrise date
    -- Hour of the day at which to end, by default 24:00
    defaultSunset = addHours 24 defaultSunrise
    sunsetDate = fromMaybe defaultSunset $ getSunset date
    -- Number of hours which the sun is up
    sunShineHours :: Double
    sunShineHours = 24 * numberOfDays sunriseDate sunsetDate
    -- The solar panels move after each interval, of this length
    interval = sunShineHours / fromIntegral nrAdjustments
