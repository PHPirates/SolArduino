module AngleFunctions (bestAngle) where

import PowerFunctions
import SunPosition
import GoldenSection

import Data.Astro.Time.JulianDate
import Data.Astro.Types
import Data.Time.Calendar.MonthDay
import Data.Time.Calendar.OrdinalDate
import Data.Tuple.Select

-- | Find the optimal angle over a certain time period.
-- This method finds a certain number of sun positions and then optimizes the angle such that the sum of the total of power from the sun at each sun position is maximal.
bestAngle :: LocalCivilTime -- ^ Start time, must take summer/winter time into account (see SunPosition#toLocalTime)
             -> LocalCivilTime -- ^ End time
             -> Int -- ^ Number of sun positions to use in this interval. More is slower but more precise.
             -> Double -- ^ Optimal angle
bestAngle lctStart lctEnd nrSunPos = fst $ goldenSectionSearch (totalPower listOfSunPos dayOfYear ) 0 90 0.1
    where -- We need to convert to JulianDate to use the times
        jdStart = lctUniversalTime lctStart
        jdEnd = lctUniversalTime lctEnd
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

