package com.orienteering.hunt.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.orienteering.hunt.ui.theme.ForestDeep
import com.orienteering.hunt.ui.theme.ForestMid
import com.orienteering.hunt.ui.theme.SunGold
import com.orienteering.hunt.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    onCheckInSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeHuntState by viewModel.activeHuntState.collectAsStateWithLifecycle()
    
    val hunt = activeHuntState.hunt
    val progress = activeHuntState.progress
    val currentLocationIndex = progress?.currentLocationIndex ?: 0
    val currentLocation = hunt?.locations?.getOrNull(currentLocationIndex)
    
    var showSuccess by remember { mutableStateOf(false) }
    var isCheckingIn by remember { mutableStateOf(false) }
    
    LaunchedEffect(activeHuntState.showCheckInSuccess) {
        if (activeHuntState.showCheckInSuccess) {
            showSuccess = true
            isCheckingIn = false
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Check In",
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (showSuccess) {
                SuccessView(
                    locationName = currentLocation?.name ?: "Location",
                    points = currentLocation?.points ?: 0,
                    onContinue = {
                        showSuccess = false
                        onCheckInSuccess()
                    }
                )
            } else {
                CheckInView(
                    locationName = currentLocation?.name ?: "Unknown Location",
                    canCheckIn = activeHuntState.canCheckIn,
                    distance = activeHuntState.distanceToTarget,
                    isCheckingIn = isCheckingIn,
                    onCheckIn = {
                        isCheckingIn = true
                        val success = viewModel.checkIn()
                        if (!success) {
                            isCheckingIn = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CheckInView(
    locationName: String,
    canCheckIn: Boolean,
    distance: Float?,
    isCheckingIn: Boolean,
    onCheckIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    if (canCheckIn) {
                        Brush.radialGradient(
                            colors = listOf(
                                ForestMid,
                                ForestDeep
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = if (canCheckIn) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = locationName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        distance?.let { dist ->
            val displayDistance = if (dist < 1000) {
                "${dist.toInt()} meters away"
            } else {
                String.format("%.1f km away", dist / 1000)
            }
            
            Text(
                text = if (canCheckIn) "You're at the location!" else displayDistance,
                style = MaterialTheme.typography.bodyLarge,
                color = if (canCheckIn) ForestMid else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onCheckIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            enabled = canCheckIn && !isCheckingIn,
            colors = ButtonDefaults.buttonColors(
                containerColor = ForestMid,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isCheckingIn) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = if (canCheckIn) "Confirm Check-In" else "Move closer to check in",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SuccessView(
    locationName: String,
    points: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                initialScale = 0.5f
            )
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                SunGold,
                                SunGold.copy(alpha = 0.8f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Location Found!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ForestMid
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = locationName,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            modifier = Modifier
                .background(
                    SunGold.copy(alpha = 0.15f),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Text(
                text = "+$points points",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SunGold
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ForestMid
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Continue Hunt",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
