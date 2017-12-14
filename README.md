# Recyclr

Recyclr is an app dedicated to reducing food waste. Our goal is simple: make it easy for restaurants or food producers to donate food that they would otherwise throw away to charitable organizations (NGOs, soup kitchens). Our app can allow producers to disclose to the organizations how much food they are able to donate and set up pick up locations for such food. Conversely, an organization can browse through nearby food pickup points and reserve food that they want.

## Getting Started

To build this project, you need to have Android Studio 3.0. In addition to that, the application is running on Google Firebase, thus requires keys to connect to the project. You can choose to setup your own Firebase Server and connect the app to it (remember to add the google-services.json to the src folder), or contact us so that we can add your SHA-1 fingerprint keys to ours.

### Installing

Assuming that you already have Android Studio, all you need to do is clone the repo:
```
git clone https://github.com/juleskt/Food-app.git
```

## Deployment

To actually run the application, you need to connect the application to Firebase. Please follow the instructions at [here](https://developer.android.com/studio/write/firebase.html) if you do not know how to do so.

As for the SHA-1 key, you need to copy it and paste it into your google-services.json file from Firebase, or if you wish to use our Firebase server, you need to contact us with your key so that we can send you a version of the JSON with the key in it. If you are unsure how to find it, please read the thread [here](https://stackoverflow.com/questions/27609442/how-to-get-the-sha-1-fingerprint-certificate-in-android-studio-for-debug-mode) to learn how to do so.

## Testing
For testing we CRUD test all of our database methods through unit tests. The testing are detailed as follows:
- testConsumerData - Full CRUD of Consumer objects with Firebase, currently asserting against name.
- testProducerData - Full CRUD of Producer objects with Firebase, currently asserting against name.
- testPointData - Full CRUD of Point objects with Firebase, asserts against createdUnixTime, expiryUnixTime, latitude, longitude, posterID, producerName, quantity, and unit
- testGeoFireData - Full CRUD of GeoFirePoint objects with Firebase, asserts against the latitude and longitude attributes and mock geo-query.
- testUserData - Full CRUD of UsereData objects with Firebase, asserts against accountType and name
We also currently extensively system test our app manually after every major update (after running unit tests).

## Contributing

Please contact one of us if you wish to contribute to the project.

## Authors

* **Julian Trinh** - <https://github.com/juleskt>
* **Anish Asthana** - <https://github.com/anishasthana>
* **Dennis Your** - <https://github.com/incidr>

See also the list of [contributors](https://github.com/juleskt/Food-app/graphs/contributors) who participated in this project.
