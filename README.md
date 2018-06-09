# Multi-Disciplinary-Design-Project-Android-UI

This repository contains the code for an Android application which is part of a larger project wherein a robot has to be created which is capable of autonomously navigate a maze.

The Android application acts as the main interface for the aforementioned robot.

The application is capable of discovering nearby Bluetooth devices and establishing a persistent connection with them.

For the scope of this project, the application establishes a Bluetooth connection with a Raspberry Pi and exchanges information.

The application sends the commands to initiate the exploration and traversal by the robot. Further, the application receives information regarding the coordinates of the robot in the maze which is rendered by the application in real-time.

Rendering the location of the robot in the maze is done by:
1. Implementing event listeners that listen for changes in the robot's location.
2. Implementing custom adapters for gridview elements that re-render the associated gridview.
