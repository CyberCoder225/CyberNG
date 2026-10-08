package com.v2ray.ang.ui.settings

import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.BuildConfig
import com.v2ray.ang.R
import com.v2ray.ang.ui.base.BaseComponentActivity
import com.v2ray.ang.ui.compose.AppTopBar

class SettingsActivity : BaseComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    @Composable
    override fun ScreenContent() {
        val mode by viewModel.connectionMode.collectAsStateWithLifecycle()
        val labels = stringArrayResource(R.array.mode_entries).toList()
        val values = stringArrayResource(R.array.mode_value).toList()

        Scaffold(
            topBar = {
                AppTopBar(
                    title = stringResource(R.string.title_settings),
                    onBackClick = { finish() },
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.title_mode),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_mode_help),
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.height(8.dp))
                values.forEachIndexed { index, value ->
                    val selected = mode == value
                    // The row is one radio-button node: tapping anywhere on it selects this mode.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected,
                                role = Role.RadioButton,
                                onClick = { viewModel.setConnectionMode(value) },
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = selected, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = labels.getOrElse(index) { value },
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.home_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
