module Data.String.Strip (strip)  where

import Data.Char

-- | This is haddock documentation. Explain here that this function trims spaces.
-- If you run stack haddock from the terminal, the docs are in .stack-work/dist/somenumber/doc/html/projectname/doc-index.html
strip :: String -> String
strip = dropWhile isSpace . reverse . dropWhile isSpace . reverse
