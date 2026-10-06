package com.kurbat.albionradar.overlay.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurbat.albionradar.core.capture.PacketCapturer
import com.kurbat.albionradar.core.settings.persistence.SettingsRepository
import com.kurbat.albionradar.decode.events.EventRouter
import com.kurbat.albionradar.decode.events.HarvestableEventHandler
import com.kurbat.albionradar.decode.events.MobEventHandler
import com.kurbat.albionradar.decode.events.PlayerEventHandler
import com.kurbat.albionradar.decode.photon.MessageTypes
import com.kurbat.albionradar.decode.photon.BinaryReader
import com.kurbat.albionradar.decode.photon.PhotonDecoder
import com.kurbat.albionradar.decode.photon.Protocol18
import com.kurbat.albionradar.domain.EventCodes
import com.kurbat.albionradar.domain.OperationCodes
import com.kurbat.albionradar.domain.Position
import com.kurbat.albionradar.overlay.presentation.state.GameState
import com.kurbat.albionradar.overlay.presentation.state.RadarUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class OverlayViewModel(
    private val capturer: PacketCapturer,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private var photonErrorCount = 0L
    private val decoder = PhotonDecoder(onParseError = ::reportPhotonError)
    private val protocol18 = Protocol18()
    private val gameState = GameState()
    private val eventRouter = EventRouter()

    private val _uiState = MutableStateFlow(RadarUiState())
    val uiState: StateFlow<RadarUiState> = _uiState.asStateFlow()

    init {
        setupEventHandlers()
        collectGameState()
        collectCaptureStats()
        collectSettings()
        startCapture()
        startCleanupTask()
    }

    private fun setupEventHandlers() {
        val playerHandler = PlayerEventHandler(
            onPlayerAdded = { gameState.addPlayer(it) },
            onPlayerMoved = { id, pos -> gameState.updatePlayerPosition(id, pos) },
            onPlayerRemoved = { gameState.removePlayer(it) },
            onPlayerHealthChanged = { id, health, max ->
                gameState.updatePlayerHealth(id, health, max)
            }
        )
        val mobHandler = MobEventHandler(
            onMobAdded = { gameState.addMob(it) },
            onMobMoved = { id, pos -> gameState.updateMobPosition(id, pos) },
            onMobRemoved = { gameState.removeMob(it) },
            onMobHealthChanged = { id, health, max ->
                gameState.updateMobHealth(id, health, max)
            }
        )
        val harvestableHandler = HarvestableEventHandler(
            onHarvestableAdded = { gameState.addHarvestable(it) },
            onHarvestableChanged = { id, size, enchantment ->
                gameState.updateHarvestableState(id, size, enchantment)
            },
            onHarvestableRemoved = { gameState.removeHarvestable(it) }
        )

        listOf(
            EventCodes.NEW_CHARACTER,
            EventCodes.MOVE,
            EventCodes.LEAVE,
            EventCodes.HEALTH_UPDATE,
            EventCodes.REGENERATION_HEALTH_CHANGED
        ).forEach { eventRouter.registerHandler(it, playerHandler) }

        listOf(
            EventCodes.NEW_MOB,
            EventCodes.MOVE,
            EventCodes.LEAVE,
            EventCodes.HEALTH_UPDATE,
            EventCodes.REGENERATION_HEALTH_CHANGED
        ).forEach { eventRouter.registerHandler(it, mobHandler) }

        listOf(
            EventCodes.NEW_SIMPLE_HARVESTABLE_OBJECT,
            EventCodes.NEW_SIMPLE_HARVESTABLE_OBJECT_LIST,
            EventCodes.NEW_HARVESTABLE_OBJECT,
            EventCodes.HARVESTABLE_CHANGE_STATE,
            EventCodes.LEAVE
        ).forEach { eventRouter.registerHandler(it, harvestableHandler) }
    }

    private fun collectGameState() {
        viewModelScope.launch {
            gameState.players.collect { players ->
                _uiState.value = _uiState.value.copy(players = players)
            }
        }

        viewModelScope.launch {
            gameState.mobs.collect { mobs ->
                _uiState.value = _uiState.value.copy(mobs = mobs)
            }
        }

        viewModelScope.launch {
            gameState.harvestables.collect { harvestables ->
                _uiState.value = _uiState.value.copy(harvestables = harvestables)
            }
        }

        viewModelScope.launch {
            gameState.localPlayer.collect { localPlayer ->
                _uiState.value = _uiState.value.copy(localPlayer = localPlayer)
            }
        }
    }

    private fun collectCaptureStats() {
        viewModelScope.launch {
            capturer.stats.collect { stats ->
                _uiState.value = _uiState.value.copy(
                    captureStats = stats,
                    isCapturing = stats.isActive
                )
            }
        }
    }

    private fun collectSettings() {
        viewModelScope.launch {
            settingsRepository.updatesFlow.collect { config ->
                _uiState.value = _uiState.value.copy(
                    config = config,
                    zoom = config.display.zoom
                )
            }
        }
    }

    private fun startCapture() {
        viewModelScope.launch {
            capturer.packets.collect { packet ->
                processPacket(packet.payload.array)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                capturer.start()
            } catch (e: Exception) {
                println("Capture error: ${e.message}")
            }
        }
    }

    private fun startCleanupTask() {
        viewModelScope.launch {
            while (isActive) {
                delay(10000)
                gameState.cleanupOldObjects()
            }
        }
    }

    private fun processPacket(payload: ByteArray) {
        val packet = decoder.decode(payload) ?: return

        packet.commands.forEach { command ->
            val commandPayload = command.payload ?: return@forEach
            try {
                when (command.messageType) {
                    MessageTypes.EVENT -> {
                        val event = protocol18.deserializeEventData(BinaryReader(commandPayload))
                        eventRouter.route(event)
                    }
                    MessageTypes.REQUEST -> {
                        val request = protocol18.deserializeOperationRequest(BinaryReader(commandPayload))
                        if (request.code == OperationCodes.MOVE) {
                            request.parameters[1].toPosition()?.let(gameState::updateLocalPlayerPosition)
                        }
                    }
                    MessageTypes.RESPONSE, MessageTypes.RESPONSE_ALT -> {
                        val response = protocol18.deserializeOperationResponse(BinaryReader(commandPayload))
                        if (response.code == OperationCodes.JOIN) {
                            response.parameters[9].toPosition()?.let(gameState::updateLocalPlayerPosition)
                        }
                    }
                }
            } catch (error: Exception) {
                reportPhotonError(error.message ?: "Protocol18 payload parsing failed")
            }
        }
    }

    private fun Any?.toPosition(): Position? = when (this) {
        is List<*> -> {
            val x = getOrNull(0) as? Number
            val y = getOrNull(1) as? Number
            if (x != null && y != null) Position(x.toFloat(), y.toFloat()) else null
        }
        is ByteArray -> if (size >= 8) {
            val reader = BinaryReader(this)
            Position(reader.readFloat32LE(), reader.readFloat32LE())
        } else {
            null
        }
        else -> null
    }

    private fun reportPhotonError(reason: String) {
        photonErrorCount++
        if (photonErrorCount % 100L == 1L) {
            println("Photon parsing errors: $photonErrorCount (last: $reason)")
        }
    }

    fun updateZoom(delta: Float) {
        val newZoom = (_uiState.value.zoom + delta).coerceIn(0.5f, 3.0f)
        _uiState.value = _uiState.value.copy(zoom = newZoom)
    }

    fun toggleOverlay() {
        _uiState.value = _uiState.value.copy(showOverlay = !_uiState.value.showOverlay)
    }

    override fun onCleared() {
        super.onCleared()
        capturer.stop()
        eventRouter.clearHandlers()
        gameState.clear()
    }
}
