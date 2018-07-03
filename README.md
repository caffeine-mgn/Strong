# Strong
Dependency injection library for Kotlin

### Using in project
##### Add repo to your project
```groovy
repositories {
    maven {
        url "http://tlsys.org/repo"
    }
}
```

##### Add dependency
```groovy
buildscript {
    ext.strong_version = '1.0.7'
}

dependencies{
    compile("org.tlsys:strong-jvm:$strong_version") // Strong Core
    compile("org.tlsys:strong-base-jvm:$strong_version") // Strong Base Provider
}
```