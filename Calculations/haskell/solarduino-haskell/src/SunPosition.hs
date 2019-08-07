module SunPosition (getSunPosition, getSunPositionYMDHMS, getSunrise, getSunset, toLocalTime) where

import           Data.Astro.CelestialObject.RiseSet
import           Data.Astro.Coordinate
import           Data.Astro.Effects
import           Data.Astro.Sun
import           Data.Astro.Time.JulianDate
import           Data.Astro.Types
import           Data.Time.Calendar

import           TimeConverters

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
getSunPositionYMDHMS year month day hour min sec = getSunPosition $ toUniversalTime year month day hour min sec

-- | Get sunrise, location is hard-coded.
-- Example:
-- import Data.Maybe
-- date = toLocalDate 2018 8 13
-- defaultTime = toUniversalTime 2018 8 13 10 0 0
-- sunrise = fromMaybe defaultTime $ getSunrise date
getSunrise :: LocalCivilDate -- ^ Date: day
            -> Maybe JulianDate -- ^ Time of sunrise, universal time
getSunrise date = fmap localToJulianDate justSunrisetime
        -- Get a RiseSetMB which is a data type with sunrise and sunset
  where
    -- A vertical shift like 0.833333 is a 'good value' according to the docs
    riseset = sunRiseAndSet location 0.833333 date
    -- This function is to unpack the RiseSetMB data
    getOnlySunrise (RiseSet sunrise sunset) = sunrise
    -- The sunrise is a Maybe Tuple, so we get the first element (the time, second one is the azimuth) as a Maybe LocalCivilTime
    justSunrisetime = fst <$> getOnlySunrise riseset

-- | Get sunset, location is hard-coded.
-- Example:
-- import Data.Maybe
-- date = toLocalDate 2018 8 13
-- defaultTime = toUniversalTime 2018 8 13 10 0 0
-- sunset = fromMaybe defaultTime $ getSunset date
getSunset :: LocalCivilDate -- ^ Date: day
            -> Maybe JulianDate -- ^ Time of sunset, universal time
getSunset date = fmap localToJulianDate justSunsettime
        -- Get a RiseSetMB which is a data type with sunset and sunset
  where
-- A vertical shift like 0.833333 is a 'good value' according to the docs
    riseset = sunRiseAndSet location 0.833333 date
        -- This function is to unpack the RiseSetMB data
    getOnlySunset (RiseSet sunrise sunset) = sunset
        -- The sunset is a Maybe Tuple, so we get the first element (the time, second one is the azimuth) as a Maybe LocalCivilTime
    justSunsettime = fst <$> getOnlySunset riseset
