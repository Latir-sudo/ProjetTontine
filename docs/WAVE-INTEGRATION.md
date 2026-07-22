# Activation de Wave Checkout

Le backend utilise directement l'API officielle Wave Checkout. Aucune clé ne doit être ajoutée au frontend, à `application.properties` ou à Git.

Configurez les variables d'environnement suivantes sur le serveur avant de démarrer l'application :

```text
WAVE_API_KEY=...
WAVE_SUCCESS_URL=https://votre-domaine/paiement/succes
WAVE_ERROR_URL=https://votre-domaine/paiement/echec
WAVE_WEBHOOK_SECRET=...
```

`WAVE_API_SIGNING_SECRET` est aussi requis si la clé API Wave a été créée avec la signature de requêtes activée. `WAVE_API_BASE_URL` est optionnelle et vaut `https://api.wave.com` par défaut.

Dans le portail Wave Business, enregistrez le webhook HTTPS suivant et conservez le secret fourni :

```text
https://votre-api-domaine/api/webhooks/wave
```

Sélectionnez les événements Checkout. Le backend valide la signature du corps brut, rejette les requêtes trop anciennes, vérifie le montant et la devise XOF, puis marque le paiement comme validé uniquement si Wave envoie `checkout_status=complete` et `payment_status=succeeded`.
