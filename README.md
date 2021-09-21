Shield: [![CC BY-SA 4.0][cc-by-sa-shield]][cc-by-sa]

![Build and Test status:](https://github.com/mvargas33/Eolian-Auriga-backend/actions/workflows/mvnCI.yml/badge.svg)

This work is licensed under a [Creative Commons Attribution-ShareAlike 4.0 International License][cc-by-sa]. 

[cc-by-sa]: http://creativecommons.org/licenses/by-sa/4.0/
[cc-by-sa-image]: https://licensebuttons.net/l/by-sa/4.0/88x31.png
[cc-by-sa-shield]: https://img.shields.io/badge/License-CC%20BY--SA%204.0-lightgrey.svg

Our aim as a team is to share this software as an academic material, and contribute to solar car's community. We also want to give attribution to the people who put the effort to make this software possible. You may license your contributions to adaptations of this BY-SA 4.0 work under [GPLv3 Licence][GPLv3], it's BY-SA 4.0 compatible.

[GPLv3]: https://www.gnu.org/licenses/gpl-3.0.html

# Eolian Auriga Telemetry System
The telemetry system runs in a Raspberry Pi inside the car. On one hand, it uses a Java Application to receive data from the car and other logistics tasks. On the other hand, it runs a VueJS application to visualize data and interact with the users. The system uses a middleware written in JS to communicate back-end and front-end.

# Dependencies and configuration

Software required:

* IntelliJ https://www.jetbrains.com/idea/
* Java JDK https://www.oracle.com/technetwork/java/javase/downloads/index.html
* NodeJS https://nodejs.org/en/

For Back-end:

* Import the directory *Protocol* into IntelliJ
* Configure the project to work with Java 8
* To import dependencies:
  * Right click in Protocol.iml and select *import modules* OR
  * Go to *File->Project Strucure->Modules* to add all the JAR files inside the *libs* directory. The JAR files inside *JUnit* must be imported for testing, not compiling

For Middleware and Front-end:

* Run `npm install` inside the *Server API* and *VueJS front end* directories to install all dependencies
* Install Quasar CLI with `npm i -g @quasar/cli`
* In Windows allow scripts to run. In Admin Power Shell run `Set-ExecutionPolicy RemoteSigned`
* Run `npm run lint -- --fix` for lint operations

# Run the applications

Back end:

* Run the *MainReceiver* and *MainSender* classes as requested

Middleware:

* With back-end running, run `node index.js` inside *Server API* to run the middleware between back-end and front-end

Front end:

* Run `npm run dev` inside *VueJS front end* to run the front-end

# Notes

The project is under development. Basic data dynamics are already implemented. Front-end must be completed. Compatibility with Eolian Fenix (older solar car) is in progress.

#  Testing Support

From 18/02/2021 this project has testing support (still in development).

For this to work several setup things were done:

1. Setting un maven support (a pom.xml file) to build and test the project.
2. Added surefire plugin to the pom, to execute tests.
3. Added junit5 (jupiter) dependency, to write tests.

# CI

CI is in development (the project migration to complete CI is a WIP), but at the future it will be done by executing
`mvn --batch-mode --update-snapshots verify`.

--batch-mode = not interactive (don't ask for prompt values, use default instead)

--update-snapshots = check dependencies (if outdated, redownload them)

verify = builds and tests the project

## Pending

Previously all dependencies where handled offline (directly having the source code on the repo). 

However just the necessary libraries (necessary enough to build the project) where transferred to the pom file.
Therefore there are many libraries that were not imported to the pom file, to keep that in mind if some error arises because of that.

## Build

To build the project run:

`mvn clean compile assembly:single`

## Tests structure

There are many tests intended for the following purposes:

### Common code

All the base code of the repository.

### Special implementations

This will depend of your own architecture and the things that you want to test of your architecture.

For example, in our project we tests things such as:
* Check if the RPI i2c bus is available.
* Check if the i2c channel is working properly.
* Check if the wireless sender is sending the correct data.

and so on...

General list:

* Check java version 1.8.0.212.
* Check if all the devices are connected properly.

  * Raspberry Pi 4B+.
  * Arduino nano (MPPT). 
  * Arduino nano (Kelly | BMS).
  * Serial to can (MPPT). 
  * Serial to can (Kelly | BMS).
  * XBee.
  * XBee antenna.
  * GPS.
  * GPS antenna.
  * Gyroscope.
  * LCD 11" screen.
  * LCD 7" screen.

* Check if all the interfaces are working.
  * i2c. Between the RPI and both arduinos.
  * canbus MPPT. Between the arduino nano and the serial to can (MPPT).
  * canbus KELLY | BMS. Between the arduino nano and the serial to can (KELLY | BMS).

* Check if all of the channels work properly.
* Check if all of the services work properly.
