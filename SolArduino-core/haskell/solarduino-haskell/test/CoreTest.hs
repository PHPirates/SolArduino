import Test.Tasty
import Test.Tasty.HUnit

import PowerFunctions

-- to run tests in terminal: stack test
main = defaultMain tests

tests :: TestTree
tests = testGroup "Tests" [unitTests]

-- Output is compared with the original Mathematica implementation
unitTests =
    testGroup
        "Unit Tests"
        [ testCase "Test directPower 0.5 10" $ compare (abs (directPower 0.5 10 - 489.607)) 0.001 @?= LT
        , testCase "Test directPower 0.34 253" $ compare (abs (directPower 0.34 253 - 526.962)) 0.001 @?= LT
        , testCase "Test directPower 0 0" $ compare (abs (directPower 0 0 - 576.186)) 0.001 @?= LT
        , testCase "Test directPower 3.14 10" $ compare (abs (directPower 3.14 10)) 0.001 @?= LT
        ]
