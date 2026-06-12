/* Service Worker — системные (Windows) уведомления мессенджера.
   Показывает нативные тосты через registration.showNotification и
   обрабатывает клик: фокусирует открытое окно и открывает нужный чат. */

self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', (e) => e.waitUntil(self.clients.claim()));

self.addEventListener('notificationclick', (e) => {
  e.notification.close();
  const chatId = e.notification.data && e.notification.data.chatId;
  e.waitUntil((async () => {
    const all = await self.clients.matchAll({ type: 'window', includeUncontrolled: true });
    for (const c of all) {
      if ('focus' in c) {
        try { await c.focus(); } catch (err) {}
        if (chatId) c.postMessage({ type: 'open-chat', chatId });
        return;
      }
    }
    if (self.clients.openWindow) {
      await self.clients.openWindow('./' + (chatId ? ('?openChat=' + encodeURIComponent(chatId)) : ''));
    }
  })());
});
