package com.orienteering.hunt.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.orienteering.hunt.ui.components.DistanceIndicator
import com.orienteering.hunt.ui.components.LocationCheckInSuccess
import com.orienteering.hunt.ui.components.ProgressStatRow
import com.orienteering.hunt.ui.theme.CompassRed
import com.orienteering.hunt.ui.theme.ForestDeep
import com.orienteering.hunt.ui.theme.ForestMid
import com.orienteering.hunt.ui.theme.SunGold
import com.orienteering.hunt.ui.theme.TrailOrange
import com.orienteering.hunt.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveHuntScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToCheckIn: () -> Unit,
    onHuntCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeHuntState by viewModel.activeHuntState.collectAsStateWithLifecycle()
    val hunt = activeHuntState.hunt
    val progress = activeHuntState.progress
    
    if (activeHuntState.isCompleted) {
        onHuntCompleted()
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = hunt?.name ?: "Active Hunt",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToMap) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "View Map"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            hunt?.let { currentHunt ->
                progress?.let { currentProgress ->
                    val currentLocationIndex = currentProgress.currentLocationIndex
                    val currentLocation = currentHunt.locations.getOrNull(currentLocationIndex)
                    val currentClue = viewModel.getCurrentClue()
                    val progressFraction = currentProgress.visitedLocations.size.toFloat() / currentHunt.locations.size
                    
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = SunGold,
                        trackColor = SunGold.copy(alpha = 0.2f)
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProgressStatRow(
                                visited = currentProgress.visitedLocations.size,
                                total = currentHunt.locations.size,
                                points = currentProgress.earnedPoints,
                                time = currentProgress.elapsedTimeFormatted
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        currentLocation?.let { location ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically(
                                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                                )
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        ForestMid,
                                                        ForestDeep
                                                    )
                                                )
                                            )
                                            .padding(24.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${currentLocationIndex + 1}",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = "Current Target",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White.copy(alpha = 0.8f)
                                                )
                                                Text(
                                                    text = location.name,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                    
                                    Column(
                                        modifier = Modifier.padding(24.dp)
                                    ) {
                                        Text(
                                            text = "Clue",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = currentClue ?: "Find your way to the next location!",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        
                                        location.hint.let { hint ->
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        TrailOrange.copy(alpha = 0.1f),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Help,
                                                    contentDescription = null,
                                                    tint = TrailOrange,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Hint: $hint",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TrailOrange
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            activeHuntState.distanceToTarget?.let { distance ->
                                DistanceIndicator(
                                    distanceMeters = distance,
                                    canCheckIn = activeHuntState.canCheckIn,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            LocationCheckInSuccess(
                                points = currentLocation.points,
                                visible = activeHuntState.showCheckInSuccess,
                                onDismiss = { viewModel.dismissCheckInSuccess() },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            AnimatedVisibility(
                                visible = !activeHuntState.showCheckInSuccess,
                                enter = fadeIn()
                            ) {
                                Column {
                                    Button(
                                        onClick = onNavigateToCheckIn,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        enabled = activeHuntState.canCheckIn,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ForestMid,
                                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (activeHuntState.canCheckIn) "Check In" else "Not at location",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    OutlinedButton(
                                        onClick = onNavigateToMap,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Explore,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("View Map")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
