package com.dynapharm.owner.presentation.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dynapharm.owner.BuildConfig
import com.dynapharm.owner.R
import com.dynapharm.owner.domain.model.Franchise
import com.dynapharm.owner.presentation.common.FranchiseSelector

/**
 * Login screen composable.
 * Displays login form with username/email and password fields.
 *
 * @param onLoginSuccess Callback invoked when login is successful
 * @param viewModel LoginViewModel instance (provided by Hilt)
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var rememberMe by rememberSaveable { mutableStateOf(false) }
    var showFranchiseSelector by remember { mutableStateOf(false) }
    var franchises by remember { mutableStateOf<List<Franchise>>(emptyList()) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Handle success state
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            val userFranchises = viewModel.getUserFranchises()
            franchises = userFranchises

            when {
                userFranchises.isEmpty() -> {
                    snackbarHostState.showSnackbar(
                        message = "No franchises available. Contact support.",
                        duration = SnackbarDuration.Long
                    )
                }
                userFranchises.size == 1 -> {
                    // Auto-selected in repository, navigate directly
                    onLoginSuccess()
                    viewModel.resetState()
                }
                else -> {
                    // Show franchise selector
                    showFranchiseSelector = true
                }
            }
        }
    }

    // Handle error state
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Error) {
            errorMessage = (uiState as LoginUiState.Error).message
            showErrorDialog = true
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LoginContent(
            username = username,
            password = password,
            rememberMe = rememberMe,
            isLoading = uiState is LoginUiState.Loading,
            onUsernameChange = { username = it },
            onPasswordChange = { password = it },
            onRememberMeChange = { rememberMe = it },
            onLoginClick = { viewModel.login(username, password) },
            modifier = Modifier.padding(paddingValues)
        )
    }

    // Franchise selector dialog
    if (showFranchiseSelector && franchises.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { /* Cannot dismiss - must select franchise */ },
            title = { Text("Select Franchise") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Please select a franchise to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    FranchiseSelector(
                        franchises = franchises,
                        selectedFranchise = null,
                        onFranchiseSelected = { franchise ->
                            viewModel.setActiveFranchise(franchise)
                            showFranchiseSelector = false
                            onLoginSuccess()
                            viewModel.resetState()
                        }
                    )
                }
            },
            confirmButton = {}
        )
    }

    // Error dialog (SweetAlert style)
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(64.dp)
                )
            },
            title = {
                Text(
                    text = "Login Failed",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
}

@Composable
private fun LoginContent(
    username: String,
    password: String,
    rememberMe: Boolean,
    isLoading: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 500.dp)  // Max 500dp for landscape/tablets
                    .fillMaxWidth(0.9f)  // Then take 90% of constrained width
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Add top spacer for vertical centering
                Spacer(modifier = Modifier.weight(1f))

            // Dynapharm Logo from server
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = "${BuildConfig.API_BASE_URL}dist/img/icons/DynaLogo.png",
                    contentDescription = "Dynapharm Logo",
                    modifier = Modifier
                        .size(width = 200.dp, height = 120.dp),
                    onError = { /* Fallback to text logo if image fails */ },
                    placeholder = painterResource(id = android.R.drawable.ic_dialog_info)
                )
            }

            // Subtitle
            Text(
                text = "Director's Portal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,  // Deep red
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sign in to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Username/Email field
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("Username or Email") },
                placeholder = { Text("Enter your username") },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                placeholder = { Text("Enter your password") },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (username.isNotBlank() && password.isNotBlank()) {
                            onLoginClick()
                        }
                    }
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                            contentDescription = if (passwordVisible) {
                                "Hide password"
                            } else {
                                "Show password"
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Remember me checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = onRememberMeChange,
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Remember me",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isLoading) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Login button
            Button(
                onClick = onLoginClick,
                enabled = !isLoading && username.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App version or additional info
            Text(
                text = "Secure Login",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

                // Add bottom spacer for vertical centering
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginContentPreview() {
    MaterialTheme {
        LoginContent(
            username = "",
            password = "",
            rememberMe = false,
            isLoading = false,
            onUsernameChange = {},
            onPasswordChange = {},
            onRememberMeChange = {},
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginContentLoadingPreview() {
    MaterialTheme {
        LoginContent(
            username = "user@example.com",
            password = "password",
            rememberMe = true,
            isLoading = true,
            onUsernameChange = {},
            onPasswordChange = {},
            onRememberMeChange = {},
            onLoginClick = {}
        )
    }
}
