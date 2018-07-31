module SunPosition where

import Data.Astro.Time.JulianDate
import Data.Astro.Coordinate
import Data.Astro.Types
import Data.Astro.Sun
import Data.Astro.Effects

ro :: GeographicCoordinates
ro = GeoC (fromDMS 51 32 48) (-(fromDMS 4 24 42))

--dt :: LocalCivilTime
--dt = lctFromYMDHMS (DH 1) 2018 7 30 10 29 0

--jd :: JulianDate
--jd = lctUniversalTime dt

--ec1 :: EquatorialCoordinates1
--ec1 = sunPosition2 jd

-- | Get the sun position from local time. Location is hard-coded.
getSunPosition :: LocalCivilTime -- ^ Local time, for example with lctFromYMDHMS (DH 1) 2018 7 30 10 29 0
            -> HorizonCoordinates -- ^ The altitude and azimuth, e.g. HC {hAltitude = DD 49.312050979507404, hAzimuth = DD 118.94723825710143}
getSunPosition date = ec1ToHC ro jd ec1
    where jd = lctUniversalTime date
          ec1 = sunPosition2 jd

--hc :: HorizonCoordinates
--hc = ec1ToHC ro jd ec1
