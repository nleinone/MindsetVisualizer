# MindsetVisualizer
Android application for visualizing brainwaves with Muse wearable headband.

Description:

The application is implemented on android device and is developed with the Android
studio version 3.4. This application is developed and tested with a devices Android
version 6 or higher.

The application, named as MindVisualizer, consists of 4 individual activities:
StartActivity, ConnectionActivity, CalibrationActivity, and VisualActivity. The
application also includes a class for the WorkManager Worker, which controls the
colour changing of the screen, and Firebase Realtime database. The application visualizes the users EEG data (provided by the Muse Wearable headband) with changing screen colours. It also calculates the average alphawave EEG value for the calibration session.  

4.1.1. StartActivity  

The StartActivity acts as a entry point for the user, as it is the first screen shown when
launching the application. The original design included a tutorial for the user, but
since the research scope was narrowed down and the test user’s consists mainly expert
users due to corona virus outbreak, time constraints, and focusing of the evaluation,
additional tutorial activity were excluded. Guides how to use the app can be found in
this documentation, as well as the application readme file.  

4.1.2. ConnectionActivity  

ConnectionActivity consists of all the necessary functions for the user’s to search for
the reachable Muse device, connect or disconnect the mobile device to/from the Muse
device and navigate to the CalibrationActivity or visualization activity. When the User
connects to the Muse Device, the data is sent to the mobile device and converted to
readable form. However, this data stream is only used in the observation of the stability
of the data link, and not saved in the database. By default, Muse Wearable headband
streams the EEG data every 0.3 seconds, which is slowed down to about 1 second by
the application inner parameters. This is done to reduce the traffic and pressure on the
phone and make the Firebase data push more stable.

4.1.3. CalibrationActivity  

As the connection is now established in the previous activity (ConnectionActivity),
Calibration activity includes an feature to set a time (in seconds, default 30 seconds)
and measure the average alpha wave value of the Muse EEG data stream. When the
application is measured the alpha wave for the given time, the average value of the
alpha wave is then pushed to the Firebase Realtime database, and VisualActivity is
opened.

4.1.4. VisualActivity  

In VisualActivity, the previously generated average alpha wave value is pulled and
shown in the screen, simultaneously showing the currently running EEG data stream
from the MuseWearable headband. In addition to the numeric values, the running data
stream is compared to the average value, and background colour is changed depending
on the difference between the currently running value and the average value. As the
studies states that higher alpha values correlates to more relaxed state of mind and
lower values correlates to the more stressed state of mind, if the running value is above
the calibrated average value, the more green the background colour is. If the running
value goes below the average value, the background colour turns more red instead.
Android Studio’s WorkManager Class is used in this implementation.

Screenshots: https://github.com/nleinone/MindsetVisualizer/wiki
