package com.example.cric8


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import com.example.cric8.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Fixtures(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedFormat by remember { mutableStateOf("ODI") }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                navController = navController,
                onNavigate = { destination ->
                    if (destination != currentRoute) {
                        navController.navigate(destination) {
                            // Pop up to the graph's start destination
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when reselecting same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFFFEB3B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.criblogo),
                                modifier = Modifier.size(24.dp),// Replace with your image resource
                                contentDescription = "Cricket Logo",

                                )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "CRICB",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                actions = {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color.Red, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
            )

            // Search Bar
            OutlinedTextField(
                value = "",
                onValueChange = { },
                placeholder = { Text("Search...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.voice),
                        modifier = Modifier.size(16.dp),
                        contentDescription = "Voice Search"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(25.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content with Gradient Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF6366F1),
                                Color(0xFF8B5CF6)
                            )
                        ),
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
            ) {
                Column {
                    // Tab Section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Card(
                            modifier = Modifier.wrapContentWidth(),
                            shape = RoundedCornerShape(25.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE3F2FD) // Light blue background
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(4.dp)
                            ) {
                                TabButton(
                                    text = "Fixtures",
                                    isSelected = selectedTab == 0,
                                    onClick = { selectedTab = 0 }
                                )
                                TabButton(
                                    text = "Results",
                                    isSelected = selectedTab == 1,
                                    onClick = { selectedTab = 1 }
                                )
                            }
                        }
                    }

                    // Format Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FormatTab("ODI", selectedFormat == "ODI") { selectedFormat = "ODI" }
                        FormatTab("T20", selectedFormat == "T20") { selectedFormat = "T20" }
                        FormatTab("TEST", selectedFormat == "TEST") { selectedFormat = "TEST" }
                        FormatTab("T10", selectedFormat == "T10") { selectedFormat = "T10" }
                        FormatTab("HUNDRED", selectedFormat == "HUNDRED") {
                            selectedFormat = "HUNDRED"
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Match Cards
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (selectedTab == 0) {
                            // Fixtures Tab - Filter by selected format
                            val filteredFixtures = fixturesData.filter {
                                it.format.contains(selectedFormat, ignoreCase = true)
                            }
                            items(filteredFixtures) { match ->
                                FixtureCard(match = match)
                            }
                        } else {
                            // Results Tab - Filter by selected format
                            val filteredResults = resultsData.filter {
                                it.series.contains(selectedFormat, ignoreCase = true)
                            }
                            items(filteredResults) { result ->
                                ResultCard(result = result)
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp)) // Space for bottom nav
                        }
                    }
                }
            }
        }

        // Bottom Navigation
    }


}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?,  // <-- Add this
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = Color.Black
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") },
            selected = currentRoute == "HomeScreen",
            onClick = { onNavigate("HomeScreen") }, // match your nav graph route
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF6366F1),
                selectedTextColor = Color(0xFF6366F1),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Fixtures"
                )
            },
            label = { Text("Fixtures") },
            selected = currentRoute == "fixtures",
            onClick = { onNavigate("fixtures") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF6366F1),
                selectedTextColor = Color(0xFF6366F1),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") },
            selected = currentRoute == "profile",
            onClick = { onNavigate("profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF6366F1),
                selectedTextColor = Color(0xFF6366F1),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Chat"
                )
            },
            label = { Text("Chat") },
            selected = currentRoute == "Chat",
            onClick = { onNavigate("Chat") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF6366F1),
                selectedTextColor = Color(0xFF6366F1),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Weather"
                )
            },
            label = { Text("Weather") },
            selected = currentRoute == "Weather",
            onClick = { onNavigate("Weather") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF6741FF),
                selectedTextColor = Color(0xFF6741FF),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
    }
}
@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFFCDFF47) else Color.Transparent
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(40.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FormatTab(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
        fontSize = 14.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun FixtureCard(match: MatchData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Match Time and Format
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${match.day} • ${match.time}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = match.format,
                    fontSize = 12.sp,
                    color = Color(0xFF6366F1),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Teams
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamInfo(match.team1, match.flag1)
                Text(
                    text = "Vs",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                TeamInfo(match.team2, match.flag2)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Match Status
            Text(
                text = match.status,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Venue
            Text(
                text = match.venue,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ResultCard(result: ResultData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Match Title and Format
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = result.matchTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = result.series,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "⚡",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Team Scores
            result.teamScores.forEach { teamScore ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = teamScore.flag,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = teamScore.teamName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (teamScore.additionalInfo.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = teamScore.additionalInfo,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Text(
                        text = teamScore.score,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Match Result
            Text(
                text = result.matchResult,
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TeamInfo(teamName: String, flag: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = flag,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = teamName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Data class for match information
data class MatchData(
    val day: String,
    val time: String,
    val format: String,
    val team1: String,
    val team2: String,
    val flag1: String,
    val flag2: String,
    val status: String,
    val venue: String
)

// Data class for result information
data class ResultData(
    val matchTitle: String,
    val series: String,
    val teamScores: List<TeamScore>,
    val matchResult: String
)

data class TeamScore(
    val flag: String,
    val teamName: String,
    val score: String,
    val additionalInfo: String = ""
)

// Sample fixtures data with different formats
val fixturesData = listOf(
    // ODI Matches
    MatchData(
        day = "Tomorrow",
        time = "6:00 AM",
        format = "3rd ODI",
        team1 = "NEP",
        team2 = "IRE",
        flag1 = "🇳🇵",
        flag2 = "🇮🇪",
        status = "Match yet to begin",
        venue = "Kiv International Stadium"
    ),
    MatchData(
        day = "Tomorrow",
        time = "8:00 PM",
        format = "2nd ODI",
        team1 = "PAK",
        team2 = "ENG",
        flag1 = "🇵🇰",
        flag2 = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
        status = "Match yet to begin",
        venue = "Mirpur International Stadium"
    ),
    MatchData(
        day = "Sunday",
        time = "2:00 PM",
        format = "1st ODI",
        team1 = "AUS",
        team2 = "NZ",
        flag1 = "🇦🇺",
        flag2 = "🇳🇿",
        status = "Match yet to begin",
        venue = "Melbourne Cricket Ground"
    ),

    // T20 Matches
    MatchData(
        day = "Today",
        time = "7:30 PM",
        format = "T20 Final",
        team1 = "IND",
        team2 = "SA",
        flag1 = "🇮🇳",
        flag2 = "🇿🇦",
        status = "Match yet to begin",
        venue = "Wankhede Stadium"
    ),
    MatchData(
        day = "Tomorrow",
        time = "3:00 PM",
        format = "T20 Match",
        team1 = "WI",
        team2 = "BAN",
        flag1 = "🇼🇸",
        flag2 = "🇧🇩",
        status = "Match yet to begin",
        venue = "Queen's Park Oval"
    ),

    // TEST Matches
    MatchData(
        day = "Monday",
        time = "10:00 AM",
        format = "1st TEST",
        team1 = "ENG",
        team2 = "AUS",
        flag1 = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
        flag2 = "🇦🇺",
        status = "Match yet to begin",
        venue = "Lord's Cricket Ground"
    ),
    MatchData(
        day = "Friday",
        time = "9:30 AM",
        format = "2nd TEST",
        team1 = "IND",
        team2 = "SL",
        flag1 = "🇮🇳",
        flag2 = "🇱🇰",
        status = "Match yet to begin",
        venue = "Eden Gardens"
    ),

    // T10 Matches
    MatchData(
        day = "Saturday",
        time = "5:00 PM",
        format = "T10 League",
        team1 = "UAE",
        team2 = "OMN",
        flag1 = "🇦🇪",
        flag2 = "🇴🇲",
        status = "Match yet to begin",
        venue = "Sheikh Zayed Stadium"
    ),

    // HUNDRED Matches
    MatchData(
        day = "Sunday",
        time = "6:00 PM",
        format = "THE HUNDRED",
        team1 = "LOR",
        team2 = "OVI",
        flag1 = "🦁",
        flag2 = "🔥",
        status = "Match yet to begin",
        venue = "The Oval"
    )
)

// Sample results data with different formats
val resultsData = listOf(
    // T20 Results
    ResultData(
        matchTitle = "INDIA VS WEST INDIES",
        series = "MEN'S T20 TRI-Series East London",
        teamScores = listOf(
            TeamScore(
                flag = "🇼🇸",
                teamName = "WI",
                score = "94/6"
            ),
            TeamScore(
                flag = "🇮🇳",
                teamName = "IND",
                score = "95/2",
                additionalInfo = "(13.5/20 ov, T-80)"
            )
        ),
        matchResult = "India won by 8 wickets"
    ),
    ResultData(
        matchTitle = "USA VS WEST INDIES",
        series = "MEN'S T20 TRI-Series East London",
        teamScores = listOf(
            TeamScore(
                flag = "🇼🇸",
                teamName = "WI",
                score = "94/6"
            ),
            TeamScore(
                flag = "🇺🇸",
                teamName = "USA",
                score = "95/2",
                additionalInfo = "(13.5/20 ov, T-95)"
            )
        ),
        matchResult = "USA won by 8 wickets"
    ),

    // ODI Results
    ResultData(
        matchTitle = "INDIA VS UAE",
        series = "MEN'S ODI Series East London",
        teamScores = listOf(
            TeamScore(
                flag = "🇦🇪",
                teamName = "UAE",
                score = "140/9"
            ),
            TeamScore(
                flag = "🇮🇳",
                teamName = "IND",
                score = "141/2",
                additionalInfo = "(13.5/50 ov, T-140)"
            )
        ),
        matchResult = "India won by 8 wickets"
    ),
    ResultData(
        matchTitle = "INDIA VS UAE",
        series = "MEN'S ODI Series East London",
        teamScores = listOf(
            TeamScore(
                flag = "🇦🇪",
                teamName = "UAE",
                score = "140/9"
            ),
            TeamScore(
                flag = "🇮🇳",
                teamName = "IND",
                score = "141/2",
                additionalInfo = "(13.5/50 ov, T-140)"
            )
        ),
        matchResult = "India won by 8 wickets"
    ),
    ResultData(
        matchTitle = "INDIA VS UAE",
        series = "MEN'S ODI Series East London",
        teamScores = listOf(
            TeamScore(
                flag = "🇦🇪",
                teamName = "UAE",
                score = "140/9"
            ),
            TeamScore(
                flag = "🇮🇳",
                teamName = "IND",
                score = "141/2",
                additionalInfo = "(13.5/50 ov, T-140)"
            )
        ),
        matchResult = "India won by 8 wickets"
    ),
    ResultData(
        matchTitle = "PAKISTAN VS ENGLAND",
        series = "MEN'S ODI Championship Series",
        teamScores = listOf(
            TeamScore(
                flag = "🇵🇰",
                teamName = "PAK",
                score = "285/7"
            ),
            TeamScore(
                flag = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
                teamName = "ENG",
                score = "287/4",
                additionalInfo = "(48.2/50 ov, T-286)"
            )
        ),
        matchResult = "England won by 6 wickets"
    ),

    // TEST Results
    ResultData(
        matchTitle = "AUSTRALIA VS NEW ZEALAND",
        series = "MEN'S TEST Championship",
        teamScores = listOf(
            TeamScore(
                flag = "🇦🇺",
                teamName = "AUS",
                score = "456 & 234/5"
            ),
            TeamScore(
                flag = "🇳🇿",
                teamName = "NZ",
                score = "289 & 187",
                additionalInfo = "(Day 4)"
            )
        ),
        matchResult = "Australia won by 214 runs"
    ),

    // T10 Results
    ResultData(
        matchTitle = "UAE VS OMAN",
        series = "MEN'S T10 League Championship",
        teamScores = listOf(
            TeamScore(
                flag = "🇦🇪",
                teamName = "UAE",
                score = "98/4"
            ),
            TeamScore(
                flag = "🇴🇲",
                teamName = "OMN",
                score = "95/6",
                additionalInfo = "(10/10 ov, T-99)"
            )
        ),
        matchResult = "UAE won by 3 runs"
    ),

    // HUNDRED Results
    ResultData(
        matchTitle = "LONDON SPIRIT VS OVAL INVINCIBLES",
        series = "THE HUNDRED Men's Competition",
        teamScores = listOf(
            TeamScore(
                flag = "🦁",
                teamName = "LOR",
                score = "156/7"
            ),
            TeamScore(
                flag = "🔥",
                teamName = "OVI",
                score = "159/3",
                additionalInfo = "(98/100 balls, T-157)"
            )
        ),
        matchResult = "Oval Invincibles won by 7 wickets"
    ),
    ResultData(
        matchTitle = "LONDON SPIRIT VS OVAL INVINCIBLES",
        series = "THE HUNDRED Men's Competition",
        teamScores = listOf(
            TeamScore(
                flag = "🦁",
                teamName = "LOR",
                score = "156/7"
            ),
            TeamScore(
                flag = "🔥",
                teamName = "OVI",
                score = "159/3",
                additionalInfo = "(98/100 balls, T-157)"
            )
        ),
        matchResult = "Oval Invincibles won by 7 wickets"
    ),
    ResultData(
        matchTitle = "LONDON SPIRIT VS OVAL INVINCIBLES",
        series = "THE HUNDRED Men's Competition",
        teamScores = listOf(
            TeamScore(
                flag = "🦁",
                teamName = "LOR",
                score = "156/7"
            ),
            TeamScore(
                flag = "🔥",
                teamName = "OVI",
                score = "159/3",
                additionalInfo = "(98/100 balls, T-157)"
            )
        ),
        matchResult = "Oval Invincibles won by 7 wickets"
    ),
    ResultData(
        matchTitle = "LONDON SPIRIT VS OVAL INVINCIBLES",
        series = "THE HUNDRED Men's Competition",
        teamScores = listOf(
            TeamScore(
                flag = "🦁",
                teamName = "LOR",
                score = "156/7"
            ),
            TeamScore(
                flag = "🔥",
                teamName = "OVI",
                score = "159/3",
                additionalInfo = "(98/100 balls, T-157)"
            )
        ),
        matchResult = "Oval Invincibles won by 7 wickets"
    ),
)