module SunPosition where

import Data.Astro.Time.JulianDate
import Data.Astro.Coordinate
import Data.Astro.Types
import Data.Astro.Sun

ro :: GeographicCoordinates
ro = GeoC (fromDMS 51 32 48) (-(fromDMS 4 24 42))

dt :: LocalCivilTime
dt = lctFromYMDHMS (DH 1) 2018 7 30 10 29 0