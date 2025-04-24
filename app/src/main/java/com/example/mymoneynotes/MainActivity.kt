package com.example.mymoneynotes

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymoneynotes.ui.theme.MyMoneyNotesTheme
import java.text.NumberFormat
import java.util.Locale

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
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFE3F2FD)) {
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

    // Use this to ensure the transaction history section is initially visible at bottom of screen
    var chartsSectionHeight by remember { mutableIntStateOf(0) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section that contains input and charts - measure its height
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        chartsSectionHeight = coordinates.size.height
                    },
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Input card
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


            }

            // Transaction history section with an attention-grabbing header
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color.White, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "↓",
                                color = Color(0xFF1976D2),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                modifier = Modifier.offset(y = (-4).dp) // shift upward slightly
                            )
                        }

                        Text(
                            "Transaction History",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            // Transaction list
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions yet", color = Color.Gray)
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 300.dp), // Scrollable inside max height
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        transactions.asReversed().forEachIndexed { index, tx ->
                            TransactionItem(tx) {
                                transactions.removeAt(transactions.lastIndex - index) // correct index after reverse
                            }
                            HorizontalDivider(color = Color.LightGray)
                        }
                    }
                }
            }

            // Add some space at the bottom for better scrolling experience
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
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
    val bg = if (tx.type == TransactionType.Income) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val fg = if (tx.type == TransactionType.Income) Color(0xFF388E3C) else Color(0xFFD32F2F)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(4.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(tx.category)
            Text(
                text = "${if (tx.type == TransactionType.Income) "+" else "-"}${"%,.2f".format(tx.amount)}",
                color = fg,
                fontWeight = FontWeight.Bold
            )
        }
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = Color.Red,
            modifier = Modifier
                .clickable { onDelete() }
                .size(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMain() {
    MyMoneyNotesTheme { MainScreen() }
}