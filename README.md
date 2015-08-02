# Jyly

## How to Build

write Instagram API key to `Jyly/instagram.gradle`

```gradle
ext {
    INSTAGRAM = [:]
    INSTAGRAM['client_id']     = 'instagram_client_id'
    INSTAGRAM['client_secret'] = 'instagram_client_secret'
    INSTAGRAM['redirect_uri']  = 'instagram_redirect_uri'
}
```

and run

```bash
./gradle installDebug
```
