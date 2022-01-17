# Setting up for the first time..

## Tweak these in the project.. and preparing the first successful run.

1. In the project level gradle file: add in the `dependencies`.
   - `classpath "io.realm:realm-gradle-plugin:10.0.0-BETA.8"` -- but thi din't worked,
   - The one from its docs](https://docs.mongodb.com/realm/sdk/android/install/): `classpath "io.realm:realm-gradle-plugin:10.9.0"` - worked.
   - This configures our app to work with realm.
2. Now in the app(Module) level gradle file.. After the `plugins{ .. }`..
   - `apply plugin: 'realm-android'`
   - In the same file.. just above the `dependencies{ .. }`..
     - `realm { syncEnabled = true }`
   - This enables the plugin.

- Now, `Sync the gradle`.
- In the `MainActivity.java` _(some main activity)_..
  ```java
  	import io.realm.Realm;	// import the library.

  	public class MainActivity extends AppCompatActivity {
  	    protected void onCreate(Bundle savedInstanceState) {
  		...
  		Realm.init(this);		// FOCUS - This should be only done once.
  						// And must, before making any call to the realm application.
  						Realm.init(this);
  	    App app = new App(new AppConfiguration.Builder(appId).build()); // This will create the instance of the realm application, which is linked to online realm.
  	    }
  	}
  ```
