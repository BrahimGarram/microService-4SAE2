/**
 * Anciens builds pointent vers la gateway en absolu (CORS). On réécrit en `/gateway/...`
 * pour même origine (ng serve proxy ou nginx Docker).
 */
export function rewriteLegacyGatewayUrl(url: string): string {
  if (!url || !url.startsWith('http')) {
    return url;
  }
  const bases = [
    'http://localhost:18090',
    'http://127.0.0.1:18090',
    'http://localhost:8090',
    'http://127.0.0.1:8090'
  ];
  for (const base of bases) {
    if (url.startsWith(base)) {
      const rest = url.slice(base.length) || '/';
      return '/gateway' + (rest.startsWith('/') ? rest : '/' + rest);
    }
  }
  return url;
}
