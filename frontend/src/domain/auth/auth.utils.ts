import { TOKEN_KEY } from "./auth.constants";

interface JwtPayload {
  sub: string;
  exp: number;
  iat: number;
}

export const tokenStorage = {
  getAccessToken: (): string | null => {
    return localStorage.getItem(TOKEN_KEY.ACCESS);
  },

  getRefreshToken: (): string | null => {
    return localStorage.getItem(TOKEN_KEY.REFRESH);
  },

  setTokens: (accessToken: string, refreshToken: string): void => {
    localStorage.setItem(TOKEN_KEY.ACCESS, accessToken);
    localStorage.setItem(TOKEN_KEY.REFRESH, refreshToken);
  },

  clearTokens: (): void => {
    localStorage.removeItem(TOKEN_KEY.ACCESS);
    localStorage.removeItem(TOKEN_KEY.REFRESH);
  },
};

export const parseJwt = (token: string): JwtPayload | null => {
  try {
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join(""),
    );
    return JSON.parse(jsonPayload) as JwtPayload;
  } catch {
    return null;
  }
};

export const isTokenExpired = (token: string): boolean => {
  const payload = parseJwt(token);
  if (!payload) return true;

  const currentTime = Math.floor(Date.now() / 1000);
  return payload.exp < currentTime;
};
