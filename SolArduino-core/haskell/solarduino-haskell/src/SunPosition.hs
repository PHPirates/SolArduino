module SunPosition (getSunPosition) where

import           Data.Astro.Coordinate
import           Data.Astro.Effects
import           Data.Astro.Sun
import           Data.Astro.Time.JulianDate
import           Data.Astro.Types

-- 51.546545, 4.411744
location :: GeographicCoordinates
location = GeoC (fromDMS 51 32 48) (fromDMS 4 24 42)

-- | Get the sun position from local time. Location is hard-coded.
getSunPosition :: Integer -- ^ Year
            -> Int -- ^ Month
            -> Int -- ^ Day
            -> Int -- ^ Hour
            -> Int -- ^ Minute
            -> TimeBaseType -- ^ Second
            -> HorizonCoordinates -- ^ The altitude and azimuth, e.g. HC {hAltitude = DD 49.312050979507404, hAzimuth = DD 118.94723825710143}
getSunPosition year month day hour min sec  = ec1ToHC location julianDate equatorialCoordinates
    where julianDate = lctUniversalTime $ lctFromYMDHMS (DH 2) year month day hour min sec -- todo summer/winter time
          equatorialCoordinates = sunPosition2 julianDate
