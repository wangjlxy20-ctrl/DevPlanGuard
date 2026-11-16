# Usage

## Setting

Setting file: ./gradle.properties

### Http proxy settings

This is optional setting, set the http proxy configuration if you behind a proxy.

```properties
systemProp.http.proxyHost=relc-proxy.jp.ykgw.net
systemProp.http.proxyPort=80
systemProp.https.proxyHost=relc-proxy.jp.ykgw.net
systemProp.https.proxyPort=80
systemProp.http.nonProxyHosts=dev-center-gitlab
```

### monitor deploy settings

Use command `gradlew deployMonitor` to deploy the `class` and `resource` files of `monitor` module into the following folder:

 `${monitor.deploy.dir}\`

Change the `${monitor.deploy.dir}` settings in the `gradle.properties` file by your EX environment.

```properties
monitor.deploy.dir=C:\\EX\\UI\\wildfly-32.0.1.Final\\standalone\\deployments\\monitor.sar
```

### src_bl deploy settings

Use command `gradlew deploy` to deploy `class` files of `src_bl` module.

Only deploy the `class` files of `src_bl` module into the following folder:

 `${src_bl.deploy.dir}\WEB-INF\classes\`

Change the `${src_bl.deploy.dir}` settings in the `gradle.properties` file by your EX environment.

```properties
src_bl.deploy.dir=C:\\EX\\UI\\wildfly-32.0.1.Final\\standalone\\deployments\\UI.war
```

## Command

### How to run the gradle command

- Use the `gradlew.bat` file to run the gradle command in the `CMD` window. 

  Example run command  `gradlew --version`,  get the following result:

  ```
  D:\example\ex_bl>gradlew --version
  -------------------------------------------
  Gradle 8.7
  -------------------------------------------
  
  Build time:   2024-03-22 15:52:46 UTC
  Revision:     650af14d7653aa949fce5e886e685efc9cf97c10
  
  Kotlin:       1.9.22
  Groovy:       3.0.17
  Ant:          Apache Ant(TM) version 1.10.13 compiled on January 4 2023
  JVM:          21.0.3 (Azul Systems, Inc. 21.0.3+9-LTS)
  OS:           Windows 11 10.0 amd64
  
  ```


### Command list

- `gradlew deploy` : Use command `gradlew deploy` to deploy  `class` files.

  Only deploy the `class` files of `src_bl` module into the following folder:

  `${src_bl.deploy.dir}\WEB-INF\classes\`

- `gradlew deployMonitor` : Use command `gradlew deployMonitor` to deploy the `class` and `resource` files of `monitor` module into the following folder:

   `${monitor.deploy.dir}\`

- `gradlew clean` : Clean the build cache files.

- `gradle build` : Compile the java files to classes files into `build` folder.

## How to import the project to Eclipse or IntelliJ Idea

Import the project to Eclipse:

- File --> Import --> Gradle (Existing Gradle Project) --> select the project folder `ex_bl` -> next --> Check [Override workspace settings] and Check [Gradle wrapper] and Select the [java home] of EX -> finish

Import the project to IntelliJ Idea:

- File --> Open --> select the project folder `ex_bl`

## How to deploy the classes of src_bl module

1. Setting the following configuration item in gradle.properties file

   ```properties
   src_bl.deploy.dir=C:\\EX\\UI\\wildfly-32.0.1.Final\\standalone\\deployments\\UI.war
   ```

2. Run the next command in CMD window

   ```
   gradlew deploy
   ```

## How to deploy the monitor

1. Setting the following configuration item in  gradle.properties file

   ```properties
   monitor.deploy.dir=C:\\EX\\UI\\wildfly-32.0.1.Final\\standalone\\deployments\\monitor.sar
   ```

2. Run the next command in CMD window

   ```
   gradlew deployMonitor
   ```

   



