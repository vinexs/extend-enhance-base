# extend-enhance-base
Simply extend the Enhance Class / Base Class and the hard works were done.

## How to setup

Step 1. Add it in your root build.gradle at the end of repositories
```
allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
  }
```
Step 2. Add the dependency
```
dependencies {
  compile 'com.github.vinexs:Extend-Enhance-Base:1.0.4'
}
```

If only require some module.
```
compile 'com.github.vinexs.extend-enhance-base:eeb-core:1.0.4'
compile 'com.github.vinexs.extend-enhance-base:eeb-camera:1.0.4'
compile 'com.github.vinexs.extend-enhance-base:eeb-net:1.0.4'
```



