module PowerFunctions where

-- | Calculate the solar insolation: the power in W/m^2 the solar panels receive from the sun under a certain angle.
-- The formula is from http://www.powerfromthesun.net/Book/chapter02/chapter02.html#ZEqnNum929295
directPower :: Float -- ^ The angle the solar panels have with the sun, in radians.
            -> Float -- ^ The day of the year. Obviously an Integer, but that doesn't compile.
            -> Float -- ^ The power the panels receive from the sun in W/m^2
directPower theta n
    | cos theta < 0 = 0 -- If the sun is more than 90 degrees off, the formula is not correct, and we set the insolation to 0
    | otherwise
        -- Parameters from urban visibility haze model
     =
        let i = 1367 * (1 + 0.034 * cos (360 * n / 365.25))
            aZero = 0.2538 - 0.0063 * 36
            aOne = 0.7678 + 0.001 * 6.5 ^ 2
            k = 0.249 + 0.081 * 2.5 ^ 2
            temp = i * (aZero + aOne * (exp 1 ** (-k / cos theta)))
            -- Use the ** power because from a cosine results a Float, not a fraction
         in temp