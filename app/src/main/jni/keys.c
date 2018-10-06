#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_exact_xtra_util_TwitchApiHelper_getClientId(JNIEnv *env, jobject instance) {
 return (*env)->  NewStringUTF(env, "ilfexgv3nnljz3isbm257gzwrzr7bi");
}