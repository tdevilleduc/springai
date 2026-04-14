# Configuration HTTPS / TLS

## Générer un keystore auto-signé (développement)

```bash
keytool -genkeypair \
  -alias springai \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore src/main/resources/keystore.p12 \
  -validity 365 \
  -storepass changeit
```

## Activer HTTPS via le fichier `.env`

```env
SSL_ENABLED=true
SSL_KEY_STORE_PASSWORD=changeit
SSL_KEY_ALIAS=springai
SERVER_PORT=8443
```

## Production

En production, utiliser un certificat signé par une CA de confiance (ex: Let's Encrypt)
et monter le keystore via un volume ou secret (Kubernetes, Docker).
Ne jamais commiter le fichier `keystore.p12` dans git.
