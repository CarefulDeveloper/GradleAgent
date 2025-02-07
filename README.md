# Gradle Agent

This is a java agent for Gradle `distributionUrl` property global mirror setting.

# How to Use

1. Download the agent jar [here](https://github.com/CarefulDeveloper/GradleAgent/releases/latest/download/GradleAgent.jar)

2. make this agent jar work
   
   if you want to it worked in command line at terminal, you need to add this system environment variable:
   
   ```
   GRADLE_OPTS = -javaagent:C:\path\to\GradleAgent.jar
   ```
   
   if you want to it worked in JetBrains IDE, you need follow [the documentation](https://www.jetbrains.com/help/idea/tuning-the-ide.html#procedure-jvm-options) and append new line in `*.vmoptions` with:
   
   ```
   -javaagent:C:\path\to\GradleAgent.jar
   ```

3. config gradle distribution mirror url template:

   add system environment variable:
   ```
   GRADLE_DISTRIBUTION_URL_TEMPLATE = https://www.mirrror.host.com/gradle/gradle-%1$s-%2$s.zip
   ```
   `%1$s` will be fill with the gradle version info, like `8.8`; `%2$s` will be filled with distribution type, like `all` `bin`.

4. restart terminal or JetBrains IDE

   make configuration effect now!

5. sync Gradle and observe the output of Gradle.

   if you never use this version of Gradle, you should see it download the Gradle distribution from the mirror website.

# Thanks

- https://github.com/ddimtirov/gwo-agent