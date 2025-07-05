# backPFT

This project uses the `jacoco-maven-plugin` for code coverage reports. In offline environments you can skip JaCoCo execution using:

```bash
mvn -Djacoco.skip=true <goal>
```

Replace `<goal>` with your desired Maven goal, for example `package`:

```bash
mvn -Djacoco.skip=true package
```

## Disabling JWT authentication locally

During local development you might want to bypass the JWT filter. Set the
`DISABLE_JWT_AUTH` environment variable to `"true"` before starting the
application. When this variable is enabled, a warning is logged and the
`JwtTokenFilter` will skip authentication checks.

Example:

```bash
export DISABLE_JWT_AUTH=true
mvn package
``` 
