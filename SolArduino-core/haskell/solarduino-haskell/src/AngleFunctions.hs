module AngleFunctions where

import PowerFunctions

import Data.Astro.Time.JulianDate (LocalCivilTime)

-- | Find the optimal angle over a certain time period.
-- This method finds a certain number of sun positions and then optimizes the angle such that the sum of the total of power from the sun at each sun position is maximal.
--bestAngle :: LocalCivilTime -- ^ Start time, must take summer/winter time into account (see SunPosition#toLocalTime)
--             -> LocalCivilTime -- ^ End time
--             -> Int -- ^ Number
--             -> Double -- ^ Optimal angle
--bestAngle =
--    where

