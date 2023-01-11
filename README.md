Utils
==================

# Index
* [Setup](#Setup)

Setup
======================
## Key Store
```
Key store path: [Trong thư mục app]
Password: password
Alias: MyApp
Password: password
First and Last Name: Duc Nguyen
Organizational Unit: Mobile
Organization: ProX Global
City or Locality: Hanoi City
State or Province: Hanoi
Country Code (XX): 84
```

## build.gradle (Module:app)
```
android {
  ...
  defaultConfig {
    ...
    setProperty("archivesBaseName", "v$versionName($versionCode)_My_App")
  }

  signingConfigs {
    debug {
      keyAlias 'MyApp'
      keyPassword 'password'
      storeFile file('MyApp.jks')
      storePassword 'password'
    }

    release {
      keyAlias 'MyApp'
      keyPassword 'password'
      storeFile file('MyApp.jks')
      storePassword 'password'
    }
  }
  buildTypes {
    debug {
      ...
      resValue "string", "test", "aaaaaaaaa"
      
      signingConfig signingConfigs.debug
    }

    release {
      ...
      resValue "string", "test", "aaaaaaaaa"
      
      signingConfig signingConfigs.release
    }
  }

  buildFeatures {
     viewBinding true
  }
}

```
