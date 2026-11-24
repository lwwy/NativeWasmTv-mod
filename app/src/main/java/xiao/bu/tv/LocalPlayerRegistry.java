package xiao.bu.tv;

import java.lang.ref.WeakReference;

/** Same-process management-page input bridge; no capture service or sender lease. */
final class LocalPlayerRegistry {
    // [lwwy-mod] Scope to this application id for coexistence with the stock build.
    static final String OPEN_MANAGEMENT = BuildConfig.APPLICATION_ID + ".OPEN_MANAGEMENT";
    private static WeakReference<MainActivity> owner = new WeakReference<MainActivity>(null);
    static void attach(MainActivity activity) { owner = new WeakReference<MainActivity>(activity); }
    static void detach(MainActivity activity) { if (owner.get() == activity) owner.clear(); }
    static MainActivity localInputOwner() { return owner.get(); }
}
