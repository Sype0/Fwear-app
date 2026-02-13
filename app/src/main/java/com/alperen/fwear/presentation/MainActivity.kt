package com.alperen.fwear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material3.AppCard
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import coil.compose.SubcomposeAsyncImage
import com.alperen.fwear.data.FDroidAppDto
import com.alperen.fwear.data.RetrofitClient
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavigation(viewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "list"
    ) {
        composable("list") {
            RepoListScreen(
                viewModel = viewModel,
                onAppClick = { packageName ->
                    viewModel.selectApp(packageName)
                    navController.navigate("details")
                }
            )
        }
        composable("details") {
            AppDetailsScreen(viewModel)
        }
    }
}

@Composable
fun RepoListScreen(
    viewModel: MainViewModel,
    onAppClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ListHeader {
                            Text(
                                "FWear Store",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(32.dp)
                                .background(Color.DarkGray, shape = MaterialTheme.shapes.small)
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchText.isEmpty()) {
                                Text("Ara...", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            }
                            BasicTextField(
                                value = searchText,
                                onValueChange = { viewModel.onSearchTextChange(it) },
                                textStyle = TextStyle(color = Color.White),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                when (val state = uiState) {
                    is UiState.Loading -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(50.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is UiState.Error -> {
                        item {
                            Text(
                                text = state.message,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    is UiState.Success -> {
                        if (state.apps.isEmpty()) {
                            item {
                                Text("No Data", modifier = Modifier.padding(16.dp))
                            }
                        } else {
                            items(state.apps) { app ->
                                RepoAppItem(app = app, onClick = { onAppClick(app.packageName) })
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "FWear v1.0",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\uD83C\uDDF9\uD83C\uDDF7",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun RepoAppItem(app: FDroidAppDto, onClick: () -> Unit) {
    val appName = app.getBestName()

    AppCard(
        onClick = onClick,
        appName = {
            Text(
                text = app.license,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        },
        appImage = {
            SubcomposeAsyncImage(
                model = app.getFullIconUrl(RetrofitClient.ICON_BASE_URL),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                loading = {
                    AppIconPlaceholder(name = appName, packageName = app.packageName, size = 24.dp)
                },
                error = {
                    AppIconPlaceholder(name = appName, packageName = app.packageName, size = 24.dp)
                }
            )
        },
        title = {
            Text(
                text = appName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        time = { /* Boş */ }
    ) {
        Text(
            text = app.getBestSummary(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall,
            color = Color.LightGray
        )
    }
}

@Composable
fun AppDetailsScreen(viewModel: MainViewModel) {
    val app by viewModel.selectedApp.collectAsState()
    val isDownloading by viewModel.isDownloading.collectAsState()
    val progress by viewModel.downloadProgress.collectAsState()
    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (app == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select App")
                }
            } else {
                val validApp = app!!
                val appName = validApp.getBestName()

                ScalingLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(20.dp))

                            SubcomposeAsyncImage(
                                model = validApp.getFullIconUrl(RetrofitClient.ICON_BASE_URL),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                loading = {
                                    AppIconPlaceholder(name = appName, packageName = validApp.packageName, size = 64.dp, fontSize = 24.sp)
                                },
                                error = {
                                    AppIconPlaceholder(name = appName, packageName = validApp.packageName, size = 64.dp, fontSize = 24.sp)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = appName,
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(12.dp))

                        if (isDownloading) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.size(40.dp),
                                    colors = ProgressIndicatorDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "%${(progress * 100).toInt()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.startDownloadAndInstall(validApp)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Download", color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        TitleCard(onClick = { }, title = { Text("explanation") }) {
                            Text(
                                text = validApp.getBestSummary(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Paket: ${validApp.packageName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun AppIconPlaceholder(
    name: String,
    packageName: String,
    size: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp
) {
    val color = getMaterialColorFor(packageName)
    val initial = name.trim().firstOrNull()?.uppercase() ?: "?"

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize
        )
    }
}

fun getMaterialColorFor(packageName: String): Color {
    val materialColors = listOf(
        Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0),
        Color(0xFF673AB7), Color(0xFF3F51B5), Color(0xFF2196F3),
        Color(0xFF03A9F4), Color(0xFF00BCD4), Color(0xFF009688),
        Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39),
        Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF9800),
        Color(0xFFFF5722), Color(0xFF795548), Color(0xFF607D8B)
    )
    val index = abs(packageName.hashCode()) % materialColors.size
    return materialColors[index]
}