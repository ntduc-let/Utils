Utils
==================

# Index
* [Setup](#Setup)

Setup
======================
## Key Store
```
Key store path: [Trong thư mục app]
Password: prox@123456
Alias: MyApp
Password: prox@123456
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
      keyPassword 'prox@123456'
      storeFile file('MyApp.jks')
      storePassword 'prox@123456'
    }

    release {
      keyAlias 'MyApp'
      keyPassword 'prox@123456'
      storeFile file('MyApp.jks')
      storePassword 'prox@123456'
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
dependencies {
  ...
  implementation 'com.github.ntduc-let.Utils:ActivityUtils:1.0.0'
  implementation 'com.github.ntduc-let.Utils:AnimationUtils:1.0.0'
  implementation 'com.github.ntduc-let.Utils:ToastUtils:1.0.0'
}
```

## gradle.properties
```
authToken=jp_4c9t9e41gasb6p7vq3arh4ujpl
```

## settings.gradle
```
dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
    maven {
      url "https://jitpack.io"
      credentials { username authToken }
    }
  }
}
```




