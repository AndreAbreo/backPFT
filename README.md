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

## LDAP configuration

Authentication against Active Directory requires connection details stored in
`src/main/resources/ldap.properties`. The file contains placeholder values:

```properties
ldap.url=ldap://ad.example.com:389
ldap.base_dn=DC=example,DC=com
ldap.user_dn=CN=serviceuser,OU=Users,DC=example,DC=com
ldap.password=changeMe
```

Edit these entries so they match your target AD server URL, base DN, service
account DN and password.
