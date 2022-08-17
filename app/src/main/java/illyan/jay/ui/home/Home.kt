package illyan.jay.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import illyan.jay.R
import illyan.jay.ui.map.MapboxMap
import kotlinx.coroutines.launch
import timber.log.Timber

@RootNavGraph(start = true)
@NavGraph
annotation class HomeNavGraph(
    val start: Boolean = false
)

@OptIn(ExperimentalMaterialApi::class)
@HomeNavGraph(start = true)
@Destination
@Composable
fun HomeScreen() {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = scaffoldState.bottomSheetState
    var isTextFieldFocused by remember { mutableStateOf(false) }
    BottomSheetScaffold(
        sheetContent = {
            MenuScreen(
                bottomSheetState = bottomSheetState,
                isTextFieldFocused = isTextFieldFocused
            ) {
                Timber.d(
                    "TextField focus changed!" +
                            "hasFocus=${it.hasFocus}"
                )
                isTextFieldFocused = it.hasFocus || it.isFocused
            }

        },
        sheetElevation = 16.dp,
        sheetPeekHeight = 64.dp,
        scaffoldState = scaffoldState,
        sheetShape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp
        ),
        modifier = Modifier
            .imePadding()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column {
            Button(onClick = {
                coroutineScope.launch { bottomSheetState.expand() }
            }) {
                Text("Expand")
            }
            Button(onClick = {
                coroutineScope.launch { bottomSheetState.collapse() }
            }) {
                Text("Collapse")
            }
            MapboxMap(
                lat = 47.481491,
                lng = 19.056219,
                zoom = 12.0
            )
        }
        Column(
            modifier = Modifier.padding(bottom = 64.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun BottomAppBarScreen(
    onTextFieldFocusChanged: (FocusState) -> Unit = {}
) {
    val cardColors = CardDefaults.elevatedCardColors(
        containerColor = Color.White
    )
    val focusRequester = remember { FocusRequester() }
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = cardColors,
        onClick = {
            focusRequester.requestFocus()
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .padding(
                        start = 4.dp,
                        top = 8.dp
                    )
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_jay_marker_icon_v3_round),
                    contentDescription = "Search Marker Icon",
                    modifier = Modifier
                        .size(48.dp)
                )
            }

            val colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
            var searchPlaceText by remember { mutableStateOf("") }
            TextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(6.dp)
                    .height(54.dp)
                    .focusable(enabled = true)
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        onTextFieldFocusChanged(it)
                    },
                value = searchPlaceText,
                onValueChange = { searchPlaceText = it },
                label = { Text("Search") },
                placeholder = { Text("Where to?") },
                colors = colors
            )
            IconButton(
                onClick = { },
                modifier = Modifier
                    .padding(
                        end = 8.dp,
                        top = 8.dp
                    ),
            ) {
                Image(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Search Marker Icon",
                    modifier = Modifier
                        .size(48.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MenuScreen(
    bottomSheetState: BottomSheetState,
    isTextFieldFocused: Boolean = false,
    onTextFieldFocusChanged: (FocusState) -> Unit = {}
) {
    // reordering composables
    // with lazy column

    BottomAppBarScreen(
        onTextFieldFocusChanged = onTextFieldFocusChanged
    )
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxHeight(
            fraction = if (isTextFieldFocused) 1f else 0.5f
        )
    ) {
        Text("Yo")
        Button(onClick = {
            coroutineScope.launch { bottomSheetState.expand() }
        }) {
            Text("Expand")
        }
        Button(onClick = {
            coroutineScope.launch { bottomSheetState.collapse() }
        }) {
            Text("Collapse")
        }
    }
}