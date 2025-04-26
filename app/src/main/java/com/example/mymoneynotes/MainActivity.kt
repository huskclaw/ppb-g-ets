package com.example.mymoneynotes

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymoneynotes.ui.theme.MyMoneyNotesTheme
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import kotlinx.coroutines.launch

// Transaction types
enum class TransactionType { Income, Expense }

// Data class for transactions
data class Transaction(val type: TransactionType, val category: String, val amount: Double)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force portrait mode
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        enableEdgeToEdge()
        setContent {
            MyMoneyNotesTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF363636)) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("MyMoney Notes", fontWeight = FontWeight.Bold) },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color(0xFF2196F3),
                                    titleContentColor = Color.White
                                )
                            )
                        }
                    ) { paddingValues ->
                        MainScreen(modifier = Modifier.padding(paddingValues))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    // Transactions state
    val transactions = remember { mutableStateListOf<Transaction>() }
    // Input state
    var type by remember { mutableStateOf(TransactionType.Income) }
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    // Scroll state for the main content
    val scrollState = rememberScrollState()

    // Bottom sheet state - configuring it to not fully hide
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        confirmValueChange = { sheetValue ->
            // Prevent the sheet from completely hiding by rejecting the Hidden state
            sheetValue != SheetValue.Hidden
        },
        skipHiddenState = true // Skip hidden state entirely
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)


    // Handle back presses to prevent the bottom sheet from hiding
    val scope = rememberCoroutineScope()
    BackHandler(enabled = sheetState.currentValue == SheetValue.Expanded) {
        scope.launch {
            sheetState.partialExpand()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetSwipeEnabled = true, // Disables swipe gestures on the entire sheet
        sheetPeekHeight = 200.dp, // Minimum visible height
        sheetDragHandle = null,
        sheetContent = {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                val maxSheetHeight = this.maxHeight * 0.891f // 80% of screen height

                val nestedScrollInterop = rememberNestedScrollInteropConnection()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = maxSheetHeight)
                ) {
                    // Header bar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1976D2))
                            .padding(top = 12.dp, bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Handle bar INSIDE the blue box
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .height(4.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Transaction History",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (transactions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No transactions yet",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .nestedScroll(nestedScrollInterop)
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(transactions.asReversed()) { index, tx ->
                                TransactionItem(tx) {
                                    transactions.removeAt(transactions.lastIndex - index)
                                }
                                if (index < transactions.size - 1) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }
                        }
                    }
                }
            }
        },
//        containerColor = Color(0xFFE3F2FD),
//        containerColor = Color(0xFF262626),
        containerColor = MaterialTheme.colorScheme.background,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        content = { innerPadding ->
            // Main content
            Column(
                modifier = modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Input card
//                Spacer(modifier = Modifier.height(75.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Type selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFBBDEFB))
                        ) {
                            TransactionType.entries.forEach { t ->
                                val selected = t == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { type = t }
                                        .background(if (selected) Color(0xFF1976D2) else Color.Transparent)
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = t.name, color = if (selected) Color.White else Color.Black)
                                }
                            }
                        }
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("Amount (Rp)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                amount.toDoubleOrNull()?.let { amt ->
                                    if (amt > 0 && category.isNotBlank()) {
                                        transactions.add(Transaction(type, category, amt))
                                        category = ""
                                        amount = ""
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Transaction")
                        }
                    }
                }

                // Totals summary
                val income = transactions.filter { it.type == TransactionType.Income }.sumOf { it.amount }
                val expense = transactions.filter { it.type == TransactionType.Expense }.sumOf { it.amount }
                val balance = income - expense
                val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply { maximumFractionDigits = 2 }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Total Income: ${fmt.format(income)}", color = Color(0xFF388E3C))
                        Text("Total Expense: ${fmt.format(expense)}", color = Color(0xFFD32F2F))
                        Text("Balance: ${fmt.format(balance)}", fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PieChart(
                        modifier = Modifier
                            .weight(1f),
                        data = transactions.filter { it.type == TransactionType.Expense },
                        label = "Expenses"
                    )
                    PieChart(
                        modifier = Modifier
                            .weight(1f),
                        data = transactions.filter { it.type == TransactionType.Income },
                        label = "Income"
                    )
                }

                // Spacer to ensure content doesn't get hidden behind the bottom sheet
                Spacer(modifier = Modifier.height(56.dp))
            }
        }
    )
}


@Composable
fun PieChart(modifier: Modifier = Modifier, data: List<Transaction>, label: String) {
    // If no data, show placeholder
    if (data.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No data", color = Color.Gray)
        }
        return
    }

    // Aggregate by category
    val grouped = data.groupBy { it.category }.mapValues { it.value.sumOf { tx -> tx.amount } }

    // Limit to top 5 categories + "Other"
    val total = grouped.values.sum()
    val sortedEntries = grouped.entries.sortedByDescending { it.value }

    val topCategories = if (sortedEntries.size <= 5) {
        sortedEntries
    } else {
        val top5 = sortedEntries.take(5)
        val otherSum = sortedEntries.drop(5).sumOf { it.value }
        top5 + mapOf("Other" to otherSum).entries
    }

    // Define fixed colors for up to 6 slices (5 categories + "Other")
    val pieColors = listOf(
        Color(0xFF4CAF50), // Green
        Color(0xFF2196F3), // Blue
        Color(0xFFFFC107), // Amber
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF607D8B)  // Blue Gray (for "Other")
    )

    Column(modifier = modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                topCategories.forEachIndexed { idx, (_, value) ->
                    val sweep = (value / total * 360).toFloat()
                    // Draw the pie slice
                    drawArc(
                        color = pieColors[idx % pieColors.size],
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true
                    )
                    startAngle += sweep
                }
            }
        }

        // Legend with percentages
        Column(modifier = Modifier.padding(top = 8.dp)) {
            topCategories.forEachIndexed { idx, (cat, value) ->
                val percentage = (value / total * 100).toInt()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(pieColors[idx % pieColors.size], RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "$cat: ${"%,.0f".format(value)} (${percentage}%)",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(tx: Transaction, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tx.category,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val amountColor = if (tx.type == TransactionType.Income)
                Color(0xFF388E3C) else Color(0xFFD32F2F)
            Text(
                text = "${if (tx.type == TransactionType.Income) "+" else "-"}${"%,.2f".format(tx.amount)}",
                color = amountColor,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color(0xFFD32F2F),
                modifier = Modifier
                    .clickable { onDelete() }
                    .size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMain() {
    MyMoneyNotesTheme { MainScreen() }
}