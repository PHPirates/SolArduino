module SummerTime (isSummerTime) where

import           Data.Time.Calendar
import           Data.Time.Calendar.WeekDate
import           Data.Tuple.Select

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