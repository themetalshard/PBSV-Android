package com.metalshard.hyperion

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.metalshard.hyperion.model.ScheduleEvent
import com.metalshard.hyperion.ui.ScheduleViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// RIP:
// TheMetalShard (developer)
// Primative_11 (main tester)
// LunarThePr0t0g3n
// Boltazon
// Kyguy329 (Mac version)
// TheSkout001 (for ideas)
//
// Well, thanks ATTG!

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            HyperionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScheduleScreen()
                }
            }
        }
    }
}

@Composable
fun HyperionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(primary = Color(0xFFD0BCFF))
        else -> lightColorScheme(primary = Color(0xFF6750A4))
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(vm: ScheduleViewModel = viewModel()) {
    val schedule by vm.schedule.collectAsState()
    val isCalendarView by vm.isCalendarView
    val activeGroup = vm.activeGroup.value
    val selectedEvent = vm.selectedEvent.value

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navItems = listOf(
                    Triple("PBST", "Shield", Icons.Filled.Shield),
                    Triple("PET", "Fire", Icons.Filled.MedicalServices),
                    Triple("TMS", "Explosion", Icons.Filled.LocalFireDepartment),
                    Triple("PBM", "Camera", Icons.Filled.PhotoCamera)
                )

                navItems.forEach { (id, label, icon) ->
                    NavigationBarItem(
                        selected = activeGroup == id,
                        onClick = { vm.activeGroup.value = id },
                        label = { Text(id) },
                        icon = { Icon(icon, contentDescription = label) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { vm.isCalendarView.value = !isCalendarView }) {
                Icon(if (isCalendarView) Icons.AutoMirrored.Filled.List else Icons.Default.CalendarViewWeek, null)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (vm.isLoading.value) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                val currentGroupData = schedule[activeGroup] ?: schedule[activeGroup.lowercase()] ?: emptyList()

                if (currentGroupData.isEmpty()) {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No events found for $activeGroup", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { vm.refresh() }, Modifier.padding(top = 8.dp)) { Text("Refresh") }
                    }
                } else if (isCalendarView) {
                    MultiColumnContent(vm, currentGroupData)
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(currentGroupData.sortedBy { it.time }) { event ->
                            EventCardItem(event) { vm.selectedEvent.value = event }
                        }
                    }
                }
            }
        }
    }

    selectedEvent?.let { event ->
        EventDetailPopup(event, onDismiss = { vm.selectedEvent.value = null })
    }
}

@Composable
fun MultiColumnContent(vm: ScheduleViewModel, events: List<ScheduleEvent>) {
    val dayFormatter = DateTimeFormatter.ofPattern("EEE dd/MM")
    val scrollState = rememberScrollState()
    val eventsByDate = events.groupBy {
        Instant.ofEpochSecond(it.time).atZone(ZoneId.systemDefault()).toLocalDate()
    }.toSortedMap()

    Row(modifier = Modifier.fillMaxSize().horizontalScroll(scrollState).padding(16.dp)) {
        eventsByDate.forEach { (date, dayEvents) ->
            Column(modifier = Modifier.width(280.dp).fillMaxHeight().padding(end = 16.dp)) {
                Text(
                    text = date.format(dayFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dayEvents.sortedBy { it.time }) { event ->
                        CompactCard(event) { vm.selectedEvent.value = event }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactCard(event: ScheduleEvent, onClick: () -> Unit) {
    val color = event.eventColor?.let { Color(it[0], it[1], it[2]) } ?: Color.Gray
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(color, RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(8.dp))
                Text(timeFormatter.format(Instant.ofEpochSecond(event.time)), style = MaterialTheme.typography.labelSmall)
            }
            Text(event.eventType, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Host: ${event.trainer}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun EventCardItem(event: ScheduleEvent, onClick: () -> Unit) {
    val color = event.eventColor?.let { Color(it[0], it[1], it[2]) } ?: Color.Gray
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp).height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.fillMaxHeight().width(4.dp), color = color, shape = RoundedCornerShape(2.dp)) {}
            Spacer(Modifier.width(16.dp))
            Column {
                Text(timeFormatter.format(Instant.ofEpochSecond(event.time)), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(event.eventType, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Host: ${event.trainer}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun EventDetailPopup(event: ScheduleEvent, onDismiss: () -> Unit) {
    val instant = Instant.ofEpochSecond(event.time)
    val localFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.systemDefault())
    val utcFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'").withZone(ZoneId.of("UTC"))

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text(event.eventType, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                DetailItem("Local start", localFormatter.format(instant))
                DetailItem("UTC start", utcFormatter.format(instant))
                DetailItem("Unix timestamp", event.time.toString())
                DetailItem("Duration", "${event.duration} minutes")
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DetailItem("Host", event.trainer ?: "N/A")
                event.notes?.let { Text("Notes: $it", style = MaterialTheme.typography.bodyMedium) }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DetailItem("UUID", event.uuid ?: "N/A", isSmall = true)
                DetailItem("Trainer ID", event.trainerId?.toString() ?: "N/A", isSmall = true)
                DetailItem("Discord ID", event.discordId ?: "N/A", isSmall = true)
            }
        }
    )
}

@Composable
fun DetailItem(label: String, value: String, isSmall: Boolean = false) {
    Text(
        text = "$label: $value",
        style = if (isSmall) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium,
        color = if (isSmall) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    )
}