module Util (toLocalTime, toLocalDate, toUniversalTime, julianToLocalTime, julianToLocalDate, localToJulianDate, gmtToCET, isSummerTime) where

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
    where tuple = gmtToCET $ toYMDHMS date -- toYMDHMS returns time in GMT timezone

-- | Convert time in the GMT timezone to CET which includes summer/winter time
-- Example:
-- timeInGmt = toYMDHMS (JD 2458344.9166666665)
-- localTime = toLocalTime $ gmtToCET timeInGmt
gmtToCET :: (Integer, Int, Int, Int, Int, TimeBaseType) -- ^ Time in GMT timezone
         -> (Integer, Int, Int, Int, Int, TimeBaseType) -- ^ Time in CET timezone
gmtToCET tuple
    | isSummerTime $ fromGregorian year month day = (year, month, day, hour + 2, min, sec)
    | otherwise = (year, month, day, hour + 1, min, sec)
    where year = sel1 tuple
          month = sel2 tuple
          day = sel3 tuple
          hour = sel4 tuple
          min = sel5 tuple
          sec = sel6 tuple

-- | Convert the local time to a universal time.
localToJulianDate :: LocalCivilTime -- ^ Local time
                  -> JulianDate -- ^ Universal time
-- To atake timezones into account, we go via a tuple and toUniversalTime instead of using lctUniversalTime
localToJulianDate lct = toUniversalTime (sel1 tuple) (sel2 tuple) (sel3 tuple) (sel4 tuple) (sel5 tuple) (sel6 tuple)
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
