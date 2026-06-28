/** Browser-facing API base URL (from host machine). */
export const PUBLIC_API_URL =
  process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

/**
 * API base URL for the current runtime.
 * Server components in Docker must use INTERNAL_API_URL (e.g. http://backend-java:8080).
 */
export function resolveApiUrl(): string {
  if (typeof window !== 'undefined') {
    return PUBLIC_API_URL;
  }
  return (
    process.env.INTERNAL_API_URL ||
    process.env.NEXT_PUBLIC_API_URL ||
    'http://localhost:8080'
  );
}
