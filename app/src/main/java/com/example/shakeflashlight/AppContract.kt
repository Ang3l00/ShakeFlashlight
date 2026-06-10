package com.example.shakeflashlight

object AppContract {
    const val ACTION_START_SERVICE = "com.example.shakeflashlight.action.START_SERVICE"
    const val ACTION_STOP_SERVICE = "com.example.shakeflashlight.action.STOP_SERVICE"
    const val ACTION_TOGGLE_FLASH = "com.example.shakeflashlight.action.TOGGLE_FLASH"
    const val ACTION_UPDATE_SENSITIVITY = "com.example.shakeflashlight.action.UPDATE_SENSITIVITY"

    const val ACTION_SERVICE_STATE_CHANGED = "com.example.shakeflashlight.action.SERVICE_STATE_CHANGED"
    const val ACTION_FLASH_STATE_CHANGED = "com.example.shakeflashlight.action.FLASH_STATE_CHANGED"

    const val EXTRA_IS_SERVICE_RUNNING = "extra_is_service_running"
    const val EXTRA_IS_FLASH_ON = "extra_is_flash_on"
    const val EXTRA_SENSITIVITY_PERCENT = "extra_sensitivity_percent"

    const val NOTIFICATION_CHANNEL_ID = "shake_flashlight_channel"
    const val NOTIFICATION_ID = 1001
}
