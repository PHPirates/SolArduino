(* ::Package:: *)

BeginPackage[ "SolArduino`"]

	angle::usage = 
		"angle[index of sunPositions table, solar panel angle in degrees, date (date object)] returns misalignment with the sun in radians"
		
	directPower::usage = 
		"directPower[index of sunPositions table, solar panel angle in degrees, date (date object)] returns power that the solar panels receive from the sun in  W/m^2 "
			
	calculatesunPos::usage = 
		"calculatesunPos[date] initializes sunPositions table with positions of the sun for this day"
		
	initialiseSunPositionByPrecision::usage =
		"initialiseSunPositionByPrecision[date,precision (hours*100)] initializes sunPositions table with positions of the sun for this day "
		
	calculatesunPosPerHour::usage = 
		"calculatesunPosPerHour[date] initializes sunPositions table with positions of the sun for this day, at every whole hour"
			
	sumOverInterval::usage = 
		"sumOverInterval[solar panel angle in degrees, date, start index, end index]returns the sum of solar power over the day at every time corresponding to the index of sunPositions, in the given interval"
	
	angleInterval::usage = 
		"angleInterval[date, start index, end index] returns optimal angle in this interval of indices"

	angleMoment::usage = 
	"angleMoment[date, index] returns a list with optimal angle and power at that moment"
			
	dayAngles::usage = 
		"dayAngles[date, number of adjustment times per day \[GreaterEqual] 1] returns List with optimal angles and power received with that angle over each interval (spread evenly over the day) after an angle adjustment"
			
	dayAnglesHour::usage = "dayAnglesHour[date] returns a list of {power output, optimal angle} at each whole hour of the time the sun is up"
			
	dayPower::usage = 
		"dayPower[list of {power,angle}] returns total of power received by the sun"
		
	dayPowerTable::usage = 
		"dayPowerTable[list of {power, angle}] returns list of power received for each angle-period"
		
	dayAnglesTable::usage = 
		"dayAnglesTable[list of {power, angle}] returns list of angles"
		
	totalPower::usage = 
		"totalPower[date, division as index of sunPositions] returns total power of the day, and list with two optimal angles with this division, to get that total power"
			
	dayAngle::usage = 
		"dayAngle[day of month, month, year] returns optimal angle for this day"
	
	monthAngle::usage = 
		"monthAngle[month, year] returns optimal angle for a specific month by taking the average of the optimal angle each day"
			
	twoAnglesOptimal::usage = 
		"twoAnglesOptimal[date] returns in a list the date/time at which to change the angle of solar panels (besides before sunrise) to get optimal power, then the two angles of the day, then the percent increase of power compared to 			one setting for the whole day, then the percent increase compared to one setting if you would adjust 			fourteen times a day, spread evenly over the day"
			
	powerOverDay::usage = 
		"powerOverDay[list of angles of a day, date] returns a list with power received for each angle-period"
		
	anglesPeriod::usage = 
		"anglesPeriod[begin date, end date] creates list of optimal angles per day over given interval"
	
	csvToList::usage =
		"csvToList[date,data] returns list of values for that date that exist in data"
	
	getSunPositionsLength::usage = "returns sunPositions table length"
	
	exportPeriod::usage = 
	"exportPeriod[begin date, end date, number of adjustments per day], exports two csv files. One with the Unix Times over the given period, another with the angles."

  Begin[ "Private`"]

(* switch off message that solar panels cannot be lower than 0 degrees *)
Off[FindMinimum::reged];
Off[FindMaximum::reged];

sunPosPrecision = 50; (* Used in calculatesunPos; 50 is steps of half an hour (hour*100) for the sun positions table, 
smaller steps is slower but more precise *)

(* parameters are forced numeric, so findMaximum in dayAngle works better *)
angle[i_?NumericQ,a_?NumericQ,d_]:= ( (* angle[index of sunPositions table, solar panel angle in degrees, 
datespec] returns misalignment with the sun (zenith) in radians *)
sunPos = sunPositions[[i]]; (* lookup sun position in the pre-initialised sunPositions list *)

(* remove degree unit, then convert to radians *)
gammaS= QuantityMagnitude[Quantity[sunPos[[1]]]]*Pi/180;  (* Azimuth of sun position, 0 due South, negative to the east *)
(* Because Mathematica gives azimuth in compass angle, we subtract 180 degrees *)
gammaS = gammaS - Pi;
thetaS= QuantityMagnitude[Quantity[sunPos[[2]]]]*Pi/180; (* Sun altitude *)
gammaP = -22 * Pi/180; (* Solar panels azimuth *)
thetaP = a * Pi/180; (* Solar panels angle with horizontal *)
ArcCos[Cos[gammaP-gammaS] Cos[thetaS] Sin[thetaP]+Cos[thetaP] Sin[thetaS]] (* The formula to find the zenith, see docs *)
)


directPower[x_?NumericQ,a_?NumericQ,d_]  := ( (* directPower[index, solar panel angle in degrees, datespec] 
returns power that the solar panels receive from the sun in  W/m^2 *)
theta=angle[x,a,d]; (* solar zenith (misalignment) in radians *)
(* here comes the magik formula from http://www.powerfromthesun.net/Book/chapter02/chapter02.html#ZEqnNum929295 *)
n= QuantityMagnitude[Quantity[d - DateObject[{2016,1,1}]]]; (* day of year *)
(* clear day parameters *)
(*i= 1367 (1+0.034 Cos[360*n/365.25]);
aZero = 0.4237 - 0.00821 36;
aOne = 0.5055 + 0.00595 6.5^2;
k = 0.2711 + 0.01858 2.5^2;*)
(* parameters from urban visibility haze model *)
i= 1367 (1+0.034 Cos[360*n/365.25]);
aZero = 0.2538 - 0.0063 36;
aOne = 0.7678 + 0.001 6.5^2;
k = 0.249 + 0.081 2.5^2;
If[Cos[theta]<0,
0, (* If the sun is more than 90 degrees off, the formula is not correct, and we set the insolation to 0 *)
i (aZero + aOne E^(-k/Cos[theta])) 
] 
)

(* Another power function which seems similar, from http://www.solarpaneltilt.com/ *)
(*directPowerWeb[x_?NumericQ,a_?NumericQ,d_]  := ( (* param: index, solar panel angle in degrees, date. returns W/m^2 *)
theta=angle[x,a,d]; (* solar zenith (misalignment) in radians *)
If[theta>Pi/2,
0,
1.35*(1/1.35)^(Sec[theta])*1000
]
)*)

calculatesunPos[date_] := ( (* calculatesunPos[date] initializes sunPositions table with positions of the sun for this day *)
position = GeoPosition[{51.546545,4.411744}]; (* position of solar panels *)
sunrise = Sunrise[position,date];
(*TimeZoneConvert[sunrise,2];*) (* can be used if using this package in a different timezone *)
sunriseHour = DateValue[sunrise,"Hour"]+DateValue[sunrise, "Minute"]/60;
sunset = Sunset[position,date];
(*TimeZoneConvert[sunset,2];*)
sunsetHour = DateValue[sunset,"Hour"] + DateValue[sunset, "Minute"]/60;
(* generate sun position table, so angle[] can access it every time. In this way the table is only generated once per day *)
sunPositions = Table[
 SunPosition[GeoPosition[{51.546545,4.411744}],DateObject[date,{h/100}]],
{h,sunriseHour*100,sunsetHour*100,sunPosPrecision} (* values are times hundred so we can iterate over the hour *)
];
)

initialiseSunPositionByPrecision[date_,p_] := ( (* initialiseSunPositionByPrecision[date,precision (hours*100)] 
initializes sunPositions table with positions of the sun for this day *)
position = GeoPosition[{51.546545,4.411744}]; (* position of solar panels *)
sunrise = Sunrise[position,date];
(*TimeZoneConvert[sunrise,2];*) (* can be used if using this package in a different timezone *)
sunriseHour = DateValue[sunrise,"Hour"]+DateValue[sunrise, "Minute"]/60;
sunset = Sunset[position,date];
(*TimeZoneConvert[sunset,2];*)
sunsetHour = DateValue[sunset,"Hour"] + DateValue[sunset, "Minute"]/60;
(* generate sun position table, so angle[] can access it every time. In this way the table is only generated once per day *)
sunPositions = Table[
 SunPosition[GeoPosition[{51.546545,4.411744}],DateObject[date,{h/100}]],
{h,sunriseHour*100,sunsetHour*100,p} (* values are times hundred so we can iterate over the hour *)
];
)

calculatesunPosPerHour[d_] := ( (* calculatesunPosPerHour[date] initializes sunPositions table with positions of the sun for this
 day, at every whole hour *)
position = GeoPosition[{51.546545,4.411744}];
sunrise = Sunrise[position,d];
sunriseHour = DateValue[sunrise,"Hour"]+1; (* first whole hour with sun *)
sunset = Sunset[position,d];
sunsetHour = DateValue[sunset,"Hour"];
(* generate sun position table, so angle[] can access it every time, steps of an hour, at the whole hour *)
sunPositions = Table[
 SunPosition[GeoPosition[{51.546545,4.411744}],DateObject[d,{h/100}]],
{h,sunriseHour*100,sunsetHour*100,100} (* values are times hundred so we can iterate over the hour *)
];
)


sumOverInterval[a_,d_,s_,e_] := ( (* sumOverInterval[solar panel angle in degrees, date, start index, end index]
returns the sum of solar power over the day at every time corresponding to the index of sunPositions, in the given interval *)
(* take sum of every index, hence steps of one *)
Sum[directPower[i,a,d],{i,s,e,1}]
)

angleInterval[d_,s_,e_] := ( (* angleInterval[date, start index, end index] 
returns optimal angle in this interval of indices *)
(* Find for which angle of the solar panels this value is maximal,starting at 0 *)
res = FindMaximum[sumOverInterval[x,d,s,e],{x,0,0,90}, AccuracyGoal->5];
{res[[1]],x /. res[[2]]}
)

angleMoment[d_,i_] := ( (* angleMoment[date, index] returns a list with optimal angle and power at that moment *)
res = FindMaximum[directPower[i,x,d],{x,0,0,90}, AccuracyGoal->5];
{res[[1]],x /. res[[2]]}
)

dayAngles[d_,n_] :=( (* dayAngles[date, number of adjustment times per day \[GreaterEqual] 1] returns List with optimal angles 
and power received with that angle over each interval (spread evenly over the day) after an angle adjustment *)
calculatesunPos[d]; 

l = Length[sunPositions];
Table[ (* calculates indices of start and end of the interval *)
angleInterval[d,1+Round[l*i/n],Round[l*(i+1)/n]]
,{i,0,n-1}] 
)

dayAnglesHour[d_] := ( (* dayAnglesHour[date] returns a list of {power output, optimal angle} at each whole 
hour of the time the sun is up *)
calculatesunPosPerHour[d];
l = Length[sunPositions];
Table[
angleMoment[d,i],
{i,1,l}
]
)

dayPower[t_] := ( (* dayPower[list of {power, angle}] returns total of power received by the sun *)
Sum[
t[[i]][[1]],
{i,1,Length[t]}
] 
)

dayPowerTable[t_] := ( (* dayPowerTable[list of {power, angle}] returns list of power received for each angle-period *)
Table[
t[[i]][[1]],
{i,1,Length[t]}
]
)

dayAnglesTable[t_] := (
(* dayAnglesTable[list of {power, angle}] returns list of angles*)
Table[
t[[i]][[2]],
{i,1,Length[t]}
]
)

(* used to determine best time for a second adjustment *)
totalPower[d_,v_] := ( (* totalPower[date, division as index of sunPositions] returns total power of the day, 
and list with two optimal angles with this division, to get that total power *)
first = angleInterval[d,1,v];
second = angleInterval[d,v+1,l];
total = first[[1]] + second[[1]];
angles = {first[[2]],second[[2]]};
{total,angles}
)

dayAngle[d_,m_,y_] := ( (* dayAngle[day of month, month, year] returns optimal angle for this day *)

date = DateObject[{y,m,d}];
calculatesunPos[date];

(* Find for which angle of the solar panels this value is maximal,starting at 50 *)
a/. FindMaximum[
Sum[directPower[h,a,date],{h,1,Length[sunPositions],1}]
,{a,50,0,90}, AccuracyGoal->5][[2]]
)

monthAngle[m_,y_] := ( (* monthAngle[month, year] returns optimal angle for a specific month by taking
 the average of the optimal angle each day  *)
(* Count how many days are in this month *)
days = With[{first=DateObject[{y,m,1}]},DayCount[first,DatePlus[first,{{1,"Month"}}]]] ;
Sum[dayAngle[x,m,y],{x,1,days}]/days
)

 (*  NOTE for a preciser adjustment time, change step size where noted. 
 will increase running time by as much steps are added times around three seconds *)
twoAnglesOptimal[d_] := (  (* twoAnglesOptimal[date] returns in a list the date/time at which to change the 
angle of solar panels (besides before sunrise) to get optimal power, then the two angles of the day, then the 
percent increase of power compared to one setting for the whole day, then the percent increase compared to one 
setting if you would adjust fourteen times a day, spread evenly over the day *)
(* for two angles per day, find optimal time of angle change *)

(* some initialisation of the sunposition table *)
calculatesunPos[d];
l = Length[sunPositions];

(* steps of about one per hour is l/14 *)
(*DiscretePlot[totalPower[d,v],{v,Round[l/2],l,Round[l/12]},ImageSize \[Rule] Large]*)
t = Table[{v,totalPower[d,v]},{v,Round[l/2],Round[4 l/5],Round[l/14]}];
max = Max[t]; (* total power over the day with this setting *)
index = t[[Position[t,Max[t]][[1]][[1]]]][[1]] ;
angles = t[[Position[t,Max[t]][[1]][[1]]]][[2]][[2]] ;
(* brackets! gives index around which the highest total resides *)
(* index back to time *)
hour =(index/l)*(sunsetHour - sunriseHour)+sunriseHour ; (* the hour at which to set the solar panels second time *)
(* find percentage increase in power *)
onePower = dayAngles[d,1][[1]][[1]]; (* find power for one time *)
(* compare with percent increase in power if you'd adjust 10 times a day (not optimised) *)
tenPower= dayPower[dayAngles[d,14]];
(* return values *)
{DateObject[d,{IntegerPart[hour],Round[FractionalPart[hour]*60]}],
angles,(max - onePower)/onePower * 100,
(tenPower - onePower)/onePower * 100}
)

powerOverDay[l_,date_] := ( (* powerOverDay[list of angles of a day, date] returns a list with power received for each angle-period *)
calculatesunPos[date];
table={};

For[j=1,j< Length[l] + 1,j++, (* for each angle, *)
	table = Join[
		table,
		Table[
			directPower[i,l[[j]],date], (* make a list with power corresponding to the angles *)
			{i,
			Round[Length[sunPositions] * ((j-1)/Length[l])+1], (* times (indices) are spread evenly over the day *)
			Round[Length[sunPositions] * (j/Length[l])]}
		]
	];
];
table
)

anglesPeriod[b_,c_] := ( (* anglesPeriod[begin date, end date] creates list of optimal 
angles per day over given interval  *)
numberOfDays = DayCount[b,c]+1;
dateByDay = Function[x, b+Quantity[(x-1), "Days"]];

t=Table[
	{dateByDay[d],
		Quiet[
			dayAngle[DateValue[dateByDay[d],"Day"],DateValue[dateByDay[d],"Month"],DateValue[dateByDay[d],"Year"]]
		]
	},{d,1,numberOfDays}
]
)

csvToList[d_,c_] := ( (* csvToList[date,data] returns list of values for that date that exist in data *)
dayData = Select[c,
DayCount[
DateObject[DateObject[
DateString[#[[1]],{"Day","-","Month","-","Year"," ","Hour",":","Minute"}]
],{0,0}] (* change hour to 0 to compare *) ,
d
] == 0 &
];
Table[dayData[[i]][[2]] ,{i,1,Length[dayData]}]
)

getSunPositionsLength[] := Length[sunPositions]

exportTimesPeriod[b_,c_,n_] := ( (*begin date, end date, number of adjustments per day, 
	returns list with UnixTimes of the period*)
	numberOfDays = DayCount[b,c]+1;
	dateByDay = Function[x, b+Quantity[(x-1), "Days"]];
	times = Flatten[Table[
			position = GeoPosition[{51.546545,4.411744}]; (* position of solar panels *)
	sunrise = UnixTime[Sunrise[position,dateByDay[d]]];
	sunset = UnixTime[Sunset[position,dateByDay[d]]];
	dayLight = sunset - sunrise;
	interval = dayLight / n;
	t = Table[sunrise+i*interval,{i,0,n-1}]
	,{d,1,numberOfDays}
]]
)

exportAnglesPeriod[b_,c_,n_] := ((*begin date, end date, number of adjustments per day, 
	returns list with angles of the period*)
	numberOfDays = DayCount[b,c] +1;
	dateByDay = Function[x, b+Quantity[(x-1), "Days"]];
	angles = Flatten[Table[
	dayAngles[dateByDay[d],n][[All,2]]
,{d,1,numberOfDays}
]]
)

exportPeriod[b_,c_,n_] := ( (* exportPeriod[begin date, end date, number of adjustments per day], exports two csv files. 
One with the Unix Times over the given period, another with the angles. *)
	angles = exportAnglesPeriod[b,c,n];
	anglesPath = NotebookDirectory[]<>"angles.csv";
	Export[anglesPath, {angles}];
	
	times = exportTimesPeriod[b,c,n];
	timesPath = NotebookDirectory[]<>"times.csv";
	Export[timesPath, {times}];
Length[angles]
)


  End[]

  EndPackage[]



