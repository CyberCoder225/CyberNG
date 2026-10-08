package com.v2ray.ang.ui.main

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.R
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.ui.compose.AppDropdownMenuItems
import com.v2ray.ang.ui.compose.DeleteConfirmDialog
import com.v2ray.ang.ui.compose.QRCodeDialog

/** Import choices shown by the home screen's Import button. */
private enum class HomeImportItem(@StringRes val labelRes: Int, val action: MainAction) {
    Clipboard(R.string.menu_item_import_config_clipboard, MainAction.ImportClipboard),
    QRCode(R.string.menu_item_import_config_qrcode, MainAction.ImportQRcode),
    LocalFile(R.string.menu_item_import_config_local, MainAction.ImportConfigLocal),
}

/** Export choices shown by the home screen's Export button. */
private enum class HomeExportItem(@StringRes val labelRes: Int, val action: MainAction) {
    CopyLinks(R.string.title_export_all, MainAction.ExportAll),
    SaveLinks(R.string.home_export_links_file, MainAction.ExportLinksToFile),
    SaveConfig(R.string.home_export_config_file, MainAction.ExportConfigToFile),
}

/**
 * Simplified home screen: connection control, custom SNI, the server list, and import/export.
 * Business state comes from [MainViewModel]; this composable only renders it and forwards actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onAction: (MainAction) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val serverGroupFlow = remember(uiState.selectedGroupId) {
        mainViewModel.serverGroupState(uiState.selectedGroupId)
    }
    val serverGroupState by serverGroupFlow.collectAsStateWithLifecycle()
    val rows = serverGroupState.rows
    val isRunning = uiState.isRunning
    val selectedGuid = uiState.selectedGuid
    val selectedName = rows.firstOrNull { it.guid == selectedGuid }?.remarks
    val statusText = mainViewModel.formatStatus(uiState.status)

    var sniInput by rememberSaveable { mutableStateOf(uiState.customSni) }
    LaunchedEffect(uiState.customSni) { sniInput = uiState.customSni }

    var showImportMenu by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    var shareTarget by remember { mutableStateOf<Pair<String, ProfileItem>?>(null) }
    var showRemoveConfirm by rememberSaveable(stateSaver = ServerDeleteTarget.Saver) {
        mutableStateOf<ServerDeleteTarget?>(null)
    }

    val removeServer: (String, String) -> Unit = { guid, name ->
        showRemoveConfirm = ServerDeleteTarget(guid, name)
    }

    showRemoveConfirm?.let { target ->
        DeleteConfirmDialog(
            message = stringResource(R.string.confirm_delete_profile),
            itemName = target.profileName,
            onConfirm = {
                showRemoveConfirm = null
                onAction(MainAction.RemoveServer(target.guid))
            },
            onDismiss = { showRemoveConfirm = null },
        )
    }

    shareTarget?.let { (guid, profile) ->
        ShareMethodDialog(
            guid = guid,
            profile = profile,
            more = true,
            onDismiss = { shareTarget = null },
            onAction = onAction,
            onRemove = { targetGuid, name ->
                shareTarget = null
                removeServer(targetGuid, name)
            },
        )
    }

    QRCodeDialog(
        bitmap = uiState.shareQRCodeBitmap,
        onDismiss = { onAction(MainAction.DismissQRCodeDialog) },
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.app_name), fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings_24dp),
                            contentDescription = stringResource(R.string.title_settings),
                        )
                    }
                },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { showImportMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.home_import))
                        }
                        DropdownMenu(
                            expanded = showImportMenu,
                            onDismissRequest = { showImportMenu = false },
                        ) {
                            AppDropdownMenuItems(
                                items = HomeImportItem.entries,
                                labelRes = { it.labelRes },
                                onSelected = { item ->
                                    showImportMenu = false
                                    onAction(item.action)
                                },
                            )
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { showExportMenu = true },
                            enabled = rows.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.home_export))
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false },
                        ) {
                            AppDropdownMenuItems(
                                items = HomeExportItem.entries,
                                labelRes = { it.labelRes },
                                onSelected = { item ->
                                    showExportMenu = false
                                    onAction(item.action)
                                },
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "connection") {
                ConnectionCard(
                    isRunning = isRunning,
                    statusText = statusText,
                    selectedName = selectedName,
                    canConnect = selectedGuid != null,
                    onToggle = { onAction(MainAction.ToggleService) },
                )
            }
            item(key = "custom_sni") {
                CustomSniCard(
                    input = sniInput,
                    savedSni = uiState.customSni,
                    onInputChange = { sniInput = it },
                    onSave = { value -> onAction(MainAction.SaveCustomSni(value)) },
                )
            }
            item(key = "servers_header") {
                ServersHeader(
                    count = rows.size,
                    onTestAll = { onAction(MainAction.TestAllServers) },
                )
            }
            if (rows.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = stringResource(R.string.home_no_servers),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                    )
                }
            }
            items(rows, key = { it.guid }) { row ->
                ServerCard(
                    name = row.remarks,
                    details = row.typeDescription,
                    delayMillis = row.testDelayMillis,
                    selected = row.guid == selectedGuid,
                    onSelect = { onAction(MainAction.SelectServer(row.guid)) },
                    onMore = { shareTarget = row.guid to row.profile },
                )
            }
        }
    }
}

@Composable
private fun ConnectionCard(
    isRunning: Boolean,
    statusText: String,
    selectedName: String?,
    canConnect: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isRunning) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = selectedName ?: stringResource(R.string.home_no_server_selected),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onToggle,
                enabled = isRunning || canConnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text(
                    text = stringResource(if (isRunning) R.string.home_disconnect else R.string.home_connect),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            if (!isRunning && !canConnect) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.home_select_server_first),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun CustomSniCard(
    input: String,
    savedSni: String,
    onInputChange: (String) -> Unit,
    onSave: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.home_sni_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.home_sni_description),
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                label = { Text(stringResource(R.string.home_sni_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onSave(input) }),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (savedSni.isEmpty()) {
                    stringResource(R.string.home_sni_none)
                } else {
                    stringResource(R.string.home_sni_current, savedSni)
                },
                style = MaterialTheme.typography.bodySmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = {
                        onInputChange("")
                        onSave("")
                    },
                    enabled = savedSni.isNotEmpty() || input.isNotEmpty(),
                ) {
                    Text(stringResource(R.string.action_clear))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onSave(input) }) {
                    Text(stringResource(R.string.action_save))
                }
            }
        }
    }
}

@Composable
private fun ServersHeader(count: Int, onTestAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.home_servers_count, count),
            style = MaterialTheme.typography.titleMedium,
        )
        TextButton(onClick = onTestAll, enabled = count > 0) {
            Text(stringResource(R.string.home_test_all))
        }
    }
}

@Composable
private fun ServerCard(
    name: String,
    details: String,
    delayMillis: Long,
    selected: Boolean,
    onSelect: () -> Unit,
    onMore: () -> Unit,
) {
    val delayText = if (delayMillis > 0) {
        stringResource(R.string.home_delay_ms, delayMillis)
    } else {
        stringResource(R.string.home_delay_not_tested)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // The whole text area is one selectable node; the menu button stays a separate control.
            Row(
                modifier = Modifier
                    .weight(1f)
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = onSelect,
                    )
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = selected, onClick = null)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "$details  ·  $delayText",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = onMore) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_vert_24dp),
                    contentDescription = stringResource(R.string.home_server_more, name),
                )
            }
        }
    }
}
