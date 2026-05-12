// sw.js - Service Worker
// On localhost: self-destruct (unregister + clear caches) to avoid blocking dev/preview testing.
// On production: legitimate fetch handler (no-op passthrough).

const isLocalhost = self.location.hostname === 'localhost' || self.location.hostname === '127.0.0.1';

if (isLocalhost) {
  // SELF-DESTRUCT MODE for localhost
  self.addEventListener('install', (event) => {
    self.skipWaiting();
  });

  self.addEventListener('activate', (event) => {
    event.waitUntil(
      (async () => {
        // Delete all caches
        const cacheNames = await caches.keys();
        await Promise.all(cacheNames.map(name => caches.delete(name)));
        // Unregister self
        await self.registration.unregister();
        // Force all clients to reload
        const clients = await self.clients.matchAll({ type: 'window' });
        clients.forEach(client => client.navigate(client.url));
      })()
    );
  });
} else {
  // PRODUCTION: legitimate passthrough
  self.addEventListener('install', (event) => {
    self.skipWaiting();
  });

  self.addEventListener('activate', (event) => {
    event.waitUntil(self.clients.claim());
  });

  // No fetch interception - let all requests pass through normally
}