package com.example.shakeflashlight

import android.Manifest
import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.shakeflashlight.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var shouldStartServiceAfterPermission = false
    private var currentSensitivityPercent = ShakeSensitivitySettings.DEFAULT_SENSITIVITY_PERCENT
    private var isRenderingWeeklyScheduleUi = false
    private var isRenderingLanguageUi = false
    private var weeklyScheduleConfig = WeeklyScheduleSettings.WeeklyScheduleConfig(
        enabled = false,
        selectedDays = emptySet(),
        startMinutes = WeeklyScheduleSettings.DEFAULT_START_MINUTES,
        endMinutes = WeeklyScheduleSettings.DEFAULT_END_MINUTES
    )

    private val serviceSwitchListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        onServiceSwitchChanged(isChecked)
    }

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            onPermissionResult()
        }

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                AppContract.ACTION_SERVICE_STATE_CHANGED -> {
                    val isRunning = intent.getBooleanExtra(AppContract.EXTRA_IS_SERVICE_RUNNING, false)
                    renderServiceState(isRunning)
                }

                AppContract.ACTION_FLASH_STATE_CHANGED -> {
                    val isFlashOn = intent.getBooleanExtra(AppContract.EXTRA_IS_FLASH_ON, false)
                    renderFlashState(isFlashOn)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppLanguageSettings.applyStoredLanguageIfNeeded(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupServiceSwitch()
        setupSensitivitySlider()
        setupWeeklySchedule()
        setupLanguageSelector()
        renderServiceState(ShakeDetectorService.isServiceRunning)
        renderFlashState(false)
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter().apply {
            addAction(AppContract.ACTION_SERVICE_STATE_CHANGED)
            addAction(AppContract.ACTION_FLASH_STATE_CHANGED)
        }
        // Why: these broadcasts are app-internal state updates.
        ContextCompat.registerReceiver(this, stateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        if (!hasRequiredPermissions()) {
            requestMissingPermissions()
        }
        applyWeeklySchedule(
            config = WeeklyScheduleSettings.readConfig(this),
            saveConfig = false,
            enforceServiceState = true
        )

        renderServiceState(ShakeDetectorService.isServiceRunning)
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(stateReceiver)
        } catch (_: IllegalArgumentException) {
            // Receiver non registrato in questo lifecycle turn.
        }
    }

    private fun setupServiceSwitch() {
        binding.switchService.setOnCheckedChangeListener(serviceSwitchListener)
    }

    private fun setupLanguageSelector() {
        binding.toggleLanguageFlags.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked || isRenderingLanguageUi) return@addOnButtonCheckedListener
            val selectedTag = languageTagByButtonId(checkedId) ?: return@addOnButtonCheckedListener
            onLanguageSelected(selectedTag)
        }
        renderLanguageSelector(AppLanguageSettings.resolveActiveLanguageTag(this))
    }

    private fun onLanguageSelected(languageTag: String) {
        val normalizedTag = AppLanguageSettings.normalizeSupportedTag(languageTag)
        val activeTag = AppLanguageSettings.resolveActiveLanguageTag(this)
        if (normalizedTag == activeTag) return

        AppLanguageSettings.saveLanguageTag(this, normalizedTag)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(normalizedTag))
    }

    private fun renderLanguageSelector(activeLanguageTag: String) {
        val normalizedActiveTag = AppLanguageSettings.normalizeSupportedTag(activeLanguageTag)
        val buttonByTag = languageButtonByTag()
        val activeButtonId = buttonByTag[normalizedActiveTag]?.id ?: binding.buttonLangIt.id

        isRenderingLanguageUi = true
        binding.toggleLanguageFlags.check(activeButtonId)
        for ((tag, button) in buttonByTag) {
            styleLanguageButton(button, isActive = tag == normalizedActiveTag)
        }
        isRenderingLanguageUi = false

        binding.textActiveLanguage.text = getString(
            R.string.language_active_format,
            languageDisplayName(normalizedActiveTag)
        )
    }

    private fun languageButtonByTag(): Map<String, MaterialButton> {
        return mapOf(
            "it" to binding.buttonLangIt,
            "en" to binding.buttonLangEn,
            "fr" to binding.buttonLangFr,
            "es" to binding.buttonLangEs,
            "pt" to binding.buttonLangPt,
            "de" to binding.buttonLangDe
        )
    }

    private fun languageTagByButtonId(buttonId: Int): String? {
        return when (buttonId) {
            R.id.buttonLangIt -> "it"
            R.id.buttonLangEn -> "en"
            R.id.buttonLangFr -> "fr"
            R.id.buttonLangEs -> "es"
            R.id.buttonLangPt -> "pt"
            R.id.buttonLangDe -> "de"
            else -> null
        }
    }

    private fun languageDisplayName(languageTag: String): String {
        return getString(
            when (languageTag) {
                "en" -> R.string.language_name_english
                "fr" -> R.string.language_name_french
                "es" -> R.string.language_name_spanish
                "pt" -> R.string.language_name_portuguese
                "de" -> R.string.language_name_german
                else -> R.string.language_name_italian
            }
        )
    }

    private fun styleLanguageButton(button: MaterialButton, isActive: Boolean) {
        val backgroundColor = MaterialColors.getColor(
            button,
            if (isActive) {
                com.google.android.material.R.attr.colorPrimaryContainer
            } else {
                com.google.android.material.R.attr.colorSurfaceVariant
            }
        )
        val textColor = MaterialColors.getColor(
            button,
            if (isActive) {
                com.google.android.material.R.attr.colorOnPrimaryContainer
            } else {
                com.google.android.material.R.attr.colorOnSurfaceVariant
            }
        )
        val strokeColor = MaterialColors.getColor(
            button,
            if (isActive) {
                com.google.android.material.R.attr.colorPrimary
            } else {
                com.google.android.material.R.attr.colorOutline
            }
        )

        button.backgroundTintList = ColorStateList.valueOf(backgroundColor)
        button.strokeColor = ColorStateList.valueOf(strokeColor)
        button.setTextColor(textColor)
    }

    private fun setupWeeklySchedule() {
        binding.switchWeeklySchedule.setOnCheckedChangeListener { _, isChecked ->
            if (isRenderingWeeklyScheduleUi) return@setOnCheckedChangeListener
            onWeeklyScheduleToggleChanged(isChecked)
        }

        for ((_, chip) in dayChipByWeekday()) {
            chip.setOnCheckedChangeListener { _, _ ->
                if (isRenderingWeeklyScheduleUi) return@setOnCheckedChangeListener
                onWeeklyScheduleDaysChanged()
            }
        }

        binding.buttonScheduleStartTime.setOnClickListener {
            showWeeklyTimePicker(isStart = true)
        }
        binding.buttonScheduleEndTime.setOnClickListener {
            showWeeklyTimePicker(isStart = false)
        }

        applyWeeklySchedule(
            config = WeeklyScheduleSettings.readConfig(this),
            saveConfig = false,
            enforceServiceState = false
        )
    }

    private fun onWeeklyScheduleToggleChanged(enabled: Boolean) {
        val updatedConfig = weeklyScheduleConfig.copy(enabled = enabled)
        if (enabled && !WeeklyScheduleSettings.canBeEnabled(updatedConfig)) {
            showWeeklyScheduleValidationError(updatedConfig)
            renderWeeklyScheduleUi(weeklyScheduleConfig)
            return
        }
        applyWeeklySchedule(
            config = updatedConfig,
            saveConfig = true,
            enforceServiceState = true
        )
    }

    private fun onWeeklyScheduleDaysChanged() {
        val selectedDays = dayChipByWeekday()
            .filterValues { it.isChecked }
            .keys
            .toSet()
        val updatedConfig = weeklyScheduleConfig.copy(selectedDays = selectedDays)
        if (weeklyScheduleConfig.enabled && selectedDays.isEmpty()) {
            Toast.makeText(this, R.string.weekly_schedule_invalid_days_message, Toast.LENGTH_SHORT).show()
            renderWeeklyScheduleUi(weeklyScheduleConfig)
            return
        }
        applyWeeklySchedule(
            config = updatedConfig,
            saveConfig = true,
            enforceServiceState = weeklyScheduleConfig.enabled
        )
    }

    private fun showWeeklyTimePicker(isStart: Boolean) {
        val initialMinutes = if (isStart) weeklyScheduleConfig.startMinutes else weeklyScheduleConfig.endMinutes
        val is24Hour = android.text.format.DateFormat.is24HourFormat(this)
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val minutesOfDay = WeeklyScheduleSettings.clampMinutes(hourOfDay * 60 + minute)
                val updatedConfig = if (isStart) {
                    weeklyScheduleConfig.copy(startMinutes = minutesOfDay)
                } else {
                    weeklyScheduleConfig.copy(endMinutes = minutesOfDay)
                }

                if (updatedConfig.enabled && updatedConfig.startMinutes == updatedConfig.endMinutes) {
                    Toast.makeText(this, R.string.weekly_schedule_invalid_time_message, Toast.LENGTH_SHORT).show()
                    return@TimePickerDialog
                }

                applyWeeklySchedule(
                    config = updatedConfig,
                    saveConfig = true,
                    enforceServiceState = updatedConfig.enabled
                )
            },
            initialMinutes / 60,
            initialMinutes % 60,
            is24Hour
        ).show()
    }

    private fun applyWeeklySchedule(
        config: WeeklyScheduleSettings.WeeklyScheduleConfig,
        saveConfig: Boolean,
        enforceServiceState: Boolean
    ) {
        var safeConfig = config
        if (safeConfig.enabled && !WeeklyScheduleSettings.canBeEnabled(safeConfig)) {
            safeConfig = safeConfig.copy(enabled = false)
        }

        weeklyScheduleConfig = safeConfig
        if (saveConfig) {
            WeeklyScheduleSettings.saveConfig(this, weeklyScheduleConfig)
        }

        if (weeklyScheduleConfig.enabled) {
            WeeklyScheduleScheduler.scheduleAll(this, weeklyScheduleConfig)
            if (enforceServiceState) {
                alignServiceStateToWeeklySchedule()
            }
        } else {
            WeeklyScheduleScheduler.cancelAll(this)
        }
        renderWeeklyScheduleUi(weeklyScheduleConfig)
    }

    private fun alignServiceStateToWeeklySchedule() {
        val shouldRun = WeeklyScheduleSettings.isActiveNow(weeklyScheduleConfig)
        if (shouldRun && !ShakeDetectorService.isServiceRunning) {
            startServiceWithPermissionCheck()
            return
        }
        if (!shouldRun && ShakeDetectorService.isServiceRunning) {
            stopShakeService()
        }
    }

    private fun renderWeeklyScheduleUi(config: WeeklyScheduleSettings.WeeklyScheduleConfig) {
        isRenderingWeeklyScheduleUi = true
        binding.switchWeeklySchedule.isChecked = config.enabled
        for ((day, chip) in dayChipByWeekday()) {
            chip.isChecked = config.selectedDays.contains(day)
        }
        isRenderingWeeklyScheduleUi = false

        val startLabel = formatMinutesAsTime(config.startMinutes)
        val endLabel = formatMinutesAsTime(config.endMinutes)
        binding.buttonScheduleStartTime.text =
            getString(R.string.weekly_schedule_start_time_label, startLabel)
        binding.buttonScheduleEndTime.text =
            getString(R.string.weekly_schedule_end_time_label, endLabel)

        binding.textWeeklyScheduleSummary.text = weeklySummaryText(config, startLabel, endLabel)
        binding.switchService.isEnabled = !config.enabled
        binding.textServiceAutomationHint.visibility = if (config.enabled) View.VISIBLE else View.GONE
    }

    private fun weeklySummaryText(
        config: WeeklyScheduleSettings.WeeklyScheduleConfig,
        startLabel: String,
        endLabel: String
    ): String {
        if (!config.enabled) {
            return getString(R.string.weekly_schedule_summary_disabled)
        }
        if (config.selectedDays.isEmpty()) {
            return getString(R.string.weekly_schedule_summary_no_days)
        }

        val dayText = WeeklyScheduleSettings.orderedDaysMondayFirst
            .filter { config.selectedDays.contains(it) }
            .joinToString(", ") { weekdayLabel(it) }
        var summary = getString(
            R.string.weekly_schedule_summary_format,
            dayText,
            startLabel,
            endLabel
        )
        if (config.isCrossMidnight()) {
            summary += getString(R.string.weekly_schedule_summary_cross_midnight_suffix)
        }
        return summary
    }

    private fun showWeeklyScheduleValidationError(config: WeeklyScheduleSettings.WeeklyScheduleConfig) {
        val messageRes = if (config.selectedDays.isEmpty()) {
            R.string.weekly_schedule_invalid_days_message
        } else {
            R.string.weekly_schedule_invalid_time_message
        }
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
    }

    private fun dayChipByWeekday(): Map<Int, Chip> {
        return mapOf(
            Calendar.MONDAY to binding.chipMonday,
            Calendar.TUESDAY to binding.chipTuesday,
            Calendar.WEDNESDAY to binding.chipWednesday,
            Calendar.THURSDAY to binding.chipThursday,
            Calendar.FRIDAY to binding.chipFriday,
            Calendar.SATURDAY to binding.chipSaturday,
            Calendar.SUNDAY to binding.chipSunday
        )
    }

    private fun weekdayLabel(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.MONDAY -> getString(R.string.day_short_monday)
            Calendar.TUESDAY -> getString(R.string.day_short_tuesday)
            Calendar.WEDNESDAY -> getString(R.string.day_short_wednesday)
            Calendar.THURSDAY -> getString(R.string.day_short_thursday)
            Calendar.FRIDAY -> getString(R.string.day_short_friday)
            Calendar.SATURDAY -> getString(R.string.day_short_saturday)
            else -> getString(R.string.day_short_sunday)
        }
    }

    private fun formatMinutesAsTime(minutes: Int): String {
        val safeMinutes = WeeklyScheduleSettings.clampMinutes(minutes)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, safeMinutes / 60)
            set(Calendar.MINUTE, safeMinutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val pattern = if (android.text.format.DateFormat.is24HourFormat(this)) {
            "HH:mm"
        } else {
            "h:mm a"
        }
        return SimpleDateFormat(pattern, Locale.getDefault()).format(calendar.time)
    }

    private fun setupSensitivitySlider() {
        currentSensitivityPercent = ShakeSensitivitySettings.readSensitivityPercent(this)
        binding.sliderSensitivity.value = currentSensitivityPercent
        renderSensitivity(currentSensitivityPercent)

        binding.sliderSensitivity.addOnChangeListener { _, value, fromUser ->
            val safeValue = ShakeSensitivitySettings.clampSensitivityPercent(value)
            renderSensitivity(safeValue)
            if (fromUser) {
                onSensitivityChanged(safeValue)
            }
        }
    }

    private fun onSensitivityChanged(sensitivityPercent: Float) {
        if (abs(sensitivityPercent - currentSensitivityPercent) < 0.01f) return

        currentSensitivityPercent = sensitivityPercent
        ShakeSensitivitySettings.saveSensitivityPercent(this, sensitivityPercent)

        if (!ShakeDetectorService.isServiceRunning) return

        val updateIntent = Intent(this, ShakeDetectorService::class.java).apply {
            action = AppContract.ACTION_UPDATE_SENSITIVITY
            putExtra(AppContract.EXTRA_SENSITIVITY_PERCENT, sensitivityPercent)
        }
        ContextCompat.startForegroundService(this, updateIntent)
    }

    private fun onServiceSwitchChanged(shouldStart: Boolean) {
        if (weeklyScheduleConfig.enabled) {
            renderServiceState(ShakeDetectorService.isServiceRunning)
            return
        }
        if (shouldStart) {
            startServiceWithPermissionCheck()
            return
        }
        stopShakeService()
    }

    private fun startServiceWithPermissionCheck() {
        if (!hasRequiredPermissions()) {
            shouldStartServiceAfterPermission = true
            requestMissingPermissions()
            renderServiceState(false)
            return
        }

        val startIntent = Intent(this, ShakeDetectorService::class.java).apply {
            action = AppContract.ACTION_START_SERVICE
        }
        ContextCompat.startForegroundService(this, startIntent)
        renderServiceState(true)
    }


    private fun stopShakeService() {
        val serviceIntent = Intent(this, ShakeDetectorService::class.java)
        stopService(serviceIntent)
        shouldStartServiceAfterPermission = false
        renderServiceState(false)
        renderFlashState(false)
    }

    private fun hasRequiredPermissions(): Boolean {
        return hasCameraPermission() && hasNotificationPermission()
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun requestMissingPermissions() {
        val missingPermissions = mutableListOf<String>()

        if (!hasCameraPermission()) {
            missingPermissions += Manifest.permission.CAMERA
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission()) {
            missingPermissions += Manifest.permission.POST_NOTIFICATIONS
        }

        if (missingPermissions.isNotEmpty()) {
            permissionRequestLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun onPermissionResult() {
        if (!hasRequiredPermissions()) {
            shouldStartServiceAfterPermission = false
            Toast.makeText(this, R.string.permissions_required_message, Toast.LENGTH_LONG).show()
            renderServiceState(false)
            return
        }

        Toast.makeText(this, R.string.permissions_granted_message, Toast.LENGTH_SHORT).show()
        if (shouldStartServiceAfterPermission) {
            shouldStartServiceAfterPermission = false
            startServiceWithPermissionCheck()
        }
    }

    private fun renderServiceState(isRunning: Boolean) {
        binding.switchService.setOnCheckedChangeListener(null)
        binding.switchService.isChecked = isRunning
        binding.switchService.setOnCheckedChangeListener(serviceSwitchListener)
        binding.switchService.stateDescription = getString(
            if (isRunning) R.string.service_state_enabled else R.string.service_state_disabled
        )

        val statusText = getString(
            if (isRunning) R.string.service_status_running else R.string.service_status_stopped
        )
        binding.textServiceStatus.text = statusText

        val containerColor = MaterialColors.getColor(
            binding.cardServiceStatus,
            if (isRunning) {
                com.google.android.material.R.attr.colorPrimaryContainer
            } else {
                com.google.android.material.R.attr.colorErrorContainer
            }
        )
        val onContainerColor = MaterialColors.getColor(
            binding.textServiceStatus,
            if (isRunning) {
                com.google.android.material.R.attr.colorOnPrimaryContainer
            } else {
                com.google.android.material.R.attr.colorOnErrorContainer
            }
        )

        binding.cardServiceStatus.setCardBackgroundColor(containerColor)
        binding.textServiceStatusTitle.setTextColor(onContainerColor)
        binding.textServiceStatus.setTextColor(onContainerColor)
        val contentDescription =
            getString(R.string.service_status_content_description, statusText)
        binding.textServiceStatus.contentDescription = contentDescription
        binding.cardServiceStatus.contentDescription = contentDescription
    }

    private fun renderFlashState(isOn: Boolean) {
        val statusText = getString(
            if (isOn) R.string.flash_status_on else R.string.flash_status_off
        )
        binding.textFlashStatus.text = statusText

        val containerColor = MaterialColors.getColor(
            binding.cardFlashStatus,
            if (isOn) {
                com.google.android.material.R.attr.colorTertiaryContainer
            } else {
                com.google.android.material.R.attr.colorSecondaryContainer
            }
        )
        val onContainerColor = MaterialColors.getColor(
            binding.textFlashStatus,
            if (isOn) {
                com.google.android.material.R.attr.colorOnTertiaryContainer
            } else {
                com.google.android.material.R.attr.colorOnSecondaryContainer
            }
        )

        binding.cardFlashStatus.setCardBackgroundColor(containerColor)
        binding.textFlashStatusTitle.setTextColor(onContainerColor)
        binding.textFlashStatus.setTextColor(onContainerColor)
        val contentDescription = getString(R.string.flash_status_content_description, statusText)
        binding.textFlashStatus.contentDescription = contentDescription
        binding.cardFlashStatus.contentDescription = contentDescription
    }

    private fun renderSensitivity(sensitivityPercent: Float) {
        val safeValue = ShakeSensitivitySettings.clampSensitivityPercent(sensitivityPercent)
        val roundedPercent = safeValue.roundToInt()
        val thresholdG = ShakeSensitivitySettings.thresholdForSensitivityPercent(safeValue)
        val sensitivityLabel = getString(
            when {
                safeValue >= 70f -> R.string.sensitivity_level_high
                safeValue >= 40f -> R.string.sensitivity_level_medium
                else -> R.string.sensitivity_level_low
            }
        )

        binding.textSensitivityValue.text =
            getString(R.string.sensitivity_value_format, roundedPercent)
        binding.textSensitivityLevel.text =
            getString(R.string.sensitivity_level_value_format, sensitivityLabel)
        binding.textSensitivityHint.text =
            getString(R.string.sensitivity_threshold_preview_format, thresholdG)

        val sliderStateDescription = getString(
            R.string.sensitivity_slider_state_description,
            roundedPercent,
            sensitivityLabel
        )
        binding.sliderSensitivity.contentDescription = sliderStateDescription
        binding.sliderSensitivity.stateDescription = sliderStateDescription
    }
}