# HTTPS Supporting

## Import cert file to trust key store

```
keytool -importcert -v -trustcacerts -alias some.host.name -file /path/to/cert/file -keystore /path/to/store/file
```

## Setup System properties

```
java -Djavax.net.ssl.trustStore=/path/to/store/file -Djavax.net.ssl.trustStorePassword=****** ...
```

> If `/path/to/store/file` is the JRE default store file, you can ignore properties above.

## Reference

* [Converting PEM-format keys to JKS format](https://docs.oracle.com/cd/E35976_01/server.740/es_admin/src/tadm_ssl_convert_pem_to_jks.html)