import NextAuth from "next-auth";
import Credentials from "next-auth/providers/credentials";

export type UserRole = "SHIPPER" | "CARRIER";

declare module "next-auth" {
  interface User {
    role: UserRole;
    tenantId: string;
  }
  interface Session {
    user: {
      id: string;
      name?: string | null;
      email?: string | null;
      role: UserRole;
      tenantId: string;
    };
  }
}

export const { handlers, auth, signIn, signOut } = NextAuth({
  session: { strategy: "jwt" },
  pages: { signIn: "/login" },
  providers: [
    Credentials({
      name: "credentials",
      credentials: {
        email: { label: "Email", type: "email" },
        password: { label: "Password", type: "password" },
      },
      async authorize(credentials) {
        // Phase 2: hardcoded demo accounts; Phase 3: call user-service REST API
        const demoUsers = [
          { id: "shipper-001", name: "Acme Shipper", email: "shipper@platform.local", password: "shipper123", role: "SHIPPER" as UserRole, tenantId: "tenant-001" },
          { id: "carrier-001", name: "FastFleet Carrier", email: "carrier@platform.local", password: "carrier123", role: "CARRIER" as UserRole, tenantId: "tenant-002" },
        ];
        const user = demoUsers.find(
          (u) => u.email === credentials?.email && u.password === credentials?.password
        );
        if (!user) return null;
        return { id: user.id, name: user.name, email: user.email, role: user.role, tenantId: user.tenantId };
      },
    }),
  ],
  callbacks: {
    async jwt({ token, user }) {
      if (user) {
        token.role = user.role;
        token.tenantId = user.tenantId;
        token.sub = user.id;
      }
      return token;
    },
    async session({ session, token }) {
      session.user.id = token.sub!;
      session.user.role = token.role as UserRole;
      session.user.tenantId = token.tenantId as string;
      return session;
    },
  },
});
