EESchema Schematic File Version 4
LIBS:circuit-cache
EELAYER 30 0
EELAYER END
$Descr User 12039 9071
encoding utf-8
Sheet 1 1
Title "SolArduino"
Date "2019-11-10"
Rev "6"
Comp "Thomas Schouten"
Comment1 ""
Comment2 ""
Comment3 ""
Comment4 ""
$EndDescr
$Comp
L solarduino:MCP3008 U4
U 1 1 5D640D67
P 8700 3950
F 0 "U4" H 8400 4500 50  0000 C CNN
F 1 "MCP3008" H 9150 3450 50  0000 C CNN
F 2 "" H 8700 3950 50  0001 C CNN
F 3 "" H 8700 3950 50  0001 C CNN
	1    8700 3950
	1    0    0    -1  
$EndComp
$Comp
L solarduino:terminal U2
U 1 1 5D63573B
P 5250 5200
F 0 "U2" H 6100 5000 50  0000 L CNN
F 1 "terminal" H 6100 4900 50  0000 L CNN
F 2 "" H 5100 5050 50  0001 C CNN
F 3 "" H 5100 5050 50  0001 C CNN
	1    5250 5200
	1    0    0    -1  
$EndComp
Wire Wire Line
	3450 4800 4700 4800
Wire Wire Line
	4700 4800 4700 4950
Wire Wire Line
	3400 4750 4800 4750
Wire Wire Line
	4800 4750 4800 4950
Wire Wire Line
	3350 4700 4900 4700
Wire Wire Line
	4900 4700 4900 4950
Wire Notes Line
	4600 6400 4600 6650
Wire Notes Line
	4600 6650 4850 6650
Wire Notes Line
	4850 6650 4850 6400
Wire Notes Line
	4850 6400 4600 6400
Wire Notes Line
	5500 6400 5500 6650
Wire Notes Line
	5500 6650 5750 6650
Wire Notes Line
	5750 6650 5750 6400
Wire Notes Line
	5750 6400 5500 6400
Wire Notes Line
	3050 7450 3050 7700
Wire Notes Line
	3050 7700 5400 7700
Wire Notes Line
	5400 7700 5400 7450
Wire Notes Line
	5400 7450 3050 7450
Wire Wire Line
	5300 4950 5300 4900
Wire Wire Line
	5300 4900 4050 4900
Wire Wire Line
	5400 4950 5400 4850
Text GLabel 4200 7600 0    50   Input ~ 0
black
Text GLabel 4500 7600 0    50   Input ~ 0
yellow
Wire Wire Line
	5700 4950 5800 4950
Wire Wire Line
	3500 4650 5900 4650
Wire Wire Line
	5900 4650 5900 4950
Text GLabel 5350 7600 0    50   Input ~ 0
white-green
Wire Wire Line
	5600 4700 5600 4950
Wire Wire Line
	4700 5500 4700 6350
Wire Wire Line
	4700 6350 5550 6350
Wire Wire Line
	5550 6350 5550 6500
Wire Wire Line
	4800 5500 4800 6300
Wire Wire Line
	4800 6300 5600 6300
Wire Wire Line
	5600 6300 5600 6500
Wire Wire Line
	4900 5500 4900 6250
Wire Wire Line
	4900 6250 5650 6250
Wire Wire Line
	5650 6250 5650 6500
Text GLabel 3900 7600 0    50   Input ~ 0
red-brown
Text GLabel 3400 7600 0    50   Input ~ 0
blue
Wire Notes Line
	5900 7450 5900 7550
Wire Notes Line
	5900 7550 6100 7550
Wire Notes Line
	6100 7550 6100 7450
Wire Notes Line
	6100 7450 5900 7450
Wire Notes Line
	5900 7600 5900 7700
Wire Notes Line
	5900 7700 6050 7700
Wire Notes Line
	6050 7700 6050 7600
Wire Notes Line
	6050 7600 5900 7600
Wire Notes Line
	6100 7600 6100 7700
Wire Notes Line
	6100 7700 6250 7700
Wire Notes Line
	6250 7700 6250 7600
Wire Notes Line
	6250 7600 6100 7600
Wire Notes Line
	5850 7400 5850 7750
Wire Notes Line
	5850 7750 6300 7750
Wire Notes Line
	6300 7750 6300 7400
Wire Notes Line
	6300 7400 5850 7400
Wire Wire Line
	5300 5500 5300 6200
Wire Wire Line
	5300 6200 5950 6200
Wire Wire Line
	5950 6200 5950 7500
Wire Wire Line
	5400 5500 5400 6150
Wire Wire Line
	5400 6150 6000 6150
Wire Wire Line
	6000 6150 6000 7500
Wire Wire Line
	5500 5500 5500 6100
Wire Wire Line
	5500 6100 6050 6100
Wire Wire Line
	6050 6100 6050 7500
Wire Wire Line
	5600 5500 5600 6050
Wire Wire Line
	5600 6050 6400 6050
Wire Wire Line
	6400 6050 6400 7850
Wire Wire Line
	6400 7850 5950 7850
Wire Wire Line
	5700 5500 5700 6000
Wire Wire Line
	5700 6000 6350 6000
Wire Wire Line
	6350 6000 6350 7800
Wire Wire Line
	6350 7800 6000 7800
Wire Wire Line
	6000 7800 6000 7650
Wire Wire Line
	5950 7850 5950 7650
Wire Wire Line
	5800 5500 5800 5950
Wire Wire Line
	5800 5950 6150 5950
Wire Wire Line
	6150 5950 6150 7650
Wire Wire Line
	5900 5500 5900 5900
Wire Wire Line
	5900 5900 6200 5900
Wire Wire Line
	6200 5900 6200 7650
Wire Bus Line
	9950 3450 9950 4450
Wire Bus Line
	10150 3450 10150 4450
Text Notes 9900 3400 0    50   ~ 0
+
Text Notes 10100 3400 0    50   ~ 0
-
Wire Wire Line
	9850 4300 9950 4300
Wire Wire Line
	9050 3650 9950 3650
Wire Wire Line
	9050 3750 9950 3750
Wire Wire Line
	9050 3850 10150 3850
Wire Wire Line
	9050 3950 9100 3950
Wire Wire Line
	11000 3050 11000 2000
Wire Wire Line
	11000 2000 10850 2000
Wire Wire Line
	9050 4350 10150 4350
Wire Wire Line
	10250 3150 10250 3650
Wire Wire Line
	10250 3650 10150 3650
Wire Wire Line
	9950 3550 9050 3550
$Comp
L solarduino:JE342 U7
U 1 1 5D89B4F8
P 7950 6550
F 0 "U7" H 8400 6000 50  0000 L CNN
F 1 "JE342" H 7300 7000 50  0000 L CNN
F 2 "" H 7950 6550 168 0001 C CNN
F 3 "" H 7950 6550 168 0001 C CNN
	1    7950 6550
	-1   0    0    1   
$EndComp
Wire Wire Line
	7750 6150 7750 6250
Wire Wire Line
	7850 6150 7850 6350
Wire Wire Line
	7850 6850 7850 6750
Wire Wire Line
	7950 6850 7950 6650
Wire Wire Line
	8050 6550 8050 6850
Wire Wire Line
	8250 3650 8300 3650
Wire Wire Line
	8050 6450 8050 6150
Text GLabel 4800 7600 0    50   Input ~ 0
green
$Comp
L solarduino:JE342 U5
U 1 1 5DED3A00
P 1800 6250
F 0 "U5" H 2350 5700 50  0000 L CNN
F 1 "JE342" H 1250 6700 50  0000 L CNN
F 2 "" H 1800 6250 168 0001 C CNN
F 3 "" H 1800 6250 168 0001 C CNN
	1    1800 6250
	-1   0    0    1   
$EndComp
Wire Wire Line
	3400 7350 1200 7350
Wire Wire Line
	1200 7350 1200 6450
Wire Wire Line
	1200 6450 1700 6450
Wire Wire Line
	1700 6450 1700 6550
Wire Wire Line
	3400 7350 3400 8400
Wire Wire Line
	2350 6150 2500 6150
Wire Wire Line
	3900 7250 1100 7250
Wire Wire Line
	1100 7250 1100 6350
Wire Wire Line
	1100 6350 1800 6350
Wire Wire Line
	1800 6350 1800 6550
Wire Wire Line
	2350 6450 2650 6450
Wire Wire Line
	3900 7250 3900 8300
Wire Wire Line
	4200 7100 1000 7100
Wire Wire Line
	1000 7100 1000 6250
Wire Wire Line
	1000 6250 1900 6250
Wire Wire Line
	1900 6250 1900 6550
Wire Wire Line
	2350 6550 2700 6550
Wire Wire Line
	4050 4900 4050 5300
Wire Wire Line
	1900 6150 1900 5850
Wire Wire Line
	4500 7000 4500 8100
Wire Wire Line
	2350 5950 2400 5950
Wire Wire Line
	4800 6900 800  6900
Wire Wire Line
	800  6900 800  6050
Wire Wire Line
	800  6050 1700 6050
Wire Wire Line
	1700 6050 1700 5850
Wire Wire Line
	4500 7000 900  7000
Wire Wire Line
	900  7000 900  6150
Wire Wire Line
	900  6150 1900 6150
Wire Wire Line
	2350 6350 2600 6350
Wire Wire Line
	4800 6900 4800 8000
Wire Wire Line
	1600 5950 1600 5850
Wire Wire Line
	2350 6050 2450 6050
Wire Wire Line
	5350 6800 5350 7900
Wire Wire Line
	5350 6800 700  6800
Wire Wire Line
	700  6800 700  5950
Wire Wire Line
	700  5950 1600 5950
Wire Wire Line
	4450 6450 4650 6450
Wire Wire Line
	4400 6550 4650 6550
Wire Wire Line
	2400 5950 2400 5600
Wire Wire Line
	2450 6050 2450 5500
Wire Wire Line
	2500 6150 2500 5400
Wire Wire Line
	2350 6250 2550 6250
Wire Wire Line
	2350 5850 2350 5700
Text Notes 1950 5300 0    50   ~ 0
ethernet cable\n
Wire Wire Line
	6750 6250 7750 6250
Wire Wire Line
	6850 6350 7850 6350
Wire Wire Line
	6950 6450 8050 6450
Wire Wire Line
	7050 6550 8050 6550
Wire Wire Line
	7150 6650 7950 6650
Wire Wire Line
	7250 6750 7850 6750
Wire Wire Line
	8500 6250 8550 6250
Wire Wire Line
	8500 6350 8600 6350
Wire Wire Line
	8500 6450 8650 6450
Wire Wire Line
	8500 6650 8750 6650
Wire Wire Line
	8500 6750 8800 6750
Wire Wire Line
	8500 6850 8850 6850
Wire Wire Line
	8550 6250 8550 5850
Wire Wire Line
	8500 6150 8500 5950
Wire Wire Line
	8600 6350 8600 5750
Wire Wire Line
	8650 6450 8650 5650
Wire Wire Line
	8500 6550 8700 6550
Text Notes 8150 5500 0    50   ~ 0
ethernet cable
Connection ~ 5600 4700
Wire Wire Line
	9050 4250 9250 4250
Wire Wire Line
	9050 4150 9200 4150
Wire Wire Line
	9050 4050 9150 4050
Wire Wire Line
	5600 4700 6850 4700
Wire Wire Line
	7900 4950 7900 2500
Wire Wire Line
	8000 4900 8000 2600
Wire Wire Line
	8100 4850 8100 2700
Wire Wire Line
	4400 4500 4400 6550
Wire Wire Line
	3250 4500 4400 4500
Wire Wire Line
	3300 2950 3300 4450
Wire Wire Line
	3300 4450 4450 4450
Wire Wire Line
	3250 4500 3250 3050
Wire Wire Line
	4450 4450 4450 6450
Wire Wire Line
	8100 2700 9500 2700
Wire Wire Line
	8000 2600 9500 2600
Wire Wire Line
	7900 2500 9500 2500
Wire Wire Line
	9050 1700 9500 1700
Wire Wire Line
	9050 3550 9050 1700
Wire Wire Line
	9500 3150 10250 3150
Wire Wire Line
	9500 2800 9500 3150
Wire Wire Line
	9250 3050 11000 3050
Wire Wire Line
	9250 4250 9250 3050
Wire Wire Line
	9200 1800 9500 1800
Wire Wire Line
	9200 4150 9200 1800
Wire Wire Line
	9150 1900 9500 1900
Wire Wire Line
	9150 4050 9150 1900
Wire Wire Line
	9100 2000 9500 2000
Wire Wire Line
	9100 3950 9100 2000
Text Notes 7450 1050 0    168  ~ 0
meter cupboard
Wire Wire Line
	6850 2150 6850 4700
$Comp
L solarduino:button S1
U 1 1 5D618DF8
P 1000 2850
F 0 "S1" H 1278 2896 50  0000 L CNN
F 1 "Down button" H 1278 2805 50  0000 L CNN
F 2 "" H 800 2900 50  0001 C CNN
F 3 "" H 800 2900 50  0001 C CNN
	1    1000 2850
	1    0    0    -1  
$EndComp
$Comp
L solarduino:button S2
U 1 1 5D61A850
P 1000 3750
F 0 "S2" H 1278 3796 50  0000 L CNN
F 1 "Up button" H 1278 3705 50  0000 L CNN
F 2 "" H 800 3800 50  0001 C CNN
F 3 "" H 800 3800 50  0001 C CNN
	1    1000 3750
	1    0    0    -1  
$EndComp
Wire Wire Line
	1000 1250 1000 2500
Wire Wire Line
	1800 1200 1800 3250
Wire Wire Line
	1800 3250 850  3250
Wire Wire Line
	850  3250 850  3150
Wire Wire Line
	850  2550 550  2550
Wire Wire Line
	550  2550 550  4250
Wire Wire Line
	550  4250 1150 4250
Wire Wire Line
	1150 4250 1150 4050
Wire Wire Line
	1000 2500 700  2500
Wire Wire Line
	700  2500 700  3200
Connection ~ 1000 2500
Wire Wire Line
	1000 2500 1000 2550
Wire Wire Line
	1150 2550 1150 2450
Wire Wire Line
	1150 2450 650  2450
Wire Wire Line
	650  2450 650  3450
Wire Wire Line
	650  3450 850  3450
Wire Wire Line
	1000 3450 1000 3400
Wire Wire Line
	1000 3400 700  3400
Wire Wire Line
	700  3400 700  4150
Wire Wire Line
	700  4150 1000 4150
Wire Wire Line
	1000 4150 1000 4050
Wire Wire Line
	1000 4150 5600 4150
Connection ~ 1000 4150
Wire Wire Line
	2600 1250 1000 1250
Wire Wire Line
	2700 1200 1800 1200
Wire Wire Line
	4100 3300 5150 3300
Wire Wire Line
	4050 3200 4050 3700
Wire Wire Line
	4100 3200 4050 3200
Wire Wire Line
	6250 2250 5550 2250
Wire Wire Line
	6250 2300 6250 2250
Wire Wire Line
	6600 2300 6250 2300
Wire Wire Line
	6650 2400 6400 2400
Wire Wire Line
	6650 3100 6600 3100
Wire Wire Line
	5650 3100 5550 3100
Wire Wire Line
	5350 2400 5650 2400
Wire Wire Line
	6650 2600 6650 2400
Wire Wire Line
	6650 2900 6650 3100
$Comp
L Device:R R5
U 1 1 5D774A20
P 6650 2750
F 0 "R5" H 6700 2800 50  0000 L CNN
F 1 "1k" H 6700 2700 50  0000 L CNN
F 2 "" V 6580 2750 50  0001 C CNN
F 3 "~" H 6650 2750 50  0001 C CNN
	1    6650 2750
	1    0    0    -1  
$EndComp
Wire Wire Line
	5650 2600 5650 2400
Wire Wire Line
	5650 2900 5650 3100
$Comp
L Device:R R2
U 1 1 5D770781
P 5650 2750
F 0 "R2" H 5720 2796 50  0000 L CNN
F 1 "1k" H 5720 2705 50  0000 L CNN
F 2 "" V 5580 2750 50  0001 C CNN
F 3 "~" H 5650 2750 50  0001 C CNN
	1    5650 2750
	1    0    0    -1  
$EndComp
Wire Wire Line
	5200 2300 6200 2300
Wire Wire Line
	5200 2250 5200 2300
Wire Wire Line
	5150 2250 5200 2250
Wire Wire Line
	6050 2150 6200 2150
Wire Wire Line
	6050 2450 6050 2150
Wire Wire Line
	5000 2150 5150 2150
Wire Wire Line
	5000 2450 5000 2150
Wire Wire Line
	6100 2450 6050 2450
Wire Bus Line
	6400 2350 6400 2500
$Comp
L Device:R R6
U 1 1 5D75E5FA
P 6250 2450
F 0 "R6" V 6050 2450 50  0000 C CNN
F 1 "10k" V 6150 2450 50  0000 C CNN
F 2 "" V 6180 2450 50  0001 C CNN
F 3 "~" H 6250 2450 50  0001 C CNN
	1    6250 2450
	0    -1   -1   0   
$EndComp
Wire Wire Line
	5050 2450 5000 2450
Wire Bus Line
	5350 2350 5350 2500
$Comp
L Device:R R3
U 1 1 5D75AC60
P 5200 2450
F 0 "R3" V 5000 2450 50  0000 C CNN
F 1 "10k" V 5100 2450 50  0000 C CNN
F 2 "" V 5130 2450 50  0001 C CNN
F 3 "~" H 5200 2450 50  0001 C CNN
	1    5200 2450
	0    -1   -1   0   
$EndComp
Wire Wire Line
	4600 2400 4300 2400
Wire Wire Line
	4600 2600 4600 2400
Wire Wire Line
	4600 3100 4500 3100
Wire Wire Line
	4600 2900 4600 3100
$Comp
L Device:R R8
U 1 1 5D7513AB
P 4600 2750
F 0 "R8" H 4670 2796 50  0000 L CNN
F 1 "1k" H 4670 2705 50  0000 L CNN
F 2 "" V 4530 2750 50  0001 C CNN
F 3 "~" H 4600 2750 50  0001 C CNN
	1    4600 2750
	1    0    0    -1  
$EndComp
Wire Wire Line
	3950 2150 4100 2150
Wire Wire Line
	3950 2450 3950 2150
Wire Wire Line
	4000 2450 3950 2450
Wire Wire Line
	4100 2300 5150 2300
Wire Wire Line
	6800 3450 6800 4600
Wire Bus Line
	6400 3350 6400 3450
Wire Wire Line
	5650 3450 5700 3450
Wire Wire Line
	4600 3450 4700 3450
Wire Bus Line
	6700 3350 6700 3500
$Comp
L Device:R R4
U 1 1 5D6A6D36
P 6550 3400
F 0 "R4" V 6750 3400 50  0000 C CNN
F 1 "4.7k" V 6650 3400 50  0000 C CNN
F 2 "" V 6480 3400 50  0001 C CNN
F 3 "~" H 6550 3400 50  0001 C CNN
	1    6550 3400
	0    1    1    0   
$EndComp
Wire Wire Line
	6800 3450 6700 3450
Wire Bus Line
	5350 3350 5350 3450
$Comp
L Transistor_BJT:BC547 Q5
U 1 1 5D6725EB
P 4300 3150
F 0 "Q5" V 4628 3150 50  0000 C CNN
F 1 "BC547" V 4537 3150 50  0000 C CNN
F 2 "Package_TO_SOT_THT:TO-92_Inline" H 4500 3075 50  0001 L CIN
F 3 "http://www.fairchildsemi.com/ds/BC/BC547.pdf" H 4300 3150 50  0001 L CNN
	1    4300 3150
	0    1    -1   0   
$EndComp
$Comp
L Device:R R7
U 1 1 5D6C0464
P 4450 3400
F 0 "R7" V 4250 3400 50  0000 C CNN
F 1 "4.7k" V 4350 3400 50  0000 C CNN
F 2 "" V 4380 3400 50  0001 C CNN
F 3 "~" H 4450 3400 50  0001 C CNN
	1    4450 3400
	0    -1   -1   0   
$EndComp
Wire Bus Line
	4600 3500 4600 3350
Wire Wire Line
	4700 3450 4700 3800
Wire Bus Line
	4500 3000 4500 3250
Wire Bus Line
	4300 3350 4300 3450
Wire Bus Line
	4300 2350 4300 2500
$Comp
L Device:R R9
U 1 1 5D6C940F
P 4150 2450
F 0 "R9" V 3950 2450 50  0000 C CNN
F 1 "10k" V 4050 2450 50  0000 C CNN
F 2 "" V 4080 2450 50  0001 C CNN
F 3 "~" H 4150 2450 50  0001 C CNN
	1    4150 2450
	0    -1   -1   0   
$EndComp
Text Notes 5750 1050 0    168  ~ 0
solar panels
Text Notes 3650 3700 0    50   ~ 0
GND
Wire Wire Line
	3500 1700 3500 4650
Wire Wire Line
	2600 1700 3500 1700
Wire Bus Line
	6600 3000 6600 3250
Wire Bus Line
	6200 3000 6200 3350
Wire Bus Line
	5150 3000 5150 3350
Wire Bus Line
	5550 3000 5550 3250
Wire Bus Line
	4100 3000 4100 3350
Wire Bus Line
	5150 2000 5150 2350
Wire Bus Line
	6200 2000 6200 2350
Wire Bus Line
	6600 2000 6600 2350
Wire Wire Line
	6850 2150 6600 2150
Wire Bus Line
	5550 2000 5550 2350
Wire Wire Line
	5700 3450 5700 3900
Wire Bus Line
	5650 3500 5650 3350
$Comp
L Device:R R1
U 1 1 5D634E9C
P 5500 3400
F 0 "R1" V 5700 3400 50  0000 C CNN
F 1 "4.7k" V 5600 3400 50  0000 C CNN
F 2 "" V 5430 3400 50  0001 C CNN
F 3 "~" H 5500 3400 50  0001 C CNN
	1    5500 3400
	0    1    1    0   
$EndComp
Wire Wire Line
	5600 4150 5600 4700
Wire Wire Line
	1950 1800 1950 1900
Wire Wire Line
	3250 3050 2600 3050
Wire Wire Line
	2600 2950 3300 2950
Wire Wire Line
	3350 2650 3350 4700
Wire Wire Line
	2600 2650 3350 2650
Wire Wire Line
	3400 2550 3400 4750
Wire Wire Line
	2600 2550 3400 2550
Wire Wire Line
	3450 2450 3450 4800
Wire Wire Line
	2600 2450 3450 2450
Connection ~ 2800 2200
Wire Wire Line
	2850 1550 3850 1550
Wire Wire Line
	2850 2200 2850 1550
Wire Wire Line
	2800 2200 2850 2200
Wire Wire Line
	1900 1700 1950 1700
Wire Wire Line
	1900 1150 1900 1700
Wire Wire Line
	2800 1150 1900 1150
Wire Wire Line
	2800 2200 2800 1150
Wire Wire Line
	2600 2200 2800 2200
Wire Wire Line
	2700 2100 2750 2100
Connection ~ 2700 2100
Wire Wire Line
	2700 2100 2700 1200
Wire Wire Line
	2600 2100 2700 2100
$Comp
L Transistor_BJT:BC547 Q3
U 1 1 5D6770F7
P 6400 3150
F 0 "Q3" V 6728 3150 50  0000 C CNN
F 1 "BC547" V 6650 3150 50  0000 C CNN
F 2 "Package_TO_SOT_THT:TO-92_Inline" H 6600 3075 50  0001 L CIN
F 3 "http://www.fairchildsemi.com/ds/BC/BC547.pdf" H 6400 3150 50  0001 L CNN
	1    6400 3150
	0    1    -1   0   
$EndComp
$Comp
L Transistor_BJT:BC547 Q1
U 1 1 5D676572
P 5350 3150
F 0 "Q1" V 5678 3150 50  0000 C CNN
F 1 "BC547" V 5587 3150 50  0000 C CNN
F 2 "Package_TO_SOT_THT:TO-92_Inline" H 5550 3075 50  0001 L CIN
F 3 "http://www.fairchildsemi.com/ds/BC/BC547.pdf" H 5350 3150 50  0001 L CNN
	1    5350 3150
	0    1    -1   0   
$EndComp
$Comp
L Transistor_BJT:BC557 Q4
U 1 1 5D66D42B
P 6400 2150
F 0 "Q4" V 6728 2150 50  0000 C CNN
F 1 "BC557" V 6637 2150 50  0000 C CNN
F 2 "Package_TO_SOT_THT:TO-92_Inline" H 6600 2075 50  0001 L CIN
F 3 "http://www.fairchildsemi.com/ds/BC/BC557.pdf" H 6400 2150 50  0001 L CNN
	1    6400 2150
	0    1    -1   0   
$EndComp
$Comp
L Transistor_BJT:BC557 Q2
U 1 1 5D669786
P 5350 2150
F 0 "Q2" V 5678 2150 50  0000 C CNN
F 1 "BC557" V 5587 2150 50  0000 C CNN
F 2 "Package_TO_SOT_THT:TO-92_Inline" H 5550 2075 50  0001 L CIN
F 3 "http://www.fairchildsemi.com/ds/BC/BC557.pdf" H 5350 2150 50  0001 L CNN
	1    5350 2150
	0    1    -1   0   
$EndComp
$Comp
L solarduino:panel-controller U1
U 1 1 5D62F629
P 2300 2300
F 0 "U1" H 2275 3265 50  0000 C CNN
F 1 "panel-controller" H 2275 3174 50  0000 C CNN
F 2 "" H 2300 2950 50  0001 C CNN
F 3 "" H 2300 2950 50  0001 C CNN
	1    2300 2300
	1    0    0    -1  
$EndComp
Wire Wire Line
	2600 1600 3400 1600
Wire Wire Line
	3400 1600 3400 2200
Wire Wire Line
	3400 2200 4100 2200
Wire Wire Line
	2600 1250 2600 1600
Connection ~ 2600 1600
Wire Wire Line
	4600 2150 4500 2150
Wire Wire Line
	2750 2100 2750 1750
Wire Wire Line
	4600 1750 4600 2150
Wire Wire Line
	2750 1750 4600 1750
Wire Bus Line
	4500 2000 4500 2350
$Comp
L Transistor_BJT:BC557 Q6
U 1 1 5D639566
P 4300 2150
F 0 "Q6" V 4628 2150 50  0000 C CNN
F 1 "BC557" V 4537 2150 50  0000 C CNN
F 2 "Package_TO_SOT_THT:TO-92_Inline" H 4500 2075 50  0001 L CIN
F 3 "http://www.fairchildsemi.com/ds/BC/BC557.pdf" H 4300 2150 50  0001 L CNN
	1    4300 2150
	0    1    -1   0   
$EndComp
Wire Bus Line
	4100 2000 4100 2350
$Comp
L solarduino:RaspberryPi_4 U3
U 1 1 5D643C9E
P 10200 1900
F 0 "U3" H 9500 3050 50  0000 C CNN
F 1 "RaspberryPi_4" H 10650 850 50  0000 C CNN
F 2 "" H 10200 1900 50  0001 C CNN
F 3 "" H 10200 1900 50  0001 C CNN
	1    10200 1900
	1    0    0    -1  
$EndComp
Wire Wire Line
	3400 8400 7250 8400
Wire Wire Line
	3900 8300 7150 8300
Wire Wire Line
	4200 8200 7050 8200
Wire Wire Line
	4500 8100 6950 8100
Wire Wire Line
	4800 8000 6850 8000
Wire Wire Line
	5350 7900 6750 7900
Wire Wire Line
	4200 7100 4200 8200
Wire Wire Line
	7250 6750 7250 8400
Wire Wire Line
	7150 6650 7150 8300
Wire Wire Line
	7050 6550 7050 8200
Wire Wire Line
	6950 6450 6950 8100
Wire Wire Line
	6850 6350 6850 8000
Wire Wire Line
	6750 6250 6750 7900
Wire Notes Line
	7400 7300 7300 7300
Wire Notes Line
	7300 7300 7300 8550
Wire Notes Line
	7400 7300 7400 500 
$Comp
L solarduino:JE342 U8
U 1 1 5EDE6F0B
P 9650 5550
F 0 "U8" H 9150 6000 50  0000 L CNN
F 1 "JE342" H 10200 5000 50  0000 L CNN
F 2 "" H 9400 5700 168 0001 C CNN
F 3 "" H 9400 5700 168 0001 C CNN
	1    9650 5550
	1    0    0    -1  
$EndComp
Wire Wire Line
	9100 5950 8850 5950
Wire Wire Line
	8850 5950 8500 5950
Wire Wire Line
	8850 5250 9100 5250
Wire Wire Line
	8850 5250 8850 6850
Wire Wire Line
	8800 5350 9100 5350
Wire Wire Line
	8800 5350 8800 6750
Wire Wire Line
	8750 5450 9100 5450
Wire Wire Line
	8750 5450 8750 6650
Wire Wire Line
	8700 5550 9100 5550
Wire Wire Line
	8700 5550 8700 6550
Wire Wire Line
	8650 5650 9100 5650
Wire Wire Line
	8600 5750 9100 5750
Wire Wire Line
	8550 5850 9100 5850
Wire Wire Line
	9750 5250 9750 5350
Wire Wire Line
	9750 5350 10250 5350
Wire Wire Line
	9650 5250 9650 5450
Wire Wire Line
	9550 5250 9550 5550
Wire Wire Line
	9850 5950 9850 5850
Wire Wire Line
	9750 5950 9750 5750
Wire Wire Line
	9550 5950 9550 5650
Wire Wire Line
	8000 4900 10250 4900
Wire Wire Line
	10250 4900 10250 5350
Wire Wire Line
	10350 5450 10350 4250
Wire Wire Line
	10150 4250 10350 4250
Wire Wire Line
	9650 5450 10350 5450
Wire Wire Line
	9550 5550 10450 5550
Wire Wire Line
	10550 5650 10550 4600
Wire Wire Line
	10550 4600 9850 4600
Wire Wire Line
	9850 4600 9850 4300
Wire Wire Line
	9550 5650 10550 5650
Wire Wire Line
	10650 4950 10650 5750
Wire Wire Line
	7900 4950 10650 4950
Wire Wire Line
	9750 5750 10650 5750
Wire Wire Line
	10800 5850 10800 4850
Wire Wire Line
	8100 4850 10800 4850
Wire Wire Line
	9850 5850 10800 5850
$Comp
L solarduino:JE342 U6
U 1 1 5F18E284
P 3350 5300
F 0 "U6" H 2850 5750 50  0000 L CNN
F 1 "JE342" H 3850 4750 50  0000 L CNN
F 2 "" H 3100 5450 168 0001 C CNN
F 3 "" H 3100 5450 168 0001 C CNN
	1    3350 5300
	1    0    0    -1  
$EndComp
Wire Wire Line
	2700 5000 2800 5000
Wire Wire Line
	2700 5000 2700 6550
Wire Wire Line
	2650 5100 2800 5100
Wire Wire Line
	2650 5100 2650 6450
Wire Wire Line
	2600 5200 2800 5200
Wire Wire Line
	2600 5200 2600 6350
Wire Wire Line
	2550 5300 2800 5300
Wire Wire Line
	2550 5300 2550 6250
Wire Wire Line
	2500 5400 2800 5400
Wire Wire Line
	2450 5500 2800 5500
Wire Wire Line
	2400 5600 2800 5600
Wire Wire Line
	2350 5700 2800 5700
Wire Wire Line
	3450 5000 3450 5100
Wire Wire Line
	3350 5000 3350 5200
Wire Wire Line
	3250 5000 3250 5300
Wire Wire Line
	3550 5700 3550 5600
Wire Wire Line
	3450 5700 3450 5500
Wire Wire Line
	3250 5700 3250 5400
Wire Wire Line
	3250 5400 4100 5400
Wire Wire Line
	4100 4850 5400 4850
Wire Wire Line
	4100 4850 4100 5400
Wire Wire Line
	4200 4600 4200 5600
Wire Wire Line
	3550 5600 4200 5600
Wire Wire Line
	4200 4600 6800 4600
Wire Wire Line
	3450 5100 3950 5100
Wire Wire Line
	3350 5200 4000 5200
Wire Wire Line
	3250 5300 4050 5300
Wire Wire Line
	3450 5500 4150 5500
Wire Wire Line
	3950 5100 3950 3800
Wire Wire Line
	3950 3800 4700 3800
Wire Wire Line
	4150 5500 4150 3900
Wire Wire Line
	4150 3900 5700 3900
Wire Bus Line
	3800 3700 6750 3700
Wire Wire Line
	6150 3300 6150 3700
Wire Wire Line
	6150 3300 6200 3300
Wire Wire Line
	5500 3700 5500 4950
Wire Wire Line
	3850 1550 3850 3700
Wire Wire Line
	4000 3700 4000 5200
Wire Wire Line
	10450 4700 10450 5550
Wire Wire Line
	8250 4700 10450 4700
Wire Wire Line
	8250 3650 8250 4700
Wire Wire Line
	700  3200 1000 3200
Wire Wire Line
	1000 3150 1000 3200
$EndSCHEMATC
