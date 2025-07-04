# backPFT

This project uses the `jacoco-maven-plugin` for code coverage reports. In offline environments you can skip JaCoCo execution using:

```bash
mvn -Djacoco.skip=true <goal>
```

Replace `<goal>` with your desired Maven goal, for example `package`:

```bash
mvn -Djacoco.skip=true package
```
