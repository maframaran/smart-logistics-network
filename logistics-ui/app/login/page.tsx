import { signIn } from "@/auth";
import { Button } from "@/components/ui/button";

export default function LoginPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50">
      <div className="w-full max-w-md rounded-xl border bg-white p-8 shadow-sm space-y-6">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-primary">Smart Logistics Network</h1>
          <p className="mt-1 text-sm text-gray-500">Sign in to your portal</p>
        </div>
        <form
          action={async (formData) => {
            "use server";
            await signIn("credentials", {
              email: formData.get("email"),
              password: formData.get("password"),
              redirectTo: "/dashboard",
            });
          }}
          className="space-y-4"
        >
          <div>
            <label className="block text-sm font-medium text-gray-700" htmlFor="email">Email</label>
            <input
              id="email" name="email" type="email" required autoComplete="email"
              placeholder="shipper@platform.local"
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700" htmlFor="password">Password</label>
            <input
              id="password" name="password" type="password" required autoComplete="current-password"
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
            />
          </div>
          <Button type="submit" className="w-full">Sign in</Button>
        </form>
        <div className="text-xs text-gray-400 text-center space-y-1">
          <p>Demo: <code>shipper@platform.local</code> / <code>shipper123</code></p>
          <p>Demo: <code>carrier@platform.local</code> / <code>carrier123</code></p>
        </div>
      </div>
    </div>
  );
}
