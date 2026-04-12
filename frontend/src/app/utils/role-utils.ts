/** Rôles Keycloak stockés dans localStorage après login. */

export function getRolesFromStorage(): string[] {
  const s = localStorage.getItem('kc-roles');
  if (!s) {
    return [];
  }
  try {
    const parsed = JSON.parse(s);
    return Array.isArray(parsed) ? parsed.map((r: unknown) => String(r)) : [];
  } catch {
    return [];
  }
}

export function isAdminRole(): boolean {
  return getRolesFromStorage().some((r) => r.toUpperCase() === 'ADMIN');
}

export function isCollaborateurRole(): boolean {
  return getRolesFromStorage().some((r) => r.toUpperCase() === 'COLLABORATEUR');
}

/** Collaborateur sans rôle admin : accès restreint à l’espace collaboration (+ pages publiques). */
export function isCollaborateurOnlyAccount(): boolean {
  return isCollaborateurRole() && !isAdminRole();
}
