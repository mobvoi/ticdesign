
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := ticwear-design-support

#LOCAL_FULL_MANIFEST_FILE := $(LOCAL_PATH)/src/main/AndroidManifest.xml
LOCAL_MANIFEST_FILE := src/main/AndroidManifest.xml

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/src/main/res

LOCAL_SRC_FILES := $(call all-java-files-under, src/main/java) \
	$(call all-Iaidl-files-under, src/main/java)

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4-latest android-support-v7-recyclerview-latest

include $(BUILD_STATIC_JAVA_LIBRARY)

