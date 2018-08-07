module SunPosition (getSunPosition, toLocalTime) where

import           Data.Astro.Coordinate
import           Data.Astro.Effects
import           Data.Astro.Sun
import           Data.Astro.Time.JulianDate
import           Data.Astro.Types

-- 51.546545, 4.411744
location :: GeographicCoordinates
location = GeoC (fromDMS 51 32 48) (fromDMS 4 24 42)

-- | Get the sun position from local time. Location is hard-coded.
getSunPosition :: LocalCivilTime -- ^ Local time, including summer/winter time
            -> HorizonCoordinates -- ^ The altitude and azimuth, e.g. HC {hAltitude = DD 49.312050979507404, hAzimuth = DD 118.94723825710143}
getSunPosition localTime = ec1ToHC location julianDate equatorialCoordinates
    where julianDate = lctUniversalTime localTime
          equatorialCoordinates = sunPosition2 julianDate

-- | Take summer and winter time into account
toLocalTime :: Integer -- ^ Year
                    -> Int -- ^ Month
                    -> Int -- ^ Day
                    -> Int -- ^ Hour
                    -> Int -- ^ Minute
                    -> TimeBaseType -- ^ Second
                    -> LocalCivilTime -- ^ Local time including winter/summer time
toLocalTime year month day hour min sec
    -- Since we cannot figure out where 'last sundays' fall, this way it's at most one week off
    | month < 4 || month > 10 = lctFromYMDHMS (DH 1) year month day hour min sec
    | otherwise = lctFromYMDHMS (DH 2) year month day hour min sec
