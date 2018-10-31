#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_github_exact7_xtra_util_TwitchApiHelper_getClientId(JNIEnv *env, jobject instance) {
 return (*env)->  NewStringUTF(env, "ilfexgv3nnljz3isbm257gzwrzr7bi");
}