module AngleFunctions (bestAngle, bestAnglesDay, bestAnglesMoreDays) where

import           GoldenSection
import           PowerFunctions
import           SunPosition

import           Data.Astro.Time.JulianDate
import           Data.Astro.Types
import           Data.List.Extras.Argmax
import           Data.Maybe
import           Data.Time.Calendar
import           Data.Time.Calendar.MonthDay
import           Data.Time.Calendar.OrdinalDate
import           Data.Tuple.Select
import           TimeConverters                           (toLocalDate)

-- | Find the optimal angle over a certain time period.
-- This method finds a certain number of sun positions and then optimizes the angle such that the sum of the total of power from the sun at each sun position is maximal.
-- Example:
-- import Data.Astro.Time.JulianDate
-- bestAngle (toUniversalTime 2018 8 11 3 0 0) (toUniversalTime 2018 8 11 22 0 0) 100
bestAngle :: JulianDate -- ^ Start time
             -> JulianDate -- ^ End time
             -> Int -- ^ Number of sun positions to sample for this interval. More is slower but more precise.
             -> Double -- ^ Optimal angle
bestAngle jdStart jdEnd nrSunPos = betterAngle
          -- We need to convert to JulianDate to use the times
  where
    intervalLengthInHours = 24 * numberOfDays jdStart jdEnd
        -- Time between sampled sun positions, such that the total number of sun positions will be as given.
    interval = intervalLengthInHours / (fromIntegral nrSunPos - 1)
    listOfSunPos = [getSunPosition (addHours (DH n) jdStart) | n <- take nrSunPos [0,interval ..]]
        -- In general the start date may not be the same as the end date, but we take the first one to optimize with.
        -- In practice it will be the same day, as setting the solar panels on an angle for multiple days is uncommon.
    date = toYMDHMS jdStart
    year = sel1 date
    month = sel2 date
    day = sel3 date
    dayOfYear = monthAndDayToDayOfYear (isLeapYear year) month day
        -- Function to optimize
    f = totalPower listOfSunPos dayOfYear
    optimizedAngle = fst $ goldenSectionSearch f 0 90 0.01
        -- Sometimes, the maximum is at one of the ends of the interval but the totalPower function can be flat just until a peak at the end, in which case goldenSectionSearch fails to find the maximum.
        -- Therefore, we take a maximum with values at either end just to be sure.
    betterAngle = argmax f [0, optimizedAngle, 90]

-- | Find a list of angles and corresponding times for a certain day.
-- The result is a list of pairs (a, t), the solar panels should be set at angle a at time t.
-- Example:
-- import Data.Astro.Time.JulianDate
-- bestAnglesDay (toLocalDate 2018 8 11) 1000 10
bestAnglesDay :: LocalCivilDate -- ^ Date for which to find the optimal angles
                -> Int -- ^ Number of sun positions to sample for this day. More is slower but more precise.
                -> Int -- ^ Number of times to adjust the solar panels
                -> [(Double, JulianDate)] -- ^ Angle, time, length at least the number of times
bestAnglesDay date nrSunPos nrAdjustments =
    take nrAdjustments [ -- This ensures not too many elements are returned
        ( bestAngle
            (addHours (DH t) sunriseDate)
            (addHours (DH (t + interval)) sunriseDate)
            (nrSunPos `div` nrAdjustments) -- Use integer division to get an integer number of samples
      , addHours (DH t) sunriseDate)
    | t <- [0,interval..]
    ]
  where
    -- Hour of the day at which to start, by default 0:00
    defaultSunrise = lcdDate date
    sunriseDate = fromMaybe defaultSunrise $ getSunrise date

    -- Hour of the day at which to end, by default 24:00
    defaultSunset = addHours 24 defaultSunrise
    sunsetDate = fromMaybe defaultSunset $ getSunset date

    -- Number of hours which the sun is up
    sunShineHours = 24 * numberOfDays sunriseDate sunsetDate

    -- The solar panels move after each interval, of this length
    interval = sunShineHours / fromIntegral nrAdjustments

-- | Find the optimal angles for a certain number of days, starting with the start date up to and including the end date.
-- Example:
-- import Data.Time.Calendar
-- bestAnglesMoreDays (fromGregorian 2018 8 15) (fromGregorian 2018 9 1) 1000 10
bestAnglesMoreDays :: Day -- ^ Start date
                  -> Day -- ^ End date
                  -> Int -- ^ Number of sun positions to sample per day
                  -> Int -- ^ Number of times to adjust the solar panels per day
                  -> [(Double, JulianDate)] -- ^ List of pairs of angle and time. For each pair, the panels should be set at the angle at that time.
bestAnglesMoreDays startDate endDate nrSunPos nrAdjustments =
    concat [oneDay (addDays n startDate) | n <- [0..nrDays]]
    where -- This function wraps the function which provides optimal angles for one day
        oneDay d = bestAnglesDay (toLocalDate (sel1 tuple) (sel2 tuple) (sel3 tuple)) nrSunPos nrAdjustments
                where tuple = toGregorian d
        nrDays = abs $ diffDays startDate endDate
