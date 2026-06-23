package com.nonsense.chat.ui.vpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nonsense.chat.data.vpn.VpnConfig
import com.nonsense.chat.data.vpn.VpnStage
import com.nonsense.chat.ui.common.TgTopAppBar
import com.nonsense.chat.ui.common.brandGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnScreen(
    onBack: () -> Unit,
    viewModel: VpnViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val config by viewModel.config.collectAsState(initial = VpnConfig())

    val vpnConsentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) viewModel.onVpnPrepared()
    }

    LaunchedEffect(Unit) {
        viewModel.prepareEvents.collect {
            val intent: Intent? = VpnService.prepare(context)
            if (intent != null) vpnConsentLauncher.launch(intent) else viewModel.onVpnPrepared()
        }
    }

    Scaffold(
        topBar = {
            TgTopAppBar(title = "VPN", onBack = onBack)
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            VpnPowerCard(
                stage = state.stage,
                error = state.lastError,
                onToggle = viewModel::toggle,
            )

            SectionTitle("Тоннель")
            ToggleRow("Использовать список прокси из настроек", config.useProxyList, viewModel::setUseProxyList)

            var outbound by rememberSaveable { mutableStateOf("") }
            LaunchedEffect(config.outboundConfig) {
                if (outbound.isEmpty() && config.outboundConfig.isNotEmpty()) outbound = config.outboundConfig
            }
            OutlinedTextField(
                value = outbound,
                onValueChange = { outbound = it; viewModel.setOutbound(it) },
                label = { Text("sing-box outbound JSON или VLESS/Reality заметка") },
                placeholder = {
                    Text("""{"type":"vless","tag":"proxy","server":"example.com","server_port":443}""")
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                minLines = 4,
            )

            Text(
                "Фаза 2A поднимает системный VPN и показывает ключик. Реальный обход DPI включится после подключения libbox.aar и рабочего сервера.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            HorizontalDivider()
            SectionTitle("Сеть")
            var dns by rememberSaveable { mutableStateOf("") }
            LaunchedEffect(config.dns) {
                if (dns.isEmpty()) dns = config.dns
            }
            OutlinedTextField(
                value = dns,
                onValueChange = { dns = it; viewModel.setDns(it) },
                label = { Text("DNS") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                singleLine = true,
            )
            ToggleRow("IPv6 маршрут", config.ipv6, viewModel::setIpv6)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun VpnPowerCard(stage: VpnStage, error: String?, onToggle: () -> Unit) {
    val connected = stage == VpnStage.CONNECTED
    val connecting = stage == VpnStage.CONNECTING || stage == VpnStage.PREPARING || stage == VpnStage.RECONNECTING
    val title = when {
        connected -> "Подключено"
        connecting -> "Подключение"
        stage == VpnStage.ERROR -> "Ошибка"
        else -> "Отключено"
    }
    val subtitle = when {
        error != null -> error
        connected -> "Системный VPN активен"
        connecting -> "Запрашиваем системный тоннель"
        else -> "Нажмите, чтобы включить VPN"
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onToggle)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val brush: Brush = if (connected || connecting) brandGradient()
        else Brush.linearGradient(listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline))
        Icon(
            if (connected || connecting) Icons.Default.Security else Icons.Default.PowerSettingsNew,
            contentDescription = null,
            tint = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier.size(54.dp).clip(CircleShape).background(brush).padding(14.dp),
        )
        Column(Modifier.weight(1f).padding(start = 16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = connected || connecting, onCheckedChange = { onToggle() })
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
