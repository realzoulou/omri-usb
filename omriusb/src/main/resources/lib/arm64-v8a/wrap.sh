#!/system/bin/sh
HERE="$(cd "$(dirname "$0")" && pwd)"
ASAN_OPTIONS_DEFAULT=log_to_syslog=false,allow_user_segv_handler=1
ASAN_OPTIONS_ADD=
export ASAN_OPTIONS=$ASAN_OPTIONS_DEFAULT$ASAN_OPTIONS_ADD
ASAN_LIB=$(ls $HERE/libclang_rt.asan-*-android.so)
if [ -f "$HERE/libc++_shared.so" ]; then
    # Workaround for https://github.com/android-ndk/ndk/issues/988.
    export LD_PRELOAD="$ASAN_LIB $HERE/libc++_shared.so"
else
    export LD_PRELOAD="$ASAN_LIB"
fi
"$@"
