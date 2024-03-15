package com.example.todo.feature.addedit

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todo.R
import com.example.todo.ui.design.TaskBackTopBar
import com.example.todo.ui.theme.ToDoTheme

@Composable
fun AddEditTaskRoute(
    @StringRes topBarTitle: Int,
    onBack: () -> Unit,
    onTaskUpdate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditTaskViewModel = hiltViewModel(),
    scaffoldState: ScaffoldState = rememberScaffoldState()
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        topBar = { TaskBackTopBar(title = topBarTitle, onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::saveTask) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = stringResource(id = R.string.cd_save_task)
                )
            }
        }
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        AddEditTaskScreen(
            isLoading = uiState.isLoading,
            title = uiState.title,
            description = uiState.description,
            onTitleChanged = viewModel::onTitleChanged,
            onDescriptionChanged = viewModel::onDescriptionChanged,
            onActionDone = viewModel::saveTask,
            modifier =
            modifier
                .padding(paddingValues)
        )

        uiState.userMessage?.let {
            val snackbarText = stringResource(id = it)
            LaunchedEffect(scaffoldState, viewModel, it, snackbarText) {
                scaffoldState.snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }

        LaunchedEffect(uiState.isTaskSaved) {
            if (uiState.isTaskSaved) {
                onTaskUpdate()
            }
        }
    }
}

@Composable
fun AddEditTaskScreen(
    isLoading: Boolean,
    title: String,
    description: String,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onActionDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Text(text = "Loading...") // temp
    } else {
        Column(
            modifier =
            modifier
                .fillMaxSize()
                .padding(all = dimensionResource(id = R.dimen.horizontal_margin))
                .verticalScroll(rememberScrollState())
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current
            val textFieldColors =
                TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = MaterialTheme.colors.secondary.copy(alpha = ContentAlpha.high)
                )
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChanged,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.title_hint),
                        style = MaterialTheme.typography.h6
                    )
                },
                textStyle = MaterialTheme.typography.h6,
                singleLine = true,
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChanged,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.description_hint),
                        style = MaterialTheme.typography.h6
                    )
                },
                textStyle = MaterialTheme.typography.h6,
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions =
                KeyboardActions(onDone = {
                    keyboardController?.hide()
                    onActionDone()
                }),
                modifier =
                modifier
                    .heightIn(200.dp, 300.dp)
                    .fillMaxSize()
            )
        }
    }
}

@Preview
@Composable
fun AddEditTaskContentPreview() {
    ToDoTheme {
        Surface {
            AddEditTaskScreen(
                isLoading = false,
                title = "",
                description = "",
                onTitleChanged = {},
                onDescriptionChanged = {},
                onActionDone = {}
            )
        }
    }
}
