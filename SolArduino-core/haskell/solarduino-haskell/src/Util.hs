module Util (toLocalTime, toLocalDate, toUniversalTime, julianToLocalTime, julianToLocalDate, localToJulianDate, isSummerTime) where

import           Data.Astro.Time.JulianDate
import           Data.Astro.Types
import           Data.Time.Calendar
import           Data.Time.Calendar.WeekDate
import           Data.Tuple.Select

-- | Take summer and winter time into account when converting to local time.
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

-- | Take summer and winter time into account when converting to local date.
toLocalDate :: Integer -- ^ Year
                -> Int -- ^ Month
                -> Int -- ^ Day
                -> LocalCivilDate -- ^ Local time including winter/summer time
toLocalDate year month day
    | isSummerTime $ fromGregorian year month day = lcdFromYMD (DH 2) year month day
    | otherwise = lcdFromYMD (DH 1) year month day

-- | Convert the universal time to a local time, taking summer/winter time into account.
julianToLocalTime :: JulianDate -- ^ Universal time
                  -> LocalCivilTime -- ^ Local time
julianToLocalTime date = toLocalTime (sel1 tuple) (sel2 tuple) (sel3 tuple) (sel4 tuple) (sel5 tuple) (sel6 tuple)
    where tuple = toYMDHMS date

-- | Convert the local time to a universal time.
localToJulianDate :: LocalCivilTime -- ^ Local time
                  -> JulianDate -- ^ Universal time
localToJulianDate lct = fromYMDHMS (sel1 tuple) (sel2 tuple) (sel3 tuple) (sel4 tuple) (sel5 tuple) (sel6 tuple)
    where tuple = lctToYMDHMS lct

-- | Convert the universal time to a local date, taking summer/winter time into account.
julianToLocalDate :: JulianDate -- ^ Universal date
                  -> LocalCivilDate -- ^ Local date
julianToLocalDate date = toLocalDate (sel1 tuple) (sel2 tuple) (sel3 tuple)
    where tuple = toYMDHMS date

-- | Take summer and winter time into account when converting to universal time
toUniversalTime :: Integer -- ^ Year
                -> Int -- ^ Month
                -> Int -- ^ Day
                -> Int -- ^ Hour
                -> Int -- ^ Minute
                -> TimeBaseType -- ^ Second
                -> JulianDate -- ^ Local time including winter/summer time
toUniversalTime year month day hour min sec = lctUniversalTime $ toLocalTime year month day hour min sec

-- | Uses only days, it does not take the exact hour into account at which summer time starts and ends.
isSummerTime :: Day -- ^ Date to check if summer time is active
            -> Bool -- ^ Whether summer time is active
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
