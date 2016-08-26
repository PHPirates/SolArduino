(* ::Package:: *)

BeginPackage[ "SolArduino`"]

	angle::usage = 
		"params: index of sunPositions table, solar panel angle in degrees, date, 
			returns misalignment with the sun in radians"
		
	directPower::usage = 
		"param: index, solar panel angle in degrees, date. 
			returns W/m^2"
			
	calculatesunPos::usage = 
		"params: date, 
			initializes sunPositions table with positions of the sun for this day"
			
	sumOverInterval::usage = 
		"params: solar panel angle in degrees, date, start index, end index
			returns sum of solar power over the day at every time of index in the given interval"
	
	angleInterval::usage = 
		"params: date, start index, end index, r
			eturns optimal angle in this interval"
			
	dayAngles::usage = 
		"params: date, number of adjustment times per day \[GreaterEqual] 1. 
			returns List with optimal angles and power received with that angle over that interval, 
			for how many adjustment times were specified"
			
	dayPower::usage = 
		"total of power"
		
	dayPowerTable::usage = 
		"list of power received for each angle-period"
		
	dayAnglesTable::usage = 
		"make list of angles"
		
	totalPower::usage = 
		"params: date, division as index of sunPositions. 
			returns total power of day, and list with two angles"
			
	dayAngle::usage = 
		"params: day of month, month, year, 
			returns optimal angle for this day"
	
	monthAngle::usage = 
		"params: month, 
			returns optimal angle for a specific month by taking the average"
			
	twoAnglesOptimal::usage = 
		"params: date; 
			returns in a list the date/time at which to change the angle of solar panels (besides before sunrise) 
			to get optimal power, then the two angles of the day, then the percent increase of power compared to 
			one setting for the whole day, then the percent increase compared to one setting if you would adjust 
			fourteen times a day, spread evenly over the day"
			
	powerOverDay::usage = 
		"param: list of angles of a day, 
			returns a list with power outputs by index of sunPositions"

	directPowerPercent::usage = 
		"percent difference by angle"
		
	solarPanelTilt::usage = 
		"TODO"
		
	f::usage = 
		"params: date, 
			returns list of values for that date"
	
	anglesPeriod::usage = 
		"params: DateObject begin date, DateObject end date,
			creates list/table of optimal angles per day over given interval."
	

  Begin[ "Private`"]
(* formula in directPower from http://www.powerfromthesun.net/Book/chapter02/chapter02.html#ZEqnNum929295 *)

(* clear days at ~25 angle: 13/5, 19/7, 17/8 *)

(* Adjusting the solar panels x times a day
The times are evenly spread over the time the sun is up *)
year = 2016;

sunPosPrecision = 50; (* 50 is steps of half an our for the sun positions table, hopefully precise enough, at least it's fast *)

(* parameters are forced numeric, so findMaximum in dayangle works better *)
angle[i_?NumericQ,a_?NumericQ,d_]:= ( (* params: index of sunPositions table, solar panel angle in degrees, date, returns misalignment with the sun in radians *)
sunPos = sunPositions[[i]]; (* lookup sun position *)

(* remove degree unit, then convert to radians *)
gammaS= QuantityMagnitude[Quantity[sunPos[[1]]]]*Pi/180;  (* Azimuth of sun position, 0 due South, negative to the east *)
(* Because Mathematica gives azimuth in compass angle, we subtract 180 degrees *)
gammaS = gammaS - Pi;
thetaS= QuantityMagnitude[Quantity[sunPos[[2]]]]*Pi/180; (* Sun altitude *)
gammaP = -22 * Pi/180; (* Solar panels azimuth *)
thetaP = a * Pi/180; (* Solar panels angle with horizontal *)
ArcCos[Cos[gammaP-gammaS] Cos[thetaS] Sin[thetaP]+Cos[thetaP] Sin[thetaS]]
)


(* parameters from urban visibility haze model *)
directPower[x_?NumericQ,a_?NumericQ,d_]  := ( (* param: index, solar panel angle in degrees, date. returns W/m^2 *)
theta=angle[x,a,d]; (* solar zenith (misalignment) in radians *)
(* here comes the magik formula from http://www.powerfromthesun.net/Book/chapter02/chapter02.html#ZEqnNum929295 for a clear day *)
n= QuantityMagnitude[Quantity[d - DateObject[{2016,1,1}]]]; (* day of year *)
(* clear day parameters *)
(*i= 1367 (1+0.034 Cos[360*n/365.25]);
aZero = 0.4237 - 0.00821 36;
aOne = 0.5055 + 0.00595 6.5^2;
k = 0.2711 + 0.01858 2.5^2;*)
i= 1367 (1+0.034 Cos[360*n/365.25]);
aZero = 0.2538 - 0.0063 36;
aOne = 0.7678 + 0.001 6.5^2;
k = 0.249 + 0.081 2.5^2;
If[Cos[theta]<0,
0,
i (aZero + aOne E^(-k/Cos[theta])) (* If the sun is more than 90 degrees off, the formula is not correct, and we set the value to be 0 *)
] 
)

(* another function which seems similar, from http://www.solarpaneltilt.com/ *)
(*directPowerWeb[x_?NumericQ,a_?NumericQ,d_]  := ( (* param: index, solar panel angle in degrees, date. returns W/m^2 *)
theta=angle[x,a,d]; (* solar zenith (misalignment) in radians *)
If[theta>Pi/2,
0,
1.35*(1/1.35)^(Sec[theta])*1000
]
)*)

calculatesunPos[d_] := ( (* params: date, initializes sunPositions table with positions of the sun for this day *)
position = GeoPosition[{51.546545,4.411744}];
sunrise = Sunrise[position,d];
TimeZoneConvert[sunrise,2];
sunriseHour = DateValue[sunrise,"Hour"]+DateValue[sunrise, "Minute"]/60;
sunset = Sunset[position,d];
TimeZoneConvert[sunset,2];
sunsetHour = DateValue[sunset,"Hour"] + DateValue[sunset, "Minute"]/60;
(* generate sun position table, so angle[] can access it every time, steps of 30 mins *)
sunPositions = Table[
 SunPosition[GeoPosition[{51.546545,4.411744}],DateObject[d,{h/100}]],
{h,sunriseHour*100,sunsetHour*100,sunPosPrecision} 
];
)

sumOverInterval:=Function[ (* params: solar panel angle in degrees, date, start index, end index
returns sum of solar power over the day at every time of index in the given interval *)
(* take sum of every index, hence steps of one *)
Sum[directPower[i,#1,#2],{i,#3,#4,1}]
]

angleInterval := Function[ (* params: date, start index, end index, returns optimal angle in this interval *)
(* Find for which angle of the solar panels this value is maximal,starting at 0 *)
res = FindMaximum[sumOverInterval[x,#1,#2,#3],{x,0,0,90}, AccuracyGoal->5];
{res[[1]],x /. res[[2]]}
]

dayAngles := Function[ (* params: date, number of adjustment times per day \[GreaterEqual] 1. returns List with optimal angles and power received with that angle over that interval, for how many adjustment times were specified *)
calculatesunPos[#1];

l = Length[sunPositions];
Table[ (* calculates indices of start and end of the interval *)
angleInterval[#1,1+Round[l*i/#2],Round[l*(i+1)/#2]]
,{i,0,#2-1}]
]

dayPower[t_] := ( (* total of power *)
sum = 0;
For[i=1,i<Length[t]+1,i++,
sum += t[[i]][[1]];
];
sum (* Total of W/m^1 received *)
)

dayPowerTable[t_] := ( (* list of power received for each angle-period *)
Table[
t[[i]][[1]],
{i,1,Length[t]}
]
)

dayAnglesTable[t_] := (
(* make list of angles *)
Table[
t[[i]][[2]],
{i,1,Length[t]}
]
)

(* find div(ision) where total W/m^m is biggest *)
totalPower[d_,v_] := ( (* params: date, division as index of sunPositions. returns total power of day, and list with two angles *)
first = angleInterval[d,1,v];
second = angleInterval[d,v+1,l];
total = first[[1]] + second[[1]];
angles = {first[[2]],second[[2]]};
{total,angles}
)

dayAngle := Function[ (* params: day of month, month, year, returns optimal angle for this day *)

date = DateObject[{#3,#2,#1}];

position = GeoPosition[{51.546545,4.411744}];
sunrise = Sunrise[position,date];
TimeZoneConvert[sunrise,2];
sunriseHour = DateValue[sunrise,"Hour"]+DateValue[sunrise, "Minute"]/60;
sunset = Sunset[position,date];
TimeZoneConvert[sunset,2];
sunsetHour = DateValue[sunset,"Hour"] + DateValue[sunset, "Minute"]/60;
(* generate sun position table, so angle[] can access it every time *)
sunPositions = Table[
 SunPosition[GeoPosition[{51.546545,4.411744}],DateObject[date,{h/100}]],
{h,sunriseHour*100,sunsetHour*100,50}
];
(* Times 100 so the sum starts closer to the real sunUpHour, and not floors it to an integer when calculating the sum. Otherwise weird jumps in graphs appear when the sunUpHour jumps one hour *)

(* Find for which angle of the solar panels this value is maximal,starting at 50 *)

a/. FindMaximum[
Sum[directPower[h,a,date],{h,1,Length[sunPositions],1}]
,{a,50,0,90}, AccuracyGoal->5][[2]]
]

monthAngle := Function [ (* params: month, returns optimal angle for a specific month by taking the average  *)
(* Count how many days are in this month *)
days = With[{first=DateObject[{year,#1,1}]},DayCount[first,DatePlus[first,{{1,"Month"}}]]] ;
Sum[dayAngle[x,#1],{x,1,days}]/days
]

 (*  NOTE for a preciser adjustment time, change step size where noted. will increase running time by as much steps are added times around three seconds *)
twoAnglesOptimal[d_] := (  (* params: date; returns in a list the date/time at which to change the angle of solar panels (besides before sunrise) to get optimal power, then the two angles of the day, then the percent increase of power compared to one setting for the whole day, then the percent increase compared to one setting if you would adjust fourteen times a day, spread evenly over the day *)
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
{DateObject[d,{IntegerPart[hour],Round[FractionalPart[hour]*60]}],angles,(max - onePower)/onePower * 100,(tenPower - onePower)/onePower * 100}
)

powerOverDay :=Function[d, (* param: list of angles of a day, returns a list with power outputs by index of sunPositions *)

legend=StringForm["Adjusting `` times", Length[d]];

calculatesunPos[date];

table={};

For[j=1,j< Length[d] + 1,j++,
table = Join[
table,
Table[
directPower[i,d[[j]],date]*factor,
{i,
Round[Length[sunPositions] * ((j-1)/Length[d])+1],
Round[Length[sunPositions] * (j/Length[d])]}
]
];
];

table
]

(* creates list/table of optimal angles per day over given interval *)
anglesPeriod = Function[{b, c}, (* Params are DateObjects, begin date, end date *)
numberOfDays = DayCount[b,c]+1;
dateByDay = Function[x, b+Quantity[(x-1), "Days"]];

t=Table[{dateByDay[d],Quiet[dayAngle[DateValue[dateByDay[d],"Day"],DateValue[dateByDay[d],"Month"],DateValue[dateByDay[d],"Year"]]]},{d,1,numberOfDays}]

];

(******************************* END OF FUNCTIONS ***************************)

(* Graph for a day *)
(* date = DateObject[{2016,7,18}];
 AbsoluteTiming[DiscretePlot[sumOverDay[x,date],{x,0,90,10},ImageSize\[Rule]Large,AxesLabel \[Rule] {"Solar panel angle", "Total of sun power, W/m^2"}] ] *)

(* Graph for a month *)
 (* month = 9;
days = With[{first=DateObject[{year,month,1}]},DayCount[first,DatePlus[first,{{1,"Month"}}]]];
AbsoluteTiming[DiscretePlot[dayAngle[a,month],{a,1,days},ImageSize\[Rule]Large,AxesLabel \[Rule] {"Day", "Optimal angle"}]] *)

(* Graph for this year, month average *)
(* AbsoluteTiming[DiscretePlot[monthAngle[x],{x,1,12},ImageSize\[Rule]Large,AxesLabel \[Rule] {"Month", "Average of optimal angle"}] ] *) (* ten minutes *)

(* Comparison with perpendicular to sun at noon, per month, http://www.gogreensolar.com/pages/solar-panel-tilt-calculator *)
(*greenList = {68,60,52,44,36,28,36,44,52,60,68,76};
list = Table[monthAngle[x],{x,1,12}];
ListPlot[{greenList,list},Filling\[Rule] Axis,PlotLegends\[Rule] {gogreensolar,calculations},ImageSize\[Rule]Large,AxesLabel \[Rule] {"Month", "Average of optimal angle"}]
*)

(* season values *)

(*monthSum[m_,i_,j_] := ( (* month, begin day, end day *)
Sum[
Quiet[dayAngle[x,m]],
{x,i,j}]
)
(* takes winter of [year] only. dates according to solarpaneltilt.com, summer april 18, autumn august 24, winter oct 7, spring march 5 *)
 summerSum = Quiet[monthSum[4,18,30] + monthSum[5,1,31] + monthSum[6,1,30] + monthSum[7,1,31]+ monthSum[8,1,23]];
summerDays = QuantityMagnitude[Quantity[DateDifference[{year,4,18},{year,8,23}]]]+1;
summerSum/summerDays (* 21 degrees *)

autumnSum = monthSum[8,24,31]+ monthSum[9,1,30] + monthSum[10,1,6];
autumnDays = DayCount[{year,8,24},{year,10,6}]+1;
autumnSum/autumnDays (* 49 degrees *)

 winterSum = Quiet[monthSum[10,7,31] + monthSum[11,1,30] +monthSum[12,1,31] + monthSum[1,1,31] + monthSum[2,1,29] + monthSum[3,1,4]];
winterDays = DayCount[{year,10,7},{year+1,3,4}]+1;
winterSum/winterDays  (* 73 degrees *)

springSum = Quiet[monthSum[3,5,31] + monthSum[4,1,17]];
springDays = DayCount[{year,3,5},{year,4,17}]+1;
springSum/springDays (* 49 degrees *)*)

(* Year average *)
(*(winterSum + summerSum + springSum + autumnSum)/366 (* 49 degrees *)*)
(* standalone *)
(*Sum[
days = With[{first=DateObject[{year,m,1}]},DayCount[first,DatePlus[first,{{1,"Month"}}]]];
Sum[
Quiet[dayAngle[d,m]]
,{d,1,days}]
,{m,1,12}]/ QuantityMagnitude[Quantity[DateDifference[{year,1,1},{year,12,31}]]]+1 (* 50 degrees *)
*)


(* Full graph of year *)
(*year = 2016;
numberOfDays = DayCount[DateObject[{year,1,1}],DateObject[{year+1,1,1}]];
dateByDay=Function[x,DateObject[{year,1,1}]+Quantity[(x-1),"Days"]];

t = Table[{dateByDay[d],Quiet[dayAngle[DateValue[dateByDay[d],"Day"],DateValue[dateByDay[d],"Month"]]]},{d,1,numberOfDays}]
DateListPlot[t,AxesLabel \[Rule] {"Degrees","2016"}]*)




(*dayAngles[DateObject[{2016,8,17}],14]*)
(*res = dayAngles[DateObject[{2016,8,19}],14]
ListPlot[Table[res[[i]][[2]] ,{i,1,Length[res]}]]*)

(* percentages *)
(*t = Table[
dayPower[dayAngles[DateObject[{2016,8,17}],x]],
{x,1,10}
]
(* relative to first *)
Table[
(t[[i]]-t[[1]])/t[[1]],
{i,2,Length[t]}
]
(* relative to previous *)
Table[
(t[[i]]-t[[i-1]])/t[[i-1]],
{i,2,Length[t]}
]*)

(*DiscretePlot[ dayPower[dayAngles[DateObject[{2016,7,18}],x]],{x,1,20,1}] //Timing*)

(*twoAnglesOptimal[DateObject[{2016,8,17}]] //Timing*)

(* list of angles for a day *)
(*dayAngles[DateObject[{2016,8,17}],14][[All,2]]*)

(* misalignment over day *)
(*ListPlot[Table[
angle[i,25,DateObject[{2016,8,17}]]
,{i,1,Length[sunPositions]}
]]*)

(* sun insolation over day, 26.4 m2 panels *)
(*ListPlot[Table[
directPower[i,25,DateObject[{2016,8,17}]]*26.4
,{i,1,Length[sunPositions]}
]]*)
(* data adapted to from sunrise, 6:33, to sunset, 21:00. Length of sunPositions and data are both 58 *)
(* day = DateObject[{2016,8,17}]; *)
dataAugust = {0,47.5,128.167,247.833,398.167,574.333,789,1053.83,1304.83,1584.33,1846.17,2144,2348.33,2555.83,2712,2908.67,3055.33,3176.33,3310.33,3419.17,3519.17,3580.67,3659.33,3707.17,3753.17,3775.17,3781,3759.5,3746.17,3728.5,3675,3599.67,3504.67,3411.83,3302.83,3197.33,3046.5,2912.83,2761.17,2574.83,2324.33,2062.5,1862.17,1688.83,1465,1254,1041.5,853.167,666,463,291.833,160.667,124.333,106.833,86,62.8333,33.3333,2};
(* day = DateObject[{2016,5,13}]; *)
dataMay = {0,19.5,57,118.667,200.833,278,301.333,497.667,691.5,995.667,1323.5,1663,1857.33,2262.67,2571,2789.83,3003,3177.5,3324.5,3483.17,3597,3719.33,3813.67,3888,3929.83,3997.83,4012.33,4036,4080.5,4067.67,4034,3957.67,3954.33,3889.17,3837,3746.83,3641.67,3453.33,3423.67,3215.5,3121.83,2954.5,2762.67,2541,2298.83,2055.33,1692,1540.17,1253,1027.17,782,579,419.5,295,226.833,185.833,158.667,115,84.5,70.1667,58.1667,20.1667,0};

(* just checking if length is about equal *)
(*Length[sunPositions]
Length[dataAugust]*)

(* calculations graph scaled to top be the same, possible because it's just about the form of the graph. otherwise, panels are 26 m2 *)
(* now shown together, but not really accurate because of length difference, better keep days in seperate graphs *)
(*ListLinePlot[{
calculatesunPos[DateObject[{2016,8,17}]];
Table[
directPower[i,25,DateObject[{2016,8,17}]]*4.22
,{i,1,Length[sunPositions]}
],
calculatesunPos[DateObject[{2016,5,13}]];
Table[
directPower[i,25,DateObject[{2016,5,13}]]*4.22
,{i,1,Length[sunPositions]}
]
,dataAugust,
dataMay},PlotLegends \[Rule] {"calculations 17/8","calculations 13/5","real data 17/8","real data 13/5"},ImageSize \[Rule] Large]*)



(* efficiency over the day *)

(*ListLinePlot[
calculatesunPos[DateObject[{2016,8,17}]];
Table[
dataAugust[[i]]*100/(directPowerUrban[i,25,DateObject[{2016,8,17}]]*26.4)
,{i,1,Length[sunPositions]}
]
]*)


(* now for adjusting panels three times a day! *)
(* for 17/8, optimal angles and times are: 50,34,0 at 6.5, 11.3, 16.2, but directPower takes indices of sunPosition  *)
(*factor = 6.7;
date = DateObject[{2016,8,23}];
(* three plots *)
ListLinePlot[ {

calculatesunPos[date];
Table[
directPower[i,25,date]*factor
,{i,1,Length[sunPositions]}
],

dataAugust,

Join[
Table[
directPower[i,49,date]*factor
,{i,1,Round[Length[sunPositions]/3]}
],
Table[
directPower[i,34,date]*factor
,{i,Round[Length[sunPositions]/3]+1,Round[Length[sunPositions]*2/3]}
],
Table[
directPower[i,0,date]*factor
,{i,Round[Length[sunPositions]*2/3]+1,Length[sunPositions]}
]
],

Join[
Table[
directPower[i,41,date]*factor
,{i,1,Round[Length[sunPositions]9/14]} (*16:00 is around 9/14  *)
],
Table[
directPower[i,0,date]*factor
,{i,Round[Length[sunPositions] 9/14]+1,Length[sunPositions]}
]
]

},PlotLegends \[Rule] {"calculations 23/8","real data 17/8","calculations adjusting three times", "adjusting two times"},ImageSize \[Rule] Large
]*)

(* plot for one day, times 6.7 to compare form of graph *)
(*ListLinePlot[{
(*calculatesunPos[DateObject[{2016,8,17}]];*)
Table[
directPowerUrban[i,25,DateObject[{2016,8,17}]]*6.7
,{i,1,Length[sunPositions]}
]
,dataAugust},PlotLegends \[Rule] {"calculations 17/8","real data 17/8"},ImageSize \[Rule] Large]*)

(* plot to compare 14 times with 2 times optimised *)
(*date = DateObject[{2016,8,17}];
dayAngles[date,14][[All,2]]
factor = 6.7;
angles14 =dayAngles[DateObject[{2016,8,17}],14][[All,2]];
ListLinePlot[{powerOverDay[angles14],

Join[
Table[
directPower[i,41,date]*factor
,{i,1,Round[Length[sunPositions]9/14]} (* 16:00 is around 9/14  *)
],
Table[
directPower[i,0,date]*factor
,{i,Round[Length[sunPositions] 9/14]+1,Length[sunPositions]}
]
]

}, ImageSize\[Rule]Large, PlotLegends\[Rule]{legend,"Adjusting 2 times"}]*)


(* percent difference by angle ******************************)

directPowerPercent := Function[
(1-directPower[#1]/860)*100
]

solarPanelTilt[z_] := (
If[z>90,
100,
i =1.35*(1/1.35)^(Sec[z*Pi/180]);
(1-i)*100
]
)

(* DiscretePlot[1-Cos[x*Pi/180],{x,0,90,2},ImageSize\[Rule]Large] *)

(*Show[DiscretePlot[directPowerPercent[x*Pi/180],{x,0,90,2},ImageSize\[Rule]Large],
DiscretePlot[100*(1-Cos[x*Pi/180]),{x,0,90,2},ImageSize\[Rule]Large],DiscretePlot[solarPanelTilt[x],{x,0,90,2},ImageSize\[Rule]Large]
]*)

(*ListLinePlot[{Table[directPowerPercent[x*Pi/180],{x,0,100}],
Table[100*(1-Cos[x*Pi/180]),{x,0,100}],
Table[solarPanelTilt[x],{x,0,100}]},Filling \[Rule] Axis,PlotLegends \[Rule] {calculations,simple cos,solarpaneltilt.com},ImageSize \[Rule] Large,AxesLabel\[Rule] {"Angle misalignment","Percent of power loss"}]*)

(************ real data ***********************)

(* change name of imported csv, change dates in ListLinePlot to dates exisiting in data *)

(*data = Import["C:\\Users\\s156757\\OneDrive\\SolArduino\\solarduino\\Documentation\\Mathematica\\data17_23.csv"];
data = Delete[data,1];*)

f[d_] := ( (* params: date, returns list of values for that date *)
dayData = Select[data,
DayCount[
DateObject[DateObject[
DateString[#[[1]],{"Day","-","Month","-","Year"," ","Hour",":","Minute"}]
],{0,0}] (* change hour to 0 to compare *) ,
d
] == 0 &
];
Table[dayData[[i]][[2]] ,{i,1,Length[dayData]}]
)

(*ListLinePlot[
f[DateObject[{2016,5,13,0,0}]]
]*)
(*
ListLinePlot[
{
f[DateObject[{2016,8,16,0,0}]],
f[DateObject[{2016,8,17,0,0}]],
f[DateObject[{2016,8,18,0,0}]],
f[DateObject[{2016,8,19,0,0}]]
},PlotLegends \[Rule] {"24","24","40","5"}
]*)

(*ListLinePlot[
{
f[DateObject[{2016,8,17,0,0}]],
f[DateObject[{2016,8,22,0,0}]],
f[DateObject[{2016,8,23,0,0}]],
},
PlotLegends \[Rule] {"17/8","22/8","23/8"}
]*)
(*Select[data,DateDifference[DateObject[ #[[1]]],{2016,8,18,0,0} ]\[LessEqual] 0 &,1 ]*)

(************************)

(* exporting *)
(* Convert all the DateObjects to YYYY-MM-DD *)
(*Do[
t[[i,1]] = 
DateString[t[[i,1]],{"Year","-","Month","-","Day"}],
{i,Length[t]}
];
(* Export the table to csv, format YYYY-MM-DD, angle *)
Export["C:\\Users\\s152337\\OneDrive\\Documenten\\SolArduino\\Documentation\\Mathematica\\data2016.csv", t];*)

  End[]

  EndPackage[]



