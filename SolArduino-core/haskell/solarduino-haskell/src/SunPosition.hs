module SunPosition (getSunPosition, getSunPositionYMDHMS, toLocalTime) where

import           Data.Astro.CelestialObject.RiseSet
import           Data.Astro.Coordinate
import           Data.Astro.Effects
import           Data.Astro.Sun
import           Data.Astro.Time.JulianDate
import           Data.Astro.Types
import           Data.Time.Calendar

import           Util

-- 51.546545, 4.411744
location :: GeographicCoordinates
location = GeoC (fromDMS 51 32 48) (fromDMS 4 24 42)

-- | Get the sun position. Location is hard-coded.
getSunPosition :: JulianDate -- ^ Universal time
            -> HorizonCoordinates -- ^ The altitude and azimuth, e.g. HC {hAltitude = DD 49.312050979507404, hAzimuth = DD 118.94723825710143}
getSunPosition julianDate = ec1ToHC location julianDate equatorialCoordinates
    where equatorialCoordinates = sunPosition2 julianDate

-- | Get the sun position from local time. the time given should not take winter/summer time into account. Location is hard-coded.
getSunPositionYMDHMS :: Integer -- ^ Year
                  -> Int -- ^ Month
                  -> Int -- ^ Day
                  -> Int -- ^ Hour
                  -> Int -- ^ Minute
                  -> TimeBaseType -- ^ Second
                  -> HorizonCoordinates -- ^ The altitude and azimuth, e.g. HC {hAltitude = DD 49.312050979507404, hAzimuth = DD 118.94723825710143}
getSunPositionYMDHMS year month day hour min sec = getSunPosition $ lctUniversalTime $ toLocalTime year month day hour min sec

-- | Get sunrise, location is hard-coded.
-- Example:
-- date = toUniversalTime 2018 8 13 12 10 0
-- maybeSunrise = getSunRise date
-- defaultTime = toUniversalTime 2018 8 13 10 0 0
-- sunrise = maybe defaultTime maybeSunrise
getSunrise :: JulianDate -- ^ Universal time
            -> Maybe JulianDate
getSunrise date = fmap localToJulianDate justSunrisetime
        -- Get a RiseSetMB which is a data type with sunrise and sunset
  where
-- A vertical shift like 0.833333 is a 'good value' according to the docs
    riseset = sunRiseAndSet location 0.833333 (julianToLocalDate date)
        -- This function is to unpack the RiseSetMB data
    getOnlySunrise (RiseSet sunrise sunset) = sunrise
        -- The sunrise is a Maybe Tuple, so we get the first element (the time, second one is the azimuth) as a Maybe LocalCivilTime
    justSunrisetime = fst <$> getOnlySunrise riseset
