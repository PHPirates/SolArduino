module SunPosition (getSunPosition, getSunPositionYMDHMS, toLocalTime) where

import           Data.Astro.Coordinate
import           Data.Astro.Effects
import           Data.Astro.Sun
import           Data.Astro.Time.JulianDate
import           Data.Astro.Types
import Data.Time.Calendar
import Data.Time.Calendar.WeekDate
import Data.Tuple.Select

-- 51.546545, 4.411744
location :: GeographicCoordinates
location = GeoC (fromDMS 51 32 48) (fromDMS 4 24 42)

-- | Get the sun position from local time. Location is hard-coded.
getSunPosition :: JulianDate -- ^ Local time, including summer/winter time
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

-- | Take summer and winter time into account
toLocalTime :: Integer -- ^ Year
                -> Int -- ^ Month
                -> Int -- ^ Day
                -> Int -- ^ Hour
                -> Int -- ^ Minute
                -> TimeBaseType -- ^ Second
                -> LocalCivilTime -- ^ Local time including winter/summer time
toLocalTime year month day hour min sec
    | isSummerTime $ fromGregorian year month day = lctFromYMDHMS (DH 2) year month day hour min sec
    | otherwise = lctFromYMDHMS (DH 1) year month day hour min sec

-- | Uses only days, it does not take the exact hour into account at which summer time starts and ends.
isSummerTime :: Day -> Bool
isSummerTime date = date > lastSundayMarch && date < lastSundayOctober
    where
        year = sel1 $ toGregorian date
        -- Find last Sunday in March
        aprilOne = fromGregorian year 4 1
        -- 1 is Monday, ..., 7 is Sunday
        aprilOneWeekDay = sel3 $ toWeekDate aprilOne
        -- Use the day number to find Sunday of the previous week: the last Sunday in March
        lastSundayMarch = addDays (-(toInteger aprilOneWeekDay)) aprilOne
        -- Same for end of summer time in October
        novemberOne = fromGregorian year 11 1
        novemberOneWeekDay = sel3 $ toWeekDate novemberOne
        lastSundayOctober = addDays (-(toInteger novemberOneWeekDay)) novemberOne
