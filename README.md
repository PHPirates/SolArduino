# SolArduino #

This project is set up to move solar panels automatically, and to be able to control them from your phone and computer. In this project are the following things.

* [Arduino code](https://github.com/PHPirates/SolArduino/raw/master/SolArduino_atom/SolArduino/SolArduino.ino)
* Mathematica calculations, the package file is [here](https://github.com/PHPirates/SolArduino/raw/master/Documentation/Mathematica/SolArduino.m) and there is also a package [demonstration](https://github.com/PHPirates/SolArduino/raw/master/Documentation/Mathematica/demonstration.nb) notebook.
* Android app, find the apk [here](https://github.com/PHPirates/SolArduino/raw/master/solappduino/solarduino/app/build/outputs/apk/app-debug.apk).
* Desktop app, source code [here](https://github.com/PHPirates/SolArduino/tree/master/SolArduino-desktop/src/SolArduino), latest built executable [here](https://github.com/PHPirates/SolArduino/raw/master/SolArduino-desktop/out/artifacts/SolArduino_desktop_jar/SolArduino-desktop.jar).

### Create executable JAR from IntelliJ project - first time ###
1. Go to File > Project Structure > Artifacts
2. Click the green plus in the left top corner (next to Project Settings)
3. In the dropdown menu, choose JAR > Form modules with dependencies...
4. For the main class, click the three dots "..." to browse for the main class `SolArduino.Main`, click OK
5. Follow the steps under "Build executable JAR"

### Build executable JAR ###
1. Run the project (Shift + F10)
2. Click Build > Build Artifacts...
3. SolArduino-desktop:jar > Build
4. Find the JAR-file in the SolArduino\SolArduino-desktop\out\artifacts\SolArduino_desktop_jar directory

## Documentation ##
Find the documentation [here](https://github.com/PHPirates/SolArduino/raw/master/Documentation/Documentation.pdf).

This project was previously developed on [Bitbucket](https://bitbucket.org/slideclimb/solappduino/overview).
