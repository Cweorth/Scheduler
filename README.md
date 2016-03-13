# Scheduler

CPSolver is a library developed for UniTime, a university timetabling system used at the Faculty of Informatics among others. The goal of this project is to extend the system for automated state examination timetabling based on the CPSolver library.

This repository contains the source code of the said system, which is currently under development.

## Runtime requirements

- Java Runtime Environment 1.8

## Build requirements

- Java Development Kit 1.8
- Maven 3

## Instructions to run a demo

1. clone this repository: ``git clone git@github.com:Cweorth/Scheduler.git``,
2. in the newly created directory run ``mvn compile package``,
3. copy demo model data directory ``cp -R test-models/random-model model/target``,
4. navigate to ``model/target``,
5. run ``java -jar scheduler.model-1.0.jar random-model``
