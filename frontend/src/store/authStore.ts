import { create } from "zustand";
import { persist } from "zustand/middleware";

interface User {
  id:          string;
  email:       string;
  displayName: string;
}

interface AuthStore {
  user:         User | null;
  accessToken:  string | null;
  isLoggedIn:   boolean;

  initAuth:  () => void;
  setTokens: (accessToken: string, user: User) => void;
  logout:    () => void;
}

export const useAuthStore = create<AuthStore>()(
  persist(
    (set) => ({
      user:        null,
      accessToken: null,
      isLoggedIn:  false,

      initAuth: () => {
        // Tokens are rehydrated via zustand/persist from localStorage.
        // Additional token validation logic goes here if needed.
      },

      setTokens: (accessToken, user) =>
        set({ accessToken, user, isLoggedIn: true }),

      logout: () =>
        set({ accessToken: null, user: null, isLoggedIn: false }),
    }),
    {
      name: "mini-maya-auth",
      partialize: (state) => ({
        accessToken: state.accessToken,
        user:        state.user,
        isLoggedIn:  state.isLoggedIn,
      }),
    }
  )
);
