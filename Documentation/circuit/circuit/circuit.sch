EESchema Schematic File Version 4
EELAYER 30 0
EELAYER END
$Descr A2 23386 16535
encoding utf-8
Sheet 1 1
Title "SolArduino"
Date "2019-08-24"
Rev "1"
Comp ""
Comment1 ""
Comment2 ""
Comment3 ""
Comment4 ""
$EndDescr
$Comp
L solarduino:button S?
U 1 1 5D618DF8
P 1850 3100
F 0 "S?" H 2128 3146 50  0000 L CNN
F 1 "Down button" H 2128 3055 50  0000 L CNN
F 2 "" H 1650 3150 50  0001 C CNN
F 3 "" H 1650 3150 50  0001 C CNN
	1    1850 3100
	1    0    0    -1  
$EndComp
$Comp
L solarduino:button S?
U 1 1 5D61A850
P 1850 4000
F 0 "S?" H 2128 4046 50  0000 L CNN
F 1 "Up button" H 2128 3955 50  0000 L CNN
F 2 "" H 1650 4050 50  0001 C CNN
F 3 "" H 1650 4050 50  0001 C CNN
	1    1850 4000
	1    0    0    -1  
$EndComp
$Comp
L solarduino:panel-controller U?
U 1 1 5D62F629
P 4400 2550
F 0 "U?" H 4375 3515 50  0000 C CNN
F 1 "panel-controller" H 4375 3424 50  0000 C CNN
F 2 "" H 4400 3200 50  0001 C CNN
F 3 "" H 4400 3200 50  0001 C CNN
	1    4400 2550
	1    0    0    -1  
$EndComp
$Comp
L solarduino:terminal U?
U 1 1 5D63573B
P 6300 5350
F 0 "U?" H 7428 4971 50  0000 L CNN
F 1 "terminal" H 7428 4880 50  0000 L CNN
F 2 "" H 6150 5200 50  0001 C CNN
F 3 "" H 6150 5200 50  0001 C CNN
	1    6300 5350
	1    0    0    -1  
$EndComp
Wire Bus Line
	6200 2200 6200 2600
$Comp
L Transistor_BJT:BC557 Q?
U 1 1 5D639566
P 6400 2400
F 0 "Q?" V 6728 2400 50  0000 C CNN
F 1 "BC557" V 6637 2400 50  0000 C CNN
F 2 "Package_TO_SOT_THT:TO-92_Inline" H 6600 2325 50  0001 L CIN
F 3 "http://www.fairchildsemi.com/ds/BC/BC557.pdf" H 6400 2400 50  0001 L CNN
	1    6400 2400
	0    -1   -1   0   
$EndComp
Wire Bus Line
	6600 2200 6600 2600
$Comp
L solarduino:MCP3008 U?
U 1 1 5D640D67
P 11300 5000
F 0 "U?" H 11525 5415 50  0000 C CNN
F 1 "MCP3008" H 11525 5324 50  0000 C CNN
F 2 "" H 11300 5000 50  0001 C CNN
F 3 "" H 11300 5000 50  0001 C CNN
	1    11300 5000
	1    0    0    -1  
$EndComp
$Comp
L solarduino:RaspberryPi_4 U?
U 1 1 5D643C9E
P 10950 1350
F 0 "U?" H 11475 1665 50  0000 C CNN
F 1 "RaspberryPi_4" H 11475 1574 50  0000 C CNN
F 2 "" H 10950 1350 50  0001 C CNN
F 3 "" H 10950 1350 50  0001 C CNN
	1    10950 1350
	1    0    0    -1  
$EndComp
$EndSCHEMATC
