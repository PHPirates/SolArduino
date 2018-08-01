module PowerFunctions (directPower, sunMisalignment, totalPower) where

import Data.Astro.Coordinate
import Data.Astro.Types

-- | Calculate the solar insolation: the power in W/m^2 the solar panels receive from the sun under a certain angle.
-- The formula is from http://www.powerfromthesun.net/Book/chapter02/chapter02.html#ZEqnNum929295
directPower :: Float -- ^ The angle the solar panels have with the sun, in radians
            -> Int -- ^ The day of the year
            -> Float -- ^ The power the panels receive from the sun in W/m^2
directPower theta n
    | cos theta < 0 = 0 -- If the sun is more than 90 degrees off, the formula is not correct, and we set the insolation to 0
    | otherwise
        -- Parameters from urban visibility haze model
     =
        let i = 1367 * (1 + 0.034 * cos (360 * fromIntegral n / 365.25))
            aZero = 0.2538 - 0.0063 * 36
            aOne = 0.7678 + 0.001 * 6.5 ^ 2
            k = 0.249 + 0.081 * 2.5 ^ 2
            -- Use the ** power because from a cosine results a Float, not a fraction
         in i * (aZero + aOne * (exp 1 ** (-k / cos theta)))

-- | Find the misalignment of the solar panels with the sun
sunMisalignment :: HorizonCoordinates -- ^ Coordinates of the sun
                -> Float -- ^ Solar panel angle in degrees
                -> Float -- ^ Misalignment of the solar panels with the sun in degrees
sunMisalignment sunCoords alpha =
        -- The realToFrac's are needed because sin/cos require doubles
        realToFrac $ acos(cos(gammaPanels - gammaSun) * cos thetaSun * sin thetaPanels + cos thetaPanels * sin thetaSun )
        where -- We require an azimuth with 0 due South and negative to the east, hence we subtract 180 degrees
            gammaSun = toRadians (hAzimuth sunCoords) - pi
            thetaSun = toRadians $ hAltitude sunCoords
            -- Same for the solar panels, but the azimuth is fixed
            gammaPanels = toRadians (-22)
            thetaPanels = realToFrac alpha * pi / 180

-- | Find the power received by the solar panels at each sun position and sum that. This does not equal the total power received by the solar panels over a certain time period. Higher is better.
totalPower :: [HorizonCoordinates] -- ^ A list of sun positions
            -> Float -- ^ The angle of the solar panels
            -> Int -- ^ Day of the year
            -> Float -- ^ Sum of the power received by the panels at each sun position
totalPower sunPositions alpha day = foldl (\acc sunPos -> acc + directPower (sunMisalignment sunPos alpha ) day  ) 0 sunPositions
