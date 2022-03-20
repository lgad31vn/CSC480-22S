# Instructions to create a private key to sign a jwt token using RS256

- Navigate to liberty/config
- Create new folder resources/security
- Then open the security directory in terminal/cmd

- Generate a private RSA key (don't skip passphrase)

```
openssl genrsa -des3 -out private.key 2048
```

- Generate a public RSA key based the new generated private.key

```
openssl rsa -in private.key -outform PEM -pubout -out public.key
```

- Generate a CA certificate (this can be used to sign a self-certificate to access to self-SSL cert|HTTPS locally)

```
openssl req -x509 -new -nodes -key private.key -sha256 -days 1825 -out CA.crt
```

- Export PKCS12 key

```
openssl pkcs12 -export -out YOUR_KEY_NAME.p12 -inkey private.key -in CA.crt
```

- Decrypt the pkcs12 key

```
openssl pkcs12 -info -in YOUR_KEY_NAME.p12 -out decrypted_pkcs12.txt
```

- Get back to CPR, navigate to server.xml, make sure the following is configured

```
<keyStore id="cpr22sKS" password="YOUR_P12_PASSWORD" location="YOUR_KEY_NAME.p12"/>

<jwtBuilder expiresInSeconds="600" id="cpr22s" issuer="YOUR DOMAIN" />
```
