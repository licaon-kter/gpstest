package com.android.wear.presentation

import android.media.Image
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.android.library.LocationLabelAndData
import com.android.wear.R
import com.android.wear.presentation.theme.GpstestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp(LocationLabelAndData.statusSample)
        }
    }
}

@Composable
fun WearApp(satStatues: List<String>) {
    GpstestTheme {
        val listState = rememberScalingLazyListState()
        Scaffold(
            timeText = {
                if (!listState.isScrollInProgress) {
                    TimeText()
                }
            },
            positionIndicator = {
                PositionIndicator(
                    scalingLazyListState = listState
                )
            }
        ) {
            val contentModifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
            ScalingLazyColumn(
                modifier = contentModifier,
                autoCentering = AutoCenteringParams(itemIndex = 0),
                state = listState
            ) {

                for (satStatue in satStatues) {
                    item {
                        Text(text = satStatue)
                    }
                }
            }
        }
    }
}