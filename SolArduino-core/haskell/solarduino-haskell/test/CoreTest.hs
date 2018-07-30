import Test.Tasty
import Test.Tasty.HUnit

import PowerFunctions

-- to run tests in terminal: stack test
main = defaultMain tests

tests :: TestTree
tests = testGroup "Tests" [unitTests]

unitTests = testGroup "Unit Tests"
    [ testCase "Test the power function" $
        directPower 2 @?= 4.0
    ]
